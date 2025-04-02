package si.aris.randomizer2.controller;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import si.aris.randomizer2.model.Predizbor;
import si.aris.randomizer2.repository.PredizborRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/predizbor")
public class PredizborController {

    @Autowired
    private PredizborRepository repository;

    @GetMapping
    public List<Predizbor> getAll() {
        return repository.findAll();
    }

    @PostMapping
    public Predizbor create(@RequestBody Predizbor predizbor) {
        return repository.save(predizbor);
    }

    @PutMapping("/{id}")
    public Predizbor update(@PathVariable int id, @RequestBody Predizbor updatedPredizbor) {
        return repository.findById(id).map(predizbor -> {
            predizbor.setPrijavaId(updatedPredizbor.getPrijavaId());
            predizbor.setRecenzentId(updatedPredizbor.getRecenzentId());
            predizbor.setStatus(updatedPredizbor.getStatus());
            return repository.save(predizbor);
        }).orElseThrow(() -> new RuntimeException("Predizbor s tem ID ne obstaja"));
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable int id) {
        repository.deleteById(id);
    }
}
