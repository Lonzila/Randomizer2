package si.aris.randomizer2.service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

        // 1. Pridobimo vse prijave s statusom "BREZ RECENZENTA"
        List<Prijava> prijave = prijavaRepository.findByStatusPrijavIdIn(List.of(statusBrezRecenzenta.getId()));
        logger.info("Predizba prijave, ki so BREZ RECENZENTA: " + prijave.size());

        // 2. Sortiramo prijave po podpodročjih
        Map<String, List<Prijava>> prijavePoKombinacijah = new HashMap<>();
        Map<String, Integer> recenzentiNaSkupino = new HashMap<>();
        for (Prijava prijava : prijave) {
            // Sestavimo unikatni ključ za grupiranje prijav
            String key = prijava.getPodpodrocje().getNaziv() + "-" +
                    prijava.getErcPodrocje().getKoda() + "-" +
                    (prijava.getDodatnoPodpodrocje() != null ? prijava.getDodatnoPodpodrocje().getNaziv() : "none") + "-" +
                    (prijava.getDodatnoErcPodrocje() != null ? prijava.getDodatnoErcPodrocje().getKoda() : "none");

            prijavePoKombinacijah
                    .computeIfAbsent(key, k -> new ArrayList<>())
                    .add(prijava);

            int steviloPrimarnihRecenzentov = recenzentRepository.countEligibleReviewers(
                    prijava.getPodpodrocje().getPodpodrocjeId(), prijava.getErcPodrocje().getErcId());
            int steviloDodatnihRecenzentov = (prijava.getDodatnoPodpodrocje() != null && prijava.getDodatnoErcPodrocje() != null) ?
                    recenzentRepository.countEligibleReviewers(prijava.getDodatnoPodpodrocje().getPodpodrocjeId(), prijava.getDodatnoErcPodrocje().getErcId()) : 0;

            recenzentiNaSkupino.put(key, steviloPrimarnihRecenzentov + steviloDodatnihRecenzentov);
        }
        //logger.info("Število unikatnih skupin prijav: {}", prijavePoKombinacijah.size());
        List<Map.Entry<String, List<Prijava>>> sortiraneSkupine = new ArrayList<>(prijavePoKombinacijah.entrySet());
        sortiraneSkupine.sort(Comparator.comparingInt(entry -> recenzentiNaSkupino.getOrDefault(entry.getKey(), Integer.MAX_VALUE)));
        //int totalGroups = 0;
        for (Map.Entry<String, List<Prijava>> entry : sortiraneSkupine) {
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

            logger.info("Ustvarjena skupina: {} - Število prijav: {}", entry.getKey(), razdeljeneSkupine.size());
            //totalGroups += razdeljeneSkupine.size();

            for (int i = 0; i < razdeljeneSkupine.size(); i++) {
                List<Prijava> skupina = razdeljeneSkupine.get(i);
            }

            // Dodelimo recenzente za vsako skupino
            for (List<Prijava> prijaveSkupine : razdeljeneSkupine) {
                dodeliRecenzenteZaSkupino(prijaveSkupine);
            }
        }
        logger.info("Število prijav brez recenzenta po predizboru: {}", prijavaRepository.countByStatusPrijavNaziv("BREZ RECENZENTA"));
        //logger.info("Skupno število ustvarjenih skupin: {}", totalGroups);
    }

    private void dodeliRecenzenteZaSkupino(List<Prijava> prijaveSkupine) {

        List<Recenzent> recenzenti = pridobiPrimerneRecenzenteZaSkupino(prijaveSkupine);
        /*if (recenzenti.size() < 5) {
            logger.warn("Ni dovolj recenzentov za dodelitev vseh prijav!");
        }*/
        for (Prijava prijava : prijaveSkupine) {
            for (Recenzent recenzent : recenzenti) {
                dodeliRecenzentaPrijavi(prijava, recenzent);
            }
        }
    }

    private List<Recenzent> pridobiPrimerneRecenzenteZaSkupino(List<Prijava> prijavePodpodrocja) {
        // Seznam recenzentov, ki ustrezajo za vse prijave v skupini
        List<Recenzent> recenzenti = new ArrayList<>();

        // Prvo pridobimo vse države, COI in osebne razloge, ki se pojavljajo v tej skupini prijav
        Set<String> vseDrzave = new HashSet<>();
        Set<Integer> vsePrijaveIds = new HashSet<>();

        var primarnoPodpodrocje = prijavePodpodrocja.getFirst().getPodpodrocje();
        var dodatnoPodpodrocje = prijavePodpodrocja.getFirst().getDodatnoPodpodrocje();
        var primarnoErcPodrocje = prijavePodpodrocja.getFirst().getErcPodrocje();
        var dodatnoErcPodrocje = prijavePodpodrocja.getFirst().getDodatnoErcPodrocje();

        /*logger.info("Iskanje recenzentov za kombinacijo: Podpodrocje={}, ERC={}", primarnoPodpodrocje.getNaziv(), primarnoErcPodrocje.getKoda());
        if (dodatnoPodpodrocje != null && dodatnoErcPodrocje != null) {
            logger.info("Dodatna kombinacija: Podpodrocje={}, ERC={}", dodatnoPodpodrocje.getNaziv(), dodatnoErcPodrocje.getKoda());
        }*/
        for (Prijava prijava : prijavePodpodrocja) {
            // Dodamo države
            vseDrzave.addAll(pridobiDrzavePartnerskihAgencij(prijava));

            // Dodamo ID prijave
            vsePrijaveIds.add(prijava.getPrijavaId());

        }

        // Poiščemo vse recenzente, ki so povezani s podpodročji teh prijav
        List<Recenzent> vsiRecenzenti = new ArrayList<>();

        vsiRecenzenti.addAll(recenzentRepository.findEligibleReviewers(primarnoPodpodrocje.getPodpodrocjeId(), primarnoErcPodrocje.getErcId()));

        // Iskanje po dodatni kombinaciji, če obstaja
        if (dodatnoPodpodrocje != null && dodatnoErcPodrocje != null) {
            vsiRecenzenti.addAll(recenzentRepository.findEligibleReviewers(dodatnoPodpodrocje.getPodpodrocjeId(), dodatnoErcPodrocje.getErcId()));
        }


        //izločimo recenzente, ki imajo kakršnekoli konflikte
        List<IzloceniCOI> izloceniRecenzenti = izloceniCOIRepository.findByPrijavaId(new ArrayList<>(vsePrijaveIds));
        vsiRecenzenti.removeIf(r -> izloceniRecenzenti.contains(r.getRecenzentId()));

        //izločimo recenzente z osebnim konfliktom
        List<IzloceniOsebni> izloceniOsebni = izloceniOsebniRepository.findByPrijavaId(new ArrayList<>(vsePrijaveIds));
        vsiRecenzenti.removeIf(r -> izloceniOsebni.stream().anyMatch(osebni -> osebni.getRecenzentId() == r.getRecenzentId()));

        //izločimo recenzente iz držav partnerskih agencij
        vsiRecenzenti.removeIf(r -> vseDrzave.contains(r.getDrzava()));

        // Preverimo, ali imajo recenzenti še prosta mesta, v bistvu ne rabim, ker imam samo predizbor in pa preverjam že v queryju v findEligibleReviewers.
        //Pomembno za kasnejše krogec
        //vsiRecenzenti.removeIf(r -> r.getProstaMesta() < prijavePodpodrocja.size());

        // Filtriramo recenzente, ki so že sprejeli 10 prijav 6 + 4 = 10
        vsiRecenzenti.removeIf(r -> r.getPrijavePredizbor() + prijavePodpodrocja.size() > 10 );

        // Ločimo recenzente na primarno in dodatno kombinacijo (podpodrocje + ercPodrocje)
        List<Recenzent> recenzentiPrimarno = new ArrayList<>();
        List<Recenzent> recenzentiDodatno = new ArrayList<>();

        for (Recenzent recenzent : vsiRecenzenti) {
            boolean hasMatchingPodpodrocje = recenzent.getRecenzentiPodrocja().stream()
                    .anyMatch(rp -> rp.getPodpodrocjeId() == primarnoPodpodrocje.getPodpodrocjeId());

            boolean hasMatchingErc = recenzent.getRecenzentiErc().stream()
                    .anyMatch(re -> re.getErcPodrocjeId() == primarnoErcPodrocje.getErcId());

            boolean isPrimarno = hasMatchingPodpodrocje && hasMatchingErc;

            boolean isDodatno = false;
            if (dodatnoPodpodrocje != null && dodatnoErcPodrocje != null) {
                boolean hasMatchingDodatnoPodpodrocje = recenzent.getRecenzentiPodrocja().stream()
                        .anyMatch(rp -> rp.getPodpodrocjeId() == dodatnoPodpodrocje.getPodpodrocjeId());

                boolean hasMatchingDodatnoErc = recenzent.getRecenzentiErc().stream()
                        .anyMatch(re -> re.getErcPodrocjeId() == dodatnoErcPodrocje.getErcId());

                isDodatno = hasMatchingDodatnoPodpodrocje && hasMatchingDodatnoErc;
            }

            if (isPrimarno) {
                recenzentiPrimarno.add(recenzent);
            }
            if (isDodatno) {
                recenzentiDodatno.add(recenzent);
            }
        }
        if (vsiRecenzenti.size() < 5) {
            logger.warn("Premalo recenzentov! Najdenih: {} za kombinacijo Podpodrocje={}, ERC={}.",
                    vsiRecenzenti.size(), primarnoPodpodrocje.getNaziv(), primarnoErcPodrocje.getKoda());
            logger.info("Število recenzentov za primarno podpodročje: {}", recenzentiPrimarno.size());
            logger.info("Število recenzentov za dodatno podpodročje: {}", recenzentiDodatno.size());
        }

        //ce je interdisciplinarna prijava
        if (prijavePodpodrocja.getFirst().isInterdisc()) {
            if (recenzentiPrimarno.size()< 2 || recenzentiDodatno.size()< 2) {
                //logger.warn("Zmanjkalo recezenzentov za predizbor. Za primarno podpodročje jih je ostalo: " + recenzentiPrimarno.size() + " Za sekundarno podpodročje jih je ostalo: " + recenzentiDodatno.size());
            } else {
                // Naključno premešamo seznam recenzentov za primarno in dodatno podpodrocje
                Collections.shuffle(recenzentiPrimarno);
                Collections.shuffle(recenzentiDodatno);

                // Izberemo 2 recenzenta iz primarnega podpodrocja
                recenzenti.addAll(recenzentiPrimarno.subList(0, 2));

                // Izberemo 2 recenzenta iz dodatnega podpodrocja
                recenzenti.addAll(recenzentiDodatno.subList(0, 2));

                // Preden izberemo enega naključnega recenzenta, odstranimo tiste, ki so že izbrani v prejšnjih dveh korakih
                List<Recenzent> prejsnjiIzbraniRecenzenti = new ArrayList<>();
                prejsnjiIzbraniRecenzenti.addAll(recenzentiPrimarno.subList(0, 2));
                prejsnjiIzbraniRecenzenti.addAll(recenzentiDodatno.subList(0, 2));

                // Filtriramo vsiRecenzenti in odstranimo že izbrane recenzente
                vsiRecenzenti.removeAll(prejsnjiIzbraniRecenzenti);

                // Izberemo 1 naključnega recenzenta iz preostalih
                if (!vsiRecenzenti.isEmpty()) {
                    Collections.shuffle(vsiRecenzenti);
                    recenzenti.add(vsiRecenzenti.getFirst());
                } else {
                    logger.warn("Ni več recenzentov za naključen izbor.");
                }
            }

        } else {
            if(vsiRecenzenti.size()<5) {
                logger.warn("Zmanjkalo recezenzentov za primarno podpodročje: " + primarnoPodpodrocje);
                Collections.shuffle(vsiRecenzenti);
                recenzenti.addAll(vsiRecenzenti.subList(0, Math.min(5, vsiRecenzenti.size())));
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
            partnerskeDrzave.add(prijava.getPartnerskaAgencija1());
        }
        if (prijava.getPartnerskaAgencija2() != null) {
            partnerskeDrzave.add(prijava.getPartnerskaAgencija2());
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
            predizbor.setStatus("NEOPREDELJEN");
            predizborRepository.save(predizbor);
            //predizborRepository.flush();
            recenzent.setPrijavePredizbor(recenzent.getPrijavePredizbor() + 1);
            recenzentRepository.save(recenzent);
            //recenzentRepository.flush();
            StatusPrijav status = statusPrijavRepository.findByNaziv("NEOPREDELJEN")
                    .orElseThrow(() -> new RuntimeException("Status 'NEOPREDELJEN' ni bil najden."));
            //System.out.println(status);
            prijava.setStatusPrijav(status);
            prijavaRepository.save(prijava);
            //prijavaRepository.flush();
        } else {
            throw new RuntimeException("Recenzent je dosegel maksimalno število prijav.");
        }
    }

    @Transactional
    public void odstraniVseDodelitve() {
        // Počisti vse dodelitve iz tabele Predizbor
        predizborRepository.deleteAll();
        logger.info("Vse dodelitve so bile uspešno odstranjene iz predizbor tabele.");

        // Ponastavi število prijav v predizboru na 0 za vse recenzente
        recenzentRepository.updatePrijavePredizborToZero();
        logger.info("Vsem recenzentom je bilo število prijav v predizboru ponastavljeno na 0.");

        // Posodobi status vseh prijav na 'BREZ RECENZENTA'
        StatusPrijav statusBrezRecenzenta = statusPrijavRepository.findByNaziv("BREZ RECENZENTA")
                .orElseThrow(() -> new RuntimeException("Status 'BREZ RECENZENTA' ni bil najden."));
        prijavaRepository.updateStatusPrijavTo(statusBrezRecenzenta.getId());
        logger.info("Vsem prijavam je bil status posodobljen na 'javaRepository.upBREZ RECENZENTA'.");
    }
}

