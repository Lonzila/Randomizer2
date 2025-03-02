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

        StatusPrijav statusNeopreden = statusPrijavRepository.findByNaziv("NEOPREDELJEN")
                .orElseThrow(() -> new RuntimeException("Status 'NEOPREDELJEN' ni bil najden."));
        StatusPrijav statusBrezRecenzenta = statusPrijavRepository.findByNaziv("BREZ RECENZENTA")
                .orElseThrow(() -> new RuntimeException("Status 'BREZ RECENZENTA' ni bil najden."));

        // Pridobimo vse prijave s statusom "NEOPREDELJEN" ali "BREZ RECENZENTA" na podlagi ID-jev
        List<Prijava> prijave = prijavaRepository.findByStatusPrijavIdIn(List.of(statusNeopreden.getId(), statusBrezRecenzenta.getId()));

        // 2. Sortiramo prijave po podpodročjih
        Map<Integer, List<Prijava>> prijavePoPodpodrocjih = new HashMap<>();
        for (Prijava prijava : prijave) {
            prijavePoPodpodrocjih
                    .computeIfAbsent(prijava.getPodpodrocjeId(), k -> new ArrayList<>())
                    .add(prijava);
        }

        // 3. Za vsako prijavo v posameznem podpodročju dodelimo recenzente
        for (List<Prijava> prijavePodpodrocja : prijavePoPodpodrocjih.values()) {

            int steviloPrijav = prijavePodpodrocja.size();
            int recenzentovNaKrog = 5; // Za vsakih 10 prijav bomo izbrali 5 recenzentov

            // 4. Dodelimo recenzente v blokih po 10 prijav
            for (int i = 0; i < steviloPrijav; i += 10) {
                // Izberemo primerne recenzente za naslednjih 10 prijav
                List<Recenzent> recenzenti = pridobiPrimerneRecenzente(prijavePodpodrocja.get(i));

                Collections.shuffle(recenzenti);
                // Preverimo, če imamo dovolj prostih recenzentov
                if (recenzenti.size() < recenzentovNaKrog) {
                    throw new RuntimeException("Ni dovolj recenzentov za dodelitev vseh prijav.");
                }

                // Dodelimo recenzente za naslednjih 10 prijav
                for (int j = i; j < Math.min(i + 10, steviloPrijav); j++) {
                    Prijava prijava = prijavePodpodrocja.get(j);

                    // Izberemo 5 recenzentov za to prijavo
                    List<Recenzent> izbraniRecenzenti = recenzenti.subList(0, recenzentovNaKrog);

                    // Dodelimo recenzente prijavi
                    for (Recenzent recenzent : izbraniRecenzenti) {
                        if (recenzent.getPrijavePredizbor() < 10) {
                            dodeliRecenzentaPrijavi(prijava, recenzent);
                            recenzent.setPrijavePredizbor(recenzent.getPrijavePredizbor() + 1);
                            recenzentRepository.save(recenzent);
                        }
                    }
                }
            }
        }
    }

    private List<Recenzent> pridobiPrimerneRecenzente(Prijava prijava) {
        // 1. Ustrezni recenzenti za podpodročje
        List<Recenzent> recenzenti = recenzentRepository.findEligibleReviewers(prijava.getPodpodrocjeId());

        // 2. Izloči recenzente s konfliktom interesov (COI)
        List<IzloceniCOI> izloceniRecenzenti = izloceniCOIRepository.findByPrijavaId(prijava.getPrijavaId());
        recenzenti.removeIf(r -> izloceniRecenzenti.contains(r.getRecenzentId()));

        // 3. Izloči recenzente z osebnim konfliktom
        List<IzloceniOsebni> izloceniOsebni = izloceniOsebniRepository.findByPrijavaId(prijava.getPrijavaId());
        recenzenti.removeIf(r -> izloceniOsebni.stream().anyMatch(osebni -> osebni.getRecenzentId() == r.getRecenzentId()));

        // 4. Izloči recenzente iz držav partnerskih agencij
        List<String> partnerskeDrzave = pridobiDrzavePartnerskihAgencij(prijava);
        recenzenti.removeIf(r -> partnerskeDrzave.contains(r.getDrzava()));

        // 5. Preveri, ali imajo recenzenti še prosta mesta
        recenzenti.removeIf(r -> r.getProstaMesta() <= 0);

        recenzenti.removeIf(r -> r.getPrijavePredizbor() >= 10);

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

    private List<Recenzent> nakljucnoIzberiRecenzente(List<Recenzent> recenzenti, int stevilo) {
        Collections.shuffle(recenzenti); // Naključno premešaj seznam
        return recenzenti.subList(0, stevilo); // Vrni prvih N recenzentov
    }

    private void dodeliRecenzentaPrijavi(Prijava prijava, Recenzent recenzent) {
        // Preverimo, ali recenzent še lahko sprejme novo prijavo
        if (recenzent.getPrijavePredizbor() < 10) { // Predvidevamo, da bo imel 10 prijav
            // Dodelimo prijavo recenzentu
            Predizbor predizbor = new Predizbor();
            predizbor.setPrijavaId(prijava.getPrijavaId());
            predizbor.setRecenzentId(recenzent.getRecenzentId());
            predizbor.setStatus("DODELJENA");
            predizborRepository.save(predizbor);

            // Posodobi število prijav recenzenta
            recenzent.setPrijavePredizbor(recenzent.getPrijavePredizbor() + 1);
            recenzentRepository.save(recenzent);

            // Poišči status po imenu, kot že imaš
            StatusPrijav status = statusPrijavRepository.findByNaziv("NEOPREDELJEN")
                    .orElseThrow(() -> new RuntimeException("Status 'NEOPREDELJEN' ni bil najden."));
            System.out.println(
                    status
            );
            // Nastavimo celotno entiteto StatusPrijav
            prijava.setStatusPrijav(status);

            // Posodobi prijavo
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

