package be.ghostwritertje.nuclearr.tracker;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TrackerService {
    private final TrackerRepository repo;

    private final TrackerRepo traditionalRepo;


    public Flux<Tracker> saveAll(Iterable<Tracker> trackerFlux) {
        return this.traditionalRepo.saveAll(trackerFlux);
    }

    public Flux<Tracker> findAllByTorrentId(Integer torrentId) {
        return this.repo.findAllByTorrentId(torrentId);
    }

    public Mono<Void> deleteAll() {
        return this.repo.deleteAll();
    }
}
