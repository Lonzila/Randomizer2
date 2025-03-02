package si.aris.randomizer2.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import si.aris.randomizer2.model.Recenzent;
import si.aris.randomizer2.repository.RecenzentRepository;

import java.util.List;
import java.util.Optional;

@Service
public class RecenzentService {

    @Autowired
    private RecenzentRepository repository;

    // Pridobi vse recenzente
    public List<Recenzent> getAllRecenzenti() {
        return repository.findAll();
    }

    // Ustvari novega recenzenta
    public Recenzent createRecenzent(Recenzent recenzent) {
        return repository.save(recenzent);
    }

    // Posodobi recenzenta
    public Optional<Recenzent> updateRecenzent(int id, Recenzent updatedRecenzent) {
        return repository.findById(id).map(recenzent -> {
            recenzent.setIme(updatedRecenzent.getIme());
            recenzent.setPriimek(updatedRecenzent.getPriimek());
            recenzent.setDrzava(updatedRecenzent.getDrzava());
            recenzent.setPrijavePredizbor(updatedRecenzent.getPrijavePredizbor());
            recenzent.setProstaMesta(updatedRecenzent.getProstaMesta());
            return repository.save(recenzent);
        });
    }

    // Izbriši recenzenta
    public void deleteRecenzent(int id) {
        repository.deleteById(id);
    }

    // Poišči vse recenzente z razpoložljivimi mesti
    public List<Recenzent> findAvailableRecenzenti() {
        return repository.findByProstaMestaGreaterThan(0);
    }
}
