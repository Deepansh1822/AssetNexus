package in.sfp.main.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import in.sfp.main.model.Employee;

import java.util.Optional;

@Repository
public interface EmployeeRepo extends JpaRepository<Employee, Long> {
    Optional<Employee> findByEmailAndActiveTrue(String email);
    Optional<Employee> findByEmail(String email); // Still need for legacy or checking exists
    Optional<Employee> findBySystemId(String systemId);
    java.util.List<Employee> findByNameContainingIgnoreCaseOrSystemIdContainingIgnoreCase(String name, String systemId);

    @org.springframework.data.jpa.repository.Query("SELECT DISTINCT e.branchName FROM Employee e WHERE e.branchName IS NOT NULL AND e.branchName != '' AND TRIM(e.branchName) != ''")
    java.util.List<String> findDistinctBranchNames();

    java.util.List<Employee> findByUserRoleAndActiveTrue(String userRole);
    java.util.List<Employee> findByActiveTrue();
}
