package be.ghostwritertje.nuclearr.tracker;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tracker")
public class Tracker {

    @Id
    @Column("id")
    Integer id;

    @Column("name")
    String name;

    @Column("torrent_id")
    Integer torrentId;
}
