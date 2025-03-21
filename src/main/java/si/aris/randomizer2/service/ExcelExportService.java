package si.aris.randomizer2.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import si.aris.randomizer2.model.Predizbor;
import si.aris.randomizer2.model.Prijava;
import si.aris.randomizer2.model.Recenzent;
import si.aris.randomizer2.repository.PredizborRepository;
import si.aris.randomizer2.repository.PrijavaRepository;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ExcelExportService {
    @Autowired
    private PredizborRepository predizborRepository;
    @Autowired
    private PrijavaRepository prijavaRepository;

    public ByteArrayResource exportPredizborToExcel() throws IOException {
        List<Prijava> prijave = prijavaRepository.findAll();
        List<Predizbor> predizbor = predizborRepository.findAll();
        Map<Integer, List<Predizbor>> predizborMap = predizbor.stream().collect(Collectors.groupingBy(Predizbor::getPrijavaId));

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Predizbor");

        Row headerRow = sheet.createRow(0);
        String[] columns = {"ID Prijave", "Številka Prijave", "Naslov", "Vodja", "Šifra Vodje", "Podpodročje", "ERC", "Dodatno Podpodročje", "Dodatno ERC", "Interdisciplinarnost", "Partnerska Agencija 1", "Partnerska Agencija 2", "Status Prijave", "Rec1", "Rec1 - Status", "Rec2", "Rec2 - Status", "Rec3", "Rec3 - Status", "Rec4", "Rec4 - Status", "Rec5", "Rec5 - Status"};

        for (int i = 0; i < columns.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
        }

        int rowNum = 1;
        for (Prijava prijava : prijave) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(prijava.getPrijavaId());
            row.createCell(1).setCellValue(prijava.getStevilkaPrijave());
            row.createCell(2).setCellValue(prijava.getNaslov());
            row.createCell(3).setCellValue(prijava.getVodja());
            row.createCell(4).setCellValue(prijava.getSifraVodje());
            row.createCell(5).setCellValue(prijava.getPodpodrocje().getNaziv());
            row.createCell(6).setCellValue(prijava.getErcPodrocje().getKoda());
            row.createCell(7).setCellValue(prijava.getDodatnoPodpodrocje() != null ? prijava.getDodatnoPodpodrocje().getNaziv() : "");
            row.createCell(8).setCellValue(prijava.getDodatnoErcPodrocje() != null ? prijava.getDodatnoErcPodrocje().getKoda() : "");
            row.createCell(9).setCellValue(prijava.isInterdisc() ? "DA" : "NE");
            row.createCell(10).setCellValue(prijava.getPartnerskaAgencija1() != null ? prijava.getPartnerskaAgencija1() : "");
            row.createCell(11).setCellValue(prijava.getPartnerskaAgencija2() != null ? prijava.getPartnerskaAgencija2() : "");
            row.createCell(12).setCellValue(prijava.getStatusPrijav().getNaziv());

            List<Predizbor> recenzenti = predizborMap.getOrDefault(prijava.getPrijavaId(), List.of());
            for (int i = 0; i < Math.min(recenzenti.size(), 5); i++) {
                row.createCell(13 + (i * 2)).setCellValue(recenzenti.get(i).getRecenzentId());
                row.createCell(14 + (i * 2)).setCellValue(recenzenti.get(i).getStatus());
            }
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        return new ByteArrayResource(outputStream.toByteArray());
    }

    public void izvozPredizborPravilnost() throws IOException {
        // 1. Pridobimo vse predizbore iz baze
        List<Predizbor> predizborList = predizborRepository.findAll();

        // 2. Ustvarimo Excel dokument
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Predizbor - Pravilnost");

        // 3. Nastavite glave stolpcev
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Številka Prijave");
        headerRow.createCell(1).setCellValue("Recenzent ID (Šifra)");

        // 4. Prehajamo skozi predizbor seznam in izpisujemo podatke
        int rowNum = 1;
        for (Predizbor predizbor : predizborList) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(predizbor.getPrijavaId());
            row.createCell(1).setCellValue(predizbor.getRecenzentId());
        }

        // 5. Shrani Excel datoteko
        try (FileOutputStream fileOut = new FileOutputStream("predizbor_pravilnost.xlsx")) {
            workbook.write(fileOut);
        }
    }
}

