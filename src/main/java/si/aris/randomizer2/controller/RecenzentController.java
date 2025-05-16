package si.aris.randomizer2.controller;

import si.aris.randomizer2.model.Recenzent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import si.aris.randomizer2.service.RecenzentImportService;

import java.util.List;

@RestController
@RequestMapping("/api/recenzenti")
public class RecenzentController {

    @Autowired
    private RecenzentImportService service;

    @GetMapping
    public List<Recenzent> getAll() {
        return service.getAllRecenzenti();
    }

    @PostMapping
    public Recenzent create(@RequestBody Recenzent recenzent) {
        return service.createRecenzent(recenzent);
    }

    @PutMapping("/{id}")
    public Recenzent update(@PathVariable int id, @RequestBody Recenzent updatedRecenzent) {
        return service.updateRecenzent(id, updatedRecenzent)
                .orElseThrow(() -> new RuntimeException("Recenzent s tem ID ne obstaja"));
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable int id) {
        service.deleteRecenzent(id);
    }
}
