package in.sfp.main.controllers;

import in.sfp.main.model.Labourer;
import in.sfp.main.service.LabourerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import in.sfp.main.model.Employee;
import in.sfp.main.model.ConstructionSite;
import in.sfp.main.repo.EmployeeRepo;
import in.sfp.main.repo.ConstructionSiteRepository;
import in.sfp.main.repo.LabourerRepository;
import java.util.List;

@RestController
@RequestMapping("/api/labour")
public class LabourerController {

    @Autowired
    private LabourerService service;


    @Autowired
    private EmployeeRepo employeeRepo;

    @Autowired
    private ConstructionSiteRepository siteRepo;


    @GetMapping("/all")
    public List<Labourer> getAll() {
        return service.getAllLabourers();
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Labourer labourer) {
        try {
            Labourer saved = service.registerLabourer(labourer);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/available")
    public List<Labourer> getAvailable() {
        return service.findByStatus("AVAILABLE");
    }

    private String getCurrentUserRole() {
        String email = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        java.util.Optional<Employee> empOpt = employeeRepo.findByEmail(email);
        if (empOpt.isPresent()) return empOpt.get().getUserRole();
        
        // Labourers only have one role: LABOURER
        java.util.Optional<Labourer> labOpt = service.findByEmail(email);
        if (labOpt.isPresent()) return "LABOURER";
        
        return "UNKNOWN";
    }

    private List<String> getAssignedSiteNamesForCurrentUser() {
        String email = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        if ("anonymousUser".equals(email)) return List.of();
        
        java.util.Optional<Employee> empOpt = employeeRepo.findByEmail(email);
        if (empOpt.isPresent() && "SITE_MANAGER".equals(empOpt.get().getUserRole())) {
            return siteRepo.findBySiteManager(empOpt.get()).stream()
                    .map(in.sfp.main.model.ConstructionSite::getName)
                    .toList();
        }
        
        // Labourers can no longer be Site Managers
        return List.of();
    }

    @GetMapping("/by-site")
    public List<Labourer> getBySite(@RequestParam String siteName) {
        String email = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        String role = getCurrentUserRole();
        java.util.Set<String> portfolio = service.getSitePortfolio(email);
        
        // Security Gate:
        if ("SITE_MANAGER".equals(role)) {
            // Manager can ONLY query their own sites
            if (!portfolio.contains(siteName)) return List.of();
        } else if (!"ADMIN".equals(role)) {
            // Other roles see nothing here
            return List.of();
        }
        
        return service.findBySite(siteName);
    }

    @GetMapping("/global-search")
    public List<Labourer> searchAll(@RequestParam String query) {
        return service.globalSearch(query);
    }

    @PostMapping("/{id:[0-9]+}/transfer")
    public Labourer transfer(@PathVariable Long id, 
                            @RequestParam String siteName,
                            @RequestParam(required = false) String shiftingMode,
                            @RequestParam(required = false) Double shiftAllowance,
                            @RequestParam(required = false) Double foodAllowance) {
        return service.updateAssignment(id, siteName, "ACTIVE", shiftingMode, shiftAllowance, foodAllowance);
    }

    @PostMapping("/{id:[0-9]+}/unassign")
    public Labourer unassign(@PathVariable Long id) {
        return service.unassign(id);
    }

    @PostMapping("/{id:[0-9]+}/dispose")
    public Labourer dispose(@PathVariable Long id) {
        return service.dispose(id);
    }

    @PostMapping("/{id:[0-9]+}/deactivate")
    public Labourer deactivate(@PathVariable Long id) {
        return service.deactivate(id);
    }

    @GetMapping("/movement-history")
    public List<in.sfp.main.model.LabourerTransferLog> getHistory() {
        return service.getMovementHistory();
    }

    @PostMapping("/{id:[0-9]+}/image")
    public ResponseEntity<?> uploadImage(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        try {
            Labourer labourer = service.findById(id).orElseThrow(() -> new RuntimeException("Not found"));
            labourer.setImageData(file.getBytes());
            labourer.setHasImage(true);
            service.save(labourer);
            return ResponseEntity.ok().build();
        } catch (IOException e) {
            return ResponseEntity.badRequest().body("Failed to read image: " + e.getMessage());
        }
    }

    @GetMapping("/{id:[0-9]+}/image")
    public ResponseEntity<byte[]> getImage(@PathVariable Long id) {
        Labourer labourer = service.findById(id).orElse(null);
        if (labourer == null || labourer.getImageData() == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(labourer.getImageData());
    }

    @GetMapping("/stats")
    public java.util.Map<String, Object> getStats() {
        return service.getDashboardStats();
    }

    @GetMapping("/{id:[0-9]+}")
    public ResponseEntity<Labourer> getById(@PathVariable Long id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id:[0-9]+}/history")
    public List<in.sfp.main.model.LabourerTransferLog> getLabourerHistory(@PathVariable Long id) {
        return service.getLabourerMovementHistory(id);
    }
}
