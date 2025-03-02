package si.aris.randomizer2.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import si.aris.randomizer2.repository.PrijavaRepository;
import si.aris.randomizer2.repository.RecenzentRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardService {

    @Autowired
    private PrijavaRepository prijavaRepository;

    @Autowired
    private RecenzentRepository recenzentRepository;

    public Map<String, Integer> getDashboardStats() {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("activeReviewers", getActiveReviewersCount());
        stats.put("openApplications", getOpenApplicationsCount());
        stats.put("assignedApplications", getAssignedApplicationsCount());
        return stats;
    }

    public int getActiveReviewersCount() {
        return recenzentRepository.countByProstaMestaGreaterThan(0);
    }

    public int getOpenApplicationsCount() {
        return prijavaRepository.countByStatusPrijavNaziv("NEOPREDELJEN");
    }

    public int getAssignedApplicationsCount() {
        return prijavaRepository.countByStatusPrijavNaziv("DODELJENA");
    }

    public Map<String, Integer> getApplicationsByStatus() {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("NEOPREDELJEN", prijavaRepository.countByStatusPrijavNaziv("NEOPREDELJEN"));
        stats.put("DODELJENA", prijavaRepository.countByStatusPrijavNaziv("DODELJENA"));
        stats.put("ZAKLJUČENA", prijavaRepository.countByStatusPrijavNaziv("ZAKLJUČENA"));
        return stats;
    }

    public Map<String, Integer> getReviewersByCountry() {
        Map<String, Integer> countryCounts = new HashMap<>();
        List<Object[]> results = recenzentRepository.countByCountry();

        for (Object[] result : results) {
            String country = (String) result[0];
            Long count = (Long) result[1];
            countryCounts.put(country, count.intValue());
        }

        return countryCounts;
    }

    public Map<String, Integer> getReviewersCapacity() {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("Prosta mesta", recenzentRepository.countByProstaMestaGreaterThan(0));
        stats.put("Zasedena mesta", recenzentRepository.countByProstaMesta(0));
        return stats;
    }
}
