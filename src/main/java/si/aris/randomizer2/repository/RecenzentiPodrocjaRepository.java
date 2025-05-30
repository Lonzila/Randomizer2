package si.aris.randomizer2.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import si.aris.randomizer2.model.RecenzentiPodrocja;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecenzentiPodrocjaRepository extends JpaRepository<RecenzentiPodrocja, Integer> {
    @Modifying(clearAutomatically = true)
    @Query("UPDATE RecenzentiPodrocja r SET r.recenzentId = :novaSifra WHERE r.recenzentId = :staraSifra")
    int updateRecenzentId(@Param("staraSifra") int staraSifra, @Param("novaSifra") int novaSifra);
}
