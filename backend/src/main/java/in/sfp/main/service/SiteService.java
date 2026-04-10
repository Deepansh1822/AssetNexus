package in.sfp.main.service;

import in.sfp.main.model.ConstructionSite;
import in.sfp.main.model.Employee;
import in.sfp.main.model.SiteManagerHistory;
import in.sfp.main.repo.ConstructionSiteRepository;
import in.sfp.main.model.Labourer;
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

    @Autowired
    private in.sfp.main.repo.LabourerRepository labourerRepo;

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
    public ConstructionSite replaceManager(Long siteId, Long newManagerId, String type) {
        ConstructionSite site = repository.findById(siteId)
                .orElseThrow(() -> new RuntimeException("Construction Site not found"));
        
        Employee oldEmp = site.getSiteManager();
        Labourer oldLab = site.getLabourerManager();

        if ("LABOURER".equalsIgnoreCase(type)) {
            Labourer newLab = labourerRepo.findById(newManagerId)
                    .orElseThrow(() -> new RuntimeException("Selected Labourer Manager not found"));
            site.setLabourerManager(newLab);
            site.setSiteManager(null); // Clear employee manager if handover to labourer
        } else {
            Employee newEmp = employeeRepo.findById(newManagerId)
                    .orElseThrow(() -> new RuntimeException("Selected Employee Manager not found"));
            site.setSiteManager(newEmp);
            site.setLabourerManager(null); // Clear labourer manager if handover to employee
            
            // For history (which currently only supports Employees), we record if it's an employee handover
            SiteManagerHistory record = new SiteManagerHistory(site, oldEmp, newEmp);
            historyRepository.save(record);
        }

        return repository.save(site);
    }

    public List<SiteManagerHistory> getSiteManagerHistory(Long siteId) {
        return historyRepository.findBySiteId(siteId);
    }
}
