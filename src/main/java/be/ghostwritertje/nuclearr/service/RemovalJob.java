package be.ghostwritertje.nuclearr.service;

import be.ghostwritertje.nuclearr.torrent.TorrentService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;
import reactor.core.publisher.Mono;

import java.util.concurrent.TimeUnit;

@Component
@AllArgsConstructor
@Slf4j
public class RemovalJob {

//    private final TorrentRemovalService torrentRemovalService;
    private final TorrentImporterServiceV2 torrentImporterService;
    private final TorrentService torrentService;

    @Scheduled(fixedDelay = 3600, initialDelay = 1, timeUnit = TimeUnit.SECONDS)
    public void scheduled() {
        log.info("deleting torrents");

        torrentService.deleteAll()
                .subscribe(ignored -> log.debug("DELETED!"), ignored -> log.error("Failed deleting torrents", ignored),
                        () -> log.info("finished deleting torrents"));
    }
}
