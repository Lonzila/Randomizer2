package si.aris.randomizer2.controller;

import si.aris.randomizer2.model.IzloceniOsebni;
import si.aris.randomizer2.repository.IzloceniOsebniRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/izloceni-osebni")
public class IzloceniOsebniController {

    @Autowired
    private IzloceniOsebniRepository repository;

    @GetMapping
    public List<IzloceniOsebni> getAll() {
        return repository.findAll();
    }

    @PostMapping
    public IzloceniOsebni create(@RequestBody IzloceniOsebni izloceniOsebni) {
        return repository.save(izloceniOsebni);
    }

    @PutMapping("/{id}")
    public IzloceniOsebni update(@PathVariable int id, @RequestBody IzloceniOsebni updatedOsebni) {
        return repository.findById(id).map(osebni -> {
            osebni.setPrijavaId(updatedOsebni.getPrijavaId());
            osebni.setRecenzentId(updatedOsebni.getRecenzentId());
            return repository.save(osebni);
        }).orElseThrow(() -> new RuntimeException("IzloceniOsebni s tem ID ne obstaja"));
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable int id) {
        repository.deleteById(id);
    }
}
