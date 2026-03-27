package in.sfp.main.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "site_stocks")
public class SiteStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String itemName; // e.g. Cement, TMT Steel, Sand

    @Column(nullable = false)
    private Double quantity = 0.0;

    @Column(nullable = false)
    private String unit; // e.g. BAGS, TONS, CFT

    private String vendor;
    private String category; // CONSUMABLE, RAW_MATERIAL, etc.

    @ManyToOne
    @JoinColumn(name = "site_id", nullable = false)
    private ConstructionSite site;

    private LocalDateTime lastRestockedAt = LocalDateTime.now();

    public SiteStock() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public Double getQuantity() { return quantity; }
    public void setQuantity(Double quantity) { this.quantity = quantity; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public String getVendor() { return vendor; }
    public void setVendor(String vendor) { this.vendor = vendor; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public ConstructionSite getSite() { return site; }
    public void setSite(ConstructionSite site) { this.site = site; }

    public LocalDateTime getLastRestockedAt() { return lastRestockedAt; }
    public void setLastRestockedAt(LocalDateTime lastRestockedAt) { this.lastRestockedAt = lastRestockedAt; }
}
