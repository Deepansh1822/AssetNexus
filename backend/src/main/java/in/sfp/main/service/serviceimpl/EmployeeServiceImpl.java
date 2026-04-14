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
        // Broadening retrieval to ensure existing legacy users are visible
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
                if (employee.getImageData() == null || employee.getImageData().length == 0) {
                    employee.setImageData(existing.getImageData());
                }
                // Preserve active state if update
                employee.setActive(existing.isActive());
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
            // Auto-generate System ID if missing
            if (employee.getSystemId() == null || employee.getSystemId().trim().isEmpty()) {
                String datePart = java.time.LocalDate.now().toString().replace("-", "");
                String randomPart = java.util.UUID.randomUUID().toString().substring(0, 4).toUpperCase();
                employee.setSystemId("EMP-" + datePart + "-" + randomPart);
            }
        }
        return employeeRepo.save(employee);
    }

    @Override
    public void deleteEmployee(Long id) {
        // We keep as soft delete for now, but redirect to status
        this.disposeEmployee(id);
    }

    @Override
    public void disposeEmployee(Long id) {
        Employee emp = employeeRepo.findById(id).orElse(null);
        if (emp != null) {
            emp.setActive(false);
            emp.setStatus("DISPOSED");
            employeeRepo.save(emp);
        }
    }

    @Override
    public Employee toggleEmployeeStatus(Long id) {
        Employee emp = employeeRepo.findById(id).orElseThrow(() -> new RuntimeException("Identity not found"));
        if ("DISABLED".equals(emp.getStatus())) {
            emp.setStatus("ACTIVE");
            emp.setActive(true);
        } else {
            emp.setStatus("DISABLED");
            emp.setActive(false);
        }
        return employeeRepo.save(emp);
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public void createPasswordResetToken(String email, String resetLinkBase) {
        Employee employee = employeeRepo.findByEmailAndActiveTrue(email).orElse(null);
        if (employee != null) {
            String token = java.util.UUID.randomUUID().toString();
            
            // Upsert (Update or Create) to avoid 'Duplicate Entry' on @OneToOne unique constraint
            in.sfp.main.model.PasswordResetToken resetToken = tokenRepo.findByEmployee(employee).orElse(null);
            
            if (resetToken != null) {
                resetToken.setToken(token);
                resetToken.setExpiryDate(java.time.LocalDateTime.now().plusMinutes(15));
            } else {
                resetToken = new in.sfp.main.model.PasswordResetToken(token, employee);
            }
            
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
            if (!resetToken.isExpired() && resetToken.getEmployee() != null) {
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

    @Override
    public List<Employee> findByUserRole(String role) {
        // Return all roles regardless of status for the ledger view
        return employeeRepo.findByUserRole(role);
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public void registerWithInvite(Employee employee, String resetLinkBase) {
        // 1. Initial save with temporary password
        if (employee.getPassword() == null || employee.getPassword().isEmpty()) {
            employee.setPassword(java.util.UUID.randomUUID().toString());
        }
        Employee saved = this.saveEmployee(employee);

        // 2. Generate Token
        String token = java.util.UUID.randomUUID().toString();
        in.sfp.main.model.PasswordResetToken resetToken = new in.sfp.main.model.PasswordResetToken(token, saved);
        tokenRepo.save(resetToken);

        // 3. Send Invitation Email
        String setupLink = resetLinkBase + "?token=" + token;
        emailService.sendEmployeeInvitationEmail(saved.getEmail(), setupLink, saved.getName());
    }

    @Override
    public String getSystemCompanyName() {
        return employeeRepo.findAll().stream()
                .map(Employee::getCompanyName)
                .filter(c -> c != null && !c.trim().isEmpty())
                .findFirst()
                .orElse("");
    }
}
