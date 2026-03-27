package in.sfp.main.controllers;

import in.sfp.main.model.SiteStock;
import in.sfp.main.service.SiteStockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/stocks")
public class SiteStockController {

    @Autowired
    private SiteStockService service;

    @GetMapping("/site/{siteId}")
    public List<SiteStock> getBySite(@PathVariable Long siteId) {
        return service.getStockBySite(siteId);
    }

    @PostMapping("/add/{siteId}")
    public SiteStock add(@PathVariable Long siteId, @RequestBody SiteStock stock) {
        return service.addStock(siteId, stock);
    }

    @PostMapping("/{stockId}/adjust")
    public SiteStock adjust(@PathVariable Long stockId, @RequestParam Double delta) {
        return service.adjustQuantity(stockId, delta);
    }

    @DeleteMapping("/{stockId}")
    public void delete(@PathVariable Long stockId) {
        // Optional delete for mistakes
    }
}
