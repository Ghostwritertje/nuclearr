package be.ghostwritertje.nuclearr.internaltorrent;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class InternalTorrent {
    private Integer id;
    private String downloadDir;
    private List<InternalTorrentFile> files;
    private String hashString;
    private String name;

    private Long seedTime;
    private String trackerList;

}
