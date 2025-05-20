package si.aris.randomizer2.service;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import si.aris.randomizer2.model.*;
import si.aris.randomizer2.repository.PredizborRepository;
import si.aris.randomizer2.repository.PrijavaRepository;
import si.aris.randomizer2.repository.RecenzentRepository;
import si.aris.randomizer2.repository.StatusPrijavRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class PregledOpredelitevService {

    private final PrijavaRepository prijavaRepository;
    private final RecenzentRepository recenzentRepository;
    private final PredizborRepository predizborRepository;
    private final StatusPrijavRepository statusPrijavRepository;

    public PregledOpredelitevService(PrijavaRepository prijavaRepository, RecenzentRepository recenzentRepository, PredizborRepository predizborRepository, StatusPrijavRepository statusPrijavRepository) {
        this.prijavaRepository = prijavaRepository;
        this.recenzentRepository = recenzentRepository;
        this.predizborRepository = predizborRepository;
        this.statusPrijavRepository = statusPrijavRepository;
    }

    private Set<Set<Integer>> generirajPare(boolean interdisc, List<RecenzentEntry> recenzenti) {
        Set<Set<Integer>> pari = new HashSet<>();

        if (interdisc) {
            List<RecenzentEntry> primarni = recenzenti.stream().filter(r -> r.primarno).toList();
            List<RecenzentEntry> dodatni = recenzenti.stream().filter(r -> !r.primarno).toList();

            for (RecenzentEntry p : primarni) {
                for (RecenzentEntry d : dodatni) {
                    pari.add(Set.of(p.recenzentId, d.recenzentId));
                }
            }
        } else {
            for (int i = 0; i < recenzenti.size(); i++) {
                for (int j = i + 1; j < recenzenti.size(); j++) {
                    pari.add(Set.of(recenzenti.get(i).recenzentId, recenzenti.get(j).recenzentId));
                }
            }
        }
        return pari;
    }

    private static class RecenzentEntry {
        int recenzentId;
        String status;
        Boolean primarno;

        public RecenzentEntry(int recenzentId, String status, Boolean primarno) {
            this.recenzentId = recenzentId;
            this.status = status;
            this.primarno = primarno;
        }
    }

    public Map<Set<Integer>, List<Integer>> izberiPareIzBazeInDodeliVocenevanje() {
        List<Predizbor> vsiOpredeljeniZDa = predizborRepository.findByStatusIgnoreCase("OPREDELJEN Z DA");
        Map<Integer, Prijava> prijaveMap = prijavaRepository.findAll().stream()
                .collect(Collectors.toMap(Prijava::getPrijavaId, p -> p));

        Map<Integer, List<RecenzentEntry>> prijavaToRecenzenti = new HashMap<>();
        for (Predizbor p : vsiOpredeljeniZDa) {
            int prijavaId = p.getPrijavaId();
            prijavaToRecenzenti
                    .computeIfAbsent(prijavaId, k -> new ArrayList<>())
                    .add(new RecenzentEntry(p.getRecenzentId(), "OPREDELJEN Z DA", p.isPrimarni()));
        }

        Map<Set<Integer>, List<Integer>> parToPrijave = new LinkedHashMap<>();
        Set<Integer> zeDodeljene = new HashSet<>();
        Set<String> zeDodeljeniPari = new HashSet<>();

        StatusPrijav statusVOcenjevanju = statusPrijavRepository.findByNaziv("V OCENJEVANJU")
                .orElseThrow(() -> new RuntimeException("Status 'V OCENJEVANJU' ni bil najden."));

        while (true) {
            Map<Set<Integer>, List<Integer>> kandidatniPari = new HashMap<>();

            for (Map.Entry<Integer, List<RecenzentEntry>> entry : prijavaToRecenzenti.entrySet()) {
                int prijavaId = entry.getKey();
                if (zeDodeljene.contains(prijavaId)) continue;

                Prijava prijava = prijaveMap.get(prijavaId);
                if (prijava == null) continue;
                if ("V OCENJEVANJU".equalsIgnoreCase(prijava.getStatusPrijav().getNaziv())) continue;

                Set<Set<Integer>> pari = generirajPare(prijava.isInterdisc(), entry.getValue());
                for (Set<Integer> par : pari) {
                    kandidatniPari.computeIfAbsent(par, k -> new ArrayList<>()).add(prijavaId);
                }
            }

            if (kandidatniPari.isEmpty()) break;

            // 📊 Izpis vseh možnih parov in pripadajočih prijav
            System.out.println("--- Možni pari in število pripadajočih prijav ---");
            kandidatniPari.entrySet().stream()
                    .sorted((e1, e2) -> Integer.compare(e2.getValue().size(), e1.getValue().size()))
                    .forEach(e -> System.out.println("Par " + e.getKey() + " → " + e.getValue().size() + " prijav"));

            Random random = new Random();
            Optional<Map.Entry<Set<Integer>, List<Integer>>> izbran = kandidatniPari.entrySet().stream()
                    .sorted((e1, e2) -> Integer.compare(e2.getValue().size(), e1.getValue().size()))
                    .map(e -> {
                        int max = e.getValue().size();
                        for (Integer recId : e.getKey()) {
                            Optional<Recenzent> rec = recenzentRepository.findById(recId);
                            int prosta = rec.map(Recenzent::getProstaMesta).orElse(0);
                            max = Math.min(max, prosta);
                        }
                        return Map.entry(e.getKey(), e.getValue().subList(0, max));
                    })
                    .filter(e -> !e.getValue().isEmpty())
                    .findFirst();

            if (izbran.isEmpty()) break;

            Set<Integer> par = izbran.get().getKey();
            List<Integer> prijave = izbran.get().getValue();
            parToPrijave.put(par, prijave);
            zeDodeljene.addAll(prijave);

            // ✏️ Posodobimo število mest
            par.forEach(id -> recenzentRepository.findById(id).ifPresent(r -> {
                r.setProstaMesta(r.getProstaMesta() - prijave.size());
                recenzentRepository.save(r);
            }));

            // 💾 Posodobi statuse
            for (int prijavaId : prijave) {
                for (int recenzentId : par) {
                    predizborRepository.findByPrijavaIdAndRecenzentId(prijavaId, recenzentId).ifPresent(p -> {
                        p.setStatus("V OCENJEVANJU");
                        predizborRepository.save(p);
                        zeDodeljeniPari.add(prijavaId + ":" + recenzentId);
                    });
                }
                prijavaRepository.findById(prijavaId).ifPresent(p -> {
                    p.setStatusPrijav(statusVOcenjevanju);
                    prijavaRepository.save(p);
                });
            }

            System.out.println("--- Dodelitev kroga ---");
            System.out.println("Par recenzentov: " + par);
            System.out.println("Prijave: " + prijave);
            System.out.println();
        }

        // 📌 Zaključno čiščenje
        System.out.println("--- Čiščenje: Neporabljeni OPREDELJEN Z DA ---");
        prijavaToRecenzenti.forEach((prijavaId, seznam) -> {
            seznam.forEach(rec -> {
                if ("OPREDELJEN Z DA".equalsIgnoreCase(rec.status)) {
                    String key = prijavaId + ":" + rec.recenzentId;
                    if (!zeDodeljeniPari.contains(key)) {
                        predizborRepository.findByPrijavaIdAndRecenzentId(prijavaId, rec.recenzentId).ifPresent(pred -> {
                            pred.setStatus("OPREDELJENO Z DA");
                            predizborRepository.save(pred);
                            System.out.println("↩️ Povrnjeno: prijava " + prijavaId + ", recenzent " + rec.recenzentId);
                        });
                    }
                }
            });
        });

        return parToPrijave;
    }
}
