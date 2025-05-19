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
    public ResponseEntity<Void> predizbor() {
        try {
            System.out.println("Začel izvajati predizbor");
            // kličete glavno metodo
            dodeljevanjeService.predizbor(); // ali druga metoda, kjer je vsebina
            return ResponseEntity.ok().build(); // samo "200 OK"
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/odstraniVseDodelitve")
    public String odstraniVseDodelitve(Model model) {
        dodeljevanjeService.odstraniVseDodelitve();
        return "dodeljevanje/predizbor"; // ali katera koli stran, na katero naj se vrne
    }
}
