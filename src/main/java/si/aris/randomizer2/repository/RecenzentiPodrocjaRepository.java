package si.aris.randomizer2.repository;

import si.aris.randomizer2.model.RecenzentiPodrocja;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecenzentiPodrocjaRepository extends JpaRepository<RecenzentiPodrocja, Integer> {
}
