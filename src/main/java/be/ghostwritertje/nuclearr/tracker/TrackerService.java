package be.ghostwritertje.nuclearr.tracker;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TrackerService {
    private final TrackerRepository repo;

    private final TrackerRepo traditionalRepo;


    public Flux<Tracker> saveAll(List<Tracker> flux) {
        log.debug("saving {} trackers", flux.size());
        return this.traditionalRepo.saveAll(flux);
    }

    public Flux<Tracker> findAllByTorrentId(Integer torrentId) {
        return this.repo.findAllByTorrentId(torrentId);
    }

    public Mono<Void> deleteAll() {
        return this.repo.deleteAll();
    }

    public Mono<Void> deleteByTorrentId(Integer torrentId) {
        return this.repo.deleteAllByTorrentId(torrentId);
    }
}
