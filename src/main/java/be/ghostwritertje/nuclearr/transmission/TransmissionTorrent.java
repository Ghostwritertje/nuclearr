package be.ghostwritertje.nuclearr.transmission;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;

import java.util.List;


@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class TransmissionTorrent {

    private Integer id;
    private String downloadDir;
    private List<TransmissionFile> files;
    private String hashString;
    private String name;
    private Long addedDate;

    private String trackerList;

    @Data
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TransmissionFile {
        private Long bytesCompleted;
        private Long length;
        private String name;
    }
}
