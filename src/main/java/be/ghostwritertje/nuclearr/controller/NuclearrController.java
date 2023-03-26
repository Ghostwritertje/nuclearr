package be.ghostwritertje.nuclearr.controller;

import be.ghostwritertje.nuclearr.hardlinks.HardlinkService;
import be.ghostwritertje.nuclearr.representation.Representation;
import be.ghostwritertje.nuclearr.representation.RepresentationService;
import be.ghostwritertje.nuclearr.service.TorrentImporterService;
import be.ghostwritertje.nuclearr.service.TorrentRemovalService;
import be.ghostwritertje.nuclearr.torrent.TorrentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

@RestController
@RequestMapping("/api/nuclearr")
@RequiredArgsConstructor
public class NuclearrController {

    private final HardlinkService hardlinkService;
    private final TorrentImporterService torrentImporterService;
    private final TorrentService torrentService;
    private final RepresentationService representationService;

    private final TorrentRemovalService torrentRemovalService;

    @GetMapping("/update-hardlinks")
    public Mono<Void> updateHardlinks() {
        return hardlinkService.updateAllHardlinks();
    }

    @GetMapping("/import-all")
    public Mono<Void> importAll() {
        return torrentService.deleteAll().then(torrentImporterService.importTorrents());
    }

    @GetMapping("/representation")
    public Flux<Representation> represent() {
        return representationService.findAll();
    }

    @GetMapping("/representation/filtered")
    public Flux<Representation> representFiltered() {
        return representationService.findAllByHardlinksIsLessThanAndSeedTimeLessThan(2, Duration.ofDays(30).toSeconds());
    }

    @DeleteMapping("/delete-all")
    public Mono<Void> deleteAll() {
        return torrentRemovalService.removeTorrents();
    }
}
