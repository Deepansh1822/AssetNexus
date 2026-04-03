package in.sfp.main.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "password_reset_tokens")
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @OneToOne(targetEntity = Employee.class, fetch = FetchType.EAGER)
    @JoinColumn(nullable = true, name = "employee_id")
    private Employee employee;

    @OneToOne(targetEntity = Labourer.class, fetch = FetchType.EAGER)
    @JoinColumn(nullable = true, name = "labourer_id")
    private Labourer labourer;

    @Column(nullable = false)
    private LocalDateTime expiryDate;

    public PasswordResetToken() {}

    public PasswordResetToken(String token, Employee employee) {
        this.token = token;
        this.employee = employee;
        this.expiryDate = LocalDateTime.now().plusHours(24);
    }

    public PasswordResetToken(String token, Labourer labourer) {
        this.token = token;
        this.labourer = labourer;
        this.expiryDate = LocalDateTime.now().plusHours(24);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public Labourer getLabourer() {
        return labourer;
    }

    public void setLabourer(Labourer labourer) {
        this.labourer = labourer;
    }

    public LocalDateTime getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDateTime expiryDate) {
        this.expiryDate = expiryDate;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiryDate);
    }
}
