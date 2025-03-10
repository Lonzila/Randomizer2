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
@Table(name = "status_prijav")
public class StatusPrijav {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "status_prijav_id") // Poimenovanje stolpca v bazi
    private int id;

    @Column(nullable = false, unique = true, length = 50)
    private String naziv;

    @Column(length = 255)
    private String opis;

}
