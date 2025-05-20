package si.aris.randomizer2.controller;

import si.aris.randomizer2.model.Prijava;
import si.aris.randomizer2.service.PrijavaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/prijave")
public class PrijavaController {

    @Autowired
    private PrijavaService prijavaService;

    // Pridobi vse prijave

    @GetMapping
    public List<Prijava> getAll() {
        return prijavaService.getAllApplications();
    }

}
