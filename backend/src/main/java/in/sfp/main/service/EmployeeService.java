package in.sfp.main.service;

import in.sfp.main.model.Employee;
import java.util.List;

public interface EmployeeService {
    List<Employee> getAllEmployees();

    Employee getEmployeeById(Long id);

    Employee saveEmployee(Employee employee);

    void deleteEmployee(Long id);
    
    void createPasswordResetToken(String email, String resetLinkBase);
    boolean resetPassword(String token, String newPassword);
    List<String> getDistinctBranchNames();
}
