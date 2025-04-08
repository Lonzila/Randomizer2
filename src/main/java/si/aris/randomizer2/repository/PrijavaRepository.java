package si.aris.randomizer2.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import si.aris.randomizer2.model.Prijava;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import si.aris.randomizer2.model.StatusPrijav;

import java.util.List;
import java.util.Optional;

@Repository
public interface PrijavaRepository extends JpaRepository<Prijava, Integer> {

    // Preštej prijave z določenim statusom
    @Query("SELECT COUNT(p) FROM Prijava p WHERE p.statusPrijav.naziv = :naziv")
    int countByStatusPrijavNaziv(@Param("naziv") String naziv);

    // Preverimo ali statusPrijav vsebuje entiteto StatusPrijav
    List<Prijava> findByStatusPrijavIn(List<StatusPrijav> statusPrijavi);

    // Ali, če želite še vedno delati z ID-ji, uporabite to:
    List<Prijava> findByStatusPrijavIdIn(List<Integer> statusPrijavIds);

    Optional<Prijava> findByStevilkaPrijave(int stevilkaPrijave);

    @Modifying
    @Transactional
    @Query("UPDATE Prijava p SET p.statusPrijav.id = :statusId")
    void updateStatusPrijavTo(@Param("statusId") int statusId);

}