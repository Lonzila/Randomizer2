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
@Table(name = "recenzentizavrnitve")
public class RecenzentiZavrnitve {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id") // Poimenovanje stolpca v bazi
    private int id;

    @Column(nullable = false, name = "prijava_id") // Poimenovanje stolpca v bazi
    private int prijavaId;

    @Column(nullable = false, name = "recenzent_id") // Poimenovanje stolpca v bazi
    private int recenzentId;

    @Column(length = 255)
    private String razlog;


}
