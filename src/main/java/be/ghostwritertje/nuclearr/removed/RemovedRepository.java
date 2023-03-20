package be.ghostwritertje.nuclearr.removed;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

interface RemovedRepository extends ReactiveCrudRepository<Removed, Integer> {
    Mono<Removed> findRemovedByTransmissionId(Integer transmissionId);
}
