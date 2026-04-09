package in.sfp.main.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "salary_slips")
public class SalarySlip {

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

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false)
    private Double amount;

    @Column(nullable = false)
    private Double baseRate;

    private String paymentType;

    private String slipCategory; // DAY, WEEK, MONTH, ADVANCE

    private LocalDateTime generatedAt = LocalDateTime.now();

    public SalarySlip() {}

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

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }

    public Double getBaseRate() { return baseRate; }
    public void setBaseRate(Double baseRate) { this.baseRate = baseRate; }

    public String getPaymentType() { return paymentType; }
    public void setPaymentType(String paymentType) { this.paymentType = paymentType; }

    public String getSlipCategory() { return slipCategory; }
    public void setSlipCategory(String slipCategory) { this.slipCategory = slipCategory; }

    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }
}
