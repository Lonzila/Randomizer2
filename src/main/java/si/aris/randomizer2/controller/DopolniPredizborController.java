package si.aris.randomizer2.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import si.aris.randomizer2.service.DopolniPredizborService;

import java.io.IOException;

@RestController
@RequestMapping("/api/dopolni-predizbor")
public class DopolniPredizborController {

    @Autowired
    private DopolniPredizborService dopolniPredizborService;

    @PostMapping
    public ResponseEntity<String> dopolni(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("Datoteka je prazna.");
            }

            var resource = new ByteArrayResource(file.getBytes());
            dopolniPredizborService.dopolniPredizbor(resource);

            return ResponseEntity.ok("Dopolnjevanje predizbora zaključeno.");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Napaka pri branju datoteke: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Napaka pri obdelavi: " + e.getMessage());
        }
    }
}

