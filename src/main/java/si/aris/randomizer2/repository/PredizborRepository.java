package si.aris.randomizer2.repository;

import si.aris.randomizer2.model.Predizbor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PredizborRepository extends JpaRepository<Predizbor, Integer> {
}
