package be.ghostwritertje.nuclearr.controller;

import be.ghostwritertje.nuclearr.domain.Torrent;
import be.ghostwritertje.nuclearr.presentation.MasterTorrentDto;
import be.ghostwritertje.nuclearr.service.RepresentationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/represent")
@RequiredArgsConstructor
public class RepresentController {

    private final RepresentationService representationService;

    @GetMapping
    public Flux<MasterTorrentDto> findAll() {
        return representationService.represent();
    }
}
