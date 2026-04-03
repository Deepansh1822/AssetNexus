package in.sfp.main.repo;

import in.sfp.main.model.SiteManagerHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SiteManagerHistoryRepository extends JpaRepository<SiteManagerHistory, Long> {
    List<SiteManagerHistory> findBySiteId(Long siteId);
}
