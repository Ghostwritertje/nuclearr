package be.ghostwritertje.nuclearr.service;

import be.ghostwritertje.nuclearr.fileitemoccurrence.FileItemOccurrence;
import be.ghostwritertje.nuclearr.torrent.Torrent;
import be.ghostwritertje.nuclearr.presentation.ChildTorrentDto;
import be.ghostwritertje.nuclearr.presentation.FileDto;
import be.ghostwritertje.nuclearr.presentation.MasterTorrentDto;
import be.ghostwritertje.nuclearr.presentation.TrackerDto;
import be.ghostwritertje.nuclearr.fileitemoccurrence.FileItemOccurrenceService;
import be.ghostwritertje.nuclearr.fileitem.FileItemService;
import be.ghostwritertje.nuclearr.torrent.TorrentService;
import be.ghostwritertje.nuclearr.tracker.TrackerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RepresentationService {

    private final TorrentService torrentService;
    private final FileItemOccurrenceService fileItemOccurrenceService;
    private final FileItemService fileItemService;
    private final TrackerService trackerService;

    public Flux<MasterTorrentDto> represent() {
        return Flux.empty();
//        return this.torrentService.findAll()
//                .flatMap(torrent -> {
//                    Flux<FileItemOccurrence> fileItemOccurrencesByTorrentId = fileItemOccurrenceService.findFileItemOccurrencesByTorrentId(torrent.getId());
//
//
//                    Flux<FileItemOccurrence> childFileItems = fileItemOccurrencesByTorrentId
//                            .map(FileItemOccurrence::getFileItemId)
//                            .collectList()
//                            .map(fileIds -> this.fileItemOccurrenceService.findFileItemOccurrencesByFileItemIdInAndTorrentIdIsNot(fileIds, torrent.getId()))
//                            .flatMapMany(Flux::concat);
//                    Flux<Torrent> childTorrentFlux = childFileItems
//                            .map(FileItemOccurrence::getTorrentId)
//                            .flatMap(torrentService::getTorrentById);
//                    Mono<List<ChildTorrentDto>> masterFlux = Flux.zip(childTorrentFlux, getFiles(childFileItems), getTrackers(childTorrentFlux))
//                            .map(tuple2 -> (ChildTorrentDto) ChildTorrentDto.builder()
//                                    .id(tuple2.getT1().getId())
//                                    .name(tuple2.getT1().getName())
//                                    .seedTime(tuple2.getT1().getSeedTime())
//                                    .transmissionId(tuple2.getT1().getTransmissionId())
//                                    .files(tuple2.getT2())
//                                    .trackerList(tuple2.getT3())
//                                    .build())
//                            .collectList();
//                    return Flux.zip(masterFlux, getFiles(fileItemOccurrencesByTorrentId), getTrackerList(torrent))
//                            .map(tuple2 -> MasterTorrentDto.builder()
//                                    .id(torrent.getId())
//                                    .name(torrent.getName())
//                                    .transmissionId(torrent.getTransmissionId())
//                                    .seedTime(torrent.getSeedTime())
//                                    .childTorrentDtos(tuple2.getT1())
//                                    .files(tuple2.getT2())
//                                    .trackerList(tuple2.getT3())
//                                    .build()
//                            );
//                });

    }

    private Mono<List<TrackerDto>> getTrackerList(Torrent torrent) {
        return this.trackerService.findAllByTorrentId(torrent.getId())
                .map(tracker -> TrackerDto.builder()
                        .name(tracker.getName())
                        .build())
                .collectList();
    }

    private Mono<List<TrackerDto>> getTrackers(Flux<Torrent> torrentFlux) {
        return torrentFlux.flatMap(torrent -> this.trackerService.findAllByTorrentId(torrent.getId()))
                .map(tracker -> TrackerDto.builder()
                        .name(tracker.getName())
                        .build())
                .collectList();
    }
    private Mono<List<FileDto>> getFiles(Flux<FileItemOccurrence> fileItemOccurrencesByTorrentId) {
        return Mono.just(List.of());
    }
}
