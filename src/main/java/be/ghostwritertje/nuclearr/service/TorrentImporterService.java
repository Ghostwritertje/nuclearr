package be.ghostwritertje.nuclearr.service;

import be.ghostwritertje.nuclearr.domain.FileItem;
import be.ghostwritertje.nuclearr.domain.FileItemOccurrence;
import be.ghostwritertje.nuclearr.domain.Torrent;
import be.ghostwritertje.nuclearr.domain.Tracker;
import be.ghostwritertje.nuclearr.repo.FileItemOccurrenceRepository;
import be.ghostwritertje.nuclearr.repo.FileItemRepository;
import be.ghostwritertje.nuclearr.repo.TorrentRepository;
import be.ghostwritertje.nuclearr.repo.TrackerRepository;
import be.ghostwritertje.nuclearr.transmission.TransmissionAdapter;
import be.ghostwritertje.nuclearr.transmission.TransmissionTorrent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TorrentImporterService {

    public static final Pattern TRACKER_PATTERN = Pattern.compile("\\/\\/(\\w+\\.\\w+\\.*\\w*)(\\:\\d+)*\\/");
    private final TorrentRepository torrentRepository;
    private final FileItemRepository fileItemRepository;
    private final FileItemService fileItemService;
    private final FileItemOccurrenceRepository fileItemOccurrenceRepository;

    private final TrackerRepository trackerRepository;


    private final TransmissionAdapter transmissionAdapter;

    public Flux<Torrent> importAllTorrents() {
        return transmissionAdapter.retrieveAllTorrents()
                .flatMapMany(list -> Flux.fromStream(list.stream())
                        .map(t -> Torrent.builder()
                                .name(t.getName())
                                .hash(t.getHashString())
                                .transmissionId(t.getId())
                                .seedTime(LocalDateTime.now().atZone(ZoneId.systemDefault()).toEpochSecond() - t.getAddedDate())
                                .build())
                        .buffer(200)
                        .map(torrentRepository::saveAll)
                        .flatMap(Flux::concat)
                );
    }

    public Flux<FileItemOccurrence> importFileItems() {
        return this.importAllTorrents()
                .flatMap(torrent -> {
                    Mono<TransmissionTorrent> details = transmissionAdapter.getDetails(torrent.getTransmissionId());

                    Flux<Tracker> trackerFlux = details.flatMapMany(TorrentImporterService::extractTrackerList)
                            .map(trackerString -> Tracker.builder()
                                    .torrentId(torrent.getId())
                                    .name(trackerString)
                                    .build())
                            .map(trackerRepository::save)
                            .flatMap(Flux::concat);
                    return trackerFlux.then(details.map(t -> Tuples.of(torrent.getId(), t)));
                })
                .flatMap(tuple2 -> Flux.fromStream(tuple2.getT2().getFiles().stream()
                                .map(transmissionFile -> FileItem.builder()
                                        .path(tuple2.getT2().getDownloadDir() + "/" + transmissionFile.getName())
                                        .build()))
                        .map(fileItemService::mergeFileItem)
                        .flatMap(Flux::concat)
                        .map(fileItem -> FileItemOccurrence.builder()
                                .fileItemId(fileItem.getId())
                                .torrentId(tuple2.getT1())
                                .build()))
                .buffer(200)
                .map(fileItemOccurrenceRepository::saveAll)
                .flatMap(Flux::concat);
    }

    private static Flux<String> extractTrackerList(TransmissionTorrent transmissionTorrent) {
        return Flux.fromStream(Arrays.stream(transmissionTorrent.getTrackerList().split("\n"))
                .map(tracker -> {
                    Matcher matcher = TRACKER_PATTERN.matcher(tracker);
                    if (matcher.find()) {
                        return matcher.group(1);
                    } else {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .filter(StringUtils::hasText));
    }

    public Mono<Void> deleteAll() {
        return this.trackerRepository.deleteAll().then(this.fileItemOccurrenceRepository.deleteAll())
                .then(this.fileItemRepository.deleteAll())
                .then(this.torrentRepository.deleteAll());
    }
}
