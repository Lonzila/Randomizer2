package si.aris.randomizer2.controller;

import si.aris.randomizer2.model.OdlocitveZep;
import si.aris.randomizer2.repository.OdlocitveZepRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/odlocitve-zep")
public class OdlocitveZepController {

    @Autowired
    private OdlocitveZepRepository repository;

    @GetMapping
    public List<OdlocitveZep> getAll() {
        return repository.findAll();
    }

    @PostMapping
    public OdlocitveZep create(@RequestBody OdlocitveZep odlocitveZep) {
        return repository.save(odlocitveZep);
    }

    @PutMapping("/{id}")
    public OdlocitveZep update(@PathVariable int id, @RequestBody OdlocitveZep updatedZep) {
        return repository.findById(id).map(zep -> {
            zep.setPrijavaId(updatedZep.getPrijavaId());
            zep.setRecenzentId(updatedZep.getRecenzentId());
            zep.setOdlocitev(updatedZep.getOdlocitev());
            zep.setKomentar(updatedZep.getKomentar());
            return repository.save(zep);
        }).orElseThrow(() -> new RuntimeException("OdlocitveZep s tem ID ne obstaja"));
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable int id) {
        repository.deleteById(id);
    }
}
