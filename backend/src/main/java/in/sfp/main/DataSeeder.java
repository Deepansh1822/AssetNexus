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
        // 1. Ensure Admin exists
        if (employeeRepo.findByEmail("admin@asset.com").isEmpty()) {
            Employee admin = new Employee();
            admin.setName("System Admin");
            admin.setEmail("admin@asset.com");
            admin.setPhone("0000000000");
            admin.setRole("Administrator");
            admin.setUserRole("ADMIN");
            admin.setPassword(passwordEncoder.encode("admin123"));
            employeeRepo.save(admin);
            System.out.println(">>> Admin user created: admin@asset.com / admin123");
        }

        // 2. Ensure all employees have a role and password (for legacy data)
        java.util.List<Employee> allEmployees = employeeRepo.findAll();
        boolean updated = false;
        for (Employee emp : allEmployees) {
            boolean empChanged = false;
            if (emp.getUserRole() == null || emp.getUserRole().isEmpty()) {
                emp.setUserRole("EMPLOYEE");
                empChanged = true;
            }
            if (emp.getPassword() == null || emp.getPassword().isEmpty()) {
                emp.setPassword(passwordEncoder.encode("password123"));
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
