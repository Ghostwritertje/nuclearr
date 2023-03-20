package be.ghostwritertje.nuclearr.internaltorrent;

import be.ghostwritertje.nuclearr.transmission.TransmissionTorrent;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

public interface InternalTorrent<X extends InternalTorrent.InternalTorrentFile> {

    Integer getId();

    String getDownloadDir();

    List<X> getFiles();

    String getHashString();

    String getName();

    //todo refactor to seedTime
    Long getAddedDate();

    String getTrackerList();

    interface InternalTorrentFile {

        Long getBytesCompleted();

        Long getLength();

        String getName();
    }
}
