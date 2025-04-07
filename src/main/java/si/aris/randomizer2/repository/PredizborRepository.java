package si.aris.randomizer2.repository;

import org.springframework.data.jpa.repository.Query;
import si.aris.randomizer2.model.Predizbor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PredizborRepository extends JpaRepository<Predizbor, Integer> {

    @Query("SELECT p.prijavaId " +
            "FROM Predizbor p " +
            "WHERE p.status = 'OPREDELJEN Z NE' " +
            "GROUP BY p.prijavaId " +
            "HAVING SUM(CASE WHEN p.status IN ('DODELJENA V OCENJEVANJE', 'V OCENJEVANJU') THEN 1 ELSE 0 END) < 2")
    List<Integer> findPrijaveWithRejectedAndNotFullyAssigned();

    Optional<Predizbor> findByPrijavaIdAndRecenzentId(int prijavaId, int recenzentId);
}
