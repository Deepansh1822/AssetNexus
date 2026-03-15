package in.sfp.main.controllers;

import in.sfp.main.model.Employee;
import in.sfp.main.repo.EmployeeRepo;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final in.sfp.main.service.EmployeeService employeeService;
    private final EmployeeRepo employeeRepo;

    public AuthController(in.sfp.main.service.EmployeeService employeeService, EmployeeRepo employeeRepo) {
        this.employeeService = employeeService;
        this.employeeRepo = employeeRepo;
    }

    @GetMapping("/me")
    public ResponseEntity<Employee> getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<Employee> employee = employeeRepo.findByEmail(email);
        return employee.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @org.springframework.web.bind.annotation.PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@org.springframework.web.bind.annotation.RequestBody java.util.Map<String, String> request, jakarta.servlet.http.HttpServletRequest httpRequest) {
        String email = request.get("email");
        if (email == null || email.isEmpty()) {
            return ResponseEntity.badRequest().body(java.util.Map.of("message", "Email is required"));
        }
        
        // Contextual Reset Link base
        String scheme = httpRequest.getScheme();
        String serverName = httpRequest.getServerName();
        int serverPort = httpRequest.getServerPort();
        String contextPath = httpRequest.getContextPath();
        String resetLinkBase = scheme + "://" + serverName + ":" + serverPort + contextPath + "/reset-password";
        
        employeeService.createPasswordResetToken(email, resetLinkBase);
        
        // We always return OK for security reasons (don't leak which emails exist)
        return ResponseEntity.ok(java.util.Map.of("message", "If an account exists with this email, a reset link has been sent."));
    }

    @org.springframework.web.bind.annotation.PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@org.springframework.web.bind.annotation.RequestBody java.util.Map<String, String> request) {
        String token = request.get("token");
        String newPassword = request.get("password");
        
        if (token == null || newPassword == null || newPassword.length() < 4) {
             return ResponseEntity.badRequest().body(java.util.Map.of("message", "Invalid request or password too short"));
        }
        
        boolean success = employeeService.resetPassword(token, newPassword);
        if (success) {
            return ResponseEntity.ok(java.util.Map.of("message", "Password has been reset successfully."));
        } else {
            return ResponseEntity.status(400).body(java.util.Map.of("message", "Invalid or expired token"));
        }
    }
}
