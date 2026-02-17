package in.sfp.main.service.serviceimpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.time.LocalDate;
import in.sfp.main.model.Asset;
import in.sfp.main.model.Employee;
import in.sfp.main.model.AssetTracking;
import in.sfp.main.repo.AssetsRepo;
import in.sfp.main.repo.EmployeeRepo;
import in.sfp.main.repo.AssetTrackingRepo;
import in.sfp.main.service.AssetsService;
import in.sfp.main.enums.Status;

@Service
public class AssetsServiceImpl implements AssetsService {

    @Autowired
    private AssetsRepo assetsRepo;

    @Autowired
    private EmployeeRepo employeeRepo;

    @Autowired
    private AssetTrackingRepo assetTrackingRepo;

    @Autowired
    private in.sfp.main.repo.VendorRepo vendorRepo;

    @Autowired
    private in.sfp.main.service.EmailService emailService;


    @Override
    public List<Asset> getAllAssets() {
        return assetsRepo.findAll();
    }

    @Override
    public Asset getAssetById(Long id) {
        return assetsRepo.findById(id).orElse(null);
    }

    @Override
    public Asset saveAsset(Asset asset) {
        // If this is a new asset (no ID), set status to AVAILABLE
        if (asset.getId() == null) {
            asset.setStatus(Status.AVAILABLE);
        }

        // Handle vendor: find existing or create new
        if (asset.getVendor() != null && asset.getVendor().getVendorName() != null) {
            in.sfp.main.model.Vendor vendor = vendorRepo.findByVendorName(asset.getVendor().getVendorName())
                    .orElseGet(() -> {
                        // Create new vendor if not found
                        in.sfp.main.model.Vendor newVendor = new in.sfp.main.model.Vendor();
                        newVendor.setVendorName(asset.getVendor().getVendorName());
                        newVendor.setPhone(asset.getVendor().getPhone());
                        newVendor.setEmail(asset.getVendor().getEmail());
                        newVendor.setWebsite(asset.getVendor().getWebsite());
                        return vendorRepo.save(newVendor);
                    });
            asset.setVendor(vendor);
        }

        return assetsRepo.save(asset);
    }

    @Override
    public List<Asset> saveAllAssets(List<Asset> assets) {
        return assetsRepo.saveAll(assets);
    }

    @Override
    public void deleteAsset(Long id) {
        assetsRepo.deleteById(id);
    }

    @Override
    public Asset assignAsset(Long assetId, Long employeeId, String notes) {
        Asset asset = assetsRepo.findById(assetId).orElse(null);
        Employee employee = employeeRepo.findById(employeeId).orElse(null);

        if (asset != null && employee != null) {
            // Validation: Check if asset is available
            if (asset.getStatus() == Status.DISPOSED) {
                throw new RuntimeException("Cannot assign a DISPOSED asset.");
            }
            if (asset.getStatus() == Status.IN_USE || asset.getEmployee() != null) {
                throw new RuntimeException("Asset is currently assigned to " + (asset.getEmployee() != null ? asset.getEmployee().getName() : "someone") + ". Please return it first.");
            }
            if (asset.getStatus() == Status.UNDER_MAINTENANCE) {
                throw new RuntimeException("Asset is currently UNDER MAINTENANCE.");
            }

            // Update Asset
            asset.setEmployee(employee);
            asset.setStatus(Status.IN_USE);
            asset.setAssignmentDate(LocalDate.now());
            assetsRepo.save(asset);

            // Create Tracking Entry
            AssetTracking tracking = new AssetTracking();
            tracking.setAsset(asset);
            tracking.setEmployee(employee);
            tracking.setAssignedDate(LocalDate.now());
            tracking.setAssignLog(notes);
            assetTrackingRepo.save(tracking);

            // Notify via email
            emailService.sendAssetAssignmentEmail(employee, asset, notes);

            return asset;
        }
        return null;
    }

    @Override
    public Asset returnAsset(Long assetId, String notes) {
        Asset asset = assetsRepo.findById(assetId).orElse(null);
        if (asset != null) {
            // Find the pending tracking entry (latest one without return date)
            List<AssetTracking> history = assetTrackingRepo.findByAssetId(assetId);
            AssetTracking lastTracking = history.stream()
                    .filter(t -> t.getReturnedDate() == null)
                    .findFirst().orElse(null);

            if (lastTracking != null) {
                lastTracking.setReturnedDate(LocalDate.now());
                lastTracking.setReturnLog(notes);
                assetTrackingRepo.save(lastTracking);
            }

            // Update Asset
            asset.setEmployee(null);
            asset.setStatus(Status.AVAILABLE);
            asset.setAssignmentDate(null);
            assetsRepo.save(asset);

            // Notify via email
            if (lastTracking != null && lastTracking.getEmployee() != null) {
                emailService.sendAssetReturnEmail(lastTracking.getEmployee(), asset, notes);
            }

            return asset;
        }
        return null;
    }

    @Override
    public Asset disposeAsset(Long assetId, String reason) {
        Asset asset = assetsRepo.findById(assetId).orElse(null);
        if (asset != null) {
            // Capture employee before clearing
            Employee previousOwner = asset.getEmployee();

            // If asset is currently assigned, return it first (this creates a tracking log)
            if (previousOwner != null) {
                returnAsset(assetId, "Auto-returned before disposal");
            }

            // Update Asset status to DISPOSED
            asset.setStatus(Status.DISPOSED);
            asset.setEmployee(null);
            assetsRepo.save(asset);

            // Create tracking log entry for disposal
            AssetTracking tracking = new AssetTracking();
            tracking.setAsset(asset);
            tracking.setEmployee(null);
            tracking.setAssignedDate(java.time.LocalDate.now());
            tracking.setReturnedDate(java.time.LocalDate.now());
            tracking.setAssignLog("Asset disposed: " + (reason != null ? reason : "No reason provided"));
            tracking.setReturnLog("DISPOSED");
            assetTrackingRepo.save(tracking);

            // Notify via email if it was attached to someone
            if (previousOwner != null) {
                 emailService.sendAssetDisposalEmail(previousOwner, asset, reason);
            }
            
            return asset;
        }
        return null;
    }
}
