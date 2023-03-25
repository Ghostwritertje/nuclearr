package be.ghostwritertje.nuclearr.internaltorrent;

import be.ghostwritertje.nuclearr.fileitemoccurrence.FileItemOccurrence;
import be.ghostwritertje.nuclearr.torrent.Torrent;
import be.ghostwritertje.nuclearr.tracker.Tracker;
import org.springframework.stereotype.Component;

@Component
public class InternalTorrentMapper {

    public Tracker mapTracker(Torrent torrent, String name) {
        return Tracker.builder()
                .torrentId(torrent.getId())
                .name(name)
                .build();
    }

    public FileItemOccurrence mapFileItemOccurrence(Torrent torrent, String path) {
        return FileItemOccurrence.builder()
                .fileItemPath(path)
                .torrentId(torrent.getId())
                .build();
    }

    public Torrent mapInternalTorrent(InternalTorrent internalTorrent) {
        return Torrent.builder()
                .name(internalTorrent.getName())
                .hash(internalTorrent.getHashString())
                .transmissionId(internalTorrent.getId())
                .seedTime(internalTorrent.getSeedTime())
                .build();
    }
}
