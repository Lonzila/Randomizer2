package si.aris.randomizer2.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import si.aris.randomizer2.model.*;
import si.aris.randomizer2.repository.*;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ExcelExportService {
    @Autowired
    private PredizborRepository predizborRepository;
    @Autowired
    private PrijavaRepository prijavaRepository;
    @Autowired
    private RecenzentRepository recenzentRepository;
    @Autowired
    private PodpodrocjeRepository podpodrocjeRepository;
    @Autowired
    private ErcPodrocjeRepository ercPodrocjeRepository;

    public ByteArrayResource exportPredizborToExcel(Set<Integer> prijaveZFallbackom) throws IOException {
        List<Prijava> prijave = prijavaRepository.findAll();
        List<Predizbor> predizbor = predizborRepository.findAll();
        Map<Integer, List<Predizbor>> predizborMap = predizbor.stream().collect(Collectors.groupingBy(Predizbor::getPrijavaId));

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Predizbor");

        Row timestampRow = sheet.createRow(0);
        Cell timestampCell = timestampRow.createCell(0);
        String timestamp = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
        timestampCell.setCellValue("Izvoz: " + timestamp);

        Row headerRow = sheet.createRow(1);
        List<String> columns = new ArrayList<>(List.of(
                "ID Prijave", "Številka Prijave", "Naslov", "Vodja", "Šifra Vodje",
                "Podpodročje ARIS - naziv", "Podpodročje ARIS - koda", "ERC",
                "Dodatno Podpodročje - naziv", "Dodatno Podpodročje ARIS - koda", "Dodatno ERC",
                "Interdisciplinarnost", "Partnerska Agencija 1", "Partnerska Agencija 2", "Status Prijave",
                "Fallback Podpodročje - brez ERC"
        ));

        // dodaj recenzent stolpce (do 10 recenzentov * 2 stolpca)
        for (int i = 1; i <= 10; i++) {
            columns.add("Rec" + i);
            columns.add("Rec" + i + " - Status");
        }

        for (int i = 0; i < columns.size(); i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns.get(i));
        }

        int rowNum = 2;
        for (Prijava prijava : prijave) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(prijava.getPrijavaId());
            row.createCell(1).setCellValue(prijava.getStevilkaPrijave());
            row.createCell(2).setCellValue(prijava.getNaslov());
            row.createCell(3).setCellValue(prijava.getVodja());
            row.createCell(4).setCellValue(prijava.getSifraVodje());
            row.createCell(5).setCellValue(prijava.getPodpodrocje().getNaziv());
            row.createCell(6).setCellValue(prijava.getPodpodrocje().getKoda());
            row.createCell(7).setCellValue(prijava.getErcPodrocje().getKoda());
            row.createCell(8).setCellValue(prijava.getDodatnoPodpodrocje() != null ? prijava.getDodatnoPodpodrocje().getNaziv() : "");
            row.createCell(9).setCellValue(prijava.getDodatnoPodpodrocje() != null ? prijava.getDodatnoPodpodrocje().getKoda() : "");
            row.createCell(10).setCellValue(prijava.getDodatnoErcPodrocje() != null ? prijava.getDodatnoErcPodrocje().getKoda() : "");
            row.createCell(11).setCellValue(prijava.isInterdisc() ? "DA" : "NE");
            row.createCell(12).setCellValue(prijava.getPartnerskaAgencija1() != null ? prijava.getPartnerskaAgencija1() : "");
            row.createCell(13).setCellValue(prijava.getPartnerskaAgencija2() != null ? prijava.getPartnerskaAgencija2() : "");
            row.createCell(14).setCellValue(prijava.getStatusPrijav().getNaziv());

            String fallbackTag = prijaveZFallbackom.contains(prijava.getPrijavaId()) ? "DA" : "NE";
            row.createCell(15).setCellValue(fallbackTag);

            List<Predizbor> recenzenti = predizborMap.getOrDefault(prijava.getPrijavaId(), List.of());
            for (int i = 0; i < Math.min(recenzenti.size(), 10); i++) {
                int recenzentId = recenzenti.get(i).getRecenzentId();
                Recenzent rec = recenzentRepository.findById(recenzentId).orElse(null);
                String sifra = rec != null ? String.valueOf(rec.getSifra()) : "NEZNANO";
                row.createCell(16 + i * 2).setCellValue(sifra);
                row.createCell(17 + i * 2).setCellValue(recenzenti.get(i).getStatus());
            }
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        return new ByteArrayResource(outputStream.toByteArray());
    }

    public void izvozPredizborPravilnost() throws IOException {
        List<Predizbor> predizborList = predizborRepository.findAll();

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Predizbor - Pravilnost");

        Row timestampRow = sheet.createRow(0);
        Cell timestampCell = timestampRow.createCell(0);
        String timestamp = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
        timestampCell.setCellValue("Izvoz: " + timestamp);

        Row headerRow = sheet.createRow(1);
        headerRow.createCell(0).setCellValue("Številka prijave");
        headerRow.createCell(1).setCellValue("Šifra recenzenta");
        headerRow.createCell(2).setCellValue("Ime in priimek");
        headerRow.createCell(3).setCellValue("Ime");
        headerRow.createCell(4).setCellValue("Priimek");
        headerRow.createCell(5).setCellValue("Podpodročje");
        headerRow.createCell(6).setCellValue("ERC");
        headerRow.createCell(7).setCellValue("Dodatno podpodročje");
        headerRow.createCell(8).setCellValue("Dodatno ERC");

        int rowNum = 2;
        for (Predizbor predizbor : predizborList) {
            Optional<Prijava> prijavaOpt = prijavaRepository.findById(predizbor.getPrijavaId());
            Optional<Recenzent> recenzentOpt = recenzentRepository.findById(predizbor.getRecenzentId());

            if (prijavaOpt.isEmpty() || recenzentOpt.isEmpty()) continue;

            Prijava prijava = prijavaOpt.get();
            Recenzent recenzent = recenzentOpt.get();

            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(prijava.getStevilkaPrijave());
            row.createCell(1).setCellValue(recenzent.getSifra());
            row.createCell(2).setCellValue(recenzent.getIme() + " " + recenzent.getPriimek());
            row.createCell(3).setCellValue(recenzent.getIme());
            row.createCell(4).setCellValue(recenzent.getPriimek());
            row.createCell(5).setCellValue(prijava.getPodpodrocje() != null ? prijava.getPodpodrocje().getKoda() : "");
            row.createCell(6).setCellValue(prijava.getErcPodrocje() != null ? prijava.getErcPodrocje().getKoda() : "");
            row.createCell(7).setCellValue(prijava.getDodatnoPodpodrocje() != null ? prijava.getDodatnoPodpodrocje().getKoda() : "");
            row.createCell(8).setCellValue(prijava.getDodatnoErcPodrocje() != null ? prijava.getDodatnoErcPodrocje().getKoda() : "");
        }

        try (FileOutputStream fileOut = new FileOutputStream("predizbor_pravilnost.xlsx")) {
            workbook.write(fileOut);
        }
    }

    public ByteArrayResource exportRecenzentiWithAreasToExcel() throws IOException {
        List<Recenzent> recenzenti = recenzentRepository.findAll();

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Recenzenti");
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Šifra", "Ime", "Priimek", "Tip", "Koda", "Naziv"};
        for (int i = 0; i < headers.length; i++) {
            headerRow.createCell(i).setCellValue(headers[i]);
        }

        int rowNum = 1;

        for (Recenzent rec : recenzenti) {
            for (RecenzentiPodrocja rp : rec.getRecenzentiPodrocja()) {
                Podpodrocje pod = podpodrocjeRepository.findById(rp.getPodpodrocjeId()).orElse(null);
                if (pod != null) {
                    Row row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(rec.getSifra());
                    row.createCell(1).setCellValue(rec.getIme());
                    row.createCell(2).setCellValue(rec.getPriimek());
                    row.createCell(3).setCellValue("Podpodročje");
                    row.createCell(4).setCellValue(pod.getKoda());
                    row.createCell(5).setCellValue(pod.getNaziv());
                }
            }

            for (RecenzentiErc re : rec.getRecenzentiErc()) {
                ErcPodrocje erc = ercPodrocjeRepository
                        .findById(Long.valueOf(re.getErcPodrocjeId()))
                        .orElse(null);
                if (erc != null) {
                    Row row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(rec.getSifra());
                    row.createCell(1).setCellValue(rec.getIme());
                    row.createCell(2).setCellValue(rec.getPriimek());
                    row.createCell(3).setCellValue("ERC");
                    row.createCell(4).setCellValue(erc.getKoda());
                    row.createCell(5).setCellValue("");
                }
            }
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        return new ByteArrayResource(outputStream.toByteArray());
    }

    public void izvoziSamoNoveZaKontrolo(List<Predizbor> novoDodeljeni) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Novi predlogi za kontrolo");

        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Številka prijave");
        headerRow.createCell(1).setCellValue("Šifra recenzenta");
        headerRow.createCell(2).setCellValue("Ime in priimek");
        headerRow.createCell(3).setCellValue("Ime");
        headerRow.createCell(4).setCellValue("Priimek");
        headerRow.createCell(5).setCellValue("Podpodročje");
        headerRow.createCell(6).setCellValue("ERC");
        headerRow.createCell(7).setCellValue("Dodatno podpodročje");
        headerRow.createCell(8).setCellValue("Dodatno ERC");

        int rowNum = 1;
        for (Predizbor p : novoDodeljeni) {
            Optional<Prijava> prijava = prijavaRepository.findById(p.getPrijavaId());
            Optional<Recenzent> recenzent = recenzentRepository.findById(p.getRecenzentId());
            if (prijava.isEmpty() || recenzent.isEmpty()) continue;

            Prijava prij = prijava.get();
            Recenzent rec = recenzent.get();

            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(prij.getStevilkaPrijave());
            row.createCell(1).setCellValue(rec.getSifra());
            row.createCell(2).setCellValue(rec.getIme() + " " + rec.getPriimek());
            row.createCell(3).setCellValue(rec.getIme());
            row.createCell(4).setCellValue(rec.getPriimek());
            row.createCell(5).setCellValue(prij.getPodpodrocje() != null ? prij.getPodpodrocje().getKoda() : "");
            row.createCell(6).setCellValue(prij.getErcPodrocje() != null ? prij.getErcPodrocje().getKoda() : "");
            row.createCell(7).setCellValue(prij.getDodatnoPodpodrocje() != null ? prij.getDodatnoPodpodrocje().getKoda() : "");
            row.createCell(8).setCellValue(prij.getDodatnoErcPodrocje() != null ? prij.getDodatnoErcPodrocje().getKoda() : "");
        }

        try (FileOutputStream fileOut = new FileOutputStream("izvoz_novih_predlogov_kontrola.xlsx")) {
            workbook.write(fileOut);
        }
    }

    public void izvoziNovoDodeljeneVeljavne() throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Veljavni predizbori novo stanje");

        Row headerRow = sheet.createRow(0);
        List<String> columns = new ArrayList<>(List.of(
                "ID Prijave", "Številka Prijave", "Naslov", "Vodja", "Šifra Vodje",
                "Podpodročje ARIS - naziv", "Podpodročje ARIS - koda", "ERC",
                "Dodatno Podpodročje - naziv", "Dodatno Podpodročje ARIS - koda", "Dodatno ERC",
                "Interdisciplinarnost", "Partnerska Agencija 1", "Partnerska Agencija 2", "Status Prijave"
        ));

        for (int i = 1; i <= 10; i++) {
            columns.add("Rec" + i);
            columns.add("Rec" + i + " - Status");
        }

        for (int i = 0; i < columns.size(); i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns.get(i));
        }

        int rowNum = 1;
        List<Prijava> prijave = prijavaRepository.findAll();
        List<Predizbor> predizbor = predizborRepository.findAll();
        Map<Integer, List<Predizbor>> mapirani = predizbor.stream()
                .filter(p -> p.getStatus().equalsIgnoreCase("OPREDELJEN Z DA")
                        || p.getStatus().equalsIgnoreCase("NEOPREDELJEN")
                        || p.getStatus().equalsIgnoreCase("V OCENJEVANJU")
                        || p.getStatus().equalsIgnoreCase("DODELJENA V OCENJEVANJE"))
                .collect(Collectors.groupingBy(Predizbor::getPrijavaId));

        for (Prijava prijava : prijave) {
            List<Predizbor> recenzenti = mapirani.getOrDefault(prijava.getPrijavaId(), List.of());
            if (recenzenti.isEmpty()) continue;

            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(prijava.getPrijavaId());
            row.createCell(1).setCellValue(prijava.getStevilkaPrijave());
            row.createCell(2).setCellValue(prijava.getNaslov());
            row.createCell(3).setCellValue(prijava.getVodja());
            row.createCell(4).setCellValue(prijava.getSifraVodje());
            row.createCell(5).setCellValue(prijava.getPodpodrocje().getNaziv());
            row.createCell(6).setCellValue(prijava.getPodpodrocje().getKoda());
            row.createCell(7).setCellValue(prijava.getErcPodrocje().getKoda());
            row.createCell(8).setCellValue(prijava.getDodatnoPodpodrocje() != null ? prijava.getDodatnoPodpodrocje().getNaziv() : "");
            row.createCell(9).setCellValue(prijava.getDodatnoPodpodrocje() != null ? prijava.getDodatnoPodpodrocje().getKoda() : "");
            row.createCell(10).setCellValue(prijava.getDodatnoErcPodrocje() != null ? prijava.getDodatnoErcPodrocje().getKoda() : "");
            row.createCell(11).setCellValue(prijava.isInterdisc() ? "DA" : "NE");
            row.createCell(12).setCellValue(prijava.getPartnerskaAgencija1() != null ? prijava.getPartnerskaAgencija1() : "");
            row.createCell(13).setCellValue(prijava.getPartnerskaAgencija2() != null ? prijava.getPartnerskaAgencija2() : "");
            row.createCell(14).setCellValue(prijava.getStatusPrijav().getNaziv());

            for (int i = 0; i < Math.min(recenzenti.size(), 10); i++) {
                int recenzentId = recenzenti.get(i).getRecenzentId();
                Recenzent rec = recenzentRepository.findById(recenzentId).orElse(null);
                String sifra = rec != null ? String.valueOf(rec.getSifra()) : "NEZNANO";
                row.createCell(15 + i * 2).setCellValue(sifra);
                row.createCell(16 + i * 2).setCellValue(recenzenti.get(i).getStatus());
            }
        }

        String timestamp = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        try (FileOutputStream fileOut = new FileOutputStream("export_predizbor_novo_stanje_" + timestamp + ".xlsx")) {
            workbook.write(fileOut);
        }
    }
}

