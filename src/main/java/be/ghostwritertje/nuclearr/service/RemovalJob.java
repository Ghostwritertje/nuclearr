package be.ghostwritertje.nuclearr.service;

import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class RemovalJob {

    private final TorrentRemovalService torrentRemovalService;
    private final TorrentImporterService torrentImporterService;

    @Scheduled(cron = "0 0 11 1/1 * ?")
    public void scheduled() {
        torrentImporterService.deleteAll()
                .thenMany(torrentImporterService.importFileItems())
                .then(torrentRemovalService.removeTorrents())
                .subscribe();
    }
}
