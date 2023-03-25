package be.ghostwritertje.nuclearr.representation;

import be.ghostwritertje.nuclearr.removed.Removed;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

interface RepresentationRepository extends ReactiveCrudRepository<Representation, String> {


}
