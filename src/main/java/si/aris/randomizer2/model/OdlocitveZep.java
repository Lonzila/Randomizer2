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
@Table(name = "odlocitve_zep")
public class OdlocitveZep {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private int prijavaId;

    @Column(nullable = false)
    private int recenzentId;

    @Column(nullable = false, length = 50)
    private String odlocitev;

    private String komentar;

}
