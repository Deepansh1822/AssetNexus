package in.sfp.main.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Audit log for all labourer site movements and status changes.
 */
@Entity
@Table(name = "labourer_transfer_logs")
public class LabourerTransferLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "labourer_id", nullable = false)
    private Labourer labourer;

    private String fromSite;
    private String toSite;
    private String fromStatus;
    private String toStatus;

    private String shiftingMode;
    private Double shiftAllowance;
    private Double foodAllowance;

    @Column(nullable = false)
    private LocalDateTime transferTime = LocalDateTime.now();

    public LabourerTransferLog() {}

    public LabourerTransferLog(Labourer labourer, String fromSite, String toSite, String fromStatus, String toStatus) {
        this.labourer = labourer;
        this.fromSite = fromSite;
        this.toSite = toSite;
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
    }

    public LabourerTransferLog(Labourer labourer, String fromSite, String toSite, String fromStatus, String toStatus, String shiftingMode, Double shiftAllowance, Double foodAllowance) {
        this.labourer = labourer;
        this.fromSite = fromSite;
        this.toSite = toSite;
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.shiftingMode = shiftingMode;
        this.shiftAllowance = shiftAllowance;
        this.foodAllowance = foodAllowance;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Labourer getLabourer() { return labourer; }
    public void setLabourer(Labourer labourer) { this.labourer = labourer; }

    public String getFromSite() { return fromSite; }
    public void setFromSite(String fromSite) { this.fromSite = fromSite; }

    public String getToSite() { return toSite; }
    public void setToSite(String toSite) { this.toSite = toSite; }

    public String getFromStatus() { return fromStatus; }
    public void setFromStatus(String fromStatus) { this.fromStatus = fromStatus; }

    public String getToStatus() { return toStatus; }
    public void setToStatus(String toStatus) { this.toStatus = toStatus; }

    public String getShiftingMode() { return shiftingMode; }
    public void setShiftingMode(String shiftingMode) { this.shiftingMode = shiftingMode; }

    public Double getShiftAllowance() { return shiftAllowance; }
    public void setShiftAllowance(Double shiftAllowance) { this.shiftAllowance = shiftAllowance; }

    public Double getFoodAllowance() { return foodAllowance; }
    public void setFoodAllowance(Double foodAllowance) { this.foodAllowance = foodAllowance; }

    public LocalDateTime getTransferTime() { return transferTime; }
    public void setTransferTime(LocalDateTime transferTime) { this.transferTime = transferTime; }
}
