package in.sfp.main.controllers;

import in.sfp.main.model.AssetTracking;
import in.sfp.main.repo.AssetTrackingRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    @Autowired
    private AssetTrackingRepo assetTrackingRepo;

    @GetMapping("/history")
    public List<AssetTracking> getFullHistory() {
        return assetTrackingRepo.findAll();
    }

    @GetMapping("/history/{assetId}")
    public List<AssetTracking> getAssetHistory(@PathVariable Long assetId) {
        return assetTrackingRepo.findByAssetId(assetId);
    }

    @GetMapping("/employee/{employeeId}")
    public List<AssetTracking> getEmployeeHistory(@PathVariable Long employeeId) {
        return assetTrackingRepo.findByEmployeeId(employeeId);
    }
}
