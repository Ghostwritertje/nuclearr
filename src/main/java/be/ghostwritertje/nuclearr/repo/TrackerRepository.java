package be.ghostwritertje.nuclearr.repo;

import be.ghostwritertje.nuclearr.domain.Torrent;
import be.ghostwritertje.nuclearr.domain.Tracker;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;


public interface TrackerRepository extends ReactiveCrudRepository<Tracker, Integer> {

    Flux<Tracker> findAllByTorrentId(Integer torrentId);
}
