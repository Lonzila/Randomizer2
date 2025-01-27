package si.aris.randomizer2.repository;

import si.aris.randomizer2.model.IzloceniCOI;
import si.aris.randomizer2.model.IzloceniOsebni;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IzloceniOsebniRepository extends JpaRepository<IzloceniOsebni, Integer> {
    List<IzloceniOsebni> findByPrijavaId(int prijavaID);
}
