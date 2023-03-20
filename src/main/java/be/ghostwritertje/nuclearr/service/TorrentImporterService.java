package be.ghostwritertje.nuclearr.service;

import be.ghostwritertje.nuclearr.fileitem.FileItem;
import be.ghostwritertje.nuclearr.fileitemoccurrence.FileItemOccurrence;
import be.ghostwritertje.nuclearr.torrent.Torrent;
import be.ghostwritertje.nuclearr.tracker.Tracker;
import be.ghostwritertje.nuclearr.fileitemoccurrence.FileItemOccurrenceService;
import be.ghostwritertje.nuclearr.fileitem.FileItemService;
import be.ghostwritertje.nuclearr.torrent.TorrentService;
import be.ghostwritertje.nuclearr.tracker.TrackerService;
import be.ghostwritertje.nuclearr.transmission.TransmissionAdapter;
import be.ghostwritertje.nuclearr.transmission.TransmissionTorrent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class TorrentImporterService {

    public static final Pattern TRACKER_PATTERN = Pattern.compile("\\/\\/(\\w+\\.\\w+\\.*\\w*)(\\:\\d+)*\\/");
    private final TorrentService torrentService;
    private final FileItemService fileItemService;
    private final FileItemOccurrenceService fileItemOccurrenceService;

    private final TrackerService trackerService;


    private final TransmissionAdapter transmissionAdapter;

    public Flux<Torrent> importAllTorrents() {
        return transmissionAdapter.retrieveAllTorrents()
                .flatMapMany(list -> {
                            Flux<Torrent> torrentsToSave = Flux.fromStream(list.stream())
                                    .map(t -> Torrent.builder()
                                            .name(t.getName())
                                            .hash(t.getHashString())
                                            .transmissionId(t.getId())
                                            .seedTime(LocalDateTime.now().atZone(ZoneId.systemDefault()).toEpochSecond() - t.getAddedDate())
                                            .build());
                            return torrentService.saveAll(torrentsToSave);
                        }
                );
    }

    public Flux<FileItemOccurrence> importFileItems() {
        Flux<FileItemOccurrence> fileItemOccurrenceFlux = this.importAllTorrents()
                .flatMap(torrent -> {
                    Mono<TransmissionTorrent> details = transmissionAdapter.getDetails(torrent.getTransmissionId());

                    Flux<Tracker> trackersToSave = details.flatMapMany(TorrentImporterService::extractTrackerList)
                            .map(trackerString -> Tracker.builder()
                                    .torrentId(torrent.getId())
                                    .name(trackerString)
                                    .build());
                    Flux<Tracker> trackerFlux = trackerService.saveAll(trackersToSave);
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
                                .build()));

        return this.fileItemOccurrenceService.saveAll(fileItemOccurrenceFlux);
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
        return this.trackerService.deleteAll().then(this.fileItemOccurrenceService.deleteAll())
                .then(this.fileItemService.deleteAll())
                .then(this.torrentService.deleteAll());
    }
}
