package in.sfp.main.repo;

import in.sfp.main.model.MaintenanceLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MaintenanceLogRepo extends JpaRepository<MaintenanceLog, Long> {
    List<MaintenanceLog> findByAssetId(Long assetId);
}
