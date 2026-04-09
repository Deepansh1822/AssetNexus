package in.sfp.main.controllers;

import in.sfp.main.model.Employee;
import in.sfp.main.model.Labourer;
import in.sfp.main.repo.EmployeeRepo;
import in.sfp.main.repo.ConstructionSiteRepository;
import in.sfp.main.service.EmployeeService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import in.sfp.main.model.ConstructionSite;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final EmployeeService employeeService;
    private final EmployeeRepo employeeRepo;
    private final ConstructionSiteRepository siteRepo;
    private final in.sfp.main.service.LabourerService labourerService;

    public AuthController(EmployeeService employeeService, EmployeeRepo employeeRepo, ConstructionSiteRepository siteRepo, in.sfp.main.service.LabourerService labourerService) {
        this.employeeService = employeeService;
        this.employeeRepo = employeeRepo;
        this.siteRepo = siteRepo;
        this.labourerService = labourerService;
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        if ("anonymousUser".equals(email)) {
            return ResponseEntity.status(401).body(Map.of("message", "Not authenticated"));
        }
        Optional<Employee> employeeOpt = employeeRepo.findByEmail(email);
        
        if (employeeOpt.isPresent()) {
            Employee e = employeeOpt.get();
            Map<String, Object> resp = new HashMap<>();
            resp.put("id", e.getId());
            resp.put("name", e.getName());
            resp.put("email", e.getEmail());
            resp.put("phone", e.getPhone());
            resp.put("role", e.getRole());
            resp.put("department", e.getDepartment());
            resp.put("branchName", e.getBranchName());
            resp.put("userRole", e.getUserRole() != null ? e.getUserRole().toUpperCase() : "UNKNOWN");
            resp.put("hasImage", e.isHasImage());
            resp.put("systemId", e.getSystemId());
            resp.put("source", "EMPLOYEE");

            if ("SITE_MANAGER".equalsIgnoreCase(e.getUserRole())) {
                List<ConstructionSite> sites = siteRepo.findBySiteManager(e);
                resp.put("assignedSites", sites);
                resp.put("assignedSiteNames", sites.stream().map(ConstructionSite::getName).toList());
            }
            return ResponseEntity.ok(resp);
        }

        // Try Labourer table (for Site Managers in project ledger)
        Optional<Labourer> labOpt = labourerService.findByEmail(email);
        if (labOpt.isPresent()) {
            Labourer l = labOpt.get();
            Map<String, Object> resp = new HashMap<>();
            resp.put("id", l.getId());
            resp.put("name", l.getName());
            resp.put("email", l.getEmail());
            resp.put("phone", l.getPhone());
            resp.put("role", l.getTrade());
            resp.put("department", "FIELD OPERATIONS");
            resp.put("branchName", "STATIONED");
            resp.put("userRole", l.getUserRole() != null ? l.getUserRole().toUpperCase() : "UNKNOWN");
            resp.put("hasImage", l.isHasImage());
            resp.put("source", "LABOURER");
            
            // Site Managers from Labourer table might also have assigned sites
            if ("SITE_MANAGER".equalsIgnoreCase(l.getUserRole())) {
                List<ConstructionSite> sites = siteRepo.findByLabourerManager(l);
                resp.put("assignedSites", sites);
                resp.put("assignedSiteNames", sites.stream().map(ConstructionSite::getName).toList());
            }
            
            return ResponseEntity.ok(resp);
        }

        return ResponseEntity.status(401).build();
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request, HttpServletRequest httpRequest) {
        String email = request.get("email");
        if (email == null || email.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email is required"));
        }
        
        String scheme = httpRequest.getScheme();
        String serverName = httpRequest.getServerName();
        int serverPort = httpRequest.getServerPort();
        String contextPath = httpRequest.getContextPath();
        String resetLinkBase = scheme + "://" + serverName + ":" + serverPort + contextPath + "/reset-password.html";
        
        employeeService.createPasswordResetToken(email, resetLinkBase);
        return ResponseEntity.ok(Map.of("message", "If an account exists with this email, a reset link has been sent."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        String newPassword = request.get("password");
        
        if (token == null || newPassword == null || newPassword.length() < 4) {
             return ResponseEntity.badRequest().body(Map.of("message", "Token and valid password required"));
        }
        
        // 1. Check Asset Ledger
        if (employeeService.resetPassword(token, newPassword)) {
            return ResponseEntity.ok(Map.of("message", "Employee password updated."));
        }

        // 2. Check Labour/Project Ledger
        if (labourerService.resetPassword(token, newPassword)) {
            return ResponseEntity.ok(Map.of("message", "Labourer password updated."));
        }

        return ResponseEntity.status(400).body(Map.of("message", "Invalid or expired token"));
    }

    @GetMapping("/check-existence")
    public ResponseEntity<?> checkExistence(@RequestParam("email") String email) {
        Optional<Employee> emp = employeeRepo.findByEmail(email);
        Optional<Labourer> lab = labourerService.findByEmail(email);
        
        Map<String, Object> response = new HashMap<>();
        response.put("existsInEmployee", emp.isPresent());
        response.put("existsInLabourer", lab.isPresent());
        response.put("employeeName", emp.isPresent() ? emp.get().getName() : null);
        response.put("labourerName", lab.isPresent() ? lab.get().getName() : null);
        
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/invite", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> invitePersonnel(
            @RequestParam("name") String name,
            @RequestParam("email") String email,
            @RequestParam("phone") String phone,
            @RequestParam("userRole") String userRole,
            @RequestParam(value = "module", defaultValue = "ASSET") String module,
            @RequestParam(value = "systemId", required = false) String systemId,
            @RequestParam(value = "personnelId", required = false) String personnelId,
            @RequestParam(value = "role", required = false) String role,
            @RequestParam(value = "department", required = false) String department,
            @RequestParam(value = "branchName", required = false) String branchName,
            @RequestParam(value = "companyName", required = false) String companyName,
            @RequestParam(value = "trade", required = false) String trade,
            @RequestParam(value = "dailyWage", required = false) String dailyWage,
            @RequestParam(value = "address", required = false) String address,
            @RequestParam(value = "skillLevel", required = false) String skillLevel,
            @RequestParam(value = "paymentType", required = false) String paymentType,
            @RequestParam(value = "image", required = false) org.springframework.web.multipart.MultipartFile image,
            HttpServletRequest httpRequest) {
        
        String setupLinkBase = httpRequest.getScheme() + "://" + httpRequest.getServerName() + ":" + httpRequest.getServerPort() + httpRequest.getContextPath() + "/reset-password.html";
        
        try {
            if ("LABOUR".equalsIgnoreCase(module)) {
                in.sfp.main.model.Labourer labourer = new in.sfp.main.model.Labourer();
                labourer.setName(name);
                labourer.setEmail(email);
                labourer.setUserRole(userRole);
                labourer.setPhone(phone);
                
                labourer.setTrade(trade != null ? trade : "MANAGEMENT");
                labourer.setSkillLevel(skillLevel != null ? skillLevel : "PRO");
                labourer.setAddress(address != null ? address : "PROJECT_HQ");
                labourer.setPaymentType(paymentType != null ? paymentType : "MONTHLY");

                if (dailyWage == null || dailyWage.trim().isEmpty()) {
                    labourer.setDailyWage(0.0);
                } else {
                    labourer.setDailyWage(Double.parseDouble(dailyWage));
                }
                labourer.setPersonnelId(personnelId != null ? personnelId : "PJ-" + System.currentTimeMillis());
                labourer.setPassword("PENDING_INVITE");
                
                if (image != null && !image.isEmpty()) {
                    labourer.setImageData(image.getBytes());
                    labourer.setHasImage(true);
                }
                
                labourerService.registerWithInvite(labourer, setupLinkBase);
                return ResponseEntity.ok(Map.of("message", "Project staff invited successfully with visual identity."));
            } else {
                Employee employee = new Employee();
                employee.setName(name);
                employee.setEmail(email);
                employee.setUserRole(userRole);
                employee.setPhone(phone);
                
                employee.setRole(role != null ? role : "SYSTEM_USER");
                employee.setDepartment(department != null ? department : "GENERAL_ADMIN");
                employee.setBranchName(branchName != null ? branchName : "HEADQUARTERS");
                employee.setCompanyName(companyName != null ? companyName : "ASSETNEXUS");
                employee.setSystemId(systemId != null ? systemId : "EMP-" + System.currentTimeMillis());
                employee.setPassword("PENDING_INVITE");
                
                if (image != null && !image.isEmpty()) {
                    employee.setImageData(image.getBytes());
                    employee.setHasImage(true);
                }
                
                employeeService.registerWithInvite(employee, setupLinkBase);
                return ResponseEntity.ok(Map.of("message", "Asset staff invited successfully with visual identity."));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Invite failed: " + e.getMessage()));
        }
    }
}
