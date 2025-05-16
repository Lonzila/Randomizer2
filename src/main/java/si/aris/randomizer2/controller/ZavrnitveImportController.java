package si.aris.randomizer2.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import si.aris.randomizer2.service.ZavrnitveImportService;

import java.io.IOException;

@RestController
@RequestMapping("/api/zavrnitve-import")
public class ZavrnitveImportController {

    @Autowired
    private ZavrnitveImportService zavrnitveImportService;

    @PostMapping
    public ResponseEntity<String> uvoziZavrnitve(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("Datoteka je prazna.");
            }

            var resource = new ByteArrayResource(file.getBytes());
            zavrnitveImportService.obdelajZavrnitve(resource);

            return ResponseEntity.ok("Uvoz zavrnitev zaključen.");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Napaka pri branju datoteke: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Napaka pri obdelavi: " + e.getMessage());
        }
    }
}