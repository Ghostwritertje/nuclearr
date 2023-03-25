package be.ghostwritertje.nuclearr.hardlinks;

import be.ghostwritertje.nuclearr.config.NuclearrConfiguration;
import be.ghostwritertje.nuclearr.fileitem.FileItem;
import be.ghostwritertje.nuclearr.fileitem.FileItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
@Slf4j
@RequiredArgsConstructor
public class HardlinkFinder {

    private final NuclearrConfiguration nuclearrConfiguration;

    public Mono<FileItem> findHardLinks(FileItem fileItem) {
        if (!nuclearrConfiguration.isHardlinksEnabled()) {
            return Mono.empty();
        }

        return Mono.just(fileItem)
                .publishOn(Schedulers.boundedElastic())
                .map(fi -> {
                    try {
                        Object attribute = Files.getAttribute(Path.of(fileItem.getPath()), "unix:nlink");
                        fileItem.setHardlinks((Integer) attribute);
                        return fileItem;
                    } catch (IOException e) {
                        log.warn("Could not find hardlinks");
                        return fileItem;
                    }
                });
    }

    public Flux<FileItem> findHardLinksMany(Flux<FileItem> fileItem) {
        return fileItem
                .flatMap(this::findHardLinks);
    }
}
