package si.aris.randomizer2.model;

import jakarta.persistence.*;

@Entity
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

    // Getterji in setterji
    public int getPodrocjeId() {
        return podrocjeId;
    }

    public void setPodrocjeId(int podrocjeId) {
        this.podrocjeId = podrocjeId;
    }

    public String getNaziv() {
        return naziv;
    }

    public void setNaziv(String naziv) {
        this.naziv = naziv;
    }

    public String getKoda() {
        return koda;
    }

    public void setKoda(String koda) {
        this.koda = koda;
    }

    public String getAngNaziv() {
        return angNaziv;
    }

    public void setAngNaziv(String angNaziv) {
        this.angNaziv = angNaziv;
    }
}
