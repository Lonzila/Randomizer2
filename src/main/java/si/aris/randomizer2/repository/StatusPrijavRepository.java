package si.aris.randomizer2.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import si.aris.randomizer2.model.StatusPrijav;

import java.util.List;
import java.util.Optional;

@Repository
public interface StatusPrijavRepository extends JpaRepository<StatusPrijav, Integer> {
    @Query("SELECT s FROM StatusPrijav s WHERE s.naziv = :naziv")
    Optional<StatusPrijav> findByNaziv(@Param("naziv") String naziv);

    List<StatusPrijav> findByNazivIn(List<String> nazivi);
}
