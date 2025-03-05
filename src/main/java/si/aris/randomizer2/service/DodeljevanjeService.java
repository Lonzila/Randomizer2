package si.aris.randomizer2.service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import si.aris.randomizer2.model.*;
import si.aris.randomizer2.repository.*;

import java.util.*;
import java.util.stream.Collectors;

import static si.aris.randomizer2.model.PartnerskeAgencije.PARTNERSKE_AGENCIJE_MAP;

@Service
public class DodeljevanjeService {

    @Autowired
    private PrijavaRepository prijavaRepository;

    @Autowired
    private RecenzentRepository recenzentRepository;

    @Autowired
    private PredizborRepository predizborRepository;

    @Autowired
    private IzloceniCOIRepository izloceniCOIRepository;

    @Autowired
    private IzloceniOsebniRepository izloceniOsebniRepository;

    @Autowired
    private StatusPrijavRepository statusPrijavRepository;

    private static final Logger logger = LoggerFactory.getLogger(DodeljevanjeService.class);

    public void predizbor() {

        StatusPrijav statusBrezRecenzenta = statusPrijavRepository.findByNaziv("BREZ RECENZENTA")
                .orElseThrow(() -> new RuntimeException("Status 'BREZ RECENZENTA' ni bil najden."));

        // Pridobimo vse prijave s statusom "BREZ RECENZENTA"
        List<Prijava> prijave = prijavaRepository.findByStatusPrijavIdIn(List.of(statusBrezRecenzenta.getId()));

        // 2. Sortiramo prijave po podpodročjih
        Map<String, List<Prijava>> prijavePoPodpodrocjih = new HashMap<>();
        for (Prijava prijava : prijave) {
            // Sestavimo ključ iz podpodročja in dodatnega podpodročja
            String key = prijava.getPodpodrocjeId() + "-" + (prijava.getDodatnoPodpodrocjeId() != null ? prijava.getDodatnoPodpodrocjeId() : "none");

            // Dodamo prijavo v ustrezno skupino
            prijavePoPodpodrocjih
                    .computeIfAbsent(key, k -> new ArrayList<>())
                    .add(prijava);
        }

        //int totalGroups = 0;
        for (Map.Entry<String, List<Prijava>> entry : prijavePoPodpodrocjih.entrySet()) {
            String key = entry.getKey();
            List<Prijava> prijavePodpodrocja = entry.getValue();
            int steviloPrijav = prijavePodpodrocja.size();

            // Izpisujemo informacije o podpodročju in številu prijav (skupina)
            //logger.info("Skupina (Podpodročje in Dodatno Podpodročje): {} ima {} prijav.", key, steviloPrijav);

            // Razdelimo skupine, če ima skupina več kot 10 prijav
            List<List<Prijava>> razdeljeneSkupine = new ArrayList<>();
            for (int i = 0; i < steviloPrijav; i += 10) {
                // Za vsako skupino, ki je večja od 10, ustvarimo novo skupino z največ 10 prijavami
                List<Prijava> skupina = prijavePodpodrocja.subList(i, Math.min(i + 10, steviloPrijav));
                razdeljeneSkupine.add(skupina);
            }

            //totalGroups += razdeljeneSkupine.size();
            //logger.info("Za podpodročje {} smo ustvarili {} skupin.", key, razdeljeneSkupine.size());

            for (int i = 0; i < razdeljeneSkupine.size(); i++) {
                List<Prijava> skupina = razdeljeneSkupine.get(i);
                //logger.info("Skupina {}: {} prijav.", i + 1, skupina.size());
            }

            // Dodelimo recenzente za vsako skupino
            for (List<Prijava> prijaveSkupine : razdeljeneSkupine) {
                dodeliRecenzenteZaSkupino(prijaveSkupine);
            }
        }

        //logger.info("Skupno število ustvarjenih skupin: {}", totalGroups);
    }

