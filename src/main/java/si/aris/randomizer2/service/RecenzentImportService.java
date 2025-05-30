package si.aris.randomizer2.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import si.aris.randomizer2.model.Recenzent;
import si.aris.randomizer2.repository.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.Resource;
import si.aris.randomizer2.model.*;
import si.aris.randomizer2.repository.*;

import java.io.InputStream;
import java.util.*;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class RecenzentImportService {

    @Autowired
    private RecenzentRepository recenzentRepository;

    @Autowired
    private RecenzentiErcRepository recenzentiErcRepository;

    @Autowired
    private RecenzentiPodrocjaRepository recenzentipodrocjaRepository;

    @Autowired
    private PodpodrocjeRepository podpodrocjeRepository;

    @Autowired
    private ErcPodrocjeRepository ercPodrocjaRepository;

    @Autowired
    private PredizborRepository predizborRepository;

    @Transactional
    public void obdelajExcel(Resource excelResource) throws Exception {
        try (InputStream is = excelResource.getInputStream(); Workbook workbook = new XSSFWorkbook(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();

            // Preskoči glavo
            if (rowIterator.hasNext()) rowIterator.next();

            while (rowIterator.hasNext()) {
                System.out.println("Obdelujem novo vrstico...");
                Row row = rowIterator.next();

                Cell sifraCell = row.getCell(3);
                Cell priimekCell = row.getCell(4);
                Cell imeCell = row.getCell(5);
                Cell emailCell = row.getCell(7);
                Cell drzavaCell = row.getCell(11);
                Cell opombeCell = row.getCell(21);
                Cell podrocjeCell = row.getCell(13);
                Cell podpodrocjeCell = row.getCell(15);
                Cell ercSifraCell = row.getCell(18);

                if (sifraCell == null || sifraCell.getCellType() != CellType.NUMERIC) continue;

                int sifra = (int) sifraCell.getNumericCellValue();
                String opombe = opombeCell != null ? opombeCell.getStringCellValue().toLowerCase() : "";

                // Preveri, če vsebuje vzorec za zamenjavo šifre
                Pattern pattern = Pattern.compile("spremeniti šifro recenzenta iz (\\d+) v (\\d+)");
                Matcher matcher = pattern.matcher(opombe);
                if (matcher.find()) {
                    int staraSifra = Integer.parseInt(matcher.group(1));
                    int novaSifra = Integer.parseInt(matcher.group(2));
                    zamenjajSifroRecenzenta(staraSifra, novaSifra);
                    continue;
                }

                Optional<Recenzent> obstojeci = recenzentRepository.findBySifra(sifra);

                if (opombe.contains("odstraniti")) {
                    obstojeci.ifPresent(r -> {
                        r.setOdpovedOdstranitev(true);
                        recenzentRepository.save(r);
                        System.out.println("Označen za odstranitev: " + r.getIme() + " " + r.getPriimek() + " (" + sifra + ")");
                    });
                    continue;
                }

                if (obstojeci.isEmpty()) {
                    Recenzent nov = new Recenzent();
                    nov.setRecenzentId(sifra);
                    nov.setSifra(sifra);
                    nov.setIme(imeCell != null ? imeCell.getStringCellValue() : "");
                    nov.setPriimek(priimekCell != null ? priimekCell.getStringCellValue() : "");
                    nov.setEPosta(emailCell != null ? emailCell.getStringCellValue() : "");
                    nov.setDrzava(drzavaCell != null ? drzavaCell.getStringCellValue() : "");
                    nov.setOdpovedOdstranitev(false);
                    nov.setProstaMesta(7);
                    nov.setPrijavePredizbor(0);

                    Recenzent saved = recenzentRepository.save(nov);

                    Set<String> vseKategorije = new HashSet<>();
                    Set<String> vppPrefixi = new HashSet<>();

                    if (podpodrocjeCell != null) {
                        String[] podpodrocja = podpodrocjeCell.getStringCellValue().split("\\|");
                        for (String koda : podpodrocja) {
                            koda = koda.trim();
                            if (!koda.isEmpty()) {
                                vseKategorije.add(koda);
                                if (koda.matches("\\d{1,2}\\.\\d{2}\\.\\d{2}")) {
                                    String[] parts = koda.split("\\.");
                                    if (parts.length == 3) {
                                        vppPrefixi.add(parts[0] + "." + parts[1]);
                                    }
                                }
                            }
                        }
                    }

                    if (podrocjeCell != null) {
                        String[] podrocja = podrocjeCell.getStringCellValue().split("\\|");
                        for (String osnovna : podrocja) {
                            String koda = osnovna.trim();
                            if (!koda.isEmpty() && !vppPrefixi.contains(koda)) {
                                vseKategorije.add(koda + ".00");
                            }
                        }
                    }

                    List<String> dodanaPodpodrocja = new ArrayList<>();
                    for (String koda : vseKategorije) {
                        Optional<Podpodrocje> pod = podpodrocjeRepository.findByKoda(koda);
                        pod.ifPresent(p -> {
                            RecenzentiPodrocja povezava = new RecenzentiPodrocja();
                            povezava.setRecenzentId(saved.getRecenzentId());
                            povezava.setPodpodrocjeId(p.getPodpodrocjeId());
                            recenzentipodrocjaRepository.save(povezava);
                            dodanaPodpodrocja.add(koda);
                        });
                    }

                    List<String> dodaniErcji = new ArrayList<>();
                    if (ercSifraCell != null) {
                        String[] ercKratice = ercSifraCell.getStringCellValue().split("\\|");
                        for (String kratica : ercKratice) {
                            Optional<ErcPodrocje> erc = ercPodrocjaRepository.findByKoda(kratica.trim());
                            erc.ifPresent(e -> {
                                RecenzentiErc povezava = new RecenzentiErc();
                                povezava.setRecenzent(saved);
                                povezava.setErcPodrocjeId(e.getErcId());
                                recenzentiErcRepository.save(povezava);
                                dodaniErcji.add(kratica.trim());
                            });
                        }
                    }

                    System.out.println("\nDodan je bil nov recenzent z ID: " + saved.getRecenzentId() + ", šifra: " + sifra + ", ime: " + saved.getIme() + ", priimek: " + saved.getPriimek());
                    System.out.println("  ARIS podpodročja: " + String.join(", ", dodanaPodpodrocja));
                    System.out.println("  ERC področja: " + String.join(", ", dodaniErcji));
                }
            }
        }
    }

    @Transactional
    public void zamenjajSifroRecenzenta(int staraSifra, int novaSifra) {
        Optional<Recenzent> recOpt = recenzentRepository.findBySifra(staraSifra);
        if (recOpt.isEmpty()) {
            System.out.println("⚠️ Recenzent s šifro " + staraSifra + " ne obstaja.");
            return;
        }

        if (recenzentRepository.findBySifra(novaSifra).isPresent()) {
            System.out.println("❌ Recenzent z novo šifro " + novaSifra + " že obstaja.");
            return;
        }

        Recenzent stari = recOpt.get();

        // 1. Ustvari nov entitetni objekt z novo šifro in ID
        Recenzent novi = new Recenzent();
        novi.setRecenzentId(novaSifra);
        novi.setSifra(novaSifra);
        novi.setIme(stari.getIme());
        novi.setPriimek(stari.getPriimek());
        novi.setEPosta(stari.getEPosta());
        novi.setDrzava(stari.getDrzava());
        novi.setPorocevalec(stari.getPorocevalec());
        novi.setOdpovedOdstranitev(stari.getOdpovedOdstranitev());
        novi.setProstaMesta(stari.getProstaMesta());
        novi.setPrijavePredizbor(stari.getPrijavePredizbor());

        // 2. Shrani novo entiteto
        recenzentRepository.saveAndFlush(novi);

        // 3. Posodobi povezave
        recenzentiErcRepository.updateRecenzentId(stari.getRecenzentId(), novi.getRecenzentId());
        recenzentipodrocjaRepository.updateRecenzentId(stari.getRecenzentId(), novi.getRecenzentId());
        predizborRepository.updateRecenzentId(stari.getRecenzentId(), novi.getRecenzentId());
        recenzentiErcRepository.flush();
        recenzentipodrocjaRepository.flush();
        predizborRepository.flush();

        // 4. Izbriši starega recenzenta
        recenzentRepository.delete(stari);

        System.out.println("✅ Recenzent " + staraSifra + " → " + novaSifra + " uspešno zamenjan.");
    }

    public List<Recenzent> getAllRecenzenti() {
        return recenzentRepository.findAll();
    }

    public Recenzent createRecenzent(Recenzent recenzent) {
        return recenzentRepository.save(recenzent);
    }

    public Optional<Recenzent> updateRecenzent(int id, Recenzent updatedRecenzent) {
        return recenzentRepository.findById(id).map(recenzent -> {
            recenzent.setIme(updatedRecenzent.getIme());
            recenzent.setPriimek(updatedRecenzent.getPriimek());
            recenzent.setDrzava(updatedRecenzent.getDrzava());
            recenzent.setPrijavePredizbor(updatedRecenzent.getPrijavePredizbor());
            recenzent.setProstaMesta(updatedRecenzent.getProstaMesta());
            return recenzentRepository.save(recenzent);
        });
    }

    public void deleteRecenzent(int id) {
        recenzentRepository.deleteById(id);
    }

    public List<Recenzent> findAvailableRecenzenti() {
        return recenzentRepository.findByProstaMestaGreaterThan(0);
    }
}
