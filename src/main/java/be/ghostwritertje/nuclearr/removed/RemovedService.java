package be.ghostwritertje.nuclearr.removed;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class RemovedService {
    private final RemovedRepository repo;

    public Mono<Removed> findRemovedByTransmissionId(Integer transmissionId){
        return this.repo.findRemovedByTransmissionId(transmissionId);
    }

    public Flux<Removed> saveAll(Flux<Removed> trackerFlux) {
        return this.repo.saveAll(trackerFlux);
    }

    public Mono<Removed> save(Removed removed) {
        return this.repo.save(removed);
    }
}
