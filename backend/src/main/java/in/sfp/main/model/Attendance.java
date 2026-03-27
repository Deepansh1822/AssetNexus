package in.sfp.main.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "labour_attendance")
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "labourer_id", nullable = false)
    private Labourer labourer;

    @ManyToOne
    @JoinColumn(name = "site_id") // Optional if not site-specific at time of attendance
    private ConstructionSite site;

    @Column(nullable = false)
    private LocalDate attendanceDate;

    @Column(nullable = false)
    private String status; // PRESENT, ABSENT, HALF_DAY, ON_LEAVE

    private String holidayReason; // Sunday, etc.
    
    @Column(nullable = false)
    private Double dailyWageAtTime; // Capture wage at date for payroll calculation if rates change

    public Attendance() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Labourer getLabourer() { return labourer; }
    public void setLabourer(Labourer labourer) { this.labourer = labourer; }

    public ConstructionSite getSite() { return site; }
    public void setSite(ConstructionSite site) { this.site = site; }

    public LocalDate getAttendanceDate() { return attendanceDate; }
    public void setAttendanceDate(LocalDate attendanceDate) { this.attendanceDate = attendanceDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getHolidayReason() { return holidayReason; }
    public void setHolidayReason(String holidayReason) { this.holidayReason = holidayReason; }

    public Double getDailyWageAtTime() { return dailyWageAtTime; }
    public void setDailyWageAtTime(Double dailyWageAtTime) { this.dailyWageAtTime = dailyWageAtTime; }
}
