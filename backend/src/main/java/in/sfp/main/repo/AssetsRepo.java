package in.sfp.main.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import in.sfp.main.model.Asset;
import in.sfp.main.enums.Status;
import java.util.List;

@Repository
public interface AssetsRepo extends JpaRepository<Asset, Long> {
    long countByCategoryIdAndStatusIn(Long categoryId, List<Status> statuses);
}
