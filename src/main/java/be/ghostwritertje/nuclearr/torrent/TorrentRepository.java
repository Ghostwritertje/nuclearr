package be.ghostwritertje.nuclearr.torrent;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

interface TorrentRepository extends ReactiveCrudRepository<Torrent, Integer> {

}
