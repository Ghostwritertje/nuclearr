package be.ghostwritertje.nuclearr.tracker;

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
public class TrackerRepo {

    private final DatabaseClient databaseClient;

    public Flux<Tracker> saveAll(Iterable<Tracker> trackerIterable) {
        return databaseClient.inConnectionMany(connection -> {
            Statement statement = connection.createStatement("insert into TRACKER (NAME, TORRENT_ID) " +
                            " VALUES ($1, $2)")
                    .returnGeneratedValues("ID", "NAME", "TORRENT_ID");
            Iterator<Tracker> iterator = trackerIterable.iterator();

            while (iterator.hasNext()) {
                Tracker tracker = iterator.next();
                statement.bind(0, tracker.getName())
                        .bind(1, tracker.getTorrentId());
                if (iterator.hasNext()) {
                    statement.add();
                }
            }
            return Flux.from(statement.execute())
                    .flatMap(result -> (Flux<Tracker>) result.map((row, rowMetadata) -> Tracker.builder()
                            .id(row.get("ID", Integer.class))
                            .name(row.get("NAME", String.class))
                            .torrentId(row.get("TORRENT_ID", Integer.class))
                            .build()));
        });
    }
}
