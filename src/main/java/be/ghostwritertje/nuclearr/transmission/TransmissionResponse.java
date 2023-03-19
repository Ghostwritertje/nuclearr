package be.ghostwritertje.nuclearr.transmission;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TransmissionResponse {
    private InnerResponse arguments;

    public TransmissionResponse() {
    }

    public InnerResponse getArguments() {
        return arguments;
    }

    public void setArguments(InnerResponse arguments) {
        this.arguments = arguments;
    }


    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class InnerResponse {
        private List<TransmissionTorrent> torrents;

        public InnerResponse() {
        }

        public List<TransmissionTorrent> getTorrents() {
            return torrents;
        }

        public void setTorrents(List<TransmissionTorrent> torrents) {
            this.torrents = torrents;
        }
    }
}
