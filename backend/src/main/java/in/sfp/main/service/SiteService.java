package in.sfp.main.service;

import in.sfp.main.model.ConstructionSite;
import in.sfp.main.repo.ConstructionSiteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class SiteService {

    @Autowired
    private ConstructionSiteRepository repository;

    public List<ConstructionSite> getAllSites() {
        return repository.findAll();
    }

    public ConstructionSite createSite(ConstructionSite site) {
        if (site.getSiteCode() == null) {
            site.setSiteCode("SITE-" + System.currentTimeMillis() % 10000);
        }
        return repository.save(site);
    }

    public Optional<ConstructionSite> findById(Long id) {
        return repository.findById(id);
    }
    
    public ConstructionSite updateProgress(Long siteId, Integer percentage) {
        ConstructionSite site = repository.findById(siteId).orElseThrow(() -> new RuntimeException("Site not found"));
        site.setTargetCompletionPercentage(percentage);
        return repository.save(site);
    }
}
