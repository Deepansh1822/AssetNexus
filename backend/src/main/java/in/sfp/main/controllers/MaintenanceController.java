package in.sfp.main.controllers;

import in.sfp.main.model.MaintenanceRequest;
import in.sfp.main.model.MaintenanceRequest.MaintenanceStatus;
import in.sfp.main.service.MaintenanceService;
import in.sfp.main.service.AssetsService;
import in.sfp.main.service.EmployeeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/maintenance")
public class MaintenanceController {

    private final MaintenanceService maintenanceService;
    private final AssetsService assetService;
    private final EmployeeService employeeService;

    public MaintenanceController(MaintenanceService maintenanceService,
                                AssetsService assetService,
                                EmployeeService employeeService) {
        this.maintenanceService = maintenanceService;
        this.assetService = assetService;
        this.employeeService = employeeService;
    }

    @GetMapping
    public ResponseEntity<List<MaintenanceRequest>> getAllRequests() {
        return ResponseEntity.ok(maintenanceService.getAllRequests());
    }

    @GetMapping("/employee-requests")
    public ResponseEntity<List<MaintenanceRequest>> getEmployeeRequests() {
        return ResponseEntity.ok(maintenanceService.getEmployeeRequests());
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<MaintenanceRequest>> getRequestsByEmployee(@PathVariable Long employeeId) {
        return ResponseEntity.ok(maintenanceService.getRequestsByEmployee(employeeId));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<MaintenanceRequest>> getRequestsByStatus(@PathVariable MaintenanceStatus status) {
        return ResponseEntity.ok(maintenanceService.getRequestsByStatus(status));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MaintenanceRequest> getRequestById(@PathVariable Long id) {
        return maintenanceService.getRequestById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createRequest(@RequestBody MaintenanceRequest request) {
        try {
            MaintenanceRequest created = maintenanceService.createRequest(request);
            return ResponseEntity.ok(created);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<MaintenanceRequest> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> payload) {
        try {
            MaintenanceStatus status = MaintenanceStatus.valueOf(payload.get("status"));
            String adminNotes = payload.get("adminNotes");
            String assignedTechnician = payload.get("assignedTechnician");
            MaintenanceRequest updated = maintenanceService.updateStatus(id, status, adminNotes, assignedTechnician);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRequest(@PathVariable Long id) {
        try {
            maintenanceService.deleteRequest(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
