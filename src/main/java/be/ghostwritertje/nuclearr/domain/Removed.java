package be.ghostwritertje.nuclearr.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "removed")
@Builder
public class Removed {

    @Id
    @Column("id")
    Integer id;
    @Column("name")
    String name;
    @Column("seed_time")
    Long seedTime;
    @Column("hardlinks")
    Integer hardlinks;
    @Column("trackers")
    String trackers;
    @Column("transmission_id")
    Integer transmissionId;
}
