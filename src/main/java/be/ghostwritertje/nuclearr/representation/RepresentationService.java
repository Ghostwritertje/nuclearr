package be.ghostwritertje.nuclearr.representation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
@Slf4j
public class RepresentationService {
    private final RepresentationRepository repo;

    public Flux<Representation> findAll() {
        return this.repo.findAll();
    }

    public Flux<Representation> findAllByHardlinksIsLessThanAndSeedTimeGreaterThan(Integer hardlinkCount, Long seedTime) {
        return this.repo.findAllByHardlinksIsLessThanAndSeedTimeGreaterThan(hardlinkCount, seedTime);
    }

}
