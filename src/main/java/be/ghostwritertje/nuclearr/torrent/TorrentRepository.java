package be.ghostwritertje.nuclearr.torrent;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

interface TorrentRepository extends ReactiveCrudRepository<Torrent, Integer> {

    Mono<Torrent> findByTransmissionId(Integer transmissionId);
}
