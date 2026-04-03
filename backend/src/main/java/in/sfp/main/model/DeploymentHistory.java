package in.sfp.main.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "deployment_history")
public class DeploymentHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "labourer_id", nullable = false)
    private Labourer labourer;

    @Column(nullable = false)
    private String siteName;

    @Column(nullable = false)
    private LocalDate startDate;

    private LocalDate endDate;

    public DeploymentHistory() {}

    public DeploymentHistory(Labourer labourer, String siteName, LocalDate startDate) {
        this.labourer = labourer;
        this.siteName = siteName;
        this.startDate = startDate;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Labourer getLabourer() { return labourer; }
    public void setLabourer(Labourer labourer) { this.labourer = labourer; }

    public String getSiteName() { return siteName; }
    public void setSiteName(String siteName) { this.siteName = siteName; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
}
