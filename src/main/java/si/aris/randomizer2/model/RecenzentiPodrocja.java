package si.aris.randomizer2.model;

import jakarta.persistence.*;

@Entity
@Table(name = "recenzentipodrocja")
public class RecenzentiPodrocja {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private int recenzentId;

    @Column(nullable = false)
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
