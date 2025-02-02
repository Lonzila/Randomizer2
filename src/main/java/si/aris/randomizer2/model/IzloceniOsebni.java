package si.aris.randomizer2.model;

import jakarta.persistence.*;

@Entity
@Table(name = "izloceniosebni")
public class IzloceniOsebni {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int izloceniosebniId; // Poimenovanje id stolpca

    @Column(name = "prijava_id", nullable = false) // Prilagodimo ime stolpca
    private int prijavaId;

    @Column(name = "recenzent_id", nullable = false) // Prilagodimo ime stolpca
    private int recenzentId;

    // Getterji in setterji
    public int getIzloceniosebniId() {
        return izloceniosebniId;
    }

    public void setIzloceniosebniId(int izloceniosebniId) {
        this.izloceniosebniId = izloceniosebniId;
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
