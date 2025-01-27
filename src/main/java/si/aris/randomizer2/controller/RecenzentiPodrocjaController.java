package si.aris.randomizer2.controller;

import si.aris.randomizer2.model.RecenzentiPodrocja;
import si.aris.randomizer2.repository.RecenzentiPodrocjaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recenzenti-podrocja")
public class RecenzentiPodrocjaController {

    @Autowired
    private RecenzentiPodrocjaRepository repository;

    @GetMapping
    public List<RecenzentiPodrocja> getAll() {
        return repository.findAll();
    }

    @PostMapping
    public RecenzentiPodrocja create(@RequestBody RecenzentiPodrocja recenzentiPodrocja) {
        return repository.save(recenzentiPodrocja);
    }

    @PutMapping("/{id}")
    public RecenzentiPodrocja update(@PathVariable int id, @RequestBody RecenzentiPodrocja updatedPodrocja) {
        return repository.findById(id).map(podrocja -> {
            podrocja.setRecenzentId(updatedPodrocja.getRecenzentId());
            podrocja.setPodpodrocjeId(updatedPodrocja.getPodpodrocjeId());
            return repository.save(podrocja);
        }).orElseThrow(() -> new RuntimeException("RecenzentiPodrocja s tem ID ne obstaja"));
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable int id) {
        repository.deleteById(id);
    }
}
