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

import java.io.InputStreamReader;
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

    public Map<Set<Integer>, List<Integer>> izracunajNajboljPogostePare(Resource excelResource) throws Exception {
        Map<Integer, List<RecenzentEntry>> prijavaToRecenzenti = new HashMap<>();

        // 1. Preberi Excel datoteko in napolni strukturo
        try (Workbook workbook = new XSSFWorkbook(excelResource.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0); // prvi list
            Iterator<Row> rowIterator = sheet.iterator();

            // Preskoči glavo
            if (rowIterator.hasNext()) rowIterator.next();

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                Cell prijavaCell = row.getCell(1); // stolpec 'prijava_id'
                Cell recenzentCell = row.getCell(2); // stolpec 'recenzent_id'
                Cell statusCell = row.getCell(3); // stolpec 'status'

                if (prijavaCell == null || recenzentCell == null || statusCell == null) continue;

                int prijavaId = (int) prijavaCell.getNumericCellValue();
                int recenzentId = (int) recenzentCell.getNumericCellValue();
                String status = statusCell.getStringCellValue().trim().toUpperCase();

                prijavaToRecenzenti
                        .computeIfAbsent(prijavaId, k -> new ArrayList<>())
                        .add(new RecenzentEntry(recenzentId, status));
            }
        }


        Map<Integer, Prijava> prijaveMap = prijavaRepository.findAll().stream()
                .collect(Collectors.toMap(Prijava::getPrijavaId, p -> p));

        // Pomožni objekt za vsak par in pripadajoče prijave
        Map<Set<Integer>, List<Integer>> parToPrijave = new LinkedHashMap<>();
        Set<Integer> zeDodeljene = new HashSet<>();
        Set<String> zeDodeljeniPari = new HashSet<>();

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
                    Optional<Predizbor> predOpt = predizborRepository.findByPrijavaIdAndRecenzentId(prijavaId, r.getRecenzentId());
                    if (predOpt.isPresent()) {
                        rec.primarno = predOpt.get().isPrimarni();
                        opredeljeni.add(rec);
                    }
                }

                Set<Set<Integer>> pari = generirajPare(prijava.isInterdisc(), opredeljeni);

                for (Set<Integer> par : pari) {
                    kandidatniPari.computeIfAbsent(par, k -> new ArrayList<>()).add(prijavaId);
                }
            }

            if (kandidatniPari.isEmpty()) break;

            // Tukaj dodamo štetje vseh parov in njihovih ponovitev:
            System.out.println("--- Možni pari in število pripadajočih prijav ---");
            kandidatniPari.entrySet().stream()
                    .sorted((e1, e2) -> Integer.compare(e2.getValue().size(), e1.getValue().size()))
                    .forEach(e -> System.out.println("Par " + e.getKey() + " -> " + e.getValue().size() + " prijav"));


            Optional<Map.Entry<Set<Integer>, List<Integer>>> izbran = kandidatniPari.entrySet().stream()
                    .sorted((e1, e2) -> Integer.compare(e2.getValue().size(), e1.getValue().size()))
                    .filter(e -> imaProstaMesta(e.getKey(), e.getValue().size()))
                    .findFirst();

            if (izbran.isEmpty()) break;

            Set<Integer> najpogostejsiPar = izbran.get().getKey();
            List<Integer> prijaveZaPar = izbran.get().getValue();

            parToPrijave.put(najpogostejsiPar, prijaveZaPar);
            zeDodeljene.addAll(prijaveZaPar);

            najpogostejsiPar.forEach(id -> recenzentRepository.findById(id).ifPresent(r -> {
                r.setProstaMesta(r.getProstaMesta() - prijaveZaPar.size());
                recenzentRepository.save(r);
            }));

            StatusPrijav statusVOcenjevanju = statusPrijavRepository.findById(3)
                    .orElseThrow(() -> new RuntimeException("Status V OCENJEVANJU ni bil najden"));

            prijaveZaPar.forEach(prijavaId -> {
                prijavaToRecenzenti.get(prijavaId).forEach(entry -> {
                    if (najpogostejsiPar.contains(entry.recenzentId)) {
                        Optional<Predizbor> pOpt = predizborRepository.findByPrijavaIdAndRecenzentId(prijavaId, entry.recenzentId);
                        pOpt.ifPresent(pred -> {
                            pred.setStatus("V OCENJEVANJU");
                            predizborRepository.save(pred);
                            zeDodeljeniPari.add(prijavaId + ":" + entry.recenzentId);

                            prijavaRepository.findById(prijavaId).ifPresent(pr -> {
                                pr.setStatusPrijav(statusVOcenjevanju);
                                prijavaRepository.save(pr);
                            });
                        });
                    }
                });
            });

            System.out.println("--- Dodelitev kroga ---");
            System.out.println("Par recenzentov: " + najpogostejsiPar);
            System.out.println("Prijave: " + prijaveZaPar);
            System.out.println();
        }

        // 6. Na koncu: Za vse preostale "OPREDELJEN Z DA", ki niso bili vključeni v "V OCENJEVANJU"
        prijavaToRecenzenti.forEach((prijavaId, seznam) -> {
            seznam.forEach(rec -> {
                if ("OPREDELJEN Z DA".equalsIgnoreCase(rec.status)) {
                    String key = prijavaId + ":" + rec.recenzentId;
                    if (!zeDodeljeniPari.contains(key)) {
                        Optional<Predizbor> pOpt = predizborRepository.findByPrijavaIdAndRecenzentId(prijavaId, rec.recenzentId);
                        pOpt.ifPresent(pred -> {
                            pred.setStatus("OPREDELJENO Z DA");
                            predizborRepository.save(pred);
                        });
                    }
                }
            });
        });
        return parToPrijave;
    }


    private boolean imaProstaMesta(Set<Integer> par, int potrebnaMesta) {
        for (Integer id : par) {
            Optional<Recenzent> rec = recenzentRepository.findById(id);
            if (rec.isEmpty() || rec.get().getProstaMesta() < potrebnaMesta) {
                System.out.println("⚠️ Recenzent " + id + " nima dovolj prostora (" +
                        rec.map(Recenzent::getProstaMesta).orElse(0) + "/" + potrebnaMesta + ")");
                return false;
            }
        }
        return true;
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
