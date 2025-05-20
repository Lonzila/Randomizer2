package si.aris.randomizer2.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import si.aris.randomizer2.service.PregledOpredelitevService;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/opredelitve")
public class PregledOpredelitveController {

    private final PregledOpredelitevService pregledOpredelitevService;

    @Autowired
    public PregledOpredelitveController(PregledOpredelitevService pregledOpredelitevService) {
        this.pregledOpredelitevService = pregledOpredelitevService;
    }

    @PostMapping("/dodeli-v-ocenjevanje")
    public ResponseEntity<String> dodeliNajboljsePare() {
        try {
            Map<Set<Integer>, List<Integer>> rezultat = pregledOpredelitevService.izberiPareIzBazeInDodeliVocenevanje();

            StringBuilder povzetek = new StringBuilder("Dodeljene prijave:\n");
            rezultat.forEach((par, prijave) -> povzetek
                    .append("Par ").append(par)
                    .append(" → ").append(prijave.size()).append(" prijav: ")
                    .append(prijave).append("\n")
            );

            return ResponseEntity.ok(povzetek.toString());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Napaka pri dodeljevanju: " + e.getMessage());
        }
    }
}

