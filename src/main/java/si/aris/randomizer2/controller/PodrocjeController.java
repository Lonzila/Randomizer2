package si.aris.randomizer2.controller;

import si.aris.randomizer2.model.Podrocje;
import si.aris.randomizer2.repository.PodrocjeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/podrocje")
public class PodrocjeController {

    @Autowired
    private PodrocjeRepository repository;

    @GetMapping
    public List<Podrocje> getAll() {
        return repository.findAll();
    }

    @PostMapping
    public Podrocje create(@RequestBody Podrocje podrocje) {
        return repository.save(podrocje);
    }

    @PutMapping("/{id}")
    public Podrocje update(@PathVariable int id, @RequestBody Podrocje updatedPodrocje) {
        return repository.findById(id).map(podrocje -> {
            podrocje.setNaziv(updatedPodrocje.getNaziv());
            podrocje.setKoda(updatedPodrocje.getKoda());
            podrocje.setAngNaziv(updatedPodrocje.getAngNaziv());
            return repository.save(podrocje);
        }).orElseThrow(() -> new RuntimeException("Podrocje s tem ID ne obstaja"));
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable int id) {
        repository.deleteById(id);
    }
}
