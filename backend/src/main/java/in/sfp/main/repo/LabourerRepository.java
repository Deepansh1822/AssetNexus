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

    @org.springframework.data.jpa.repository.Query("SELECT l FROM Labourer l WHERE " +
            "(LOWER(l.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(l.personnelId) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(l.trade) LIKE LOWER(CONCAT('%', :query, '%'))) AND " +
            "l.status IN ('AVAILABLE', 'ACTIVE')")
    List<Labourer> globalSearch(@org.springframework.data.repository.query.Param("query") String query);
}
