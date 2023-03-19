package be.ghostwritertje.nuclearr.transmission;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransmissionRequest {
    public static final String TORRENT_GET = "torrent-get";
    public static final String TORRENT_REMOVE = "torrent-remove";

    @Builder.Default
    private String method = TORRENT_GET;
    @Builder.Default
    private TransmissionArguments arguments = new TransmissionArguments();
}
