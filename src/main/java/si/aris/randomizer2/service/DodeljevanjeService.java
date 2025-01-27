package si.aris.randomizer2.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import si.aris.randomizer2.model.*;
import si.aris.randomizer2.repository.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

    public void dodeliRecenzente() {

        // 1. Pridobi status "DODELJENA" iz baze
        StatusPrijav statusDodeljena = statusPrijavRepository.findByNaziv("DODELJENA")
                .orElseThrow(() -> new RuntimeException("Status 'DODELJENA' ne obstaja v bazi"));

        // 1. Pridobi vse prijave s statusom "NEOPREDELJEN" ali "BREZ RECENZENTA"
        List<Prijava> prijave = prijavaRepository.findByStatusOceneIn(List.of("NEOPREDELJEN", "BREZ RECENZENTA"));

        for (Prijava prijava : prijave) {
            // 2. Pridobi ustrezne recenzente
            List<Recenzent> primerniRecenzenti = pridobiPrimerneRecenzente(prijava);

            if (primerniRecenzenti.size() >= 2) {
                // 3. Naključno izberi 2 recenzenta
                List<Recenzent> izbraniRecenzenti = nakljucnoIzberiRecenzente(primerniRecenzenti, 5);

                // 4. Dodeli recenzente prijavi
                for (Recenzent recenzent : izbraniRecenzenti) {
                    dodeliRecenzentaPrijavi(prijava, recenzent);
                }

                // 5. Posodobi status prijave na "DODELJENA"
                prijava.setStatusOcene(statusDodeljena);
                prijavaRepository.save(prijava);
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
        Predizbor predizbor = new Predizbor();
        predizbor.setPrijavaId(prijava.getPrijavaId());
        predizbor.setRecenzentId(recenzent.getRecenzentId());
        predizbor.setStatus("DODELJENA");
        predizborRepository.save(predizbor);

        // Posodobi prosta mesta recenzenta
        recenzent.setProstaMesta(recenzent.getProstaMesta() - 1);
        recenzentRepository.save(recenzent);
    }
}

