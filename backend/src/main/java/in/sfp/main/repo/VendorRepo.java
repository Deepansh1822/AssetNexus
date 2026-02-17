package in.sfp.main.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import in.sfp.main.model.Vendor;
import java.util.Optional;

@Repository
public interface VendorRepo extends JpaRepository<Vendor, Long> {
    Optional<Vendor> findByVendorName(String vendorName);
}
