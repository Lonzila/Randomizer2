package si.aris.randomizer2.model;

import jakarta.persistence.*;

@Entity
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

    // Getterji in setterji...
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNaziv() {
        return naziv;
    }

    public void setNaziv(String naziv) {
        this.naziv = naziv;
    }

    public String getOpis() {
        return opis;
    }

    public void setOpis(String opis) {
        this.opis = opis;
    }
}
