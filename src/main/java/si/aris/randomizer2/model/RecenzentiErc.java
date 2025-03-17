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
    private int id;

    @ManyToOne
    @JoinColumn(name = "recenzent_id", nullable = false)
    private Recenzent recenzentId;

    @Column(name = "erc_id", nullable = false)
    private int ercPodrocjeId;

    @ManyToOne
    @JoinColumn(name = "recenzent_id", referencedColumnName = "recenzent_id", insertable = false, updatable = false)
    private Recenzent recenzent; // Dodajte povezavo do Recenzent entitete
}
