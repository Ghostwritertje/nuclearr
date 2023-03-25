package be.ghostwritertje.nuclearr.service;

import be.ghostwritertje.nuclearr.fileitem.FileItem;
import be.ghostwritertje.nuclearr.fileitem.FileItemService;
import be.ghostwritertje.nuclearr.fileitemoccurrence.FileItemOccurrence;
import be.ghostwritertje.nuclearr.fileitemoccurrence.FileItemOccurrenceService;
import be.ghostwritertje.nuclearr.internaltorrent.InternalTorrent;
import be.ghostwritertje.nuclearr.internaltorrent.InternalTorrentFile;
import be.ghostwritertje.nuclearr.internaltorrent.TorrentClientAdapter;
import be.ghostwritertje.nuclearr.torrent.Torrent;
import be.ghostwritertje.nuclearr.torrent.TorrentService;
import be.ghostwritertje.nuclearr.tracker.Tracker;
import be.ghostwritertje.nuclearr.tracker.TrackerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.ConnectableFlux;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class TorrentImporterService {
    public static final Pattern TRACKER_PATTERN = Pattern.compile("//([^/]+)(:\\d+)/");
    private final TorrentClientAdapter torrentClientAdapter;
    private final TorrentService torrentService;
    private final TrackerService trackerService;
    private final FileItemService fileItemService;
    private final FileItemOccurrenceService fileItemOccurrenceService;

    private static Flux<String> extractTrackerList(InternalTorrent internalTorrent) {
        //todo this mapping should occurr in clientAdapter
        return Flux.fromStream(Arrays.stream(internalTorrent.getTrackerList().split("\n"))
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

    private static Tracker mapTracker(Torrent torrent, String s) {
        return Tracker.builder()
                .torrentId(torrent.getId())
                .name(s)
                .build();
    }

    private static FileItemOccurrence mapFileItemOccurrence(Torrent torrent, String path) {
        return FileItemOccurrence.builder()
                .fileItemPath(path)
                .torrentId(torrent.getId())
                .build();
    }

    private static FileItem mapFileItem(InternalTorrent internalTorrent, InternalTorrentFile internalTorrentFile) {
        return FileItem.builder()
                .path(internalTorrent.getDownloadDir() + "/" + internalTorrentFile.getName()) //todo: path mapping should occurr in clientAdapter
                .build();
    }

    private static Torrent mapInternalTorrent(InternalTorrent internalTorrent) {
        return Torrent.builder()
                .name(internalTorrent.getName())
                .hash(internalTorrent.getHashString())
                .transmissionId(internalTorrent.getId())
                .seedTime(internalTorrent.getSeedTime())
                .build();
    }

    public Mono<Void> importTorrents() {
        log.info("Importing torrents");
        ConnectableFlux<InternalTorrent> internalTorrentFlux = torrentClientAdapter.getTorrents()
                .publish();

        ConnectableFlux<Torrent> torrentFlux = internalTorrentFlux
                .map(TorrentImporterService::mapInternalTorrent)
                .buffer(250)
                .doOnEach(ignored -> log.info("saving 250 Torrents {}", ignored.getType()))
                .flatMap(torrentService::saveAll)
                .publish();


        Flux<FileItem> fileItemFlux = internalTorrentFlux.flatMap(internalTorrent -> Flux.fromIterable(internalTorrent.getFiles()))
                .map(InternalTorrentFile::getName)
                .map(s -> FileItem.builder().path(s).build())
                .distinct(FileItem::getPath)
                .buffer(250)
                .doOnEach(ignored -> log.info("saving 250 FileItems {}", ignored.getType()))
                .flatMap(fileItemService::saveAll);

        Flux<FileItemOccurrence> fileItemOccurrenceFlux = Flux.zip(internalTorrentFlux.onBackpressureBuffer(), torrentFlux.onBackpressureBuffer())
                .map(tuple -> {
                    log.debug("tuple {} - {}", tuple.getT1().getId(), tuple.getT2().getTransmissionId());
                    return tuple;
                })
                .flatMap(tuple2 -> Flux.fromStream(tuple2.getT1().getFiles().stream().map(file -> mapFileItemOccurrence(tuple2.getT2(), file.getName()))))
                .buffer(250)
                .doOnEach(ignored -> log.info("saving 250 fileItemOccurrences {}", ignored.getType()))
                .flatMap(fileItemOccurrenceService::saveAll);

        Flux<Tracker> trackerFlux = Flux.zip(internalTorrentFlux.onBackpressureBuffer(), torrentFlux.onBackpressureBuffer())
                .flatMap(tuple -> extractTrackerList(tuple.getT1()).map(tracker -> mapTracker(tuple.getT2(), tracker)))
                .buffer(250)
                .doOnEach(ignored -> log.info("saving 250 trackers {}", ignored.getType()))
                .flatMap(trackerService::saveAll);

        internalTorrentFlux.connect();
        torrentFlux.connect();

        return Flux.zip(fileItemFlux.then(), fileItemOccurrenceFlux.then(), trackerFlux.then())
                .then(Mono.fromRunnable(() -> log.info("finished importing everything")));
    }
}
