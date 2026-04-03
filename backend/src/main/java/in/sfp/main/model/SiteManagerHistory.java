package in.sfp.main.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "site_manager_history")
public class SiteManagerHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "site_id")
    private ConstructionSite site;

    @ManyToOne
    @JoinColumn(name = "to_manager_id")
    private Employee toManager;

    @ManyToOne
    @JoinColumn(name = "from_manager_id")
    private Employee fromManager;

    private LocalDateTime changeDate = LocalDateTime.now();

    public SiteManagerHistory() {}

    public SiteManagerHistory(ConstructionSite site, Employee fromManager, Employee toManager) {
        this.site = site;
        this.fromManager = fromManager;
        this.toManager = toManager;
        this.changeDate = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public ConstructionSite getSite() { return site; }
    public void setSite(ConstructionSite site) { this.site = site; }
    public Employee getToManager() { return toManager; }
    public void setToManager(Employee toManager) { this.toManager = toManager; }
    public Employee getFromManager() { return fromManager; }
    public void setFromManager(Employee fromManager) { this.fromManager = fromManager; }
    public LocalDateTime getChangeDate() { return changeDate; }
    public void setChangeDate(LocalDateTime changeDate) { this.changeDate = changeDate; }
}
