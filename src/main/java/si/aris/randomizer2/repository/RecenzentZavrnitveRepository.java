package si.aris.randomizer2.repository;

import si.aris.randomizer2.model.RecenzentiZavrnitve;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecenzentZavrnitveRepository extends JpaRepository<RecenzentiZavrnitve, Integer> {
}
