package in.sfp.main.model;

import jakarta.persistence.*;
import in.sfp.main.model.Employee;
import java.time.LocalDate;

@Entity
@Table(name = "construction_sites")
public class ConstructionSite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String siteCode; // e.g., SKY-1

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String location;

    @Column(nullable = false)
    private String status = "ACTIVE"; // ACTIVE, COMPLETED, ON_HOLD

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "site_manager_id")
    private Employee siteManager;

    private Integer targetCompletionPercentage = 0;
    
    private LocalDate startDate;
    private LocalDate estimatedEndDate;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "site_image", length = 5242880) // 5MB limit
    private byte[] siteImage;

    private boolean hasImage = false;

    public ConstructionSite() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSiteCode() { return siteCode; }
    public void setSiteCode(String siteCode) { this.siteCode = siteCode; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Employee getSiteManager() { return siteManager; }
    public void setSiteManager(Employee siteManager) { this.siteManager = siteManager; }

    @com.fasterxml.jackson.annotation.JsonProperty("managerName")
    public String getManagerName() { return siteManager != null ? siteManager.getName() : "Not Assigned"; }

    public Integer getTargetCompletionPercentage() { return targetCompletionPercentage; }
    public void setTargetCompletionPercentage(Integer targetCompletionPercentage) { this.targetCompletionPercentage = targetCompletionPercentage; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEstimatedEndDate() { return estimatedEndDate; }
    public void setEstimatedEndDate(LocalDate estimatedEndDate) { this.estimatedEndDate = estimatedEndDate; }

    public byte[] getSiteImage() { return siteImage; }
    public void setSiteImage(byte[] siteImage) { this.siteImage = siteImage; }

    public boolean isHasImage() { return hasImage; }
    public void setHasImage(boolean hasImage) { this.hasImage = hasImage; }
}
