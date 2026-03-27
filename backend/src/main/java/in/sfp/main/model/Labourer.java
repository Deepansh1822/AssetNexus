package in.sfp.main.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "labourers")
public class Labourer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String personnelId;

    @Column(nullable = false)
    private String trade;

    @Column(nullable = false)
    private String skillLevel;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false)
    private Double dailyWage;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(nullable = false)
    private String status = "AVAILABLE"; // AVAILABLE, ACTIVE, DISPOSED

    private String currentSite; // Nullable if in AVAILABLE

    private LocalDate registrationDate = LocalDate.now();

    public Labourer() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPersonnelId() { return personnelId; }
    public void setPersonnelId(String personnelId) { this.personnelId = personnelId; }

    public String getTrade() { return trade; }
    public void setTrade(String trade) { this.trade = trade; }

    public String getSkillLevel() { return skillLevel; }
    public void setSkillLevel(String skillLevel) { this.skillLevel = skillLevel; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public Double getDailyWage() { return dailyWage; }
    public void setDailyWage(Double dailyWage) { this.dailyWage = dailyWage; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCurrentSite() { return currentSite; }
    public void setCurrentSite(String currentSite) { this.currentSite = currentSite; }

    public LocalDate getRegistrationDate() { return registrationDate; }
    public void setRegistrationDate(LocalDate registrationDate) { this.registrationDate = registrationDate; }
}
