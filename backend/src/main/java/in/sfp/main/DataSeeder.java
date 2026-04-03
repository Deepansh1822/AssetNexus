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
    private final in.sfp.main.repo.ConstructionSiteRepository siteRepo;
    private final in.sfp.main.repo.LabourerRepository labourerRepo;
    private final org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    public DataSeeder(EmployeeRepo employeeRepo, PasswordEncoder passwordEncoder, 
                      in.sfp.main.repo.ConstructionSiteRepository siteRepo,
                      in.sfp.main.repo.LabourerRepository labourerRepo,
                      org.springframework.jdbc.core.JdbcTemplate jdbcTemplate) {
        this.employeeRepo = employeeRepo;
        this.passwordEncoder = passwordEncoder;
        this.siteRepo = siteRepo;
        this.labourerRepo = labourerRepo;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) throws Exception {
        // 0. Manual Schema Fix for Identity-Agnostic Tokens
        try {
            jdbcTemplate.execute("ALTER TABLE password_reset_tokens MODIFY COLUMN employee_id BIGINT NULL;");
            System.out.println(">>> Database Schema Fixed: employee_id is now optional in tokens.");
        } catch (Exception e) {
            // Probably already null or not needed
        }
        // 1. Ensure Admin exists
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
            System.out.println(">>> Admin created: admin@asset.com / admin123");
        }

        // 2. Ensure a Site Manager exists for demo
        Employee manager = employeeRepo.findByEmail("manager@site.com").orElse(null);
        if (manager == null) {
            manager = new Employee();
            manager.setName("Site Supervisor");
            manager.setEmail("manager@site.com");
            manager.setPhone("9999999999");
            manager.setRole("Project Manager");
            manager.setUserRole("SITE_MANAGER");
            manager.setDepartment("Construction");
            manager.setBranchName("Field Ops");
            manager.setCompanyName("AssetNexus Org");
            manager.setSystemId("MGR-001");
            manager.setPassword(passwordEncoder.encode("manager123"));
            manager = employeeRepo.save(manager);
            System.out.println(">>> Site Manager created: manager@site.com / manager123");
        }

        // 3. Seed Initial Sites
        if (siteRepo.count() == 0) {
            in.sfp.main.model.ConstructionSite s1 = new in.sfp.main.model.ConstructionSite();
            s1.setName("Skyline Plaza");
            s1.setLocation("Downtown District");
            s1.setSiteCode("SKY-01");
            s1.setSiteManager(manager);
            s1.setTargetCompletionPercentage(45);
            siteRepo.save(s1);

            in.sfp.main.model.ConstructionSite s2 = new in.sfp.main.model.ConstructionSite();
            s2.setName("Greenwood Residency");
            s2.setLocation("North Suburbs");
            s2.setSiteCode("GRN-02");
            s2.setSiteManager(manager);
            s2.setTargetCompletionPercentage(20);
            siteRepo.save(s2);
            System.out.println(">>> Site data seeded with assigned managers.");
        }

        // 3. Seed Initial Labourers
        if (labourerRepo.count() == 0) {
            in.sfp.main.model.Labourer l1 = new in.sfp.main.model.Labourer();
            l1.setName("Rajesh Kumar");
            l1.setPersonnelId("LAB-101");
            l1.setTrade("MASON");
            l1.setSkillLevel("MASTER");
            l1.setDailyWage(950.0);
            l1.setPhone("9876543210");
            l1.setCurrentSite("Skyline Plaza");
            l1.setStatus("ACTIVE");
            labourerRepo.save(l1);

            in.sfp.main.model.Labourer l2 = new in.sfp.main.model.Labourer();
            l2.setName("Suresh Pal");
            l2.setPersonnelId("LAB-102");
            l2.setTrade("GENERAL");
            l2.setSkillLevel("APPRENTICE");
            l2.setDailyWage(650.0);
            l2.setPhone("9876543211");
            l2.setStatus("AVAILABLE");
            labourerRepo.save(l2);
            System.out.println(">>> Labourer data seeded.");
        }
        
        // 4. Existing legacy employee sync logic...

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
