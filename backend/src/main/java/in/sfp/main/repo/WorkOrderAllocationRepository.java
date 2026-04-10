package in.sfp.main.repo;

import in.sfp.main.model.WorkOrderAllocation;
import in.sfp.main.model.WorkOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface WorkOrderAllocationRepository extends JpaRepository<WorkOrderAllocation, Long> {
    List<WorkOrderAllocation> findByWorkOrder(WorkOrder workOrder);
}
