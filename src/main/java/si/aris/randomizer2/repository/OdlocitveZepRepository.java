package si.aris.randomizer2.repository;

import si.aris.randomizer2.model.OdlocitveZep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OdlocitveZepRepository extends JpaRepository<OdlocitveZep, Integer> {
}
