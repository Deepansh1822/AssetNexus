package in.sfp.main.service;

import in.sfp.main.model.ConstructionSite;
import in.sfp.main.model.Employee;
import in.sfp.main.model.SiteManagerHistory;
import in.sfp.main.repo.ConstructionSiteRepository;
import in.sfp.main.repo.EmployeeRepo;
import in.sfp.main.repo.SiteManagerHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class SiteService {

    @Autowired
    private ConstructionSiteRepository repository;

    @Autowired
    private SiteManagerHistoryRepository historyRepository;

    @Autowired
    private EmployeeRepo employeeRepo;

    public List<ConstructionSite> getAllSites() {
        return repository.findAll();
    }

    public ConstructionSite createSite(ConstructionSite site) {
        if (site.getSiteCode() == null) {
            site.setSiteCode("SITE-" + System.currentTimeMillis() % 10000);
        }
        return repository.save(site);
    }

    public ConstructionSite saveSite(ConstructionSite site) {
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

    @Transactional
    public ConstructionSite replaceManager(Long siteId, Long newManagerId) {
        ConstructionSite site = repository.findById(siteId)
                .orElseThrow(() -> new RuntimeException("Construction Site not found"));
        Employee newManager = employeeRepo.findById(newManagerId)
                .orElseThrow(() -> new RuntimeException("Selected Manager not found"));

        Employee oldManager = site.getSiteManager();

        // Update Site
        site.setSiteManager(newManager);
        ConstructionSite saved = repository.save(site);

        // Record History
        SiteManagerHistory record = new SiteManagerHistory(saved, oldManager, newManager);
        historyRepository.save(record);

        return saved;
    }

    public List<SiteManagerHistory> getSiteManagerHistory(Long siteId) {
        return historyRepository.findBySiteId(siteId);
    }
}
