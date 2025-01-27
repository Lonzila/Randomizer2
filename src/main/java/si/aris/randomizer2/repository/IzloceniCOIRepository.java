package si.aris.randomizer2.repository;
import si.aris.randomizer2.model.IzloceniCOI;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IzloceniCOIRepository extends JpaRepository<IzloceniCOI, Integer> {
    List<IzloceniCOI> findByPrijavaId(int prijavaID);
}