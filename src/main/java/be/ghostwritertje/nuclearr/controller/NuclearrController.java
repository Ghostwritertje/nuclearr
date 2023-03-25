package be.ghostwritertje.nuclearr.controller;

import be.ghostwritertje.nuclearr.hardlinks.HardlinkService;
import be.ghostwritertje.nuclearr.presentation.MasterTorrentDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/nuclearr")
@RequiredArgsConstructor
public class NuclearrController {

    private final HardlinkService hardlinkService;

    @GetMapping("/update-hardlinks")
    public Mono<Void> updateHardlinks() {
        return hardlinkService.updateAllHardlinks();
    }
}
