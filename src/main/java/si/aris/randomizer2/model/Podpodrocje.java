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
@Table(name = "podpodrocje")
public class Podpodrocje {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "podpodrocje_id") // Poimenovanje stolpca v bazi
    private int podpodrocjeId;

    @Column(name = "podrocje_id", nullable = false) // Poimenovanje stolpca v bazi
    private int podrocjeId;

    @Column(nullable = false, length = 255)
    private String naziv;

    @Column(nullable = false, length = 255)
    private String koda;

}
