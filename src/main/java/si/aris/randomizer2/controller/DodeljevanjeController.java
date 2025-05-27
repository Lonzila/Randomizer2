package si.aris.randomizer2.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import si.aris.randomizer2.service.DodeljevanjeService;
import si.aris.randomizer2.service.ExcelExportService;

@RestController
@RequestMapping("/api/dodeljevanje")
public class DodeljevanjeController {

    @Autowired
    private DodeljevanjeService dodeljevanjeService;
    @Autowired
    private ExcelExportService excelExportService;

    @PostMapping("/predizbor")
    public ResponseEntity<Void> predizbor() {
        try {
            System.out.println("Začel izvajati predizbor");

            dodeljevanjeService.predizbor();

            // to je potrebno še testirat
            excelExportService.exportPredizborToExcel(dodeljevanjeService.getPrijaveZFallbackom());

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/odstraniVseDodelitve")
    public String odstraniVseDodelitve(Model model) {
        dodeljevanjeService.odstraniVseDodelitve();
        return "dodeljevanje/predizbor";
    }
}
