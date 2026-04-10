package in.sfp.main.service;

import in.sfp.main.model.SiteStock;
import in.sfp.main.model.ConstructionSite;
import in.sfp.main.repo.SiteStockRepository;
import in.sfp.main.repo.ConstructionSiteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class SiteStockService {

    @Autowired
    private SiteStockRepository repository;

    @Autowired
    private ConstructionSiteRepository siteRepo;
    
    @Autowired
    private in.sfp.main.repo.EmployeeRepo employeeRepo;

    @Autowired
    private in.sfp.main.repo.LabourerRepository labourerRepository;
    
    private List<ConstructionSite> getAssignedSitesForCurrentUser() {
        String email = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        if ("anonymousUser".equals(email)) return List.of();
        
        java.util.Optional<in.sfp.main.model.Employee> empOpt = employeeRepo.findByEmail(email);
        if (empOpt.isPresent() && "SITE_MANAGER".equals(empOpt.get().getUserRole())) {
            return siteRepo.findBySiteManager(empOpt.get());
        }
        
        java.util.Optional<in.sfp.main.model.Labourer> labOpt = labourerRepository.findByEmail(email);
        if (labOpt.isPresent() && "SITE_MANAGER".equals(labOpt.get().getUserRole())) {
            return siteRepo.findByLabourerManager(labOpt.get());
        }
        return List.of();
    }

    public List<SiteStock> getStockBySite(Long siteId) {
        List<ConstructionSite> managedSites = getAssignedSitesForCurrentUser();
        ConstructionSite site = siteRepo.findById(siteId).orElseThrow();
        
        // If manager, check if this site is theirs
        if (!managedSites.isEmpty() && managedSites.stream().noneMatch(s -> s.getId().equals(siteId))) {
            return java.util.Collections.emptyList();
        }
        
        return repository.findBySite(site);
    }

    @Autowired
    private in.sfp.main.service.WorkOrderService workOrderService;

    @Autowired
    private in.sfp.main.repo.WorkOrderAllocationRepository allocationRepository;

    public SiteStock addStock(Long siteId, SiteStock stock) {
        ConstructionSite site = siteRepo.findById(siteId).orElseThrow();
        stock.setSite(site);
        stock.setLastRestockedAt(LocalDateTime.now());
        return repository.save(stock);
    }

    @org.springframework.transaction.annotation.Transactional
    public SiteStock addStockAndAllocate(Long siteId, SiteStock stock, Long workOrderId) {
        SiteStock savedStock = addStock(siteId, stock);
        
        if (workOrderId != null) {
            in.sfp.main.model.WorkOrder workOrder = workOrderService.getWorkOrderById(workOrderId);
            // Create allocation for the newly added stock
            allocationRepository.save(new in.sfp.main.model.WorkOrderAllocation(workOrder, savedStock, stock.getQuantity()));
        }
        
        return savedStock;
    }

    public List<SiteStock> getAllStocks() {
        List<ConstructionSite> managedSites = getAssignedSitesForCurrentUser();
        List<SiteStock> all = repository.findAll();
        if (managedSites.isEmpty()) return all;
        
        List<Long> siteIds = managedSites.stream().map(ConstructionSite::getId).toList();
        return all.stream().filter(s -> s.getSite() != null && siteIds.contains(s.getSite().getId())).toList();
    }

    public SiteStock adjustQuantity(Long stockId, Double delta) {
        SiteStock stock = repository.findById(stockId).orElseThrow();
        stock.setQuantity(stock.getQuantity() + delta);
        if (stock.getQuantity() < 0) stock.setQuantity(0.0);
        stock.setLastRestockedAt(LocalDateTime.now());
        return repository.save(stock);
    }
}
