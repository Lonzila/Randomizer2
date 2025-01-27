package si.aris.randomizer2.controller;

import si.aris.randomizer2.model.RecenzentiZavrnitve;
import si.aris.randomizer2.repository.RecenzentZavrnitveRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recenzenti-zavrnitve")
public class RecenzentZavrnitveController {

    @Autowired
    private RecenzentZavrnitveRepository repository;

    @GetMapping
    public List<RecenzentiZavrnitve> getAll() {
        return repository.findAll();
    }

    @PostMapping
    public RecenzentiZavrnitve create(@RequestBody RecenzentiZavrnitve zavrnitev) {
        return repository.save(zavrnitev);
    }

    @PutMapping("/{id}")
    public RecenzentiZavrnitve update(@PathVariable int id, @RequestBody RecenzentiZavrnitve updatedZavrnitev) {
        return repository.findById(id).map(zavrnitev -> {
            zavrnitev.setPrijavaId(updatedZavrnitev.getPrijavaId());
            zavrnitev.setRecenzentId(updatedZavrnitev.getRecenzentId());
            zavrnitev.setRazlog(updatedZavrnitev.getRazlog());
            return repository.save(zavrnitev);
        }).orElseThrow(() -> new RuntimeException("RecenzentZavrnitve s tem ID ne obstaja"));
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable int id) {
        repository.deleteById(id);
    }
}
