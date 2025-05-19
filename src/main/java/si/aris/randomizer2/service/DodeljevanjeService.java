package si.aris.randomizer2.service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import si.aris.randomizer2.model.*;
import si.aris.randomizer2.repository.*;

import java.io.IOException;
import java.security.SecureRandom;
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

    @Autowired
    private ExcelExportService excelExportService;

    private final Set<Integer> prijaveZFallbackom = new HashSet<>();

    private static final Logger logger = LoggerFactory.getLogger(DodeljevanjeService.class);

    public ByteArrayResource predizbor() throws IOException{

        StatusPrijav statusBrezRecenzenta = statusPrijavRepository.findByNaziv("BREZ RECENZENTA")
                .orElseThrow(() -> new RuntimeException("Status 'BREZ RECENZENTA' ni bil najden."));

        // 1. Pridobim vse prijave s statusom "BREZ RECENZENTA"
        List<Prijava> prijave = prijavaRepository.findByStatusPrijavIdIn(List.of(statusBrezRecenzenta.getId()));
        logger.info("Predizba prijave, ki so BREZ RECENZENTA: " + prijave.size());

        List<Integer> dodatnePrijaveIds = predizborRepository.findPrijaveWithRejectedAndNotFullyAssigned();
        List<Prijava> dodatnePrijave = prijavaRepository.findAllById(dodatnePrijaveIds);

        prijave.addAll(dodatnePrijave);

        // 2. Sortiram prijave po podpodročjih
        Map<String, List<Prijava>> prijavePoKombinacijah = new HashMap<>();
        Map<String, Integer> recenzentiNaSkupino = new HashMap<>();
        for (Prijava prijava : prijave) {
            // Sestavim unikatni ključ za grupiranje prijav
            String key = prijava.getPodpodrocje().getNaziv() + "-" +
                    prijava.getErcPodrocje().getKoda() + "-" +
                    (prijava.getDodatnoPodpodrocje() != null ? prijava.getDodatnoPodpodrocje().getNaziv() : "none") + "-" +
                    (prijava.getDodatnoErcPodrocje() != null ? prijava.getDodatnoErcPodrocje().getKoda() : "none");

            prijavePoKombinacijah
                    .computeIfAbsent(key, k -> new ArrayList<>())
                    .add(prijava);

            int steviloPrimarnihRecenzentov = recenzentRepository.countEligibleByPodpodrocjeOnly(
                    prijava.getPodpodrocje().getPodpodrocjeId());
            int steviloDodatnihRecenzentov = (prijava.getDodatnoPodpodrocje() != null && prijava.getDodatnoErcPodrocje() != null) ?
                    recenzentRepository.countEligibleByPodpodrocjeOnly(prijava.getDodatnoPodpodrocje().getPodpodrocjeId()) : Integer.MAX_VALUE; // Če ni dodatnega, naj ne vpliva na min()

            int najmanjsaMoznost = Math.min(steviloPrimarnihRecenzentov, steviloDodatnihRecenzentov);
            recenzentiNaSkupino.put(key, najmanjsaMoznost);

            //recenzentiNaSkupino.put(key, steviloPrimarnihRecenzentov + steviloDodatnihRecenzentov);
        }
        //logger.info("Število unikatnih skupin prijav: {}", prijavePoKombinacijah.size());
        List<Map.Entry<String, List<Prijava>>> sortiraneSkupine = new ArrayList<>(prijavePoKombinacijah.entrySet());
        sortiraneSkupine.sort(Comparator.comparingInt(entry -> recenzentiNaSkupino.getOrDefault(entry.getKey(), Integer.MAX_VALUE)));
        //int totalGroups = 0;
        for (Map.Entry<String, List<Prijava>> entry : sortiraneSkupine) {
            List<Prijava> prijavePodpodrocja = entry.getValue();
            int steviloPrijav = prijavePodpodrocja.size();


            // Izpisujem informacije o podpodročju in številu prijav (skupina)
            //logger.info("Skupina (Podpodročje in Dodatno Podpodročje): {} ima {} prijav.", key, steviloPrijav);

            // Razdelim skupine, če ima skupina več kot 10 prijav
            List<List<Prijava>> razdeljeneSkupine = new ArrayList<>();
            for (int i = 0; i < steviloPrijav; i += 10) {
                // Za vsako skupino, ki je večja od 10, ustvarim novo skupino z največ 10 prijavami
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
        return excelExportService.exportPredizborToExcel(prijaveZFallbackom);
    }

    /*private void dodeliRecenzenteZaSkupino(List<Prijava> prijaveSkupine) {
        Map<Integer, Boolean> recenzentJePrimarni  = new HashMap<>();
        Set<Recenzent> recenzenti = pridobiPrimerneRecenzenteZaSkupino(prijaveSkupine, recenzentJePrimarni);

        for (Prijava prijava : prijaveSkupine) {
            for (Recenzent recenzent : recenzenti) {
                boolean jePrimarni = recenzentJePrimarni.getOrDefault(recenzent.getRecenzentId(), true); // fallback true
                dodeliRecenzentaPrijavi(prijava, recenzent, jePrimarni);
            }
        }
    }*/
    private void dodeliRecenzenteZaSkupino(List<Prijava> prijaveSkupine) {
        for (Prijava prijava : prijaveSkupine) {
            List<Predizbor> obstojeciPredizbori = predizborRepository.findByPrijavaId(prijava.getPrijavaId());

            // Ohrani aktivne recenzente
            List<Predizbor> aktivni = obstojeciPredizbori.stream()
                    .filter(p -> p.getStatus().equals("DODELJENA V OCENJEVANJE") || p.getStatus().equals("V OCENJEVANJU") || p.getStatus().equals("OPREDELJEN Z DA"))
                    .toList();

            // Zavrnitev
            List<Predizbor> zavrnjeni = obstojeciPredizbori.stream()
                    .filter(p -> p.getStatus().equals("OPREDELJEN Z NE"))
                    .toList();

            Map<Integer, Boolean> recenzentJePrimarni = new HashMap<>();
            Set<Recenzent> noviRecenzenti = pridobiPrimerneRecenzenteZaSkupino(List.of(prijava), recenzentJePrimarni);
            //System.out.println("Število recenzenotv, ki pride na koncu ven: " + noviRecenzenti.size());
            if(noviRecenzenti.size() < 5){
                noviRecenzenti.forEach(r -> System.out.println("✅ Novi Recenzent " + r.getRecenzentId() + " (" + r.getIme() + " " + r.getPriimek() + ") JE USTREZEN"));
            }

            if (obstojeciPredizbori.isEmpty()) {
                // Prva dodelitev
                for (Recenzent recenzent : noviRecenzenti) {
                    boolean jePrimarni = recenzentJePrimarni.getOrDefault(recenzent.getRecenzentId(), true);
                    dodeliRecenzentaPrijavi(prijava, recenzent, jePrimarni);
                }
            } else {
                // Zamenjaj samo zavrnjene

                // 1. Shrani že dodeljene recenzente
                List<Integer> zeDodeljeni = aktivni.stream()
                        .map(Predizbor::getRecenzentId)
                        .toList();

                // 2. Razdeli zavrnjene na primarne in sekundarne
                List<Predizbor> zavrnjeniPrimarni = zavrnjeni.stream()
                        .filter(Predizbor::isPrimarni).toList();

                List<Predizbor> zavrnjeniSekundarni = zavrnjeni.stream()
                        .filter(p -> !p.isPrimarni()).toList();

                List<Recenzent> kandidati = noviRecenzenti.stream()
                        .filter(r -> !zeDodeljeni.contains(r.getRecenzentId()))
                        .toList();

                for (Predizbor zavrnjen : zavrnjeniPrimarni) {
                    Optional<Recenzent> nadomestni = kandidati.stream()
                            .filter(r -> recenzentJePrimarni.getOrDefault(r.getRecenzentId(), true)) // mora biti primarni
                            .findFirst();

                    nadomestni.ifPresent(rec -> {
                        dodeliRecenzentaPrijavi(prijava, rec, true);
                        kandidati.remove(rec);
                    });
                }

                for (Predizbor zavrnjen : zavrnjeniSekundarni) {
                    Optional<Recenzent> nadomestni = kandidati.stream()
                            .filter(r -> !recenzentJePrimarni.getOrDefault(r.getRecenzentId(), true)) // mora biti sekundarni
                            .findFirst();

                    nadomestni.ifPresent(rec -> {
                        dodeliRecenzentaPrijavi(prijava, rec, false);
                        kandidati.remove(rec);
                    });
                }
            }
        }
    }

    private Set<Recenzent> filtrirajRecenzente(Set<Recenzent> kandidati, Set<Integer> prijaveIds, Set<String> drzaveZaIzlocitev) {
        // Izloči po COI
        List<Integer> izloceniCoi = izloceniCOIRepository.findByPrijavaId(new ArrayList<>(prijaveIds))
                .stream().map(IzloceniCOI::getRecenzentId).collect(Collectors.toList());

        // Izloči po osebnih
        List<Integer> izloceniOsebni = izloceniOsebniRepository.findByPrijavaId(new ArrayList<>(prijaveIds))
                .stream().map(IzloceniOsebni::getRecenzentId).collect(Collectors.toList());

        // Pridobi recenzente, ki so že dodeljeni kateri koli izmed teh prijav
        List<Predizbor> obstojeci = predizborRepository.findByPrijavaIdIn(new ArrayList<>(prijaveIds));
        Set<Integer> zeDodeljeni = obstojeci.stream()
                .map(Predizbor::getRecenzentId)
                .collect(Collectors.toSet());

        /*for (Recenzent r : kandidati) {
            boolean izlocen = false;
            int id = r.getRecenzentId();

            if (izloceniCoi.contains(id)) {
                System.out.println("Recenzent " + id + " izločen zaradi COI.");
                izlocen = true;
            }

            if (izloceniOsebni.contains(id)) {
                System.out.println("Recenzent " + id + " izločen zaradi osebnih razlogov.");
                izlocen = true;
            }

            if (drzaveZaIzlocitev.contains(r.getDrzava())) {
                System.out.println("Recenzent " + id + " izločen zaradi države: " + r.getDrzava());
                izlocen = true;
            }

            if (r.getPrijavePredizbor() + prijaveIds.size() > 10) {
                System.out.println("Recenzent " + id + " presega dovoljeno število prijav.");
                izlocen = true;
            }

            if (zeDodeljeni.contains(id)) {
                System.out.println("Recenzent " + id + " že dodeljen tej prijavi.");
                izlocen = true;
            }

            if (!izlocen) {
                System.out.println("Recenzent " + id + " je USTREZEN.");
            }
        } */
        return kandidati.stream()
                .filter(r -> !izloceniCoi.contains(r.getRecenzentId()))
                .filter(r -> !izloceniOsebni.contains(r.getRecenzentId()))
                .filter(r -> !drzaveZaIzlocitev.contains(r.getDrzava()))
                .filter(r -> r.getPrijavePredizbor() + prijaveIds.size() <= 10)
                .filter(r -> Boolean.FALSE.equals(r.getOdpovedOdstranitev()))
                .filter(r -> !zeDodeljeni.contains(r.getRecenzentId()))  // ❗️izloči že dodeljene
                .collect(Collectors.toSet());
    }

    Set<Recenzent> pridobiPrimerneRecenzenteZaSkupino(List<Prijava> prijavePodpodrocja,
                                                      Map<Integer, Boolean> recenzentJePrimarni) {
        // Seznam recenzentov, ki ustrezajo za vse prijave v skupini
        Set<Recenzent> recenzenti = new HashSet<>();

        // Prvo pridobim vse države, COI in osebne razloge, ki se pojavljajo v tej skupini prijav
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

        // Poiščem vse recenzente, ki so povezani s podpodročji teh prijav
        Set<Recenzent> vsiRecenzenti = new HashSet<>();

        vsiRecenzenti.addAll(recenzentRepository.findEligibleReviewers(primarnoPodpodrocje.getPodpodrocjeId(), primarnoErcPodrocje.getErcId()));

        // Iskanje po dodatni kombinaciji, če obstaja
        if (dodatnoPodpodrocje != null && dodatnoErcPodrocje != null) {
            vsiRecenzenti.addAll(recenzentRepository.findEligibleReviewers(dodatnoPodpodrocje.getPodpodrocjeId(), dodatnoErcPodrocje.getErcId()));
        }

        vsiRecenzenti = filtrirajRecenzente(vsiRecenzenti, vsePrijaveIds, vseDrzave);

        Set<Integer> zeUporabljeni = vsiRecenzenti.stream().map(Recenzent::getRecenzentId).collect(Collectors.toSet());

        Set<Recenzent> vsiRecenzentiList = new HashSet<>(vsiRecenzenti);
        List<Recenzent> recenzentiPrimarnoPolno = new ArrayList<>();
        List<Recenzent> recenzentiPrimarnoSamoPodpodrocje = new ArrayList<>();
        List<Recenzent> recenzentiDodatnoPolno = new ArrayList<>();
        List<Recenzent> recenzentiDodatnoSamoPodpodrocje = new ArrayList<>();

        boolean interdisc = prijavePodpodrocja.getFirst().isInterdisc();

        for (Recenzent r : vsiRecenzentiList) {
            boolean primarnoMatchP = r.getRecenzentiPodrocja().stream().anyMatch(rp -> rp.getPodpodrocjeId() == primarnoPodpodrocje.getPodpodrocjeId());
            boolean primarnoMatchE = r.getRecenzentiErc().stream().anyMatch(re -> re.getErcPodrocjeId() == primarnoErcPodrocje.getErcId());

            boolean dodatnoMatchP = dodatnoPodpodrocje != null && r.getRecenzentiPodrocja().stream().anyMatch(rp -> rp.getPodpodrocjeId() == dodatnoPodpodrocje.getPodpodrocjeId());
            boolean dodatnoMatchE = dodatnoErcPodrocje != null && r.getRecenzentiErc().stream().anyMatch(re -> re.getErcPodrocjeId() == dodatnoErcPodrocje.getErcId());

            boolean izlocenNaPrimarnem = interdisc && !Boolean.TRUE.equals(r.getPorocevalec()) && primarnoMatchP && primarnoMatchE;

            if (izlocenNaPrimarnem) {
                System.out.println("Recenzent " + r.getRecenzentId() + " izločen iz primarnega podpodročja, ker ni poročevalec.");
            }

            //if (primarnoMatchP && primarnoMatchE) recenzentiPrimarnoPolno.add(r);
            if (!izlocenNaPrimarnem && primarnoMatchP && primarnoMatchE) {
                recenzentiPrimarnoPolno.add(r);
            }
            if (dodatnoMatchP && dodatnoMatchE) recenzentiDodatnoPolno.add(r);
        }

        if (vsiRecenzentiList.size() < 5) {
            logger.warn("Premalo recenzentov! Najdenih: {} za kombinacijo Podpodrocje={}, ERC={}.",
                    vsiRecenzentiList.size(), primarnoPodpodrocje.getNaziv(), primarnoErcPodrocje.getKoda());
            logger.info("Število recenzentov za primarno podpodročje: {}", recenzentiPrimarnoPolno.size());
            logger.info("Število recenzentov za dodatno podpodročje: {}", recenzentiDodatnoPolno.size());
        }

        //ce je interdisciplinarna prijava
        if (interdisc) {
            if (recenzentiPrimarnoPolno.size() < 2) {
                System.out.println("⚠️ Premalo recenzentov za PRIMARNO (najdenih: " + recenzentiPrimarnoPolno.size() + "), sprožam fallback.");

                Set<Recenzent> fallbackPrimarno = new HashSet<>(recenzentRepository.findEligibleByPodpodrocjeOnly(primarnoPodpodrocje.getPodpodrocjeId()));
                fallbackPrimarno.removeIf(r -> zeUporabljeni.contains(r.getRecenzentId()));
                fallbackPrimarno = filtrirajRecenzente(fallbackPrimarno, vsePrijaveIds, vseDrzave);

                for (Recenzent r : fallbackPrimarno) {
                    if (!recenzentiPrimarnoPolno.contains(r)) {
                        recenzentiPrimarnoSamoPodpodrocje.add(r);
                        System.out.println("✅ Dodan PRIMARNI fallback recenzent: " + r.getRecenzentId() + " - " + r.getIme() + " " + r.getPriimek());
                    }
                }

                System.out.println("🔎 Skupno PRIMARNIH fallback recenzentov: " + recenzentiPrimarnoSamoPodpodrocje.size());
            }
            if (recenzentiDodatnoPolno.size() < 3) {
                System.out.println("⚠️ Premalo recenzentov za DODATNO (najdenih: " + recenzentiDodatnoPolno.size() + "), sprožam fallback.");
                System.out.println("➡️ Prijava ID " + prijavePodpodrocja.getFirst().getPrijavaId() + " sproža fallback.");
                Set<Recenzent> fallbackDodatno = new HashSet<>(recenzentRepository.findEligibleByPodpodrocjeOnly(dodatnoPodpodrocje.getPodpodrocjeId()));
                fallbackDodatno.removeIf(r -> zeUporabljeni.contains(r.getRecenzentId()));
                fallbackDodatno = filtrirajRecenzente(fallbackDodatno, vsePrijaveIds, vseDrzave);

                for (Recenzent r : fallbackDodatno) {
                    if (!recenzentiDodatnoPolno.contains(r)) {
                        recenzentiDodatnoSamoPodpodrocje.add(r);
                        System.out.println("✅ Dodan DODATNI fallback recenzent: " + r.getRecenzentId() + " - " + r.getIme() + " " + r.getPriimek());
                    }
                }

                System.out.println("🔎 Skupno DODATNIH fallback recenzentov: " + recenzentiDodatnoSamoPodpodrocje.size());
            }
        } else if (recenzentiPrimarnoPolno.size() < 5) {
            Set<Recenzent> fallback = new HashSet<>(recenzentRepository.findEligibleByPodpodrocjeOnly(primarnoPodpodrocje.getPodpodrocjeId()));
            fallback.removeIf(r -> zeUporabljeni.contains(r.getRecenzentId()));
            fallback = filtrirajRecenzente(fallback, vsePrijaveIds, vseDrzave);
            for (Recenzent r : fallback) {
                if (!recenzentiPrimarnoPolno.contains(r)) {
                    recenzentiPrimarnoSamoPodpodrocje.add(r);
                }
            }
        }

        if (interdisc) {
            Set<Recenzent> primarniIzbrani = new HashSet<>();
            Set<Recenzent> dodatniIzbrani = new HashSet<>();

            SecureRandom secureRandom = new SecureRandom();
            Map<Integer, Integer> nakljucneVrednosti = new HashMap<>();
            vsiRecenzenti.forEach(r -> nakljucneVrednosti.put(r.getRecenzentId(), secureRandom.nextInt()));

            Comparator<Recenzent> comparator = Comparator
                    .comparingInt(Recenzent::getPrijavePredizbor).reversed()
                    .thenComparing(r -> nakljucneVrednosti.getOrDefault(r.getRecenzentId(), 0));

            recenzentiPrimarnoPolno.sort(comparator);
            recenzentiPrimarnoSamoPodpodrocje.sort(comparator);
            recenzentiDodatnoPolno.sort(comparator);
            recenzentiDodatnoSamoPodpodrocje.sort(comparator);
            /*
            Collections.shuffle(recenzentiPrimarnoPolno);
            Collections.shuffle(recenzentiPrimarnoSamoPodpodrocje);
            Collections.shuffle(recenzentiDodatnoPolno);
            Collections.shuffle(recenzentiDodatnoSamoPodpodrocje);
            */
            int manjkajociPrimarni = 2;
            for (Recenzent r : recenzentiPrimarnoPolno) {
                if (manjkajociPrimarni == 0) break;
                primarniIzbrani.add(r);
                recenzentJePrimarni.put(r.getRecenzentId(), true);
                manjkajociPrimarni--;
            }
            for (Recenzent r : recenzentiPrimarnoSamoPodpodrocje) {
                if (manjkajociPrimarni == 0) break;
                if (!primarniIzbrani.contains(r)) {
                    primarniIzbrani.add(r);
                    recenzentJePrimarni.put(r.getRecenzentId(), true);
                    manjkajociPrimarni--;
                }
            }

            int manjkajociSekundarni = 3;
            for (Recenzent r : recenzentiDodatnoPolno) {
                if (manjkajociSekundarni == 0 || primarniIzbrani.contains(r)) continue;
                dodatniIzbrani.add(r);
                recenzentJePrimarni.put(r.getRecenzentId(), false);
                manjkajociSekundarni--;
            }
            for (Recenzent r : recenzentiDodatnoSamoPodpodrocje) {
                if (manjkajociSekundarni == 0 || primarniIzbrani.contains(r) || dodatniIzbrani.contains(r)) continue;
                dodatniIzbrani.add(r);
                recenzentJePrimarni.put(r.getRecenzentId(), false);
                manjkajociSekundarni--;
            }

            recenzenti.addAll(primarniIzbrani);
            recenzenti.addAll(dodatniIzbrani);

            // Debug, če je manj kot 5
            if (recenzenti.size() < 5) {
                System.out.println("=== IZBRANI PRIMARNI ===");
                primarniIzbrani.forEach(r ->
                        System.out.println("✅ Primarni: " + r.getRecenzentId() + " - " + r.getIme() + " " + r.getPriimek()));
                System.out.println("=== IZBRANI SEKUNDARNI ===");
                dodatniIzbrani.forEach(r ->
                        System.out.println("✅ Sekundarni: " + r.getRecenzentId() + " - " + r.getIme() + " " + r.getPriimek()));
            }
        } else {
            Set<Recenzent> primarniIzbrani = new HashSet<>();

            //Collections.shuffle(recenzentiPrimarnoPolno);
            //Collections.shuffle(recenzentiPrimarnoSamoPodpodrocje);

            /*Comparator<Recenzent> poPredizborIzkusnjah = Comparator.comparingInt(Recenzent::getPrijavePredizbor).reversed()
                    .thenComparing(r -> new SecureRandom().nextInt());

            recenzentiPrimarnoPolno.sort(poPredizborIzkusnjah);
            recenzentiPrimarnoSamoPodpodrocje.sort(poPredizborIzkusnjah);*/

            // Ustvari mapo z naključnimi vrednostmi za vsak recenzenta (SecureRandom za boljšo razpršenost)
            SecureRandom secureRandom = new SecureRandom();
            Map<Integer, Integer> nakljucneVrednosti = new HashMap<>();
            vsiRecenzenti.forEach(r -> nakljucneVrednosti.put(r.getRecenzentId(), secureRandom.nextInt()));



            Comparator<Recenzent> poPredizborIzkusnjah = Comparator
                    .comparingInt(Recenzent::getPrijavePredizbor).reversed()
                    .thenComparing(r -> nakljucneVrednosti.getOrDefault(r.getRecenzentId(), 0));


            recenzentiPrimarnoPolno.sort(poPredizborIzkusnjah);
            recenzentiPrimarnoSamoPodpodrocje.sort(poPredizborIzkusnjah);

            int manjkajoci = 5;
            primarniIzbrani.addAll(recenzentiPrimarnoPolno.subList(0, Math.min(5, recenzentiPrimarnoPolno.size())));
            manjkajoci -= primarniIzbrani.size();

            if (manjkajoci > 0) {
                primarniIzbrani.addAll(recenzentiPrimarnoSamoPodpodrocje.subList(0, Math.min(manjkajoci, recenzentiPrimarnoSamoPodpodrocje.size())));
            }

            recenzenti.addAll(primarniIzbrani);
            for (Recenzent r : primarniIzbrani) recenzentJePrimarni.put(r.getRecenzentId(), true);
        }

        if (interdisc) {
            boolean jeFallback = recenzenti.stream().anyMatch(recenzentiPrimarnoSamoPodpodrocje::contains)
                    || recenzenti.stream().anyMatch(recenzentiDodatnoSamoPodpodrocje::contains);
            if (jeFallback) {
                for (Prijava p : prijavePodpodrocja) {
                    prijaveZFallbackom.add(p.getPrijavaId());
                }
                logger.info("Fallback za prijave: {} — fallback primarno={}, fallback dodatno={}",
                        prijavePodpodrocja.stream().map(Prijava::getPrijavaId).collect(Collectors.toList()),
                        recenzenti.stream().filter(recenzentiPrimarnoSamoPodpodrocje::contains).count(),
                        recenzenti.stream().filter(recenzentiDodatnoSamoPodpodrocje::contains).count()
                );
            }
        } else {
            boolean jeFallback = recenzenti.stream().anyMatch(recenzentiPrimarnoSamoPodpodrocje::contains);
            if (jeFallback) {
                for (Prijava p : prijavePodpodrocja) {
                    prijaveZFallbackom.add(p.getPrijavaId());
                }
                logger.info("Fallback za prijave: {} — fallback primarno={}",
                        prijavePodpodrocja.stream().map(Prijava::getPrijavaId).collect(Collectors.toList()),
                        recenzenti.stream().filter(recenzentiPrimarnoSamoPodpodrocje::contains).count()
                );
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

    public Predizbor dodeliRecenzentaPrijavi(Prijava prijava, Recenzent recenzent, boolean primarni) {
        // Preverimo, ali recenzent še lahko sprejme novo prijavo v predizbor
        if (recenzent.getPrijavePredizbor() < 10) {
            Predizbor predizbor = new Predizbor();
            predizbor.setPrijavaId(prijava.getPrijavaId());
            predizbor.setRecenzentId(recenzent.getRecenzentId());
            predizbor.setStatus("NEOPREDELJEN");
            predizbor.setPrimarni(primarni);
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
            return predizborRepository.save(predizbor);
        } else {
            System.out.println("Število ki jih ima v predizboru + 1:" + recenzent.getPrijavePredizbor() + " 1");
            throw new RuntimeException("Recenzent " + recenzent.getRecenzentId() +
                    " je dosegel maksimalno število prijav (" + recenzent.getPrijavePredizbor() + ").");
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

