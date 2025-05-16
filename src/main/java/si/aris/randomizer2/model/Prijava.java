package si.aris.randomizer2.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "prijave")
public class Prijava {

    @Id
    //@GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "prijava_id") // Poimenovanje stolpca v bazi
    private int prijavaId;

    @Column(name = "stevilka_prijave", nullable = false) // Poimenovanje stolpca v bazi
    private int stevilkaPrijave;

    @Column(name = "vrsta_projekta", nullable = false, length = 255) // Poimenovanje stolpca v bazi
    private String vrstaProjekta;

    @ManyToOne
    @JoinColumn(name = "podpodrocje_id", nullable = false)
    private Podpodrocje podpodrocje;

    @ManyToOne
    @JoinColumn(name = "dodatno_podpodrocje_id") // Poimenovanje stolpca v bazi
    private Podpodrocje dodatnoPodpodrocje;

    @ManyToOne
    @JoinColumn(name = "erc_id", nullable = false)
    private ErcPodrocje ercPodrocje;

    @ManyToOne
    @JoinColumn(name = "dodatno_erc_id")
    private ErcPodrocje dodatnoErcPodrocje;

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
    @JoinColumn(name = "status_prijav_id") // Poimenovanje stolpca v bazi
    private StatusPrijav statusPrijav;

    @Column(name = "ang_naslov", length = 500) // Poimenovanje stolpca v bazi
    private String angNaslov;

    @Column(name = "partnerska_agencija1", length = 255) // Poimenovanje stolpca v bazi
    private String partnerskaAgencija1;

    @Column(name = "partnerska_agencija2", length = 255) // Poimenovanje stolpca v bazi
    private String partnerskaAgencija2;

    @Column(name = "sifra_vodje", length = 255) // Poimenovanje stolpca v bazi
    private String sifraVodje;


}
