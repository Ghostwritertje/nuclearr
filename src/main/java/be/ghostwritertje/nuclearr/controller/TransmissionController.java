package be.ghostwritertje.nuclearr.controller;

import be.ghostwritertje.nuclearr.domain.Torrent;
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
@RequestMapping("/api/transmission")
public class TransmissionController {

    private final TransmissionAdapter transmissionAdapter;

    public TransmissionController(TransmissionAdapter transmissionClient) {
        this.transmissionAdapter = transmissionClient;
    }

    @GetMapping
    public Mono<List<TransmissionTorrent>> transmissionTorrents() {
        return transmissionAdapter.retrieveAllTorrents();
    }

    @GetMapping("/{id}")
    public Mono<Torrent> findOne(@PathVariable Integer id) {

        return transmissionAdapter.getDetails(id)
                .map(t -> Torrent.builder()
                        .id(t.getId())
                        .name(t.getName())
                        .build());
    }
}
