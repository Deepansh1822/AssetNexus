package in.sfp.main.repo;

import in.sfp.main.model.WorkOrderConsumption;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface WorkOrderConsumptionRepository extends JpaRepository<WorkOrderConsumption, Long> {
    List<WorkOrderConsumption> findByWorkOrderId(Long workOrderId);
    List<WorkOrderConsumption> findByStockItemId(Long stockItemId);
}
