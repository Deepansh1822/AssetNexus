package in.sfp.main.service;

import in.sfp.main.model.Employee;
import in.sfp.main.repo.EmployeeRepo;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final EmployeeRepo employeeRepo;
    private final in.sfp.main.repo.LabourerRepository labourerRepo;

    public CustomUserDetailsService(EmployeeRepo employeeRepo, in.sfp.main.repo.LabourerRepository labourerRepo) {
        this.employeeRepo = employeeRepo;
        this.labourerRepo = labourerRepo;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // 1. Try Asset Management Ledger
        java.util.Optional<in.sfp.main.model.Employee> empOpt = employeeRepo.findByEmail(email);
        if (empOpt.isPresent()) {
            in.sfp.main.model.Employee emp = empOpt.get();
            boolean accountEnabled = emp.isActive();
            
            // Check for contract expiration
            if ("TEMPORARY".equalsIgnoreCase(emp.getEmploymentType()) && emp.getContractEndDate() != null) {
                if (emp.getContractEndDate().isBefore(java.time.LocalDate.now())) {
                    accountEnabled = false;
                }
            }

            return User.builder()
                    .username(emp.getEmail())
                    .password(emp.getPassword())
                    .roles(emp.getUserRole())
                    .disabled(!accountEnabled)
                    .build();
        }

        // 2. Try Project Management Ledger (Staff from Labourer table)
        in.sfp.main.model.Labourer labourer = labourerRepo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Security Identity not found in any ledger: " + email));

        return User.builder()
                .username(labourer.getEmail())
                .password(labourer.getPassword())
                .roles(labourer.getUserRole())
                .build();
    }
}
