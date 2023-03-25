package be.ghostwritertje.nuclearr.fileitemoccurrence;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileItemOccurrenceService {
    private final FileItemOccurrenceRepository repo;
    private final FileItemOccurrenceRepo traditionalRepo;

    public Flux<FileItemOccurrence> saveAll(Iterable<FileItemOccurrence> flux) {
        return this.traditionalRepo.saveAll(flux);
    }

    public Mono<FileItemOccurrence> save(FileItemOccurrence fileItemOccurrence) {
        return this.repo.save(fileItemOccurrence);
    }

    public Mono<Void> deleteAll() {
        return this.repo.deleteAll();
    }

    public Flux<FileItemOccurrence> findFileItemOccurrencesByTorrentId(Integer torrentId) {
        return this.repo.findFileItemOccurrencesByTorrentId(torrentId);
    }

    public Flux<FileItemOccurrence> findFileItemOccurrencesByFileItemPathInAndTorrentIdNot(List<String> fileItemPaths, Integer torrentId) {
        return this.repo.findFileItemOccurrencesByFileItemPathInAndTorrentIdNot(fileItemPaths, torrentId);
    }

}
