package si.aris.randomizer2.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import si.aris.randomizer2.service.ExcelExportService;

import java.io.IOException;

@RestController
@RequestMapping("/api/excel")
public class ExcelExportController {

    @Autowired
    private ExcelExportService excelExportService;

    // HTTP GET zahteva za izvoz predizbora v ZEP obliko
    /*@GetMapping("/predizbor")
    public ResponseEntity<ByteArrayResource> exportPredizbor() throws IOException {
        ByteArrayResource resource = excelExportService.exportPredizborToExcel();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=predizbor.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }*/

    // HTTP GET zahteva za izvoz predizbora za pravilnost
    @GetMapping("/izvoz-predizbor-pravilnost")
    public String izvozPredizborPravilnost() {
        try {
            excelExportService.izvozPredizborPravilnost();  // Kliče funkcijo za izvoz
            return "Datoteka za pravilnost je bila uspešno izvožena!";
        } catch (IOException e) {
            e.printStackTrace();
            return "Napaka pri izvozu datoteke za pravilnost!";
        }
    }
    @GetMapping("/recenzenti-areas")
    public ResponseEntity<ByteArrayResource> exportRecenzentiWithAreas() {
        try {
            ByteArrayResource resource = excelExportService.exportRecenzentiWithAreasToExcel();
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=recenzenti_areas.xlsx")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
