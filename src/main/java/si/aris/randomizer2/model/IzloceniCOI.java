package si.aris.randomizer2.model;

import jakarta.persistence.*;

@Entity
@Table(name = "izlocenicoi")
public class IzloceniCOI {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int izlocenicoiId; // Uporabimo novo poimenovanje za id

    @Column(name = "prijava_id", nullable = false) // Prilagodimo ime stolpca
    private int prijavaId;

    @Column(name = "recenzent_id", nullable = false) // Prilagodimo ime stolpca
    private int recenzentId;

    @Column(name = "razlog") // Prilagodimo ime stolpca
    private String razlog;

    // Getterji in setterji
    public int getIzlocenicoiId() {
        return izlocenicoiId;
    }

    public void setIzlocenicoiId(int izlocenicoiId) {
        this.izlocenicoiId = izlocenicoiId;
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

    public String getRazlog() {
        return razlog;
    }

    public void setRazlog(String razlog) {
        this.razlog = razlog;
    }
}
