package in.sfp.main.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import in.sfp.main.model.AssetTracking;
import java.util.List;

@Repository
public interface AssetTrackingRepo extends JpaRepository<AssetTracking, Long> {
    List<AssetTracking> findByAssetId(Long assetId);
    List<AssetTracking> findByEmployeeId(Long employeeId);
}
