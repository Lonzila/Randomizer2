package si.aris.randomizer2.model;

import jakarta.persistence.*;

@Entity
@Table(name = "recenzentipodrocja")
public class RecenzentiPodrocja {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id") // Poimenovanje stolpca v bazi
    private int id;

    @Column(nullable = false, name = "recenzent_id") // Poimenovanje stolpca v bazi
    private int recenzentId;

    @Column(nullable = false, name = "podpodrocje_id") // Poimenovanje stolpca v bazi
    private int podpodrocjeId;

    // Getterji in setterji
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getRecenzentId() {
        return recenzentId;
    }

    public void setRecenzentId(int recenzentId) {
        this.recenzentId = recenzentId;
    }

    public int getPodpodrocjeId() {
        return podpodrocjeId;
    }

    public void setPodpodrocjeId(int podpodrocjeId) {
        this.podpodrocjeId = podpodrocjeId;
    }
}
