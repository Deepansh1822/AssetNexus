package in.sfp.main.controllers;

import in.sfp.main.model.WorkOrder;
import in.sfp.main.service.WorkOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/work-orders")
public class WorkOrderController {

    @Autowired
    private WorkOrderService service;

    @GetMapping("/all")
    public List<WorkOrder> getAll() {
        return service.getAllWorkOrders();
    }

    @GetMapping("/by-site/{siteId:[0-9]+}")
    public List<WorkOrder> getBySite(@PathVariable Long siteId) {
        return service.getWorkOrdersBySite(siteId);
    }

    @PostMapping("/create")
    public WorkOrder create(@RequestBody WorkOrder workOrder) {
        return service.createWorkOrder(workOrder);
    }

    @PostMapping("/{id:[0-9]+}/update-progress")
    public WorkOrder updateProgress(@PathVariable Long id, @RequestParam Integer progress) {
        return service.updateProgress(id, progress);
    }

    @PostMapping("/{id:[0-9]+}/update-with-materials")
    public WorkOrder updateProgressWithMaterials(@PathVariable Long id, 
                                                 @RequestParam Integer progress,
                                                 @RequestBody(required = false) java.util.Map<Long, Double> materialUsage) {
        return service.updateProgressWithMaterials(id, progress, materialUsage);
    }
}
