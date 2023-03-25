package be.ghostwritertje.nuclearr.fileitemoccurrence;

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
public class FileItemOccurrenceRepo {

    private final DatabaseClient databaseClient;

    public Flux<FileItemOccurrence> saveAll(Iterable<FileItemOccurrence> trackerIterable) {
        return databaseClient.inConnectionMany(connection -> {
            Statement statement = connection.createStatement("insert into FILE_ITEM_OCCURRENCE (FILE_ITEM_PATH, TORRENT_ID) " +
                            " VALUES ($1, $2)")
                    .returnGeneratedValues("ID", "FILE_ITEM_PATH", "TORRENT_ID");
            Iterator<FileItemOccurrence> iterator = trackerIterable.iterator();

            while (iterator.hasNext()) {
                FileItemOccurrence fileItem = iterator.next();
                statement.bind(0, fileItem.getFileItemPath())
                        .bind(1, fileItem.getTorrentId());
                if (iterator.hasNext()) {
                    statement.add();
                }
            }
            return Flux.from(statement.execute())
                    .flatMap(result -> (Flux<FileItemOccurrence>) result.map((row, rowMetadata) -> FileItemOccurrence.builder()
                            .id(row.get("ID", Integer.class))
                            .fileItemPath(row.get("FILE_ITEM_PATH", String.class))
                            .torrentId(row.get("TORRENT_ID", Integer.class))
                            .build()));
        });
    }
}
