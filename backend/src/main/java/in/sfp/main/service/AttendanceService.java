package in.sfp.main.service;

import in.sfp.main.model.Attendance;
import in.sfp.main.model.ConstructionSite;
import in.sfp.main.model.Labourer;
import in.sfp.main.repo.AttendanceRepository;
import in.sfp.main.repo.ConstructionSiteRepository;
import in.sfp.main.repo.LabourerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class AttendanceService {

    @Autowired
    private AttendanceRepository repository;
    
    @Autowired
    private LabourerRepository labourerRepository;
    
    @Autowired
    private ConstructionSiteRepository siteRepository;

    public List<Attendance> getAttendanceByDate(LocalDate date) {
        return repository.findByAttendanceDate(date);
    }

    public List<Attendance> getAttendanceByRange(LocalDate start, LocalDate end) {
        return repository.findByAttendanceDateBetween(start, end);
    }

    public Attendance markAttendance(Long labourerId, LocalDate date, String status, Long siteId) {
        // Validation: Prevent future date marking
        if (date.isAfter(LocalDate.now())) {
            throw new RuntimeException("Cannot mark attendance for future dates.");
        }

        Labourer labourer = labourerRepository.findById(labourerId)
                .orElseThrow(() -> new RuntimeException("Labourer not found"));
        
        ConstructionSite site = (siteId != null) ? 
                siteRepository.findById(siteId).orElseThrow(() -> new RuntimeException("Site not found")) : null;

        // Check existing records for this day (all sites) to prevent over-marking
        List<Attendance> dayRecords = repository.findByLabourerAndAttendanceDate(labourer, date);
        double currentTotal = dayRecords.stream().mapToDouble(r -> getStatusWeight(r.getStatus())).sum();
        
        // Find existing for THIS site if we are updating it
        Attendance attendance = dayRecords.stream()
                .filter(r -> (site == null && r.getSite() == null) || (site != null && r.getSite() != null && r.getSite().getId().equals(siteId)))
                .findFirst()
                .orElse(new Attendance());

        double oldWeight = getStatusWeight(attendance.getStatus());
        double newWeight = getStatusWeight(status);

        if (currentTotal - oldWeight + newWeight > 1.0) {
            throw new RuntimeException("Over-marking error: Labourer already has " + (currentTotal - oldWeight) + " days recorded at other sites for today. Total cannot exceed 1.0.");
        }

        attendance.setLabourer(labourer);
        attendance.setAttendanceDate(date);
        attendance.setStatus(status.toUpperCase());
        attendance.setDailyWageAtTime(labourer.getDailyWage());
        attendance.setSite(site);
        
        return repository.save(attendance);
    }
    
    private Double getStatusWeight(String status) {
        if (status == null) return 0.0;
        switch (status.toUpperCase()) {
            case "PRESENT": 
            case "PAID_LEAVE": 
            case "HOLIDAY": 
                return 1.0;
            case "HALF_DAY": 
                return 0.5;
            default: 
                return 0.0;
        }
    }

    public Double calculatePayroll(Long labourerId, LocalDate start, LocalDate end) {
        Labourer labourer = labourerRepository.findById(labourerId)
                .orElseThrow(() -> new RuntimeException("Labourer not found"));
        
        if ("FIXED".equals(labourer.getPaymentType())) {
            // Salaried/Fixed professionals get their full rate regardless of fluctuations
            return labourer.getDailyWage();
        }

        List<Attendance> records = repository.findByLabourerAndAttendanceDateBetween(labourer, start, end);
        
        return records.stream().mapToDouble(rec -> 
            getStatusWeight(rec.getStatus()) * rec.getDailyWageAtTime()
        ).sum();
    }
}
