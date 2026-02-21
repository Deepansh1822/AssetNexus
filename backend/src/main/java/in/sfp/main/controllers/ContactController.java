package in.sfp.main.controllers;

import in.sfp.main.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/contact")
public class ContactController {

    @Autowired
    private EmailService emailService;

    @PostMapping("/send")
    public ResponseEntity<?> sendContactMessage(@RequestBody Map<String, String> payload) {
        String name     = payload.getOrDefault("name", "").trim();
        String email    = payload.getOrDefault("email", "").trim();
        String subject  = payload.getOrDefault("subject", "General Inquiry").trim();
        String priority = payload.getOrDefault("priority", "medium").trim();
        String message  = payload.getOrDefault("message", "").trim();

        if (name.isEmpty() || email.isEmpty() || message.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Name, email, and message are required."));
        }

        try {
            emailService.sendContactFormEmail(name, email, subject, priority, message);
            return ResponseEntity.ok(Map.of("success", true, "message", "Your message has been sent successfully!"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to send email. Please try again later."));
        }
    }
}
