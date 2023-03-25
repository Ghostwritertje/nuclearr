package be.ghostwritertje.nuclearr.internaltorrent;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InternalTorrentFile {
    private Long bytesCompleted;
    private Long length;
    private String name;
}