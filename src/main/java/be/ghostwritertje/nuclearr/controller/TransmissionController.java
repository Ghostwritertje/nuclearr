package be.ghostwritertje.nuclearr.controller;

import be.ghostwritertje.nuclearr.internaltorrent.InternalTorrent;
import be.ghostwritertje.nuclearr.internaltorrent.TorrentClientAdapter;
import be.ghostwritertje.nuclearr.torrent.Torrent;
import be.ghostwritertje.nuclearr.transmission.TransmissionAdapter;
import be.ghostwritertje.nuclearr.transmission.TransmissionTorrent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/torrent-client")
public class TransmissionController {

    private final TorrentClientAdapter<?> torrentClientAdapter;

    public TransmissionController(TorrentClientAdapter<?> torrentClientAdapter) {
        this.torrentClientAdapter = torrentClientAdapter;
    }

    @GetMapping
    public Flux<? extends InternalTorrent<?>> torrents() {
        return torrentClientAdapter.getTorrents();
    }

}
