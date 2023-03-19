package be.ghostwritertje.nuclearr.repo;

import be.ghostwritertje.nuclearr.domain.Removed;
import be.ghostwritertje.nuclearr.domain.Torrent;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface RemovedRepository extends ReactiveCrudRepository<Removed, Integer> {
    Mono<Removed> findRemovedByTransmissionId(Integer transmissionId);
}
