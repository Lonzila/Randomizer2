package si.aris.randomizer2.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import si.aris.randomizer2.model.ErcPodrocje;

import java.util.Optional;

public interface ErcPodrocjeRepository extends JpaRepository<ErcPodrocje, Long> {
    Optional<ErcPodrocje> findByKoda(String trim);
}

