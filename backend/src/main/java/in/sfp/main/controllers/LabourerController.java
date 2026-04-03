package in.sfp.main.controllers;

import in.sfp.main.model.Labourer;
import in.sfp.main.service.LabourerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/labour")
public class LabourerController {

    @Autowired
    private LabourerService service;


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
    public List<Labourer> getPool() {
        return service.findByStatus("AVAILABLE");
    }

    @GetMapping("/by-site")
    public List<Labourer> getBySite(@RequestParam String siteName) {
        return service.findBySite(siteName);
    }

    @PostMapping("/{id}/transfer")
    public Labourer transfer(@PathVariable Long id, @RequestParam String siteName) {
        return service.updateAssignment(id, siteName, "ACTIVE");
    }

    @PostMapping("/{id}/unassign")
    public Labourer unassign(@PathVariable Long id) {
        return service.unassign(id);
    }

    @PostMapping("/{id}/dispose")
    public Labourer dispose(@PathVariable Long id) {
        return service.dispose(id);
    }

    @PostMapping("/{id}/deactivate")
    public Labourer deactivate(@PathVariable Long id) {
        return service.deactivate(id);
    }

    @GetMapping("/movement-history")
    public List<in.sfp.main.model.LabourerTransferLog> getHistory() {
        return service.getMovementHistory();
    }

    @PostMapping("/{id}/image")
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

    @GetMapping("/{id}/image")
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

    @GetMapping("/{id}")
    public ResponseEntity<Labourer> getById(@PathVariable Long id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/history")
    public List<in.sfp.main.model.LabourerTransferLog> getLabourerHistory(@PathVariable Long id) {
        return service.getLabourerMovementHistory(id);
    }
}
