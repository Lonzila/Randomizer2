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
@RequestMapping("/api/opredelitev")
public class PregledOpredelitveController {

    private final PregledOpredelitevService pregledOpredelitevService;

    @Autowired
    public PregledOpredelitveController(PregledOpredelitevService pregledOpredelitevService) {
        this.pregledOpredelitevService = pregledOpredelitevService;
    }

    @PostMapping("/najpogostejsi-pari")
    public ResponseEntity<?> izracunajNajpogostejsiPare(@RequestParam("file") MultipartFile file) {
        try {
            Resource resource = file.getResource();

            Map<Set<Integer>, List<Integer>> rezultat = pregledOpredelitevService.izracunajNajboljPogostePare(resource);

            return ResponseEntity.ok(rezultat);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Napaka pri obdelavi datoteke: " + e.getMessage());
        }
    }
}

