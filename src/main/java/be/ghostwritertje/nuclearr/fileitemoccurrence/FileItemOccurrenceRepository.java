package be.ghostwritertje.nuclearr.fileitemoccurrence;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

interface FileItemOccurrenceRepository extends ReactiveCrudRepository<FileItemOccurrence, Integer> {

    Flux<FileItemOccurrence> findFileItemOccurrencesByTorrentId(Integer torrentId);

    Flux<FileItemOccurrence> findFileItemOccurrencesByFileItemPathInAndTorrentIdNot(List<String> fileItemPaths, Integer torrentId);

    Mono<Void> deleteAllByTorrentId(Integer torrentId);
}
