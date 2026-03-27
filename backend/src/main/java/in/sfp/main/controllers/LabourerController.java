package in.sfp.main.controllers;

import in.sfp.main.model.Labourer;
import in.sfp.main.service.LabourerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/labour")
public class LabourerController {

    @Autowired
    private LabourerService service;

    @GetMapping("/{id}")
    public ResponseEntity<Labourer> getById(@PathVariable Long id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

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
}
