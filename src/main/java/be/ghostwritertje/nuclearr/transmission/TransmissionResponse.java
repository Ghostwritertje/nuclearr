package be.ghostwritertje.nuclearr.transmission;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransmissionResponse {
    private InnerResponse arguments;


    public InnerResponse getArguments() {
        return arguments;
    }

    public void setArguments(InnerResponse arguments) {
        this.arguments = arguments;
    }


    @JsonIgnoreProperties(ignoreUnknown = true)
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InnerResponse {
        private List<TransmissionTorrent> torrents;

        public List<TransmissionTorrent> getTorrents() {
            return torrents;
        }

        public void setTorrents(List<TransmissionTorrent> torrents) {
            this.torrents = torrents;
        }
    }
}
