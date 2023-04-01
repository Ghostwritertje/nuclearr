package be.ghostwritertje.nuclearr.service;

import be.ghostwritertje.nuclearr.hardlinks.HardlinkService;
import be.ghostwritertje.nuclearr.torrent.TorrentService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@AllArgsConstructor
@Slf4j
public class RemovalJob {

    private final TorrentRemovalService torrentRemovalService;
    private final TorrentImporterService torrentImporterService;
    private final HardlinkService hardlinkService;
    private final TorrentService torrentService;

    @Scheduled(fixedDelay = 1440, initialDelay = 1, timeUnit = TimeUnit.MINUTES)
    public void scheduled() {
        log.info("resetting torrents in database");
        var count = new AtomicInteger(0);

        removeTorrentFlux()
                .subscribe(ignored -> log.info("DELETED {}", count.incrementAndGet()), ignored -> log.error("failed job", ignored),
                        () -> log.info("finished and removed {} torrents", count.get()));
    }

    Flux<Void> removeTorrentFlux() {
        return torrentService.deleteAll()
                .then(torrentImporterService.importTorrents())
                .then(hardlinkService.updateAllHardlinks())
                .thenMany(torrentRemovalService.removeTorrents());
    }
}
