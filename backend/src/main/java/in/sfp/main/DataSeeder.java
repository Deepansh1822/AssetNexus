package in.sfp.main;

import in.sfp.main.model.Employee;
import in.sfp.main.repo.EmployeeRepo;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class DataSeeder implements CommandLineRunner {

    private final EmployeeRepo employeeRepo;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(EmployeeRepo employeeRepo, PasswordEncoder passwordEncoder) {
        this.employeeRepo = employeeRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        // 1. Ensure Admin exists and has correct branch
        Employee admin = employeeRepo.findByEmail("admin@asset.com").orElse(null);
        if (admin == null) {
            admin = new Employee();
            admin.setName("System Admin");
            admin.setEmail("admin@asset.com");
            admin.setPhone("0000000000");
            admin.setRole("Administrator");
            admin.setUserRole("ADMIN");
            admin.setDepartment("Management");
            admin.setBranchName("Head Office");
            admin.setCompanyName("AssetNexus Org");
            admin.setSystemId("SYS-ADMIN-001");
            admin.setPassword(passwordEncoder.encode("admin123"));
            employeeRepo.save(admin);
            System.out.println(">>> Admin user created: admin@asset.com / admin123");
        } else if (admin.getBranchName() == null || admin.getBranchName().isEmpty() || admin.getBranchName().equals("Main")) {
            // Specifically ensure Admin is in Head Office if they were previously defaulted to "Main"
            admin.setBranchName("Head Office");
            employeeRepo.save(admin);
            System.out.println(">>> Admin branch updated to: Head Office");
        }

        // 2. Ensure all employees have valid fields (for legacy data)
        java.util.List<Employee> allEmployees = employeeRepo.findAll();
        boolean updated = false;
        for (Employee emp : allEmployees) {
            boolean empChanged = false;
            
            // Skip admin since we handled it specifically above
            if (emp.getEmail().equals("admin@asset.com")) continue;

            if (emp.getUserRole() == null || emp.getUserRole().isEmpty()) {
                emp.setUserRole("EMPLOYEE");
                empChanged = true;
            }
            if (emp.getPassword() == null || emp.getPassword().isEmpty()) {
                emp.setPassword(passwordEncoder.encode("password123"));
                empChanged = true;
            }
            if (emp.getDepartment() == null || emp.getDepartment().isEmpty()) {
                emp.setDepartment("General");
                empChanged = true;
            }
            if (emp.getBranchName() == null || emp.getBranchName().isEmpty()) {
                emp.setBranchName("Main");
                empChanged = true;
            }
            if (emp.getCompanyName() == null || emp.getCompanyName().isEmpty()) {
                emp.setCompanyName("AssetNexus Org");
                empChanged = true;
            }
            if (emp.getSystemId() == null || emp.getSystemId().isEmpty()) {
                emp.setSystemId("SYS-EMP-" + emp.getId());
                empChanged = true;
            }
            if (empChanged) {
                employeeRepo.save(emp);
                updated = true;
            }
        }
        
        if (updated) {
            System.out.println(">>> Legacy employees updated with default passwords (password123) and roles.");
        }
    }
}
