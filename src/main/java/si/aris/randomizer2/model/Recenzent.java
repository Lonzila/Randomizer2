package si.aris.randomizer2.model;

import jakarta.persistence.*;

@Entity
@Table(name = "recenzenti")
public class Recenzent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recenzent_id") // Poimenovanje stolpca v bazi
    private int recenzentId;

    @Column(nullable = false, name = "sifra") // Poimenovanje stolpca v bazi
    private int sifra;

    @Column(nullable = false, length = 255, name = "ime") // Poimenovanje stolpca v bazi
    private String ime;

    @Column(nullable = false, length = 255, name = "priimek") // Poimenovanje stolpca v bazi
    private String priimek;

    @Column(length = 255, name = "e_posta") // Poimenovanje stolpca v bazi
    private String ePosta;

    @Column(name = "max_stevilo_projektov", columnDefinition = "integer default 7") // Poimenovanje stolpca v bazi
    private int maxSteviloProjektov = 7;

    @Column(nullable = false, length = 255, name = "drzava") // Poimenovanje stolpca v bazi
    private String drzava;

    @Column(name = "porocevalec") // Poimenovanje stolpca v bazi
    private Boolean porocevalec;

    @Column(name = "odpoved_pred_dolocitvijo") // Poimenovanje stolpca v bazi
    private Boolean odpovedPredDolocitvijo;

    @Column(name = "prosta_mesta", columnDefinition = "integer default 7") // Poimenovanje stolpca v bazi
    private int prostaMesta = 7;

    // Getterji in setterji
    public int getRecenzentId() {
        return recenzentId;
    }

    public void setRecenzentId(int recenzentId) {
        this.recenzentId = recenzentId;
    }

    public int getSifra() {
        return sifra;
    }

    public void setSifra(int sifra) {
        this.sifra = sifra;
    }

    public String getIme() {
        return ime;
    }

    public void setIme(String ime) {
        this.ime = ime;
    }

    public String getPriimek() {
        return priimek;
    }

    public void setPriimek(String priimek) {
        this.priimek = priimek;
    }

    public String getePosta() {
        return ePosta;
    }

    public void setePosta(String ePosta) {
        this.ePosta = ePosta;
    }

    public int getMaxSteviloProjektov() {
        return maxSteviloProjektov;
    }

    public void setMaxSteviloProjektov(int maxSteviloProjektov) {
        this.maxSteviloProjektov = maxSteviloProjektov;
    }

    public String getDrzava() {
        return drzava;
    }

    public void setDrzava(String drzava) {
        this.drzava = drzava;
    }

    public Boolean getPorocevalec() {
        return porocevalec;
    }

    public void setPorocevalec(Boolean porocevalec) {
        this.porocevalec = porocevalec;
    }

    public Boolean getOdpovedPredDolocitvijo() {
        return odpovedPredDolocitvijo;
    }

    public void setOdpovedPredDolocitvijo(Boolean odpovedPredDolocitvijo) {
        this.odpovedPredDolocitvijo = odpovedPredDolocitvijo;
    }

    public int getProstaMesta() {
        return prostaMesta;
    }

    public void setProstaMesta(int prostaMesta) {
        this.prostaMesta = prostaMesta;
    }
}
