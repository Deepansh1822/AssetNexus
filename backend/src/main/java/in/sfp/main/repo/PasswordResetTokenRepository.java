package in.sfp.main.repo;

import in.sfp.main.model.Employee;
import in.sfp.main.model.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(String token);
    Optional<PasswordResetToken> findByEmployee(Employee employee);
    void deleteByEmployee(Employee employee);
}
