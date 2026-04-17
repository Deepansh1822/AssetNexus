package in.sfp.main.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import in.sfp.main.model.Asset;
import in.sfp.main.enums.Status;
import java.util.List;

@Repository
public interface AssetsRepo extends JpaRepository<Asset, Long> {
    long countByCategoryIdAndStatusIn(Long categoryId, List<Status> statuses);
    boolean existsByAssetTag(String assetTag);

    @org.springframework.data.jpa.repository.Query("SELECT DISTINCT b.name FROM Branch b")
    List<String> findDistinctLocations();

    @org.springframework.data.jpa.repository.Query("SELECT DISTINCT a.location FROM Asset a WHERE a.location IS NOT NULL AND a.location <> ''")
    List<String> findDistinctLocationsFromRaw();
}
