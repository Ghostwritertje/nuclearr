package be.ghostwritertje.nuclearr.fileitem;

import io.r2dbc.spi.Statement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.Iterator;

@Repository
@RequiredArgsConstructor
@Slf4j
public class FileItemRepo {

    private final DatabaseClient databaseClient;

    public Flux<FileItem> saveAll(Iterable<FileItem> trackerIterable) {
        return databaseClient.inConnectionMany(connection -> {
            Statement statement = connection.createStatement("insert into file_item (path, hard_links) " +
                            " VALUES ($1, $2)")
                    .returnGeneratedValues("ID", "PATH", "HARD_LINKS");
            Iterator<FileItem> iterator = trackerIterable.iterator();

            while (iterator.hasNext()) {
                FileItem fileItem = iterator.next();
                statement.bind(0, fileItem.getPath());
                statement = fileItem.getHardlinks() != null ? statement.bind(1, fileItem.getHardlinks()) : statement.bindNull(1, Integer.class);
                if (iterator.hasNext()) {
                    statement.add();
                }
            }
            return Flux.from(statement.execute())
                    .flatMap(result -> (Flux<FileItem>) result.map((row, rowMetadata) -> FileItem.builder()
                            .id(row.get("ID", Integer.class))
                            .path(row.get("PATH", String.class))
                            .hardlinks(row.get("HARD_LINKS", Integer.class))
                            .build()));
        });
    }
}
