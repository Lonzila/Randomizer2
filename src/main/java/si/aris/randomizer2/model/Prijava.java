package si.aris.randomizer2.model;

import jakarta.persistence.*;

@Entity
@Table(name = "prijave")
public class Prijava {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int prijavaId;

    @Column(nullable = false)
    private int stevilkaPrijave;

    @Column(nullable = false, length = 255)
    private String vrstaProjekta;

    @Column(nullable = false)
    private int podpodrocjeId;

    private Integer dodatnoPodpodrocjeId;

    @Column(nullable = false, length = 500)
    private String naslov;

    @Column(nullable = false)
    private int steviloRecenzentov;

    @Column(nullable = false)
    private boolean interdisc;

    @Column(length = 255)
    private String partnerskaAgencija1;

    @Column(length = 255)
    private String partnerskaAgencija2;

    @Column(length = 500)
    private String angNaslov;

    @Column(length = 255)
    private String vodja;

    @Column(length = 255)
    private String sifraVodje;

    @Column(length = 255)
    private String nazivRO;

    @Column(length = 255)
    private String angNazivRO;

    @Column(length = 255)
    private String sifraRO;

    @ManyToOne
    @JoinColumn(name = "StatusOceneID", nullable = false)
    private StatusPrijav statusOcene;

    // Getterji in setterji
    public int getPrijavaId() {
        return prijavaId;
    }

    public void setPrijavaId(int prijavaId) {
        this.prijavaId = prijavaId;
    }

    public int getStevilkaPrijave() {
        return stevilkaPrijave;
    }

    public void setStevilkaPrijave(int stevilkaPrijave) {
        this.stevilkaPrijave = stevilkaPrijave;
    }

    public String getVrstaProjekta() {
        return vrstaProjekta;
    }

    public void setVrstaProjekta(String vrstaProjekta) {
        this.vrstaProjekta = vrstaProjekta;
    }

    public int getPodpodrocjeId() {
        return podpodrocjeId;
    }

    public void setPodpodrocjeId(int podpodrocjeId) {
        this.podpodrocjeId = podpodrocjeId;
    }

    public Integer getDodatnoPodpodrocjeId() {
        return dodatnoPodpodrocjeId;
    }

    public void setDodatnoPodpodrocjeId(Integer dodatnoPodpodrocjeId) {
        this.dodatnoPodpodrocjeId = dodatnoPodpodrocjeId;
    }

    public String getNaslov() {
        return naslov;
    }

    public void setNaslov(String naslov) {
        this.naslov = naslov;
    }

    public int getSteviloRecenzentov() {
        return steviloRecenzentov;
    }

    public void setSteviloRecenzentov(int steviloRecenzentov) {
        this.steviloRecenzentov = steviloRecenzentov;
    }

    public boolean isInterdisc() {
        return interdisc;
    }

    public void setInterdisc(boolean interdisc) {
        this.interdisc = interdisc;
    }

    public String getPartnerskaAgencija1() {
        return partnerskaAgencija1;
    }

    public void setPartnerskaAgencija1(String partnerskaAgencija1) {
        this.partnerskaAgencija1 = partnerskaAgencija1;
    }

    public String getPartnerskaAgencija2() {
        return partnerskaAgencija2;
    }

    public void setPartnerskaAgencija2(String partnerskaAgencija2) {
        this.partnerskaAgencija2 = partnerskaAgencija2;
    }

    public String getAngNaslov() {
        return angNaslov;
    }

    public void setAngNaslov(String angNaslov) {
        this.angNaslov = angNaslov;
    }

    public String getVodja() {
        return vodja;
    }

    public void setVodja(String vodja) {
        this.vodja = vodja;
    }

    public String getSifraVodje() {
        return sifraVodje;
    }

    public void setSifraVodje(String sifraVodje) {
        this.sifraVodje = sifraVodje;
    }

    public String getNazivRO() {
        return nazivRO;
    }

    public void setNazivRO(String nazivRO) {
        this.nazivRO = nazivRO;
    }

    public String getAngNazivRO() {
        return angNazivRO;
    }

    public void setAngNazivRO(String angNazivRO) {
        this.angNazivRO = angNazivRO;
    }

    public String getSifraRO() {
        return sifraRO;
    }

    public void setSifraRO(String sifraRO) {
        this.sifraRO = sifraRO;
    }

    public StatusPrijav getStatusOcene() {
        return statusOcene;
    }

    public void setStatusOcene(StatusPrijav statusOcene) {
        this.statusOcene = statusOcene;
    }


}
