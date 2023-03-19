package be.ghostwritertje.nuclearr.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "torrent")
@Builder
public class Torrent {

    @Id
    @Column("id")
    Integer id;
    @Column("name")
    String name;
    @Column("hash_name")
    String hash;
    @Column("transmission_id")
    Integer transmissionId;

    @Column("seed_time")
    Long seedTime;


    @Transient
    private List<FileItem> fileItems;
}
