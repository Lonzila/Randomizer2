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

import java.io.InputStream;
import java.util.Iterator;
import java.util.Optional;

@Service
public class StatusImportService {

    @Autowired
    private PredizborRepository predizborRepository;

    public void obdelajZavrnitve(Resource excelResource) throws Exception {
        try (InputStream is = excelResource.getInputStream(); Workbook workbook = new XSSFWorkbook(is)) {
            Sheet sheet = workbook.getSheet("Export");
            Iterator<Row> rowIterator = sheet.iterator();

            if (rowIterator.hasNext()) rowIterator.next(); // preskoči glavo

            int posodobljeni = 0;
            int preskoceni = 0;

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();

                Cell prijavaCell = row.getCell(0); // "Prijava"
                Cell recenzentCell = row.getCell(4); // "ID Recenzenta"
                Cell statusCell = row.getCell(6); // "Status"

                if (prijavaCell == null || recenzentCell == null || statusCell == null) {
                    preskoceni++;
                    continue;
                }

                String status = statusCell.getStringCellValue().toLowerCase().trim();

                int prijavaId = (int) prijavaCell.getNumericCellValue();
                int recenzentId = (int) recenzentCell.getNumericCellValue();

                Optional<Predizbor> opt = predizborRepository.findByPrijavaIdAndRecenzentId(prijavaId, recenzentId);
                if (opt.isPresent()) {
                    Predizbor p = opt.get();
                    if (status.startsWith("can't evaluate")) {
                        p.setStatus("OPREDELJEN Z NE");
                        System.out.println("Posodobljen na OPREDELJEN Z NE: prijava_id=" + prijavaId + ", recenzent_id=" + recenzentId);
                    } else if (status.contains("withdrawn")) {
                        p.setStatus("WITHDRAWN");
                        System.out.println("Posodobljen na WITHDRAWN: prijava_id=" + prijavaId + ", recenzent_id=" + recenzentId);
                    } else if (status.contains("statement") || status.contains("declined")) {
                        p.setStatus("STATEMENTS DECLINED");
                        System.out.println("Posodobljen na STATEMENTS DECLINED: prijava_id=" + prijavaId + ", recenzent_id=" + recenzentId);
                    } else {
                        preskoceni++;
                        continue;
                    }
                    predizborRepository.save(p);
                    posodobljeni++;
                } else {
                    System.out.println("⚠️ Ni najden predizbor za prijava_id=" + prijavaId + ", recenzent_id=" + recenzentId);
                    preskoceni++;
                }
            }

            System.out.println("Skupno posodobljenih: " + posodobljeni);
            System.out.println("Preskočenih: " + preskoceni);
        }
    }

    public void obdelajPotrditve(Resource excelResource) throws Exception {
        try (InputStream is = excelResource.getInputStream(); Workbook workbook = new XSSFWorkbook(is)) {
            Sheet sheet = workbook.getSheet("Export");
            Iterator<Row> rowIterator = sheet.iterator();

            if (rowIterator.hasNext()) rowIterator.next(); // preskoči glavo

            int posodobljeni = 0;
            int preskoceni = 0;

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();

                Cell prijavaCell = row.getCell(1); // "Prijava"
                Cell recenzentCell = row.getCell(5); // "ID Recenzenta"
                Cell statusCell = row.getCell(7); // "Status"

                if (prijavaCell == null || recenzentCell == null || statusCell == null) {
                    preskoceni++;
                    continue;
                }

                String status = statusCell.getStringCellValue().toLowerCase().trim();

                if (!status.startsWith("can evaluate")) {
                    preskoceni++;
                    continue;
                }

                int prijavaId = (int) prijavaCell.getNumericCellValue();
                int recenzentId = (int) recenzentCell.getNumericCellValue();

                Optional<Predizbor> opt = predizborRepository.findByPrijavaIdAndRecenzentId(prijavaId, recenzentId);
                if (opt.isPresent()) {
                    Predizbor p = opt.get();
                    p.setStatus("OPREDELJEN Z DA");
                    predizborRepository.save(p);
                    System.out.println("✅ Posodobljen na OPREDELJEN Z DA: prijava_id=" + prijavaId + ", recenzent_id=" + recenzentId);
                    posodobljeni++;
                } else {
                    System.out.println("⚠️ Ni najden predizbor za prijava_id=" + prijavaId + ", recenzent_id=" + recenzentId);
                    preskoceni++;
                }
            }

            System.out.println("Skupno OPREDELJEN Z DA: " + posodobljeni);
            System.out.println("Preskočenih vrstic: " + preskoceni);
        }
    }

    public void obdelajOpredelitve(Resource excelResource) throws Exception {
        try (InputStream is = excelResource.getInputStream(); Workbook workbook = new XSSFWorkbook(is)) {
            Sheet sheet = workbook.getSheet("Export");
            Iterator<Row> rowIterator = sheet.iterator();

            if (rowIterator.hasNext()) rowIterator.next(); // preskoči glavo

            int posodobljeni = 0;
            int preskoceni = 0;

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();

                Cell prijavaCell = row.getCell(1); // "Prijava"
                Cell recenzentCell = row.getCell(5); // "ID Recenzenta"
                Cell statusCell = row.getCell(7); // "Status"

                if (prijavaCell == null || recenzentCell == null || statusCell == null) {
                    preskoceni++;
                    continue;
                }

                String statusRaw = statusCell.getStringCellValue();
                if (statusRaw == null) {
                    preskoceni++;
                    continue;
                }

                String status = statusRaw.toLowerCase().trim();
                String novStatus = null;

                if (status.startsWith("can evaluate")) {
                    novStatus = "OPREDELJEN Z DA";
                } else if (status.startsWith("can't evaluate")) {
                    novStatus = "OPREDELJEN Z NE";
                } else if (status.contains("withdrawn")) {
                    novStatus = "WITHDRAWN";
                } else if (status.contains("statement") || status.contains("declined")) {
                    novStatus = "STATEMENTS DECLINED";
                } else {
                    preskoceni++;
                    continue;
                }

                int prijavaId = (int) prijavaCell.getNumericCellValue();
                int recenzentId = (int) recenzentCell.getNumericCellValue();

                Optional<Predizbor> opt = predizborRepository.findByPrijavaIdAndRecenzentId(prijavaId, recenzentId);
                if (opt.isPresent()) {
                    Predizbor p = opt.get();
                    String prejsnji = p.getStatus();
                    if (!novStatus.equalsIgnoreCase(prejsnji)) {
                        p.setStatus(novStatus);
                        predizborRepository.save(p);
                        System.out.println("✅ Posodobljen: prijava_id=" + prijavaId + ", recenzent_id=" + recenzentId +
                                " → " + prejsnji + " → " + novStatus);
                    }
                    posodobljeni++;
                } else {
                    System.out.println("⚠️ Ni najden predizbor za prijava_id=" + prijavaId + ", recenzent_id=" + recenzentId);
                    preskoceni++;
                }
            }

            System.out.println("✔️ Skupno posodobljenih: " + posodobljeni);
            System.out.println("ℹ️ Preskočenih vrstic: " + preskoceni);
        }
    }


}

