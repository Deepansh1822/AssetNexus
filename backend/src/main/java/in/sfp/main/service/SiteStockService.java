package in.sfp.main.service;

import in.sfp.main.model.SiteStock;
import in.sfp.main.model.ConstructionSite;
import in.sfp.main.repo.SiteStockRepository;
import in.sfp.main.repo.ConstructionSiteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class SiteStockService {

    @Autowired
    private SiteStockRepository repository;

    @Autowired
    private ConstructionSiteRepository siteRepo;

    public List<SiteStock> getStockBySite(Long siteId) {
        ConstructionSite site = siteRepo.findById(siteId).orElseThrow();
        return repository.findBySite(site);
    }

    public SiteStock addStock(Long siteId, SiteStock stock) {
        ConstructionSite site = siteRepo.findById(siteId).orElseThrow();
        stock.setSite(site);
        stock.setLastRestockedAt(LocalDateTime.now());
        return repository.save(stock);
    }

    public List<SiteStock> getAllStocks() {
        return repository.findAll();
    }

    public SiteStock adjustQuantity(Long stockId, Double delta) {
        SiteStock stock = repository.findById(stockId).orElseThrow();
        stock.setQuantity(stock.getQuantity() + delta);
        if (stock.getQuantity() < 0) stock.setQuantity(0.0);
        stock.setLastRestockedAt(LocalDateTime.now());
        return repository.save(stock);
    }
}
