package si.aris.randomizer2.model;

import jakarta.persistence.*;

@Entity
@Table(name = "prijave")
public class Prijava {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "prijava_id") // Poimenovanje stolpca v bazi
    private int prijavaId;

    @Column(name = "stevilka_prijave", nullable = false) // Poimenovanje stolpca v bazi
    private int stevilkaPrijave;

    @Column(name = "vrsta_projekta", nullable = false, length = 255) // Poimenovanje stolpca v bazi
    private String vrstaProjekta;

    @Column(name = "podpodrocje_id", nullable = false) // Poimenovanje stolpca v bazi
    private int podpodrocjeId;

    @Column(name = "dodatno_podpodrocje_id") // Poimenovanje stolpca v bazi
    private Integer dodatnoPodpodrocjeId;

    @Column(name = "naslov", nullable = false, length = 500) // Poimenovanje stolpca v bazi
    private String naslov;

    @Column(name = "stevilo_recenzentov", nullable = false) // Poimenovanje stolpca v bazi
    private int steviloRecenzentov;

    @Column(name = "interdisc", nullable = false) // Poimenovanje stolpca v bazi
    private boolean interdisc;

    @Column(name = "vodja", length = 255) // Poimenovanje stolpca v bazi
    private String vodja;

    @Column(name = "naziv_ro", length = 255) // Poimenovanje stolpca v bazi
    private String nazivRO;

    @Column(name = "ang_naziv_ro", length = 255) // Poimenovanje stolpca v bazi
    private String angNazivRO;

    @Column(name = "sifra_ro", length = 255) // Poimenovanje stolpca v bazi
    private String sifraRO;

    @ManyToOne
    @JoinColumn(name = "status_ocene_id") // Poimenovanje stolpca v bazi
    private StatusPrijav statusOcene;

    @Column(name = "ang_naslov", length = 500) // Poimenovanje stolpca v bazi
    private String angNaslov;

    @Column(name = "partnerska_agencija1", length = 255) // Poimenovanje stolpca v bazi
    private String partnerskaAgencija1;

    @Column(name = "partnerska_agencija2", length = 255) // Poimenovanje stolpca v bazi
    private String partnerskaAgencija2;

    @Column(name = "sifra_vodje", length = 255) // Poimenovanje stolpca v bazi
    private String sifraVodje;


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

    public String getVodja() {
        return vodja;
    }

    public void setVodja(String vodja) {
        this.vodja = vodja;
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

    public String getAngNaslov() {
        return angNaslov;
    }

    public void setAngNaslov(String angNaslov) {
        this.angNaslov = angNaslov;
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

    public String getSifraVodje() {
        return sifraVodje;
    }

    public void setSifraVodje(String sifraVodje) {
        this.sifraVodje = sifraVodje;
    }

}
