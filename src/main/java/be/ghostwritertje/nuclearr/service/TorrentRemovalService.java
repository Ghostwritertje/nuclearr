package be.ghostwritertje.nuclearr.service;

import be.ghostwritertje.nuclearr.config.NuclearrConfiguration;
import be.ghostwritertje.nuclearr.internaltorrent.TorrentClientAdapter;
import be.ghostwritertje.nuclearr.removed.Removed;
import be.ghostwritertje.nuclearr.removed.RemovedService;
import be.ghostwritertje.nuclearr.representation.Representation;
import be.ghostwritertje.nuclearr.representation.RepresentationService;
import be.ghostwritertje.nuclearr.torrent.TorrentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class TorrentRemovalService {

    private final RemovedService removedService;
    private final TorrentClientAdapter torrentClientAdapter;
    private final RepresentationService representationService;

    private final NuclearrConfiguration nuclearrConfiguration;
    private final TorrentService torrentService;


    public Flux<Void> removeTorrents() {
        long minimumSeedTime = TimeUnit.DAYS.toSeconds(30);

        return representationService.findAllByHardlinksIsLessThanAndSeedTimeGreaterThan(2, minimumSeedTime)
                .filter(masterTorrentDto -> Objects.nonNull(masterTorrentDto.getSeedTime()))
                .filter(masterTorrentDto -> masterTorrentDto.getSeedTime() > minimumSeedTime)
                .filter(masterTorrentDto -> Objects.nonNull(masterTorrentDto.getHardlinks()))
                .filter(masterTorrentDto -> masterTorrentDto.getHardlinks() < 2)
                .filter(filterBasedOnTrackers())
                .flatMap(this::createRemovedItem)
                .doOnEach(ignored -> log.info("Removing torrent with name {}", ignored.get() != null ? ignored.get().getName() : null))
                .map(Removed::getTransmissionId)
                .flatMap(this::removeTorrentFromTorrentClient);
    }

    private Predicate<Representation> filterBasedOnTrackers() {
        return masterTorrentDto -> {
            boolean canBeRemoved = nuclearrConfiguration.getTrackers().containsAll(Arrays.asList(masterTorrentDto.getTrackers()));
            if (!canBeRemoved) {
                ArrayList<String> trackersNotConfigured = new ArrayList<>(Arrays.asList(masterTorrentDto.getTrackers()));
                trackersNotConfigured.removeAll(nuclearrConfiguration.getTrackers());
                log.info("Torrent {} cannot be removed because trackers {} are not configured for removal",
                        masterTorrentDto.getName(), String.join(",", trackersNotConfigured));

            }

            return canBeRemoved;
        };
    }

    private Flux<Removed> createRemovedItem(Representation representation) {
        return Flux.fromArray(representation.getChildTorrentTransmissionIds())
                .flatMap(id -> this.createRemovedItem(id, representation));
    }

    private Mono<Removed> createRemovedItem(Integer id, Representation representation) {
        return removedService.findRemovedByTransmissionId(id) //todo should not use id
                .switchIfEmpty(removedService.save(Removed.builder()
                        .name(representation.getName())
                        .seedTime(representation.getSeedTime())
                        .hardlinks(representation.getHardlinks())
                        .trackers(Arrays.stream(representation.getTrackers()).distinct().collect(Collectors.joining(", ")))
                        .transmissionId(id)
                        .build()));
    }

    public Mono<Void> removeTorrentFromTorrentClient(Integer transmissionId) {
        Mono<Void> voidMono;
        if (!nuclearrConfiguration.isRemoveEnabled()) {
            voidMono = Mono.fromRunnable(() -> log.info("Not removing torrent with id {} (removing is DISABLED)", transmissionId));
        } else {
            voidMono = torrentClientAdapter.removeTorrent(transmissionId);
        }
        return Mono.from(Flux.merge(voidMono, torrentService.deleteByTransmissionId(transmissionId)));
    }
}
