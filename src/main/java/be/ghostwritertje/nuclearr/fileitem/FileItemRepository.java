package be.ghostwritertje.nuclearr.fileitem;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

interface FileItemRepository extends ReactiveCrudRepository<FileItem, Integer> {

    Mono<FileItem> findFileItemByPath(String path);
}
