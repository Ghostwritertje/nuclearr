package be.ghostwritertje.nuclearr.service;

import be.ghostwritertje.nuclearr.domain.FileItem;
import be.ghostwritertje.nuclearr.domain.FileItemOccurrence;
import be.ghostwritertje.nuclearr.repo.FileItemRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@AllArgsConstructor
public class FileItemService {

    private final FileItemRepository fileItemRepository;

    private final HardlinkFinder hardlinkFinder;

    public Mono<FileItem> mergeFileItem(FileItem fileItem) {
        Mono<FileItem> newItemMono = hardlinkFinder.findHardLinks(fileItem)
                .flatMap(this.fileItemRepository::save);

        return this.fileItemRepository.findFileItemByPath(fileItem.getPath())
                .switchIfEmpty(newItemMono);
    }
}
