package in.sfp.main.controllers;

import in.sfp.main.model.SalarySlip;
import in.sfp.main.model.Labourer;
import in.sfp.main.repo.SalarySlipRepository;
import in.sfp.main.repo.LabourerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/salary-slips")
@CrossOrigin(origins = "*")
public class SalarySlipController {

    @Autowired
    private SalarySlipRepository salarySlipRepository;

    @Autowired
    private LabourerRepository labourerRepository;

    @PostMapping("/create")
    public ResponseEntity<?> createSlip(@RequestBody SalarySlip slip, @RequestParam Long labourerId) {
        Labourer labourer = labourerRepository.findById(labourerId).orElse(null);
        if (labourer == null) return ResponseEntity.badRequest().body("Labourer not found");
        
        // CHECK FOR DUPLICATES
        if (salarySlipRepository.findByLabourerIdAndStartDateAndEndDate(labourerId, slip.getStartDate(), slip.getEndDate()).isPresent()) {
            return ResponseEntity.status(409).body("Payroll record for this personnel and interval already exists in the audit ledger.");
        }

        slip.setLabourer(labourer);
        SalarySlip saved = salarySlipRepository.save(slip);
        return ResponseEntity.ok(saved);
    }

    @Autowired
    private in.sfp.main.repo.EmployeeRepo employeeRepo;

    @Autowired
    private in.sfp.main.repo.ConstructionSiteRepository siteRepo;

    private List<String> getAssignedSiteNamesForCurrentUser() {
        String email = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        if ("anonymousUser".equals(email)) return List.of();
        
        java.util.Optional<in.sfp.main.model.Employee> empOpt = employeeRepo.findByEmail(email);
        if (empOpt.isPresent() && "SITE_MANAGER".equals(empOpt.get().getUserRole())) {
            return siteRepo.findBySiteManager(empOpt.get()).stream()
                    .map(in.sfp.main.model.ConstructionSite::getName)
                    .toList();
        }
        
        java.util.Optional<Labourer> labOpt = labourerRepository.findByEmail(email);
        if (labOpt.isPresent() && "SITE_MANAGER".equals(labOpt.get().getUserRole())) {
             return siteRepo.findByLabourerManager(labOpt.get()).stream()
                    .map(in.sfp.main.model.ConstructionSite::getName)
                    .toList();
        }
        return List.of();
    }

    @GetMapping("/all")
    public List<SalarySlip> getAllSlips() {
        String email = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        if ("anonymousUser".equals(email)) return List.of();

        // 1. Resolve Global Portfolio using Dual-Keys (Name and Code)
        java.util.Set<String> authorizedSites = new java.util.HashSet<>();
        boolean restricted = false;

        // Identity check 1: Employee/Staff Directory
        java.util.Optional<in.sfp.main.model.Employee> empOpt = employeeRepo.findByEmail(email);
        if (empOpt.isPresent()) {
            in.sfp.main.model.Employee e = empOpt.get();
            String role = e.getUserRole().toUpperCase();
            if ("ADMIN".equals(role)) return salarySlipRepository.findAllByOrderByGeneratedAtDesc();
            if ("SITE_MANAGER".equals(role)) {
                restricted = true;
                siteRepo.findBySiteManager(e).forEach(s -> {
                    authorizedSites.add(s.getName());
                    authorizedSites.add(s.getSiteCode());
                });
            }
        }

        // Identity check 2: Regional Project Ledger
        java.util.Optional<Labourer> labOpt = labourerRepository.findByEmail(email);
        if (labOpt.isPresent()) {
            Labourer l = labOpt.get();
            String role = l.getUserRole().toUpperCase();
            if ("SITE_MANAGER".equals(role)) {
                restricted = true;
                siteRepo.findByLabourerManager(l).forEach(s -> {
                    authorizedSites.add(s.getName());
                    authorizedSites.add(s.getSiteCode());
                });
            }
            if ("ADMIN".equals(role)) return salarySlipRepository.findAllByOrderByGeneratedAtDesc();
        }

        List<SalarySlip> all = salarySlipRepository.findAllByOrderByGeneratedAtDesc();
        if (!restricted) return all; // Fallback for Admins or un-assigned staffers

        return all.stream()
                .filter(s -> authorizedSites.contains(s.getSiteName()))
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<SalarySlip> getSlipById(@PathVariable Long id) {
        SalarySlip slip = salarySlipRepository.findById(id).orElse(null);
        if (slip == null) return ResponseEntity.notFound().build();
        
        String email = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        java.util.Set<String> authorizedSites = new java.util.HashSet<>();
        boolean isAdmin = false;
        boolean isManager = false;

        java.util.Optional<in.sfp.main.model.Employee> empOpt = employeeRepo.findByEmail(email);
        if (empOpt.isPresent()) {
            if ("ADMIN".equals(empOpt.get().getUserRole())) isAdmin = true;
            if ("SITE_MANAGER".equals(empOpt.get().getUserRole())) {
                isManager = true;
                 siteRepo.findBySiteManager(empOpt.get()).forEach(s -> {
                    authorizedSites.add(s.getName());
                    authorizedSites.add(s.getSiteCode());
                });
            }
        }

        java.util.Optional<Labourer> labOpt = labourerRepository.findByEmail(email);
        if (labOpt.isPresent() && "SITE_MANAGER".equals(labOpt.get().getUserRole())) {
            isManager = true;
             siteRepo.findByLabourerManager(labOpt.get()).forEach(s -> {
                authorizedSites.add(s.getName());
                authorizedSites.add(s.getSiteCode());
            });
        }

        if (!isAdmin && isManager && !authorizedSites.contains(slip.getSiteName())) {
            return ResponseEntity.status(403).build();
        }
        
        return ResponseEntity.ok(slip);
    }

    @GetMapping("/labourer/{labourerId}")
    public List<SalarySlip> getSlipsByLabourer(@PathVariable Long labourerId) {
        return salarySlipRepository.findByLabourerId(labourerId);
    }
}
