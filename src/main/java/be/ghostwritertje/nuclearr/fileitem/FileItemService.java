package be.ghostwritertje.nuclearr.fileitem;

import be.ghostwritertje.nuclearr.hardlinks.HardlinkFinder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileItemService {
    private final FileItemRepository repo;
    private final FileItemRepo traditionalRepo;
    private final HardlinkFinder hardlinkFinder;


    public Mono<FileItem> findByPath(String path) {
        return this.repo.findFileItemByPath(path);
    }

    public Mono<FileItem> save(FileItem fileItem) {
        return this.repo.save(fileItem);
    }

    public Flux<FileItem> saveAll(List<FileItem> flux) {
        log.debug("saving {} fileItems", flux.size());
        return this.traditionalRepo.saveAll(flux);
    }

    public Mono<Void> deleteAll() {
        return this.repo.deleteAll();
    }

    @Deprecated
    //TODO refactor
    public Mono<FileItem> mergeFileItem(FileItem fileItem) {
        return this.findByPath(fileItem.getPath())
                .switchIfEmpty(Mono.defer(() -> hardlinkFinder.findHardLinks(fileItem)
                        .flatMap(this::save)));
    }

    public Flux<FileItem> findAll() {
        return this.repo.findAll();
    }
}