    private void dodeliRecenzenteZaSkupino(List<Prijava> prijavePodpodrocja) {

        List<Recenzent> recenzenti = pridobiPrimerneRecenzenteZaSkupino(prijavePodpodrocja);

        if (recenzenti.size() < 5) {
            throw new RuntimeException("Ni dovolj recenzentov za dodelitev vseh prijav.");
        }

        for (Prijava prijava : prijavePodpodrocja) {
            //mogoce nepotrebno, itak vrnem 5
            List<Recenzent> izbraniRecenzenti = recenzenti.subList(0, 5);

            for (Recenzent recenzent : izbraniRecenzenti) {
                if (recenzent.getPrijavePredizbor() < 10) {
                    dodeliRecenzentaPrijavi(prijava, recenzent);
                    recenzent.setPrijavePredizbor(recenzent.getPrijavePredizbor() + 1);
                    recenzentRepository.save(recenzent);
                }
            }
        }
    }

    private List<Recenzent> pridobiPrimerneRecenzenteZaSkupino(List<Prijava> prijavePodpodrocja) {
        // Seznam recenzentov, ki ustrezajo za vse prijave v skupini
        List<Recenzent> recenzenti = new ArrayList<>();

        // Prvo pridobimo vse države, COI in osebne razloge, ki se pojavljajo v tej skupini prijav
        Set<String> vseDrzave = new HashSet<>();
        Set<Integer> vsePrijaveIds = new HashSet<>();

        var primarnoPodpodrocje = prijavePodpodrocja.getFirst().getPodpodrocjeId();
        var dodatnoPodpodrocje = prijavePodpodrocja.getFirst().getDodatnoPodpodrocjeId();

        for (Prijava prijava : prijavePodpodrocja) {
            // Dodamo države
            vseDrzave.addAll(pridobiDrzavePartnerskihAgencij(prijava));

            // Dodamo ID prijave
            vsePrijaveIds.add(prijava.getPrijavaId());

        }

        // Poiščemo vse recenzente, ki so povezani s podpodročji teh prijav
        List<Recenzent> vsiRecenzenti = new ArrayList<>();

        vsiRecenzenti.addAll(recenzentRepository.findEligibleReviewers(primarnoPodpodrocje));

        if (dodatnoPodpodrocje != null) {
            vsiRecenzenti.addAll(recenzentRepository.findEligibleReviewers(dodatnoPodpodrocje));
        }

        //izločimo recenzente, ki imajo kakršnekoli konflikte
        List<IzloceniCOI> izloceniRecenzenti = izloceniCOIRepository.findByPrijavaId(new ArrayList<>(vsePrijaveIds));
        vsiRecenzenti.removeIf(r -> izloceniRecenzenti.contains(r.getRecenzentId()));

        //izločimo recenzente z osebnim konfliktom
        List<IzloceniOsebni> izloceniOsebni = izloceniOsebniRepository.findByPrijavaId(new ArrayList<>(vsePrijaveIds));
        vsiRecenzenti.removeIf(r -> izloceniOsebni.stream().anyMatch(osebni -> osebni.getRecenzentId() == r.getRecenzentId()));

        //izločimo recenzente iz držav partnerskih agencij
        vsiRecenzenti.removeIf(r -> vseDrzave.contains(r.getDrzava()));

        // Preverimo, ali imajo recenzenti še prosta mesta
        vsiRecenzenti.removeIf(r -> r.getProstaMesta() <= prijavePodpodrocja.size());

        // Filtriramo recenzente, ki so že sprejeli 10 prijav
        vsiRecenzenti.removeIf(r -> r.getPrijavePredizbor() >= 10 + prijavePodpodrocja.size());

        logger.info("Število vseh primernih recenzentov:  " + vsiRecenzenti.size());

        // Ločimo recenzente na primarno in dodatno podpodročje
        List<Recenzent> recenzentiPrimarnoPodpodrocje = new ArrayList<>();
        List<Recenzent> recenzentiDodatnoPodpodrocje = new ArrayList<>();
        logger.info("Primarno podpodročje: " + primarnoPodpodrocje);
        for (Recenzent recenzent : vsiRecenzenti) {
            // Preverimo, ali je recenzent povezan s primarnim podpodročjem
            boolean isPrimarnoPodpodrocje = recenzent.getRecenzentiPodrocja().stream()
                    .anyMatch(rp -> rp.getPodpodrocjeId() == primarnoPodpodrocje);
            boolean isDodatnoPodpodrocje = false;
            if (dodatnoPodpodrocje != null) {
                // Preverimo, ali je recenzent povezan z dodatnim podpodročjem
                isDodatnoPodpodrocje = recenzent.getRecenzentiPodrocja().stream()
                        .anyMatch(rp -> rp.getPodpodrocjeId() == dodatnoPodpodrocje);
            }
            if (isPrimarnoPodpodrocje) {
                recenzentiPrimarnoPodpodrocje.add(recenzent);
            }

            if (isDodatnoPodpodrocje) {
                recenzentiDodatnoPodpodrocje.add(recenzent);
            }
        }

        logger.info("Število recenzentov za primarno podpodročje: {}", recenzentiPrimarnoPodpodrocje.size());
        logger.info("Število recenzentov za dodatno podpodročje: {}", recenzentiDodatnoPodpodrocje.size());


        //če je interdisciplinarna prijava
        if (prijavePodpodrocja.getFirst().isInterdisc()) {
            // Naključno premešamo seznam recenzentov za primarno in dodatno podpodročje
            Collections.shuffle(recenzentiPrimarnoPodpodrocje);
            Collections.shuffle(recenzentiDodatnoPodpodrocje);
            Collections.shuffle(vsiRecenzenti);

            // Izberemo 2 recenzenta iz primarnega podpodročja
            recenzenti.addAll(recenzentiPrimarnoPodpodrocje.subList(0, 2));

            // Izberemo 2 recenzenta iz dodatnega podpodročja
            recenzenti.addAll(recenzentiDodatnoPodpodrocje.subList(0, 2));

            // Izberemo 1 naključnega recenzenta iz celotnega seznama
            //tukaj potrebujem narediti tako, da ne more izbrati tistega, ki ga je že izbral v prejšnjih dveh
            recenzenti.add(vsiRecenzenti.getFirst());
        } else {
            if(vsiRecenzenti.size()<5) {
                logger.info("Zmanjkalo recezenzentov za podpodročje: " + primarnoPodpodrocje);
            } else {
                // Za ne-interdisciplinarne prijave izberemo 5 naključnih recenzentov
                Collections.shuffle(vsiRecenzenti);
                recenzenti.addAll(vsiRecenzenti.subList(0, 5));
            }
        }

        return recenzenti;
    }

