package si.aris.randomizer2.repository;

import si.aris.randomizer2.model.Prijava;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import java.util.List;

@Repository
public interface PrijavaRepository extends JpaRepository<Prijava, Integer> {

    List<Prijava> findByStatusOceneIn(List<String> statusi);




}