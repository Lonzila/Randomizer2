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
@Table(name = "podrocje")
public class Podrocje {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "podrocje_id") // Poimenovanje stolpca v bazi
    private int podrocjeId;

    @Column(nullable = false, length = 255)
    private String naziv;

    @Column(nullable = false, length = 255)
    private String koda;

    @Column(name = "ang_naziv", length = 255) // Poimenovanje stolpca v bazi
    private String angNaziv;

}
