package be.ghostwritertje.nuclearr.torrent;

import io.r2dbc.spi.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.stream.IntStream;

@ActiveProfiles("test")
@SpringBootTest
class TorrentRepositoryTest {

    @Autowired
    private TorrentRepository torrentRepository;
    @Autowired
    private TorrentRepo torrentRepo;
    @Autowired
    private DatabaseClient databaseClient;


    @BeforeEach
    public void cleanup() {
        StepVerifier.create(torrentRepository.deleteAll()).verifyComplete();
    }

    @Test
    public void testSave() {
        Mono<Torrent> result = torrentRepository.save(Torrent.builder()
                .name("test")
                .hash("tst-hash")
                .transmissionId(9998)
                .build());

        StepVerifier.create(result)
                .expectNextCount(1)
                .verifyComplete();
    }


    @Test
    @Timeout(1)
    public void testSaveManyTraditional() {
        Flux<Torrent> result = Flux.fromStream(IntStream.range(1, 1001).boxed())
                .map(i -> Torrent.builder()
                        .name("test-many" + i)
                        .hash("tst-hash" + i)
                        .transmissionId(i)
                        .seedTime(i * 5L)
                        .build())
                .buffer(150)
                .flatMap(list -> torrentRepo.saveAll(list));

        StepVerifier.create(result)
                .expectNextCount(1000)
                .verifyComplete();
    }

    @Test
    @Timeout(20)
    public void testH2() {
        Mono<List<Long>> listMono = databaseClient.inConnection(conn -> Flux.from(conn.createStatement("insert into REMOVED (name) values ($1)")
                        .bind(0, 1).add()
                        .bind(0, 2).execute())
                .flatMap(Result::getRowsUpdated)
                .collectList());

        StepVerifier.create(listMono)
                .expectNextCount(1)
                .verifyComplete();
    }
}