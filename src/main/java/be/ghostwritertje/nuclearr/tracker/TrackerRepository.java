package be.ghostwritertje.nuclearr.tracker;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;


interface TrackerRepository extends ReactiveCrudRepository<Tracker, Integer> {

    Flux<Tracker> findAllByTorrentId(Integer torrentId);
}
