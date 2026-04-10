package in.sfp.main.model;

import jakarta.persistence.*;

@Entity
@Table(name = "work_order_allocation")
public class WorkOrderAllocation {

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
    private Double allocatedQuantity;

    public WorkOrderAllocation() {}

    public WorkOrderAllocation(WorkOrder workOrder, SiteStock stockItem, Double allocatedQuantity) {
        this.workOrder = workOrder;
        this.stockItem = stockItem;
        this.allocatedQuantity = allocatedQuantity;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public WorkOrder getWorkOrder() { return workOrder; }
    public void setWorkOrder(WorkOrder workOrder) { this.workOrder = workOrder; }

    public SiteStock getStockItem() { return stockItem; }
    public void setStockItem(SiteStock stockItem) { this.stockItem = stockItem; }

    public Double getAllocatedQuantity() { return allocatedQuantity; }
    public void setAllocatedQuantity(Double allocatedQuantity) { this.allocatedQuantity = allocatedQuantity; }
}
