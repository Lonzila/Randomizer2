package si.aris.randomizer2.repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import si.aris.randomizer2.model.IzloceniCOI;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public interface IzloceniCOIRepository extends JpaRepository<IzloceniCOI, Integer> {
    //List<IzloceniCOI> findByPrijavaId(int prijavaID);

    @Query("SELECT i FROM IzloceniCOI i WHERE i.prijavaId IN :prijavaIds")
    List<IzloceniCOI> findByPrijavaId(@Param("prijavaIds") List<Integer> prijavaIds);
}