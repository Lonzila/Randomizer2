package si.aris.randomizer2.service;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import si.aris.randomizer2.model.*;
import si.aris.randomizer2.repository.PrijavaRepository;
import si.aris.randomizer2.repository.RecenzentRepository;

import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PregledOpredelitevService {

    private final PrijavaRepository prijavaRepository;
    private final RecenzentRepository recenzentRepository;

    public PregledOpredelitevService(PrijavaRepository prijavaRepository, RecenzentRepository recenzentRepository) {
        this.prijavaRepository = prijavaRepository;
        this.recenzentRepository = recenzentRepository;
    }

    public Map<Set<Integer>, List<Integer>> izracunajNajboljPogostePare(Resource csvResource) throws Exception {
        Map<Integer, List<RecenzentEntry>> prijavaToRecenzenti = new HashMap<>();

        // 1. Preberi CSV in napolni strukturo
        CSVParser parser = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(new InputStreamReader(csvResource.getInputStream()));
        for (CSVRecord record : parser) {
            int prijavaId = Integer.parseInt(record.get("prijava_id"));
            int recenzentId = Integer.parseInt(record.get("recenzent_id"));
            String status = record.get("status").trim().toUpperCase();

            prijavaToRecenzenti
                    .computeIfAbsent(prijavaId, k -> new ArrayList<>())
                    .add(new RecenzentEntry(recenzentId, status));
        }

        Map<Integer, Prijava> prijaveMap = prijavaRepository.findAll().stream()
                .collect(Collectors.toMap(Prijava::getPrijavaId, p -> p));

        // Pomožni objekt za vsak par in pripadajoče prijave
        Map<Set<Integer>, List<Integer>> parToPrijave = new LinkedHashMap<>();
        Set<Integer> zeDodeljene = new HashSet<>();

        while (true) {
            Map<Set<Integer>, List<Integer>> kandidatniPari = new HashMap<>();

            for (Map.Entry<Integer, List<RecenzentEntry>> entry : prijavaToRecenzenti.entrySet()) {
                int prijavaId = entry.getKey();
                if (zeDodeljene.contains(prijavaId)) continue;

                Prijava prijava = prijaveMap.get(prijavaId);
                List<RecenzentEntry> opredeljeni = new ArrayList<>();

                for (RecenzentEntry rec : entry.getValue()) {
                    if (!"OPREDELJEN Z DA".equalsIgnoreCase(rec.status)) continue;
                    Optional<Recenzent> rOpt = recenzentRepository.findById(rec.recenzentId);
                    if (rOpt.isEmpty()) continue;
                    Recenzent r = rOpt.get();
                    Boolean primarno = dolociPrimarnost(prijava, r);
                    if (primarno != null) {
                        rec.primarno = primarno;
                        opredeljeni.add(rec);
                    }
                }

                Set<Set<Integer>> pari = generirajPare(prijava.isInterdisc(), opredeljeni);

                for (Set<Integer> par : pari) {
                    kandidatniPari.computeIfAbsent(par, k -> new ArrayList<>()).add(prijavaId);
                }
            }

            if (kandidatniPari.isEmpty()) break;

            Set<Integer> najpogostejsiPar = kandidatniPari.entrySet().stream()
                    .max(Comparator.comparingInt(e -> e.getValue().size()))
                    .map(Map.Entry::getKey)
                    .orElse(null);

            if (najpogostejsiPar == null) break;

            List<Integer> prijaveZaPar = kandidatniPari.get(najpogostejsiPar);
            parToPrijave.put(najpogostejsiPar, prijaveZaPar);
            zeDodeljene.addAll(prijaveZaPar);

            // 🖨️ LOG: izpiši v konzolo kaj je bilo dodeljeno
            System.out.println("--- Dodelitev kroga ---");
            System.out.println("Par recenzentov: " + najpogostejsiPar);
            System.out.println("Prijave: " + prijaveZaPar);
        }

        return parToPrijave;
    }

    private Boolean dolociPrimarnost(Prijava prijava, Recenzent recenzent) {
        boolean imaPrimarno = recenzent.getRecenzentiPodrocja().stream()
                .anyMatch(rp -> rp.getPodpodrocjeId() == prijava.getPodpodrocje().getPodpodrocjeId())
                && recenzent.getRecenzentiErc().stream()
                .anyMatch(re -> re.getErcPodrocjeId() == prijava.getErcPodrocje().getErcId());

        if (imaPrimarno) return true;

        if (prijava.getDodatnoPodpodrocje() != null && prijava.getDodatnoErcPodrocje() != null) {
            boolean imaDodatno = recenzent.getRecenzentiPodrocja().stream()
                    .anyMatch(rp -> rp.getPodpodrocjeId() == prijava.getDodatnoPodpodrocje().getPodpodrocjeId())
                    && recenzent.getRecenzentiErc().stream()
                    .anyMatch(re -> re.getErcPodrocjeId() == prijava.getDodatnoErcPodrocje().getErcId());

            if (imaDodatno) return false;
        }
        return null;
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
        Boolean primarno = null;

        public RecenzentEntry(int recenzentId, String status) {
            this.recenzentId = recenzentId;
            this.status = status;
        }
    }
}
