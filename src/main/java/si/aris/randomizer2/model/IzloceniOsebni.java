package si.aris.randomizer2.model;

import jakarta.persistence.*;

@Entity
@Table(name = "izloceniosebni")
public class IzloceniOsebni {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private int prijavaId;

    @Column(nullable = false)
    private int recenzentId;

    // Getterji in setterji
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPrijavaId() {
        return prijavaId;
    }

    public void setPrijavaId(int prijavaId) {
        this.prijavaId = prijavaId;
    }

    public int getRecenzentId() {
        return recenzentId;
    }

    public void setRecenzentId(int recenzentId) {
        this.recenzentId = recenzentId;
    }
}
