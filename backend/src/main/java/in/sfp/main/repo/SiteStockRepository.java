package in.sfp.main.repo;

import in.sfp.main.model.SiteStock;
import in.sfp.main.model.ConstructionSite;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SiteStockRepository extends JpaRepository<SiteStock, Long> {
    List<SiteStock> findBySite(ConstructionSite site);
}
