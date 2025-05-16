package si.aris.randomizer2.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import si.aris.randomizer2.model.Predizbor;
import si.aris.randomizer2.repository.PredizborRepository;

import java.io.InputStream;
import java.util.Iterator;
import java.util.Optional;

@Service
public class ZavrnitveImportService {

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
}

