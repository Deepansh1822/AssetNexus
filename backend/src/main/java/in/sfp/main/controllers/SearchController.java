package in.sfp.main.controllers;

import in.sfp.main.dto.SearchResultDTO;
import in.sfp.main.repo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    @Autowired
    private AssetsRepo assetsRepo;

    @Autowired
    private EmployeeRepo employeeRepo;

    @Autowired
    private CategoryRepo categoryRepo;

    @Autowired
    private MaintenanceRequestRepo maintenanceRepo;

    @Autowired
    private LabourerRepository labourerRepo;

    @Autowired
    private ConstructionSiteRepository siteRepo;

    @GetMapping
    public List<SearchResultDTO> search(@RequestParam String q, org.springframework.security.core.Authentication authentication) {
        if (q == null || q.trim().length() < 2) return Collections.emptyList();
        
        String query = q.toLowerCase().trim();
        List<SearchResultDTO> results = new ArrayList<>();
        
        String email = authentication.getName();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        
        in.sfp.main.model.Employee currentEmp = employeeRepo.findByEmail(email).orElse(null);
        List<in.sfp.main.model.Asset> myAssets = (isAdmin || currentEmp == null) ? Collections.emptyList() : 
            assetsRepo.findAll().stream()
                .filter(a -> a.getEmployee() != null && a.getEmployee().getId().equals(currentEmp.getId()))
                .collect(Collectors.toList());

        // --- Search Assets ---
        assetsRepo.findAll().stream()
                .filter(a -> isAdmin || (currentEmp != null && a.getEmployee() != null && a.getEmployee().getId().equals(currentEmp.getId())))
                .filter(a -> (a.getName() != null && a.getName().toLowerCase().contains(query)) || 
                             (a.getAssetTag() != null && a.getAssetTag().toLowerCase().contains(query)))
                .limit(4)
                .forEach(a -> results.add(new SearchResultDTO(
                        "Asset", a.getId(), a.getName(), 
                        "Tag: " + a.getAssetTag() + " | " + a.getStatus(),
                        "/asset-details.html?id=" + a.getId()
                )));

        // --- Search Personnel (Admin, Site Manager, Asset Staff) ---
        employeeRepo.findAll().stream()
                .filter(e -> isAdmin || (currentEmp != null && e.getId().equals(currentEmp.getId())))
                .filter(e -> (e.getName() != null && e.getName().toLowerCase().contains(query)) || 
                             (e.getEmail() != null && e.getEmail().toLowerCase().contains(query)) ||
                             (e.getSystemId() != null && e.getSystemId().toLowerCase().contains(query)))
                .limit(4)
                .forEach(e -> results.add(new SearchResultDTO(
                        "Personnel", e.getId(), e.getName(), 
                        e.getUserRole() + " | " + e.getDepartment(),
                        "/employee-details.html?id=" + e.getId()
                )));

        // --- Search workforce (Labourers) ---
        if (isAdmin || (currentEmp != null && "SITE_MANAGER".equals(currentEmp.getUserRole()))) {
            labourerRepo.findAll().stream()
                .filter(l -> (l.getName() != null && l.getName().toLowerCase().contains(query)) || 
                             (l.getPersonnelId() != null && l.getPersonnelId().toLowerCase().contains(query)) ||
                             (l.getTrade() != null && l.getTrade().toLowerCase().contains(query)))
                .limit(4)
                .forEach(l -> results.add(new SearchResultDTO(
                        "Workforce", l.getId(), l.getName(), 
                        "Labourer | " + l.getTrade() + " | " + (l.getCurrentSite() != null ? l.getCurrentSite() : "Pool"),
                        "/labour-details.html?id=" + l.getId()
                )));
        }

        // --- Search Construction Sites ---
        siteRepo.findAll().stream()
                .filter(s -> (s.getName() != null && s.getName().toLowerCase().contains(query)) || 
                             (s.getSiteCode() != null && s.getSiteCode().toLowerCase().contains(query)) ||
                             (s.getLocation() != null && s.getLocation().toLowerCase().contains(query)))
                .limit(3)
                .forEach(s -> results.add(new SearchResultDTO(
                        "Site", s.getId(), s.getName(), 
                        s.getSiteCode() + " | " + s.getLocation(),
                        "/site-details.html?id=" + s.getId()
                )));

        // --- Search Categories ---
        categoryRepo.findAll().stream()
                .filter(c -> isAdmin || myAssets.stream().anyMatch(a -> a.getCategory() != null && a.getCategory().getId().equals(c.getId())))
                .filter(c -> c.getName() != null && c.getName().toLowerCase().contains(query))
                .limit(3)
                .forEach(c -> results.add(new SearchResultDTO(
                        "Category", c.getId(), c.getName(), 
                        "Asset Category",
                        "/category-details.html?id=" + c.getId()
                )));

        // --- Search Maintenance Requests ---
        maintenanceRepo.findAll().stream()
                .filter(r -> isAdmin || (currentEmp != null && r.getRequestedBy() != null && r.getRequestedBy().getId().equals(currentEmp.getId())))
                .filter(r -> (r.getAsset() != null && r.getAsset().getName() != null && r.getAsset().getName().toLowerCase().contains(query)) ||
                             (r.getIssueDescription() != null && r.getIssueDescription().toLowerCase().contains(query)))
                .limit(3)
                .forEach(r -> {
                    results.add(new SearchResultDTO(
                        "Maintenance", r.getId(), "#" + r.getId() + " - " + (r.getAsset() != null ? r.getAsset().getName() : "N/A"), 
                        (r.getStatus() != null ? r.getStatus().toString() : "PENDING") + " | " + r.getIssueDescription(),
                        "/maintenance-details.html?id=" + r.getId()
                    ));
                });

        return results;
    }
}
