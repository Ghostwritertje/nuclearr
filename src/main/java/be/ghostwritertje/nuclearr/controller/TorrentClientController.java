package be.ghostwritertje.nuclearr.controller;

import be.ghostwritertje.nuclearr.internaltorrent.InternalTorrent;
import be.ghostwritertje.nuclearr.internaltorrent.TorrentClientAdapter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/torrent-client")
@RequiredArgsConstructor
public class TorrentClientController {

    private final TorrentClientAdapter torrentClientAdapter;


    @GetMapping
    public Flux<? extends InternalTorrent> torrents() {
        return torrentClientAdapter.getTorrents();
    }

}
