package be.ghostwritertje.nuclearr.controller;

import be.ghostwritertje.nuclearr.service.TorrentImporterService;
import be.ghostwritertje.nuclearr.service.TorrentRemovalService;
import be.ghostwritertje.nuclearr.torrent.Torrent;
import be.ghostwritertje.nuclearr.torrent.TorrentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/torrent")
@RequiredArgsConstructor
public class TorrentController {

    private final TorrentService torrentService;
    private final TorrentImporterService torrentImporterService;
    private final TorrentRemovalService torrentRemovalService;


    @GetMapping
    public Flux<Torrent> findAll() {
        return torrentService.findAll();
    }

    @PostMapping("/file")
    public Mono<Void> importFileItemOccurrences() {
        return torrentImporterService.importTorrents();
    }

    @DeleteMapping
    public Mono<Void> deleteAll() {
        return torrentRemovalService.removeTorrents();
    }

    @DeleteMapping("/{id}")
    public Mono<Void> delete(@PathVariable Integer id) {
        return torrentRemovalService.removeTorrent(id);
    }
}
