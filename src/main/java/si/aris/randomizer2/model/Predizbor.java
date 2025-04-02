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
@Table(name = "predizbor")
public class Predizbor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "predizbor_id") // Poimenovanje stolpca v bazi
    private int id;

    @Column(name = "prijava_id", nullable = false) // Poimenovanje stolpca v bazi
    private int prijavaId;

    @Column(name = "recenzent_id", nullable = false) // Poimenovanje stolpca v bazi
    private int recenzentId;

    @Column(nullable = false, length = 50)
    private String status = "NEOPREDELJEN";

    @Column(name = "primarni", nullable = false)
    private boolean primarni = true;

}
