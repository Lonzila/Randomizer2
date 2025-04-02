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

@RestController
@RequestMapping("/api/dodeljevanje")
public class DodeljevanjeController {

    @Autowired
    private DodeljevanjeService dodeljevanjeService;

    @PostMapping("/predizbor")
    public ResponseEntity<ByteArrayResource> predizbor() {
        try {
            System.out.println("Začel izvajati predizbor");
            ByteArrayResource datoteka = dodeljevanjeService.predizbor();  // nova metoda v service
            System.out.println("Predizbor zaključen");

            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=predizbor.xlsx")
                    .header("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                    .body(datoteka);

        } catch (Exception e) {
            e.printStackTrace();  // Izpiši natančne informacije o napaki
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/odstraniVseDodelitve")
    public String odstraniVseDodelitve(Model model) {
        dodeljevanjeService.odstraniVseDodelitve();
        return "dodeljevanje/predizbor"; // ali katera koli stran, na katero naj se vrne
    }
}
