package si.aris.randomizer2.controller;

import org.springframework.beans.factory.annotation.Autowired;
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
    @GetMapping("/izvoz-predizbor-zep")
    public String izvozPredizborZEP() {
        try {
            excelExportService.izvozPredizborZEP();  // Kliče funkcijo za izvoz
            return "Datoteka za ZEP je bila uspešno izvožena!";
        } catch (IOException e) {
            e.printStackTrace();
            return "Napaka pri izvozu datoteke za ZEP!";
        }
    }

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
}
