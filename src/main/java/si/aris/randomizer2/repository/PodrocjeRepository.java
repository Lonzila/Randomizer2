package si.aris.randomizer2.repository;

import si.aris.randomizer2.model.Podrocje;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PodrocjeRepository extends JpaRepository<Podrocje, Integer> {
}
