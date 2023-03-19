package be.ghostwritertje.nuclearr.repo;

import be.ghostwritertje.nuclearr.domain.FileItem;
import be.ghostwritertje.nuclearr.domain.Torrent;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.io.File;

public interface FileItemRepository extends ReactiveCrudRepository<FileItem, Integer> {

    Mono<FileItem> findFileItemByPath(String path);
}