    private List<String> pridobiDrzavePartnerskihAgencij(Prijava prijava) {
        List<String> partnerskeDrzave = new ArrayList<>();
        if (prijava.getPartnerskaAgencija1() != null) {
            partnerskeDrzave.add(PARTNERSKE_AGENCIJE_MAP.get(prijava.getPartnerskaAgencija1()));
        }
        if (prijava.getPartnerskaAgencija2() != null) {
            partnerskeDrzave.add(PARTNERSKE_AGENCIJE_MAP.get(prijava.getPartnerskaAgencija2()));
        }
        return partnerskeDrzave;
    }

    /*private List<Recenzent> nakljucnoIzberiRecenzente(List<Recenzent> recenzenti, int stevilo) {
        Collections.shuffle(recenzenti);
        return recenzenti.subList(0, stevilo);
    }*/

    private void dodeliRecenzentaPrijavi(Prijava prijava, Recenzent recenzent) {
        // Preverimo, ali recenzent še lahko sprejme novo prijavo v predizbor
        if (recenzent.getPrijavePredizbor() < 10) {
            Predizbor predizbor = new Predizbor();
            predizbor.setPrijavaId(prijava.getPrijavaId());
            predizbor.setRecenzentId(recenzent.getRecenzentId());
            predizbor.setStatus("DODELJENA");
            predizborRepository.save(predizbor);

            recenzent.setPrijavePredizbor(recenzent.getPrijavePredizbor() + 1);
            recenzentRepository.save(recenzent);

            StatusPrijav status = statusPrijavRepository.findByNaziv("NEOPREDELJEN")
                    .orElseThrow(() -> new RuntimeException("Status 'NEOPREDELJEN' ni bil najden."));
            System.out.println(
                    status
            );
            prijava.setStatusPrijav(status);

            prijavaRepository.save(prijava);
        } else {
            throw new RuntimeException("Recenzent je dosegel maksimalno število prijav.");
        }
    }

    public void odstraniVseDodelitve() {
        // Počisti vse dodelitve iz tabele Predizbor
        predizborRepository.deleteAll();
        logger.info("Vse dodelitve so bile uspešno odstranjene iz predizbor tabele.");
    }
}

