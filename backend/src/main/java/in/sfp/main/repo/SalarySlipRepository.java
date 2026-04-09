package in.sfp.main.repo;

import in.sfp.main.model.SalarySlip;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SalarySlipRepository extends JpaRepository<SalarySlip, Long> {
    List<SalarySlip> findByLabourerId(Long labourerId);
    List<SalarySlip> findBySiteName(String siteName);
    List<SalarySlip> findAllByOrderByGeneratedAtDesc();
    
    // Overlap prevention finder
    @org.springframework.data.jpa.repository.Query("SELECT s FROM SalarySlip s WHERE s.labourer.id = :labourerId AND s.slipCategory IN :categories AND s.startDate <= :endDate AND s.endDate >= :startDate")
    List<SalarySlip> findOverlappingSlips(Long labourerId, java.time.LocalDate startDate, java.time.LocalDate endDate, List<String> categories);

    // Duplicate prevention finder (scoped by category - keeps exact check for non-overlapping types like Advance)
    java.util.Optional<SalarySlip> findByLabourerIdAndStartDateAndEndDateAndSlipCategory(Long labourerId, java.time.LocalDate startDate, java.time.LocalDate endDate, String slipCategory);

    // Find all slips for deduction calculation
    List<SalarySlip> findByLabourerIdAndSlipCategoryAndStartDateBetween(Long labourerId, String category, java.time.LocalDate start, java.time.LocalDate end);
}
