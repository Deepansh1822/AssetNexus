package in.sfp.main.service.serviceimpl;

import in.sfp.main.model.Employee;
import in.sfp.main.repo.EmployeeRepo;
import in.sfp.main.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeRepo employeeRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private in.sfp.main.repo.PasswordResetTokenRepository tokenRepo;

    @Autowired
    private in.sfp.main.service.EmailService emailService;

    @Override
    public List<Employee> getAllEmployees() {
        return employeeRepo.findAll();
    }

    @Override
    public Employee getEmployeeById(Long id) {
        return employeeRepo.findById(id).orElse(null);
    }

    @Override
    public Employee saveEmployee(Employee employee) {
        if (employee.getId() != null) {
            Employee existing = employeeRepo.findById(employee.getId()).orElse(null);
            if (existing != null) {
                // Handle password preservation
                if (employee.getPassword() == null || employee.getPassword().isEmpty()) {
                    employee.setPassword(existing.getPassword());
                } else if (!employee.getPassword().startsWith("$2a$") && !employee.getPassword().startsWith("$2b$")) {
                    // New password provided, encode it
                    employee.setPassword(passwordEncoder.encode(employee.getPassword()));
                }
                
                // Handle image preservation if not provided
                if (employee.getEmployeeImage() == null || employee.getEmployeeImage().length == 0) {
                    employee.setEmployeeImage(existing.getEmployeeImage());
                }
            } else {
                // New employee but ID was provided (shouldn't happen with IDENTITY, but for safety)
                if (employee.getPassword() != null && !employee.getPassword().isEmpty()) {
                    employee.setPassword(passwordEncoder.encode(employee.getPassword()));
                }
            }
        } else {
            // New employee
            if (employee.getPassword() != null && !employee.getPassword().isEmpty()) {
                employee.setPassword(passwordEncoder.encode(employee.getPassword()));
            }
        }
        return employeeRepo.save(employee);
    }

    @Override
    public void deleteEmployee(Long id) {
        employeeRepo.deleteById(id);
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public void createPasswordResetToken(String email, String resetLinkBase) {
        Employee employee = employeeRepo.findByEmail(email).orElse(null);
        if (employee != null) {
            // Delete any existing token for this employee
            tokenRepo.deleteByEmployee(employee);
            
            String token = java.util.UUID.randomUUID().toString();
            in.sfp.main.model.PasswordResetToken resetToken = new in.sfp.main.model.PasswordResetToken(token, employee);
            tokenRepo.save(resetToken);
            
            String resetLink = resetLinkBase + "?token=" + token;
            emailService.sendPasswordResetEmail(employee.getEmail(), resetLink, employee.getName());
        }
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public boolean resetPassword(String token, String newPassword) {
        java.util.Optional<in.sfp.main.model.PasswordResetToken> tokenOpt = tokenRepo.findByToken(token);
        if (tokenOpt.isPresent()) {
            in.sfp.main.model.PasswordResetToken resetToken = tokenOpt.get();
            if (!resetToken.isExpired()) {
                Employee employee = resetToken.getEmployee();
                employee.setPassword(passwordEncoder.encode(newPassword));
                employeeRepo.save(employee);
                tokenRepo.delete(resetToken);
                return true;
            }
        }
        return false;
    }

    @Override
    public List<String> getDistinctBranchNames() {
        return employeeRepo.findDistinctBranchNames();
    }
}
