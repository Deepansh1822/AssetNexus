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

    @GetMapping("/{id:[0-9]+}")
    public WorkOrder getById(@PathVariable Long id) {
        return service.getWorkOrderById(id);
    }

    @GetMapping("/{id:[0-9]+}/consumption")
    public List<in.sfp.main.model.WorkOrderConsumption> getConsumption(@PathVariable Long id) {
        return service.getConsumptionByWorkOrder(id);
    }

    @GetMapping("/{id:[0-9]+}/allocations")
    public List<in.sfp.main.model.WorkOrderAllocation> getAllocations(@PathVariable Long id) {
        return service.getAllocationsByWorkOrder(id);
    }

    @PostMapping("/{id:[0-9]+}/allocate")
    public void allocate(@PathVariable Long id, @RequestBody java.util.Map<Long, Double> allocations) {
        service.allocateMaterials(id, allocations);
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
