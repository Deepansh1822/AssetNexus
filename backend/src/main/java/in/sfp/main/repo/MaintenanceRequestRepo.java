package in.sfp.main.repo;

import in.sfp.main.model.MaintenanceRequest;
import in.sfp.main.model.MaintenanceRequest.MaintenanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MaintenanceRequestRepo extends JpaRepository<MaintenanceRequest, Long> {
    
    List<MaintenanceRequest> findByStatus(MaintenanceStatus status);
    
    List<MaintenanceRequest> findByRequestedById(Long employeeId);
    
    List<MaintenanceRequest> findByAssetId(Long assetId);
    
    List<MaintenanceRequest> findByRequestedByAdminFalse();
    
    List<MaintenanceRequest> findAllByOrderByRequestedDateDesc();

    boolean existsByAssetIdAndStatusIn(Long assetId, List<MaintenanceStatus> statuses);
}
