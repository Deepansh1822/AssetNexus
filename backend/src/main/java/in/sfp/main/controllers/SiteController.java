package in.sfp.main.controllers;

import in.sfp.main.model.ConstructionSite;
import in.sfp.main.service.SiteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/sites")
public class SiteController {

    @Autowired
    private SiteService service;

    @GetMapping("/all")
    public List<ConstructionSite> getAll() {
        return service.getAllSites();
    }

    @GetMapping("/{id}")
    public ConstructionSite getById(@PathVariable Long id) {
        return service.getAllSites().stream()
                .filter(s -> s.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Site not found"));
    }

    @PostMapping("/create")
    public ConstructionSite create(@RequestBody ConstructionSite site) {
        return service.createSite(site);
    }

    @PostMapping("/{id}/update-progress")
    public ConstructionSite updateProgress(@PathVariable Long id, @RequestParam Integer percentage) {
        return service.updateProgress(id, percentage);
    }
}
