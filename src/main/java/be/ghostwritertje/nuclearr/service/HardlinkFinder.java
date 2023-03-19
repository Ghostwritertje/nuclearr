package be.ghostwritertje.nuclearr.service;

import be.ghostwritertje.nuclearr.domain.FileItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
@Slf4j
public class HardlinkFinder {

    @Value("${nuclearr.download.path:/downloads}")
    private String downloadPath;

    public Mono<FileItem> findHardLinks(FileItem fileItem) {
        try {
            Object attribute = Files.getAttribute(Path.of(fileItem.getPath()), "unix:nlink");
            fileItem.setHardlinks((Integer) attribute);
            return Mono.just(fileItem);
        } catch (IOException e) {
            log.warn("Could not find hardlinks");
            return Mono.just(fileItem);
        }
    }
}
