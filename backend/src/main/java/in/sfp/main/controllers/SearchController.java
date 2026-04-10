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
        final List<String> managedSites = new ArrayList<>();
        if (!isAdmin && currentEmp != null && "SITE_MANAGER".equals(currentEmp.getUserRole())) {
            siteRepo.findBySiteManager(currentEmp).stream()
                    .map(in.sfp.main.model.ConstructionSite::getName)
                    .forEach(managedSites::add);
        }
        
        // Handle Site Manager from Labourer context if applicable
        if (managedSites.isEmpty() && !isAdmin) {
             labourerRepo.findByEmail(email).ifPresent(l -> {
                 if ("SITE_MANAGER".equals(l.getUserRole())) {
                     siteRepo.findByLabourerManager(l).forEach(s -> managedSites.add(s.getName()));
                 }
             });
        }
        
        // Ensure effectively final variables for lambda use
        final List<String> siteFilter = managedSites;
        final in.sfp.main.model.Employee userContext = currentEmp;

        // --- Search Assets ---
        if (isAdmin || (userContext != null && "EMPLOYEE".equals(userContext.getUserRole()))) {
            assetsRepo.findAll().stream()
                .filter(a -> isAdmin || 
                    (userContext != null && a.getEmployee() != null && a.getEmployee().getId().equals(userContext.getId()))
                )
                .filter(a -> (a.getName() != null && a.getName().toLowerCase().contains(query)) || 
                             (a.getAssetTag() != null && a.getAssetTag().toLowerCase().contains(query)))
                .limit(4)
                .forEach(a -> results.add(new SearchResultDTO(
                        "Asset", a.getId(), a.getName(), 
                        "Tag: " + a.getAssetTag() + " | " + a.getStatus(),
                        "/asset-details.html?id=" + a.getId()
                )));
        }

        // --- Search Personnel ---
        if (isAdmin || !siteFilter.isEmpty()) {
            employeeRepo.findAll().stream()
                .filter(e -> isAdmin || (userContext != null && e.getId().equals(userContext.getId())) ||
                    (!siteFilter.isEmpty() && siteFilter.contains(e.getBranchName()))
                )
                .filter(e -> (e.getName() != null && e.getName().toLowerCase().contains(query)) || 
                             (e.getEmail() != null && e.getEmail().toLowerCase().contains(query)) ||
                             (e.getSystemId() != null && e.getSystemId().toLowerCase().contains(query)))
                .limit(4)
                .forEach(e -> results.add(new SearchResultDTO(
                        "Personnel", e.getId(), e.getName(), 
                        e.getUserRole() + " | " + e.getDepartment(),
                        "/employee-details.html?id=" + e.getId()
                )));
        }

        // --- Search workforce (Labourers) ---
        if (isAdmin || !siteFilter.isEmpty()) {
            labourerRepo.findAll().stream()
                .filter(l -> isAdmin || (!siteFilter.isEmpty() && siteFilter.contains(l.getCurrentSite())))
                .filter(l -> (l.getName() != null && l.getName().toLowerCase().contains(query)) || 
                             (l.getPersonnelId() != null && l.getPersonnelId().toLowerCase().contains(query)) ||
                             (l.getTrade() != null && l.getTrade().toLowerCase().contains(query)))
                .limit(4)
                .forEach(l -> results.add(new SearchResultDTO(
                        "Workforce", l.getId(), l.getName(), 
                        "Labourer | " + l.getTrade() + " | " + (l.getCurrentSite() != null ? l.getCurrentSite() : "Available"),
                        "/labour-details.html?id=" + l.getId()
                )));
        }

        // --- Search Construction Sites ---
        if (isAdmin || !siteFilter.isEmpty()) {
            siteRepo.findAll().stream()
                .filter(s -> isAdmin || (!siteFilter.isEmpty() && siteFilter.contains(s.getName())))
                .filter(s -> (s.getName() != null && s.getName().toLowerCase().contains(query)) || 
                             (s.getSiteCode() != null && s.getSiteCode().toLowerCase().contains(query)) ||
                             (s.getLocation() != null && s.getLocation().toLowerCase().contains(query)))
                .limit(3)
                .forEach(s -> results.add(new SearchResultDTO(
                        "Site", s.getId(), s.getName(), 
                        s.getSiteCode() + " | " + s.getLocation(),
                        "/site-details.html?id=" + s.getId()
                )));
        }

        // --- Search Categories ---
        if (isAdmin) {
            categoryRepo.findAll().stream()
                .filter(c -> c.getName() != null && c.getName().toLowerCase().contains(query))
                .limit(3)
                .forEach(c -> results.add(new SearchResultDTO(
                        "Category", c.getId(), c.getName(), 
                        "Asset Category",
                        "/category-details.html?id=" + c.getId()
                )));
        }

        // --- Search Maintenance Requests ---
        if (isAdmin || (userContext != null && "EMPLOYEE".equals(userContext.getUserRole())) || !siteFilter.isEmpty()) {
            maintenanceRepo.findAll().stream()
                .filter(r -> isAdmin || 
                    (userContext != null && r.getRequestedBy() != null && r.getRequestedBy().getId().equals(userContext.getId())) ||
                    (!siteFilter.isEmpty() && r.getRequestedBy() != null && siteFilter.contains(r.getRequestedBy().getBranchName()))
                )
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
        }

        return results;
    }
}
