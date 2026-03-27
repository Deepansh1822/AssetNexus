package in.sfp.main.repo;

import in.sfp.main.model.ConstructionSite;
import in.sfp.main.model.WorkOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface WorkOrderRepository extends JpaRepository<WorkOrder, Long> {
    List<WorkOrder> findBySite(ConstructionSite site);
    List<WorkOrder> findByStatus(String status);
}
