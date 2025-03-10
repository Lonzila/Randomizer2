package si.aris.randomizer2.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "recenzentipodrocja")
public class RecenzentiPodrocja {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id") // Poimenovanje stolpca v bazi
    private int id;

    @Column(nullable = false, name = "recenzent_id") // Poimenovanje stolpca v bazi
    private int recenzentId;

    @Column(nullable = false, name = "podpodrocje_id") // Poimenovanje stolpca v bazi
    private int podpodrocjeId;

    @ManyToOne
    @JoinColumn(name = "recenzent_id", referencedColumnName = "recenzent_id", insertable = false, updatable = false)
    private Recenzent recenzent; // Dodajte povezavo do Recenzent entitete


}
