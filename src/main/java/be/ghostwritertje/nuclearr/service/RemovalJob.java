package be.ghostwritertje.nuclearr.service;

import be.ghostwritertje.nuclearr.torrent.TorrentService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@AllArgsConstructor
@Slf4j
public class RemovalJob {

//    private final TorrentRemovalService torrentRemovalService;
    private final TorrentImporterService torrentImporterService;
    private final TorrentService torrentService;

    @Scheduled(fixedDelay = 3600, initialDelay = 1, timeUnit = TimeUnit.SECONDS)
    public void scheduled() {
        log.info("deleting torrents");

        torrentService.deleteAll()
                .then(torrentImporterService.importTorrents())
                .subscribe(ignored -> log.debug("DELETED!"), ignored -> log.error("failed job", ignored),
                        () -> log.info("finished job"));
    }
}
