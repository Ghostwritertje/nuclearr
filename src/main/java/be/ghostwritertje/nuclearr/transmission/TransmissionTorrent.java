package be.ghostwritertje.nuclearr.transmission;

import be.ghostwritertje.nuclearr.internaltorrent.InternalTorrent;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;


@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TransmissionTorrent implements InternalTorrent<TransmissionTorrent.TransmissionFile> {

    private Integer id;
    private String downloadDir;
    private List<TransmissionFile> files;
    private String hashString;
    private String name;
    private Long addedDate;

    private String trackerList;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TransmissionFile implements InternalTorrentFile {
        private Long bytesCompleted;
        private Long length;
        private String name;
    }
}
