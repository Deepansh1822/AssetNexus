package in.sfp.main.service;

import in.sfp.main.model.Employee;
import java.util.List;

public interface EmployeeService {
    List<Employee> getAllEmployees();

    Employee getEmployeeById(Long id);

    Employee saveEmployee(Employee employee);

    void deleteEmployee(Long id);
    
    void disposeEmployee(Long id);
    Employee toggleEmployeeStatus(Long id);
    
    void createPasswordResetToken(String email, String resetLinkBase);
    boolean resetPassword(String token, String newPassword);
    List<String> getDistinctBranchNames();
    List<Employee> findByUserRole(String role);
    void registerWithInvite(Employee employee, String resetLinkBase);
    String getSystemCompanyName();
    java.util.Optional<Employee> findByEmail(String email);
}
