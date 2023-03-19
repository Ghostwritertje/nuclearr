package be.ghostwritertje.nuclearr.repo;

import be.ghostwritertje.nuclearr.domain.FileItemOccurrence;
import be.ghostwritertje.nuclearr.domain.Torrent;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

import java.util.List;

public interface FileItemOccurrenceRepository extends ReactiveCrudRepository<FileItemOccurrence, Integer> {

    Flux<FileItemOccurrence> findFileItemOccurrencesByTorrentId(Integer torrentId);
    Flux<FileItemOccurrence> findFileItemOccurrencesByFileItemIdInAndTorrentIdIsNot(List<Integer> fileItemIds, Integer torrentId);
}
