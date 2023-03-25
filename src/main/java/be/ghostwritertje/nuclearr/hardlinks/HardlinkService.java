package be.ghostwritertje.nuclearr.hardlinks;

import be.ghostwritertje.nuclearr.config.NuclearrConfiguration;
import be.ghostwritertje.nuclearr.fileitem.FileItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class HardlinkService {

    private final HardlinkFinder hardlinkFinder;
    private final FileItemService fileItemService;
    private final NuclearrConfiguration nuclearrConfiguration;

    public Mono<Void> updateAllHardlinks() {
        return fileItemService.findAll()
                .flatMap(hardlinkFinder::findHardLinks)
                .flatMap(fileItemService::save)
                .then(Mono.fromRunnable(() -> log.info("Finished updating hardlinks")));
    }
}
