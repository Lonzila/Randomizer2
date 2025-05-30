package si.aris.randomizer2.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import si.aris.randomizer2.model.RecenzentiErc;

public interface RecenzentiErcRepository extends JpaRepository<RecenzentiErc, Long> {

    @Modifying(clearAutomatically = true)
    @Query("UPDATE RecenzentiErc r SET r.recenzent.recenzentId = :nova WHERE r.recenzent.recenzentId = :stara")
    int updateRecenzentId(@Param("stara") int staraId, @Param("nova") int novaId);
}