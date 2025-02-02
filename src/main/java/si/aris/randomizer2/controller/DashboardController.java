package si.aris.randomizer2.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import si.aris.randomizer2.service.DashboardService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/stats")
    public Map<String, Integer> getDashboardStats() {
        return dashboardService.getDashboardStats();
    }

    @GetMapping("/stats/status-count")
    public Map<String, Integer> getApplicationsByStatus() {
        return dashboardService.getApplicationsByStatus();
    }

    @GetMapping("/stats/reviewers-by-country")
    public Map<String, Integer> getReviewersByCountry() {
        return dashboardService.getReviewersByCountry();
    }

    @GetMapping("/stats/reviewers-capacity")
    public Map<String, Integer> getReviewersCapacity() {
        return dashboardService.getReviewersCapacity();
    }
}

