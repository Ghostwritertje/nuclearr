package be.ghostwritertje.nuclearr.fileitemoccurrence;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

import java.util.List;

interface FileItemOccurrenceRepository extends ReactiveCrudRepository<FileItemOccurrence, Integer> {

    Flux<FileItemOccurrence> findFileItemOccurrencesByTorrentId(Integer torrentId);
}
