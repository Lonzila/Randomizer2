package si.aris.randomizer2.controller;

import si.aris.randomizer2.model.IzloceniCOI;
import si.aris.randomizer2.repository.IzloceniCOIRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/izloceni-coi")
public class IzloceniCOIController {

    @Autowired
    private IzloceniCOIRepository repository;

    @GetMapping
    public List<IzloceniCOI> getAll() {
        return repository.findAll();
    }

    @PostMapping
    public IzloceniCOI create(@RequestBody IzloceniCOI izloceniCOI) {
        return repository.save(izloceniCOI);
    }

    @PutMapping("/{id}")
    public IzloceniCOI update(@PathVariable int id, @RequestBody IzloceniCOI updatedCOI) {
        return repository.findById(id).map(coi -> {
            coi.setPrijavaId(updatedCOI.getPrijavaId());
            coi.setRecenzentId(updatedCOI.getRecenzentId());
            coi.setRazlog(updatedCOI.getRazlog());
            return repository.save(coi);
        }).orElseThrow(() -> new RuntimeException("IzloceniCOI s tem ID ne obstaja"));
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable int id) {
        repository.deleteById(id);
    }
}
