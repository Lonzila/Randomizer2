package si.aris.randomizer2.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import si.aris.randomizer2.model.Predizbor;
import si.aris.randomizer2.model.Prijava;
import si.aris.randomizer2.model.Recenzent;
import si.aris.randomizer2.repository.PredizborRepository;
import si.aris.randomizer2.repository.PrijavaRepository;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DopolniPredizborService {

    @Autowired
    private PrijavaRepository prijavaRepository;

    @Autowired
    private PredizborRepository predizborRepository;

    @Autowired
    private DodeljevanjeService dodeljevanjeService;

    @Autowired
    private ExcelExportService excelExportService;

    public void dopolniPredizbor(Resource excelResource) throws Exception {
        List<Predizbor> novoDodeljeni = new ArrayList<>();
        Set<Integer> prijaveZFallbackom = new HashSet<>();
        try (InputStream is = excelResource.getInputStream(); Workbook workbook = new XSSFWorkbook(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();
            if (rowIterator.hasNext()) rowIterator.next(); // glava

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                Cell stevilkaPrijaveCell = row.getCell(0); // "Št. prijava"

                if (stevilkaPrijaveCell == null || stevilkaPrijaveCell.getCellType() != CellType.NUMERIC)
                    continue;

                int stevilkaPrijave = (int) stevilkaPrijaveCell.getNumericCellValue();
                Optional<Prijava> optPrijava = prijavaRepository.findByStevilkaPrijave(stevilkaPrijave);
                if (optPrijava.isEmpty()) {
                    System.out.println("⚠️ Prijava s številko " + stevilkaPrijave + " ni bila najdena.");
                    continue;
                }

                Prijava prijava = optPrijava.get();
                List<Predizbor> vsi = predizborRepository.findByPrijavaId(prijava.getPrijavaId());

                /*List<Predizbor> neustrezni = vsi.stream()
                        .filter(p -> p.getStatus().equalsIgnoreCase("OPREDELJEN Z NE")
                                || p.getStatus().equalsIgnoreCase("WITHDRAWN")
                                || p.getStatus().equalsIgnoreCase("STATEMENTS DECLINED"))
                        .toList();

                long primarnihNeustreznih = neustrezni.stream().filter(Predizbor::isPrimarni).count();
                long sekundarnihNeustreznih = neustrezni.stream().filter(p -> !p.isPrimarni()).count();
                */
                List<Predizbor> veljavni = vsi.stream()
                        .filter(p -> p.getStatus().equalsIgnoreCase("OPREDELJEN Z DA")
                                || p.getStatus().equalsIgnoreCase("NEOPREDELJEN")
                                || p.getStatus().equalsIgnoreCase("V OCENJEVANJU")
                                || p.getStatus().equalsIgnoreCase("DODELJENA V OCENJEVANJE"))
                        .toList();

                boolean interdisc = prijava.isInterdisc();

                long trenutniPrimarni = veljavni.stream().filter(Predizbor::isPrimarni).count();
                long trenutniSekundarni = veljavni.stream().filter(p -> !p.isPrimarni()).count();

                long primarnihManjka;
                long sekundarnihManjka;

                if (interdisc) {
                    primarnihManjka = Math.max(0, 2 - trenutniPrimarni);
                    sekundarnihManjka = Math.max(0, 3 - trenutniSekundarni);
                } else {
                    primarnihManjka = Math.max(0, 5 - trenutniPrimarni);
                    sekundarnihManjka = 0; // ni sekundarnih za ne-interdisc
                }

                if (primarnihManjka > 0 || sekundarnihManjka > 0) {
                    System.out.println("Prijava " + stevilkaPrijave + " potrebuje dopolnitev: " +
                            primarnihManjka + " primarnih, " + sekundarnihManjka + " sekundarnih");

                    Map<Integer, Boolean> primarnostMap = new HashMap<>();
                    Set<Recenzent> kandidati = dodeljevanjeService.pridobiPrimerneRecenzenteZaSkupino(
                            List.of(prijava), primarnostMap);

                    Set<Integer> zeDodeljeni = vsi.stream().map(Predizbor::getRecenzentId).collect(Collectors.toSet());

                    List<Recenzent> kandidatiSeznam = kandidati.stream()
                            .filter(r -> !zeDodeljeni.contains(r.getRecenzentId()))
                            .toList();

                    int dodanih = 0;
                    for (Recenzent r : kandidatiSeznam) {
                        boolean jePrimarni = primarnostMap.getOrDefault(r.getRecenzentId(), true);
                        if (jePrimarni && primarnihManjka > 0) {
                            Predizbor p = dodeljevanjeService.dodeliRecenzentaPrijavi(prijava, r, true);
                            primarnihManjka--;
                            dodanih++;
                            novoDodeljeni.add(p);
                        } else if (!jePrimarni && sekundarnihManjka > 0) {
                            Predizbor p = dodeljevanjeService.dodeliRecenzentaPrijavi(prijava, r, false);
                            sekundarnihManjka--;
                            dodanih++;
                            novoDodeljeni.add(p);
                        }
                        if (primarnihManjka == 0 && sekundarnihManjka == 0) break;
                    }

                    System.out.println("  → Dodanih novih recenzentov: " + dodanih);

                }
            }
            excelExportService.izvoziSamoNoveZaKontrolo(novoDodeljeni);
            excelExportService.izvoziNovoDodeljeneVeljavne();
            excelExportService.izvozPredizborPravilnost();
            excelExportService.exportPredizborToExcel(prijaveZFallbackom);
        }
    }
}
