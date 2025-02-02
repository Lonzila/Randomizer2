package si.aris.randomizer2.model;

import jakarta.persistence.*;

@Entity
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

    // Getterji in setterji
    public int getPodpodrocjeId() {
        return podpodrocjeId;
    }

    public void setPodpodrocjeId(int podpodrocjeId) {
        this.podpodrocjeId = podpodrocjeId;
    }

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
}
