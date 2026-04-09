package in.sfp.main.controllers;

import in.sfp.main.model.ConstructionSite;
import in.sfp.main.service.SiteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/sites")
public class SiteController {

    @Autowired
    private SiteService service;

    @GetMapping("/all")
    public List<ConstructionSite> getAll() {
        return service.getAllSites();
    }

    @GetMapping("/{id:[0-9]+}")
    public ResponseEntity<ConstructionSite> getById(@PathVariable Long id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/create")
    public ConstructionSite create(@RequestBody ConstructionSite site) {
        return service.createSite(site);
    }

    @PostMapping("/{id:[0-9]+}/update-progress")
    public ConstructionSite updateProgress(@PathVariable Long id, @RequestParam Integer percentage) {
        return service.updateProgress(id, percentage);
    }

    @PostMapping("/{id:[0-9]+}/replace-manager")
    public ConstructionSite replaceManager(@PathVariable Long id, @RequestParam Long newManagerId) {
        return service.replaceManager(id, newManagerId);
    }

    @GetMapping("/{id:[0-9]+}/history")
    public List<in.sfp.main.model.SiteManagerHistory> getHistory(@PathVariable Long id) {
        return service.getSiteManagerHistory(id);
    }

    @PostMapping("/{id:[0-9]+}/image")
    public ResponseEntity<?> uploadImage(@PathVariable Long id, @RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        try {
            ConstructionSite site = service.findById(id).orElseThrow(() -> new RuntimeException("Not found"));
            site.setSiteImage(file.getBytes());
            site.setHasImage(true);
            service.saveSite(site); // Use the existing service method
            return ResponseEntity.ok().build();
        } catch (java.io.IOException e) {
            return ResponseEntity.badRequest().body("Failed to read image: " + e.getMessage());
        }
    }

    @GetMapping("/{id:[0-9]+}/image")
    public ResponseEntity<byte[]> getImage(@PathVariable Long id) {
        ConstructionSite site = service.findById(id).orElse(null);
        if (site == null || site.getSiteImage() == null) {
            return ResponseEntity.notFound().build();
        }
        
        // Return as image/jpeg (you can make this dynamic if needed)
        return ResponseEntity.ok()
                .contentType(org.springframework.http.MediaType.IMAGE_JPEG)
                .body(site.getSiteImage());
    }
}
