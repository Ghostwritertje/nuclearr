package be.ghostwritertje.nuclearr.torrent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class TorrentService {
    private final TorrentRepository repo;

    public Flux<Torrent> findAll() {
        return this.repo.findAll();
    }

    public Mono<Torrent> getTorrentById(Integer id) {
        return this.repo.findById(id);
    }


    public Flux<Torrent> saveAll(Flux<Torrent> flux) {
        return this.repo.saveAll(flux);
    }

    public Mono<Void> deleteAll() {
        return this.repo.deleteAll();
    }
}
