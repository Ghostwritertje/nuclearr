package be.ghostwritertje.nuclearr.representation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "representation")
public class Representation {

    @Id
    @Column("name")
    String name;

    @Column("child_torrent_transmission_ids")
    Integer[] childTorrentTransmissionIds;

    @Column("child_tracker_names")
    String[] trackers;

    @Column("hardlinks")
    Integer hardlinks;

    @Column("seed_time")
    Long seedTime;

}
