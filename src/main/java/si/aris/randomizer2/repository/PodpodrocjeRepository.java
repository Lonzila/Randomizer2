package si.aris.randomizer2.repository;

import si.aris.randomizer2.model.Podpodrocje;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PodpodrocjeRepository extends JpaRepository<Podpodrocje, Integer> {
}
