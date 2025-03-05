package si.aris.randomizer2.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "erc_podrocja")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ErcPodrocje {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "erc_id")
    private Long ercId;

    @Column(nullable = false, unique = true)
    private String koda;
}
