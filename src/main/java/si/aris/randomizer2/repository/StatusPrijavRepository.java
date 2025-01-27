package si.aris.randomizer2.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import si.aris.randomizer2.model.StatusPrijav;

import java.util.Optional;

@Repository
public interface StatusPrijavRepository extends JpaRepository<StatusPrijav, Integer> {
    Optional<StatusPrijav> findByNaziv(String naziv);
}
