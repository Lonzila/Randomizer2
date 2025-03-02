package si.aris.randomizer2.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import si.aris.randomizer2.model.Predizbor;
import si.aris.randomizer2.model.Prijava;
import si.aris.randomizer2.model.Recenzent;
import si.aris.randomizer2.repository.PredizborRepository;
import si.aris.randomizer2.repository.PrijavaRepository;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExcelExportService {
    @Autowired
    private PredizborRepository predizborRepository;
    @Autowired
    private PrijavaRepository prijavaRepository;
    public void izvozPredizborZEP() throws IOException {
        // 1. Pridobimo vse predizbore iz baze
        List<Predizbor> predizborList = predizborRepository.findAll();

        // 2. Ustvarimo Excel dokument
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Predizbor - ZEP");

        // 3. Nastavite glave stolpcev
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Številka Prijave");
        headerRow.createCell(1).setCellValue("Recenzent 1 (Šifra)");
        headerRow.createCell(2).setCellValue("Recenzent 2 (Šifra)");
        headerRow.createCell(3).setCellValue("Recenzent 3 (Šifra)");
        headerRow.createCell(4).setCellValue("Recenzent 4 (Šifra)");
        headerRow.createCell(5).setCellValue("Recenzent 5 (Šifra)");

        // 4. Prehajamo skozi predizbor seznam in izpisujemo podatke
        int rowNum = 1;
        for (Prijava prijava : prijavaRepository.findAll()) {
            // Filtriramo predizbore za to prijavo
            List<Predizbor> predizborForPrijava = predizborList.stream()
                    .filter(p -> p.getPrijavaId() == prijava.getPrijavaId())
                    .toList();

            // Ustvarimo novo vrstico v Excelu za to prijavo
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(prijava.getStevilkaPrijave());

            // Dodajamo recenzente v vrstico (tako da so v eni vrstici)
            for (int i = 0; i < predizborForPrijava.size(); i++) {
                // Zapišemo šifro recenzenta
                row.createCell(i + 1).setCellValue(predizborForPrijava.get(i).getRecenzentId());
            }
        }

        // 5. Shrani Excel datoteko
        try (FileOutputStream fileOut = new FileOutputStream("predizbor_za_zeps.xlsx")) {
            workbook.write(fileOut);
        }
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

