package in.sfp.main.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import java.util.List;
import in.sfp.main.model.Asset;
import in.sfp.main.service.AssetsService;

@RestController
@RequestMapping("/api/assets")
public class AssetController {

    @Autowired
    private AssetsService assetsService;

    @Autowired
    private in.sfp.main.service.EmailService emailService;

    @Autowired
    private in.sfp.main.service.PdfService pdfService;

    @GetMapping
    public List<Asset> getAllAssets(Authentication authentication) {
        if (authentication == null) {
            return java.util.Collections.emptyList();
        }

        if (authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return assetsService.getAllAssets();
        }
        
        // For regular employees, only show their own assets
        String email = authentication.getName();
        return assetsService.getAllAssets().stream()
                .filter(a -> a.getEmployee() != null && email.equals(a.getEmployee().getEmail()))
                .toList();
    }

    @GetMapping("/{id}")
    public Asset getAssetById(@PathVariable Long id) {
        return assetsService.getAssetById(id);
    }

    @PostMapping
    public Asset createAsset(@RequestBody Asset asset) {
        return assetsService.saveAsset(asset);
    }

    @PutMapping("/{id}")
    public Asset updateAsset(@PathVariable Long id, @RequestBody Asset asset) {
        asset.setId(id);
        return assetsService.saveAsset(asset);
    }

    @DeleteMapping("/{id}")
    public void deleteAsset(@PathVariable Long id) {
        assetsService.deleteAsset(id);
    }

    @PostMapping("/{id}/assign")
    public Asset assignAsset(@PathVariable Long id, @RequestParam Long employeeId,
            @RequestParam(required = false) String notes) {
        return assetsService.assignAsset(id, employeeId, notes);
    }

    @PostMapping("/{id}/return")
    public Asset returnAsset(@PathVariable Long id, @RequestParam(required = false) String notes) {
        return assetsService.returnAsset(id, notes);
    }

    @PostMapping("/{id}/dispose")
    public Asset disposeAsset(@PathVariable Long id,
            @RequestBody(required = false) java.util.Map<String, String> body) {
        String notes = body != null ? body.get("notes") : null;
        return assetsService.disposeAsset(id, notes);
    }

    @GetMapping("/{id}/image")
    public ResponseEntity<byte[]> getAssetImage(@PathVariable Long id) {
        Asset asset = assetsService.getAssetById(id);
        if (asset != null && asset.getAssetImage() != null) {
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .body(asset.getAssetImage());
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/{id}/share")
    public ResponseEntity<String> shareDetails(@PathVariable Long id, @RequestParam String email) {
        Asset asset = assetsService.getAssetById(id);
        if (asset != null) {
            emailService.sendAssetDetailsEmail(email, asset);
            return ResponseEntity.ok("Email sent successfully");
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable Long id) {
        System.out.println("Generating PDF for Asset ID: " + id);
        Asset asset = assetsService.getAssetById(id);
        if (asset != null) {
            try {
                byte[] pdfBytes = pdfService.generateAssetIdentityPdf(asset);
                if (pdfBytes != null) {
                    String tag = asset.getAssetTag() != null ? asset.getAssetTag() : "NA";
                    return ResponseEntity.ok()
                            .header("Content-Disposition", "attachment; filename=\"Asset_Identity_" + tag + ".pdf\"")
                            .contentType(MediaType.APPLICATION_PDF)
                            .body(pdfBytes);
                }
            } catch (Exception e) {
                System.err.println("Error generating PDF in controller: " + e.getMessage());
                e.printStackTrace();
            }
        }
        return ResponseEntity.notFound().build();
    }
}
