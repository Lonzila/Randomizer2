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
@Table(name = "izloceniosebni")
public class IzloceniOsebni {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int izloceniosebniId; // Poimenovanje id stolpca

    @Column(name = "prijava_id", nullable = false) // Prilagodimo ime stolpca
    private int prijavaId;

    @Column(name = "recenzent_id", nullable = false) // Prilagodimo ime stolpca
    private int recenzentId;

}
