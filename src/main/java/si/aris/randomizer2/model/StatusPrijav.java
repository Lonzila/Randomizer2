package si.aris.randomizer2.model;

import jakarta.persistence.*;


@Entity
@Table(name = "StatusPrijav")
public class StatusPrijav {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false, unique = true, length = 50)
    private String naziv;

    @Column(length = 255)
    private String opis;

    // Getterji in setterji...
}

