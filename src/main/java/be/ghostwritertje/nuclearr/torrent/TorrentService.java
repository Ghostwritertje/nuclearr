package be.ghostwritertje.nuclearr.torrent;

import be.ghostwritertje.nuclearr.fileitem.FileItemService;
import be.ghostwritertje.nuclearr.fileitemoccurrence.FileItemOccurrenceService;
import be.ghostwritertje.nuclearr.tracker.TrackerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TorrentService {
    private final TorrentRepository repo;
    private final TorrentRepo traditionalRepo;

    private final FileItemOccurrenceService fileItemOccurrenceService;
    private final FileItemService fileItemService;
    private final TrackerService trackerService;

    public Flux<Torrent> findAll() {
        return this.repo.findAll();
    }

    public Mono<Torrent> getTorrentById(Integer id) {
        return this.repo.findById(id);
    }

    public Mono<Torrent> save(Torrent torrent) {
        return this.repo.save(torrent);
    }

    public Flux<Torrent> saveAll(Flux<Torrent> flux) {
        return this.repo.saveAll(flux);
    }

    public Flux<Torrent> saveAll(List<Torrent> flux) {
        log.debug("saving {} Torrents", flux.size());
        return this.traditionalRepo.saveAll(flux);
    }

    public Mono<Void> deleteAll() {
        return this.trackerService.deleteAll()
                .then(this.fileItemOccurrenceService.deleteAll())
                .then(this.fileItemService.deleteAll())
                .then(this.repo.deleteAll());
    }

    public Mono<Void> deleteByTransmissionId(Integer transmissionId) {
        return Mono.from(this.repo.findByTransmissionId(transmissionId)
                .flatMapMany(torrent -> {
                    Mono<Void> occurrencesDelete = this.fileItemOccurrenceService.deleteByTorrentId(torrent.getId());
                    Mono<Void> trackersDelete = this.trackerService.deleteByTorrentId(torrent.getId());

                    return Flux.merge(occurrencesDelete, trackersDelete);
                }));
    }
}
