package in.sfp.main.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PagesController {

    @GetMapping("/")
    public String index(org.springframework.security.core.Authentication authentication) {
        if (authentication != null) {
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            if (!isAdmin) {
                return "redirect:/assets";
            }
        }
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/assets")
    public String assets() {
        return "assets";
    }

    @GetMapping("/assets/add")
    public String addAsset() {
        return "add-asset";
    }

    @GetMapping("/assets/update")
    public String updateAsset() {
        return "update-asset";
    }

    @GetMapping("/assets/assign")
    public String assignAsset() {
        return "assign-asset";
    }

    @GetMapping("/assets/dispose")
    public String disposeAsset() {
        return "dispose-asset";
    }

    @GetMapping("/categories")
    public String categories() {
        return "categories";
    }

    @GetMapping("/categories/add")
    public String addCategory() {
        return "add-category";
    }

    @GetMapping("/categories/update")
    public String updateCategory() {
        return "update-category";
    }

    @GetMapping("/employees")
    public String employees() {
        return "employees";
    }

    @GetMapping("/employees/add")
    public String addEmployee() {
        return "add-employee";
    }

    @GetMapping("/employees/update")
    public String updateEmployee() {
        return "update-employee";
    }

    @GetMapping("/profile")
    public String profile() {
        return "profile";
    }

    @GetMapping("/reports")
    public String reports() {
        return "reports";
    }

    // Maintenance routes
    @GetMapping("/maintenance/requests")
    public String maintenanceRequests() {
        return "maintenance-requests";
    }

    @GetMapping("/maintenance/create")
    public String createMaintenance() {
        return "create-maintenance";
    }

    @GetMapping("/maintenance/history")
    public String maintenanceHistory() {
        return "maintenance-history";
    }

    @GetMapping("/maintenance/employee")
    public String employeeMaintenance() {
        return "employee-maintenance";
    }

    // New Detail View Routes
    @GetMapping("/assets/view/{id}")
    public String assetDetails() {
        return "asset-details";
    }

    @GetMapping("/employees/view/{id}")
    public String employeeDetails() {
        return "employee-details";
    }

    @GetMapping("/categories/view/{id}")
    public String categoryDetails() {
        return "category-details";
    }
}
