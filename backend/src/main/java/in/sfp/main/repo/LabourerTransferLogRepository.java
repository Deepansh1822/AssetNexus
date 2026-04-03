package in.sfp.main.repo;

import in.sfp.main.model.LabourerTransferLog;
import in.sfp.main.model.Labourer;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LabourerTransferLogRepository extends JpaRepository<LabourerTransferLog, Long> {
    List<LabourerTransferLog> findByLabourerId(Long labourerId);
    List<LabourerTransferLog> findAllByOrderByTransferTimeDesc();
}
