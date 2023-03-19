package be.ghostwritertje.nuclearr.repo;

import be.ghostwritertje.nuclearr.domain.Torrent;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TorrentRepository extends ReactiveCrudRepository<Torrent, Integer> {

    Flux<Torrent> getTorrentById(Integer id);
    Mono<Torrent> getTorrentByTransmissionId(Integer transmissionId);
}
