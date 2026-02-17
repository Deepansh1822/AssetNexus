package in.sfp.main.service;

import in.sfp.main.model.MaintenanceRequest;
import in.sfp.main.model.MaintenanceRequest.MaintenanceStatus;
import in.sfp.main.repo.MaintenanceRequestRepo;
import in.sfp.main.repo.AssetsRepo;
import in.sfp.main.repo.EmployeeRepo;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class MaintenanceService {

    private final MaintenanceRequestRepo maintenanceRequestRepo;
    private final AssetsRepo assetRepo;
    private final EmployeeRepo employeeRepo;
    private final in.sfp.main.repo.MaintenanceLogRepo maintenanceLogRepo;
    private final in.sfp.main.service.EmailService emailService;

    public MaintenanceService(MaintenanceRequestRepo maintenanceRequestRepo, 
                            AssetsRepo assetRepo, 
                            EmployeeRepo employeeRepo,
                            in.sfp.main.repo.MaintenanceLogRepo maintenanceLogRepo,
                            in.sfp.main.service.EmailService emailService) {
        this.maintenanceRequestRepo = maintenanceRequestRepo;
        this.assetRepo = assetRepo;
        this.employeeRepo = employeeRepo;
        this.maintenanceLogRepo = maintenanceLogRepo;
        this.emailService = emailService;
    }

    public List<MaintenanceRequest> getAllRequests() {
        return maintenanceRequestRepo.findAllByOrderByRequestedDateDesc();
    }

    public List<MaintenanceRequest> getEmployeeRequests() {
        // Updated for Unified Management: Return all requests (Admin & Employee)
        return maintenanceRequestRepo.findAllByOrderByRequestedDateDesc();
    }

    public List<MaintenanceRequest> getRequestsByEmployee(Long employeeId) {
        return maintenanceRequestRepo.findByRequestedById(employeeId);
    }

    public List<MaintenanceRequest> getRequestsByStatus(MaintenanceStatus status) {
        return maintenanceRequestRepo.findByStatus(status);
    }

    public Optional<MaintenanceRequest> getRequestById(Long id) {
        return maintenanceRequestRepo.findById(id);
    }

    public MaintenanceRequest createRequest(MaintenanceRequest request) {
        if (request.getAsset() == null || request.getAsset().getId() == null) {
            throw new RuntimeException("Asset information is required.");
        }

        // Check for existing active requests (PENDING or IN_PROGRESS)
        boolean hasActive = maintenanceRequestRepo.existsByAssetIdAndStatusIn(
                request.getAsset().getId(),
                List.of(MaintenanceStatus.PENDING, MaintenanceStatus.IN_PROGRESS)
        );

        if (hasActive) {
            throw new RuntimeException("This asset already has an active maintenance request (Pending or In-Progress).");
        }

        // Force status to PENDING for all new requests (Employee or Admin)
        request.setStatus(MaintenanceStatus.PENDING);
        
        // Since it's PENDING, we don't update asset status yet.
        // Asset status will change to UNDER_MAINTENANCE only when status moves to IN_PROGRESS.
        MaintenanceRequest saved = maintenanceRequestRepo.save(request);
        
        // Notify via email
        emailService.sendMaintenanceStatusUpdateEmail(saved);
        
        return saved;
    }

    public MaintenanceRequest updateStatus(Long id, MaintenanceStatus status, String adminNotes, String assignedTechnician) {
        MaintenanceRequest request = maintenanceRequestRepo.findById(id)
            .orElseThrow(() -> new RuntimeException("Maintenance request not found"));
        
        MaintenanceStatus oldStatus = request.getStatus();
        request.setStatus(status);
        if (adminNotes != null && !adminNotes.isEmpty()) {
            request.setAdminNotes(adminNotes);
        }
        if (assignedTechnician != null && !assignedTechnician.isEmpty()) {
            request.setAssignedTechnician(assignedTechnician);
        }
        
        if (status == MaintenanceStatus.COMPLETED) {
            request.setCompletedDate(LocalDateTime.now());
            // When completed, asset becomes available
            updateAssetStatus(request.getAsset().getId(), in.sfp.main.enums.Status.AVAILABLE);

            // Create a permanent MaintenanceLog
            in.sfp.main.model.MaintenanceLog log = new in.sfp.main.model.MaintenanceLog();
            log.setAsset(request.getAsset());
            log.setMaintenanceDate(java.time.LocalDate.now());
            log.setMaintenanceReason(request.getIssueDescription());
            log.setMaintenanceType(request.getMaintenanceType());
            log.setMaintenanceNotes(request.getAdminNotes());
            log.setMaintenanceBy(request.getAssignedTechnician() != null ? request.getAssignedTechnician() : "Internal Admin");
            log.setMaintenanceStatus(in.sfp.main.enums.MaintenanceStatus.COMPLETED);
            log.setMaintenanceCost(0.0); // Default cost
            maintenanceLogRepo.save(log);
        } else if (status == MaintenanceStatus.IN_PROGRESS && oldStatus != MaintenanceStatus.IN_PROGRESS) {
            updateAssetStatus(request.getAsset().getId(), in.sfp.main.enums.Status.UNDER_MAINTENANCE);
        } else if (status == MaintenanceStatus.CANCELLED && oldStatus == MaintenanceStatus.IN_PROGRESS) {
            // If cancelled while it was in progress, revert to available
            updateAssetStatus(request.getAsset().getId(), in.sfp.main.enums.Status.AVAILABLE);
        }
        
        MaintenanceRequest updated = maintenanceRequestRepo.save(request);

        // Notify via email
        emailService.sendMaintenanceStatusUpdateEmail(updated);
        
        return updated;
    }

    private void updateAssetStatus(Long assetId, in.sfp.main.enums.Status status) {
        assetRepo.findById(assetId).ifPresent(asset -> {
            asset.setStatus(status);
            // If going to maintenance, we might want to clear employee? 
            // Usually maintenance happens after it's returned, but for "Repair in Place" it might stay.
            // For now, let's just update the status.
            assetRepo.save(asset);
        });
    }

    public void deleteRequest(Long id) {
        maintenanceRequestRepo.deleteById(id);
    }
}
