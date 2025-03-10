package si.aris.randomizer2.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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

    @Column(name = "prijave_predizbor") // Poimenovanje stolpca v bazi
    private int prijavePredizbor = 0;

    @Column(nullable = false, length = 255, name = "drzava") // Poimenovanje stolpca v bazi
    private String drzava;

    @Column(name = "porocevalec") // Poimenovanje stolpca v bazi
    private Boolean porocevalec;

    @Column(name = "odpoved_pred_dolocitvijo") // Poimenovanje stolpca v bazi
    private Boolean odpovedPredDolocitvijo;

    @Column(name = "prosta_mesta", columnDefinition = "integer default 7") // Poimenovanje stolpca v bazi
    private int prostaMesta = 7;

    // Getterji in setterji
    @Getter
    @OneToMany(mappedBy = "recenzent")
    private List<RecenzentiPodrocja> recenzentiPodrocja;

    @Getter
    @OneToMany(mappedBy = "recenzent")
    private List<RecenzentiErc> recenzentiErc;

}
