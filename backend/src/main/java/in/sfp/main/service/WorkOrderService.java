package in.sfp.main.service;

import in.sfp.main.model.WorkOrder;
import in.sfp.main.model.ConstructionSite;
import in.sfp.main.repo.WorkOrderRepository;
import in.sfp.main.repo.ConstructionSiteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class WorkOrderService {

    @Autowired
    private WorkOrderRepository repository;

    @Autowired
    private ConstructionSiteRepository siteRepository;

    public List<WorkOrder> getAllWorkOrders() {
        return repository.findAll();
    }

    public List<WorkOrder> getWorkOrdersBySite(Long siteId) {
        ConstructionSite site = siteRepository.findById(siteId)
                .orElseThrow(() -> new RuntimeException("Site not found"));
        return repository.findBySite(site);
    }

    public WorkOrder createWorkOrder(WorkOrder workOrder) {
        return repository.save(workOrder);
    }

    public WorkOrder updateProgress(Long id, Integer progress) {
        WorkOrder workOrder = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Work Order not found"));
        
        workOrder.setCurrentProgress(progress);
        if (progress >= 100) {
            workOrder.setStatus("COMPLETED");
        } else if (progress > 0) {
            workOrder.setStatus("IN_PROGRESS");
        }
        
        return repository.save(workOrder);
    }
}
