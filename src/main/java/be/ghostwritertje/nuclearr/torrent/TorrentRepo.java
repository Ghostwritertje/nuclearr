package be.ghostwritertje.nuclearr.torrent;

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
public class TorrentRepo {

    private final DatabaseClient databaseClient;

    public Flux<Torrent> saveAll(Iterable<Torrent> torrentList) {
        return databaseClient.inConnectionMany(connection -> {
            Statement statement = connection.createStatement("insert into TORRENT (NAME, HASH_NAME, TRANSMISSION_ID, SEED_TIME) " +
                            " VALUES ($1, $2, $3, $4)")
                    .returnGeneratedValues("ID", "NAME", "HASH_NAME", "TRANSMISSION_ID", "SEED_TIME");
            Iterator<Torrent> iterator = torrentList.iterator();

            while (iterator.hasNext()) {
                Torrent torrent = iterator.next();
                statement.bind(0, torrent.getName())
                        .bind(1, torrent.getHash())
                        .bind(2, torrent.getTransmissionId())
                        .bind(3, torrent.getSeedTime());
                if (iterator.hasNext()) {
                    statement.add();
                }
            }
            return Flux.from(statement.execute())
                    .flatMap(result -> (Flux<Torrent>) result.map((row, rowMetadata) -> Torrent.builder()
                            .id(row.get("ID", Integer.class))
                            .name(row.get("NAME", String.class))
                            .hash(row.get("HASH_NAME", String.class))
                            .transmissionId(row.get("TRANSMISSION_ID", Integer.class))
                            .seedTime(row.get("SEED_TIME", Long.class))
                            .build()));
        });
    }
}
