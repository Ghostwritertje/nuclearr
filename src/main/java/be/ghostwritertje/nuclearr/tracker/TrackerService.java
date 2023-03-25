package be.ghostwritertje.nuclearr.tracker;

import be.ghostwritertje.nuclearr.fileitem.FileItemService;
import be.ghostwritertje.nuclearr.fileitemoccurrence.FileItemOccurrenceService;
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



    public Flux<Tracker> saveAll(Flux<Tracker> trackerFlux) {
        return this.repo.saveAll(trackerFlux);
    }

    public Flux<Tracker> saveAll(Iterable<Tracker> trackerFlux) {
        return this.repo.saveAll(trackerFlux);
    }

    public Flux<Tracker> findAllByTorrentId(Integer torrentId) {
        return this.repo.findAllByTorrentId(torrentId);
    }

    public Mono<Void> deleteAll() {
        return this.repo.deleteAll();
    }
}
