package si.aris.randomizer2.controller;

import si.aris.randomizer2.model.Podpodrocje;
import si.aris.randomizer2.repository.PodpodrocjeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/podpodrocje")
public class PodpodrocjeController {

    @Autowired
    private PodpodrocjeRepository repository;

    @GetMapping
    public List<Podpodrocje> getAll() {
        return repository.findAll();
    }

    @PostMapping
    public Podpodrocje create(@RequestBody Podpodrocje podpodrocje) {
        return repository.save(podpodrocje);
    }

    @PutMapping("/{id}")
    public Podpodrocje update(@PathVariable int id, @RequestBody Podpodrocje updatedPodpodrocje) {
        return repository.findById(id).map(podpodrocje -> {
            podpodrocje.setNaziv(updatedPodpodrocje.getNaziv());
            podpodrocje.setKoda(updatedPodpodrocje.getKoda());
            podpodrocje.setPodrocjeId(updatedPodpodrocje.getPodrocjeId());
            return repository.save(podpodrocje);
        }).orElseThrow(() -> new RuntimeException("Podpodrocje s tem ID ne obstaja"));
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable int id) {
        repository.deleteById(id);
    }
}
