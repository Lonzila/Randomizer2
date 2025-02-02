package si.aris.randomizer2.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import si.aris.randomizer2.model.Prijava;
import si.aris.randomizer2.repository.PrijavaRepository;

import java.util.List;

@Service
public class PrijavaService {

    @Autowired
    private PrijavaRepository prijavaRepository;

    // Vrača vse prijave
    public List<Prijava> getAllApplications() {
        return prijavaRepository.findAll();
    }
}
