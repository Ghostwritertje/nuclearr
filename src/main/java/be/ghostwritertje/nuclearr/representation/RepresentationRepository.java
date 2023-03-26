package be.ghostwritertje.nuclearr.representation;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

interface RepresentationRepository extends ReactiveCrudRepository<Representation, String> {

    Flux<Representation> findAllByHardlinksIsLessThanAndSeedTimeLessThan(Integer hardlinkCount, Long seedTime);
}
