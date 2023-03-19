package be.ghostwritertje.nuclearr.controller;

import be.ghostwritertje.nuclearr.domain.FileItem;
import be.ghostwritertje.nuclearr.domain.FileItemOccurrence;
import be.ghostwritertje.nuclearr.domain.Torrent;
import be.ghostwritertje.nuclearr.repo.TorrentRepository;
import be.ghostwritertje.nuclearr.service.TorrentImporterService;
import be.ghostwritertje.nuclearr.service.TorrentRemovalService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/torrent")
@RequiredArgsConstructor
public class TorrentController {

    private final TorrentRepository torrentRepository;
    private final TorrentImporterService torrentImporterService;
    private final TorrentRemovalService torrentRemovalService;


    @GetMapping
    public Flux<Torrent> findAll() {
        return torrentRepository.findAll();
    }

    @PostMapping("/file")
    public Flux<FileItemOccurrence> importFileItemOccurrences() {
        return torrentImporterService.deleteAll().thenMany(torrentImporterService.importFileItems());
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
