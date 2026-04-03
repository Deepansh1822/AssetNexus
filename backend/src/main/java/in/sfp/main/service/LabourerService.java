package in.sfp.main.service;

import in.sfp.main.model.Labourer;
import in.sfp.main.model.LabourerTransferLog;
import in.sfp.main.repo.LabourerRepository;
import in.sfp.main.repo.LabourerTransferLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class LabourerService {

    @Autowired
    private LabourerRepository repository;
    
    @Autowired
    private LabourerTransferLogRepository logRepository;

    @Autowired
    private in.sfp.main.repo.DeploymentHistoryRepository deploymentRepository;

    @Autowired
    private in.sfp.main.repo.PasswordResetTokenRepository tokenRepo;

    @Autowired
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Autowired
    private in.sfp.main.service.EmailService emailService;

    // --- IDENTITY MANAGEMENT ---
    
    @org.springframework.transaction.annotation.Transactional
    public void registerWithInvite(Labourer labourer, String resetLinkBase) {
        // 1. Initial save with temporary password
        if (labourer.getPassword() == null || labourer.getPassword().isEmpty()) {
            labourer.setPassword(passwordEncoder.encode(java.util.UUID.randomUUID().toString()));
        }
        // Force status for staff
        labourer.setStatus("ACTIVE");
        if (labourer.getPersonnelId() == null) labourer.setPersonnelId("PROJ-STAFF-" + java.util.UUID.randomUUID().toString().substring(0,6).toUpperCase());
        
        Labourer saved = repository.save(labourer);

        // 2. Generate Identity Token
        String token = java.util.UUID.randomUUID().toString();
        in.sfp.main.model.PasswordResetToken resetToken = new in.sfp.main.model.PasswordResetToken(token, saved);
        tokenRepo.save(resetToken);

        // 3. Send Invitation Email
        String setupLink = resetLinkBase + "?token=" + token;
        emailService.sendEmployeeInvitationEmail(saved.getEmail(), setupLink, saved.getName());
    }

    @org.springframework.transaction.annotation.Transactional
    public boolean resetPassword(String token, String newPassword) {
        java.util.Optional<in.sfp.main.model.PasswordResetToken> tokenOpt = tokenRepo.findByToken(token);
        if (tokenOpt.isPresent()) {
            in.sfp.main.model.PasswordResetToken resetToken = tokenOpt.get();
            if (!resetToken.isExpired() && resetToken.getLabourer() != null) {
                Labourer personnel = resetToken.getLabourer();
                personnel.setPassword(passwordEncoder.encode(newPassword));
                repository.save(personnel);
                tokenRepo.delete(resetToken);
                return true;
            }
        }
        return false;
    }

    public List<Labourer> getAllLabourers() {
        return repository.findAll().stream()
                .filter(l -> !"DEACTIVATED".equals(l.getStatus()) && !"DISPOSED".equals(l.getStatus()))
                .toList();
    }

    public Labourer save(Labourer labourer) {
        return repository.save(labourer);
    }

    public Labourer registerLabourer(Labourer labourer) {
        // Sanitise Email (Multiple NULLs allowed in Unique constraint, but not multiple "")
        if (labourer.getEmail() != null && labourer.getEmail().trim().isEmpty()) {
            labourer.setEmail(null);
        }

        // Auto-generate ID if empty
        if (labourer.getPersonnelId() == null || labourer.getPersonnelId().isEmpty()) {
            labourer.setPersonnelId("LAB-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase());
        }
        
        // Ensure starting status is AVAILABLE
        labourer.setStatus("AVAILABLE");
        labourer.setCurrentSite(null);
        
        return repository.save(labourer);
    }

    public Optional<Labourer> findByEmail(String email) {
        return repository.findByEmail(email);
    }

    public Optional<Labourer> findById(Long id) {
        return repository.findById(id);
    }
    
    public List<Labourer> findByStatus(String status) {
        return repository.findByStatus(status);
    }

    public List<Labourer> findBySite(String siteName) {
        return repository.findByCurrentSite(siteName);
    }

    public Labourer updateAssignment(Long labourerId, String siteName, String status) {
        Labourer labourer = repository.findById(labourerId)
                .orElseThrow(() -> new RuntimeException("Labourer not found"));
        
        String fromSite = labourer.getCurrentSite();
        String fromStatus = labourer.getStatus();
        
        // 1. Close existing DeploymentHistory if site changed
        if (fromSite != null && !fromSite.equals(siteName)) {
            deploymentRepository.findByLabourerAndEndDateIsNull(labourer)
                .ifPresent(d -> {
                    d.setEndDate(java.time.LocalDate.now());
                    deploymentRepository.save(d);
                });
        }

        // 2. Open new DeploymentHistory if we ARE moving to a site
        if (siteName != null && !siteName.isEmpty() && !siteName.equals(fromSite)) {
             deploymentRepository.save(new in.sfp.main.model.DeploymentHistory(labourer, siteName, java.time.LocalDate.now()));
        }

        labourer.setCurrentSite(siteName);
        labourer.setStatus(status);
        
        Labourer saved = repository.save(labourer);
        
        // Audit log
        logRepository.save(new LabourerTransferLog(saved, fromSite, siteName, fromStatus, status));
        
        return saved;
    }

    public Labourer unassign(Long labourerId) {
        Labourer labourer = repository.findById(labourerId)
                .orElseThrow(() -> new RuntimeException("Labourer not found"));
        
        String fromSite = labourer.getCurrentSite();
        String fromStatus = labourer.getStatus();
        
        // Close existing DeploymentHistory
        deploymentRepository.findByLabourerAndEndDateIsNull(labourer)
            .ifPresent(d -> {
                d.setEndDate(java.time.LocalDate.now());
                deploymentRepository.save(d);
            });

        labourer.setCurrentSite(null);
        labourer.setStatus("AVAILABLE");
        
        Labourer saved = repository.save(labourer);
        
        // Audit log
        logRepository.save(new LabourerTransferLog(saved, fromSite, "NONE", fromStatus, "AVAILABLE"));

        return saved;
    }

    public Labourer dispose(Long labourerId) {
        Labourer labourer = repository.findById(labourerId)
                .orElseThrow(() -> new RuntimeException("Labourer not found"));
        
        String fromSite = labourer.getCurrentSite();
        String fromStatus = labourer.getStatus();
        
        labourer.setCurrentSite(null);
        labourer.setStatus("DISPOSED");
        
        Labourer saved = repository.save(labourer);
        
        // Audit log
        logRepository.save(new LabourerTransferLog(saved, fromSite, "DECOMMISSIONED", fromStatus, "DISPOSED"));

        return saved;
    }
    
    public Labourer deactivate(Long labourerId) {
        Labourer labourer = repository.findById(labourerId)
                .orElseThrow(() -> new RuntimeException("Labourer not found"));
        
        String fromSite = labourer.getCurrentSite();
        String fromStatus = labourer.getStatus();
        
        labourer.setCurrentSite(null);
        labourer.setStatus("DEACTIVATED");
        
        Labourer saved = repository.save(labourer);
        
        // Audit log
        logRepository.save(new LabourerTransferLog(saved, fromSite, "OFFBOARDED", fromStatus, "DEACTIVATED"));

        return saved;
    }
    
    public List<LabourerTransferLog> getMovementHistory() {
        return logRepository.findAllByOrderByTransferTimeDesc();
    }
    
    public List<LabourerTransferLog> getLabourerMovementHistory(Long id) {
        return logRepository.findByLabourerId(id);
    }

    public java.util.Map<String, Object> getDashboardStats() {
        List<Labourer> all = getAllLabourers();
        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        
        stats.put("totalWorkforce", all.size());
        stats.put("poolCount", all.stream().filter(l -> "AVAILABLE".equals(l.getStatus())).count());
        stats.put("activeOnSites", all.stream().filter(l -> l.getCurrentSite() != null && !l.getCurrentSite().isEmpty()).count());
        stats.put("sitesCount", all.stream().map(Labourer::getCurrentSite).filter(s -> s != null && !s.isEmpty()).distinct().count());
        
        // Distribution of trades
        java.util.Map<String, Long> tradeMap = all.stream()
                .collect(java.util.stream.Collectors.groupingBy(Labourer::getTrade, java.util.stream.Collectors.counting()));
        stats.put("tradeDistribution", tradeMap);

        return stats;
    }
}
