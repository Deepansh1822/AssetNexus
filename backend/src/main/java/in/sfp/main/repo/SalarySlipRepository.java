package in.sfp.main.repo;

import in.sfp.main.model.SalarySlip;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SalarySlipRepository extends JpaRepository<SalarySlip, Long> {
    List<SalarySlip> findByLabourerId(Long labourerId);
    List<SalarySlip> findBySiteName(String siteName);
    List<SalarySlip> findAllByOrderByGeneratedAtDesc();
    
    // Duplicate prevention finder
    java.util.Optional<SalarySlip> findByLabourerIdAndStartDateAndEndDate(Long labourerId, java.time.LocalDate startDate, java.time.LocalDate endDate);
}
