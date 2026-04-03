package in.sfp.main.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "work_order_consumption")
public class WorkOrderConsumption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "work_order_id", nullable = false)
    private WorkOrder workOrder;

    @ManyToOne
    @JoinColumn(name = "stock_item_id", nullable = false)
    private SiteStock stockItem;

    @Column(nullable = false)
    private Double quantityUsed;

    private LocalDateTime consumptionDate = LocalDateTime.now();

    public WorkOrderConsumption() {}

    public WorkOrderConsumption(WorkOrder workOrder, SiteStock stockItem, Double quantityUsed) {
        this.workOrder = workOrder;
        this.stockItem = stockItem;
        this.quantityUsed = quantityUsed;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public WorkOrder getWorkOrder() { return workOrder; }
    public void setWorkOrder(WorkOrder workOrder) { this.workOrder = workOrder; }

    public SiteStock getStockItem() { return stockItem; }
    public void setStockItem(SiteStock stockItem) { this.stockItem = stockItem; }

    public Double getQuantityUsed() { return quantityUsed; }
    public void setQuantityUsed(Double quantityUsed) { this.quantityUsed = quantityUsed; }

    public LocalDateTime getConsumptionDate() { return consumptionDate; }
    public void setConsumptionDate(LocalDateTime consumptionDate) { this.consumptionDate = consumptionDate; }
}
