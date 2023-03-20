package be.ghostwritertje.nuclearr.fileitem;

import be.ghostwritertje.nuclearr.service.HardlinkFinder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileItemService {
    private final FileItemRepository repo;
    private final HardlinkFinder hardlinkFinder;

    public Mono<FileItem> findById(Integer id) {
        return this.repo.findById(id);
    }

    public Mono<FileItem> findByPath(String path) {
        return this.repo.findFileItemByPath(path);
    }

    public Mono<FileItem> save(FileItem fileItem) {
        return this.repo.save(fileItem);
    }

    public Flux<FileItem> saveAll(Flux<FileItem> flux) {
        return this.repo.saveAll(flux);
    }

    public Mono<Void> deleteAll() {
        return this.repo.deleteAll();
    }

    @Deprecated
    //TODO refactor
    public Mono<FileItem> mergeFileItem(FileItem fileItem) {
        Mono<FileItem> newItemMono = hardlinkFinder.findHardLinks(fileItem)
                .flatMap(this::save);

        return this.findByPath(fileItem.getPath())
                .switchIfEmpty(newItemMono);
    }
}
