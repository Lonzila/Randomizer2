package si.aris.randomizer2.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import si.aris.randomizer2.model.IzloceniCOI;
import si.aris.randomizer2.model.IzloceniOsebni;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public interface IzloceniOsebniRepository extends JpaRepository<IzloceniOsebni, Integer> {
    //List<IzloceniOsebni> findByPrijavaId(int prijavaID);

    @Query("SELECT i FROM IzloceniOsebni i WHERE i.prijavaId IN :prijavaIds")
    List<IzloceniOsebni> findByPrijavaId(@Param("prijavaIds") List<Integer> prijavaIds);
}
