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

    @GetMapping("/all")
    public List<SalarySlip> getAllSlips() {
        return salarySlipRepository.findAllByOrderByGeneratedAtDesc();
    }

    @GetMapping("/{id}")
    public ResponseEntity<SalarySlip> getSlipById(@PathVariable Long id) {
        return salarySlipRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/labourer/{labourerId}")
    public List<SalarySlip> getSlipsByLabourer(@PathVariable Long labourerId) {
        return salarySlipRepository.findByLabourerId(labourerId);
    }
}
