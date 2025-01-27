package si.aris.randomizer2.controller;

import si.aris.randomizer2.model.Prijava;
import si.aris.randomizer2.repository.PrijavaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/prijave")
public class PrijavaController {

    @Autowired
    private PrijavaRepository repository;

    @GetMapping
    public List<Prijava> getAll() {
        return repository.findAll();
    }

    @PostMapping
    public Prijava create(@RequestBody Prijava prijava) {
        return repository.save(prijava);
    }

    @PutMapping("/{id}")
    public Prijava update(@PathVariable int id, @RequestBody Prijava updatedPrijava) {
        return repository.findById(id).map(prijava -> {
            prijava.setNaslov(updatedPrijava.getNaslov());
            prijava.setStevilkaPrijave(updatedPrijava.getStevilkaPrijave());
            prijava.setVrstaProjekta(updatedPrijava.getVrstaProjekta());
            prijava.setSteviloRecenzentov(updatedPrijava.getSteviloRecenzentov());
            return repository.save(prijava);
        }).orElseThrow(() -> new RuntimeException("Prijava s tem ID ne obstaja"));
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable int id) {
        repository.deleteById(id);
    }
}
