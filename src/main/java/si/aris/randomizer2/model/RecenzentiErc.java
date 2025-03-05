package si.aris.randomizer2.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "recenzenti_erc")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RecenzentiErc {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "recenzent_id", nullable = false)
    private Recenzent recenzent;

    @ManyToOne
    @JoinColumn(name = "erc_id", nullable = false)
    private ErcPodrocje ercPodroce;
}
