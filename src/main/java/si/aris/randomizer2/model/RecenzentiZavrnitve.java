package si.aris.randomizer2.model;

import jakarta.persistence.*;

@Entity
@Table(name = "recenzentizavrnitve")
public class RecenzentiZavrnitve {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private int prijavaId;

    @Column(nullable = false)
    private int recenzentId;

    @Column(length = 255)
    private String razlog;

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

    public String getRazlog() {
        return razlog;
    }

    public void setRazlog(String razlog) {
        this.razlog = razlog;
    }
}
