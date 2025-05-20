package si.aris.randomizer2.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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
                Cell podrocjeCell = row.getCell(13); // Področje ARIS (VP)
                Cell podpodrocjeCell = row.getCell(15); // Podpodročje ARIS (VPP)
                Cell ercSifraCell = row.getCell(18);     // npr. "PE1 / PE6"


                /*if (sifraCell == null) {
                    System.out.println("⚠️ Manjka šifra v vrstici – preskakujem.");
                } else {
                    System.out.println("Najdena šifra: " + sifraCell);
                }*/

                if (sifraCell == null || sifraCell.getCellType() != CellType.NUMERIC) continue;

                int sifra = (int) sifraCell.getNumericCellValue();
                String opombe = opombeCell != null ? opombeCell.getStringCellValue().toLowerCase() : "";

                Optional<Recenzent> obstojeci = recenzentRepository.findBySifra(sifra);

                if (opombe.contains("odstraniti")) {
                    // Označi obstoječega kot odpovedal
                    obstojeci.ifPresent(r -> {
                        r.setOdpovedOdstranitev(true);
                        recenzentRepository.save(r);
                        System.out.println("Označen za odstranitev: " + r.getIme() + " " + r.getPriimek() + " (" + sifra + ")");
                    });
                    continue;
                }

                // Nov vnos
                if (obstojeci.isEmpty()) {
                    Recenzent nov = new Recenzent();
                    nov.setRecenzentId(sifra);
                    nov.setSifra(sifra);
                    nov.setIme(imeCell != null ? imeCell.getStringCellValue() : "");
                    nov.setPriimek(priimekCell != null ? priimekCell.getStringCellValue() : "");
                    nov.setEPosta(emailCell != null ? emailCell.getStringCellValue() : "");
                    nov.setDrzava(drzavaCell != null ? drzavaCell.getStringCellValue() : "");
                    //nov.setPorocevalec(false); // default
                    nov.setProstaMesta(7);
                    nov.setPrijavePredizbor(0);

                    Recenzent saved = recenzentRepository.save(nov);

                    // Združimo podpodročja iz VPP in .00 dodatke iz VP
                    Set<String> vseKategorije = new HashSet<>();
                    Set<String> vppPrefixi = new HashSet<>();

                    // 1. Parse VPP (konkretna podpodročja)
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

                    // 2. Parse VP (generične .00 podkategorije)
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

    // Pridobi vse recenzente
    public List<Recenzent> getAllRecenzenti() {
        return recenzentRepository.findAll();
    }

    // Ustvari novega recenzenta
    public Recenzent createRecenzent(Recenzent recenzent) {
        return recenzentRepository.save(recenzent);
    }

    // Posodobi recenzenta
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

    // Izbriši recenzenta
    public void deleteRecenzent(int id) {
        recenzentRepository.deleteById(id);
    }

    // Poišči vse recenzente z razpoložljivimi mesti
    public List<Recenzent> findAvailableRecenzenti() {
        return recenzentRepository.findByProstaMestaGreaterThan(0);
    }
}
