package in.sfp.main.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "asset_tracking")
public class AssetTracking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "assigned_date")
    private LocalDate assignedDate;

    @Column(name = "returned_date")
    private LocalDate returnedDate;

    @Column(name = "assign_log")
    private String assignLog;

    @Column(name = "return_log")
    private String returnLog;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "asset_id")
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({ "assetTracking", "maintenanceLogs" })
    private Asset asset;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "employee_id")
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({ "assets", "assetTracking" })
    private Employee employee;

    public AssetTracking() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getAssignedDate() {
        return assignedDate;
    }

    public void setAssignedDate(LocalDate assignedDate) {
        this.assignedDate = assignedDate;
    }

    public LocalDate getReturnedDate() {
        return returnedDate;
    }

    public void setReturnedDate(LocalDate returnedDate) {
        this.returnedDate = returnedDate;
    }

    public String getAssignLog() {
        return assignLog;
    }

    public void setAssignLog(String assignLog) {
        this.assignLog = assignLog;
    }

    public String getReturnLog() {
        return returnLog;
    }

    public void setReturnLog(String returnLog) {
        this.returnLog = returnLog;
    }

    public Asset getAsset() {
        return asset;
    }

    public void setAsset(Asset asset) {
        this.asset = asset;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }
}
