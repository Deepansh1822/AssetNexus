package in.sfp.main.repo;

import in.sfp.main.model.Labourer;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface LabourerRepository extends JpaRepository<Labourer, Long> {
    Optional<Labourer> findByPersonnelId(String personnelId);
    List<Labourer> findByStatus(String status);
    List<Labourer> findByCurrentSite(String currentSite);
    Optional<Labourer> findByEmail(String email);
    List<Labourer> findByUserRole(String userRole);
}
