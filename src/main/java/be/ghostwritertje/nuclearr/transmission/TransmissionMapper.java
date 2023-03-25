package be.ghostwritertje.nuclearr.transmission;

import be.ghostwritertje.nuclearr.internaltorrent.InternalTorrent;
import be.ghostwritertje.nuclearr.internaltorrent.InternalTorrentFile;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class TransmissionMapper {

    public InternalTorrent mapInternalTorrent(TransmissionTorrent transmissionTorrent) {
        return InternalTorrent.builder()
                .id(transmissionTorrent.getId())
                .downloadDir(transmissionTorrent.getDownloadDir())
                .files(transmissionTorrent.getFiles().stream().map(this::mapInternalTorrentFile).collect(Collectors.toList()))
                .hashString(transmissionTorrent.getHashString())
                .name(transmissionTorrent.getName())
                .addedDate(transmissionTorrent.getAddedDate())
                .trackerList(transmissionTorrent.getTrackerList())
                .build();
    }

    private InternalTorrentFile mapInternalTorrentFile(TransmissionTorrent.TransmissionFile transmissionFile) {
        return InternalTorrentFile.builder()
                .bytesCompleted(transmissionFile.getBytesCompleted())
                .length(transmissionFile.getLength())
                .name(transmissionFile.getName())
                .build();
    }
}
