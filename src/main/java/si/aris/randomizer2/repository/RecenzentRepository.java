package si.aris.randomizer2.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import si.aris.randomizer2.model.Recenzent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecenzentRepository extends JpaRepository<Recenzent, Integer> {
    List<Recenzent> findByProstaMestaGreaterThan(int prostaMesta);

    @Query("SELECT r FROM Recenzent r " +
            "JOIN RecenzentiPodrocja rp ON r.recenzentId = rp.recenzentId " +
            "WHERE rp.podpodrocjeId = :podpodrocjeId AND r.prostaMesta > 0")
    List<Recenzent> findEligibleReviewers(@Param("podpodrocjeId") int podpodrocjeId);
}
