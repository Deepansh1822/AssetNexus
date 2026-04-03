package in.sfp.main.repo;

import in.sfp.main.model.DeploymentHistory;
import in.sfp.main.model.Labourer;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface DeploymentHistoryRepository extends JpaRepository<DeploymentHistory, Long> {
    List<DeploymentHistory> findByLabourer(Labourer labourer);
    // Find active deployment (where end date is null)
    Optional<DeploymentHistory> findByLabourerAndEndDateIsNull(Labourer labourer);
}
