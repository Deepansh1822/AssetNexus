package in.sfp.main.service;

import in.sfp.main.model.Attendance;
import in.sfp.main.model.Labourer;
import in.sfp.main.repo.AttendanceRepository;
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

    public List<Attendance> getAttendanceByDate(LocalDate date) {
        return repository.findByAttendanceDate(date);
    }

    public List<Attendance> getAttendanceByRange(LocalDate start, LocalDate end) {
        return repository.findByAttendanceDateBetween(start, end);
    }

    public Attendance markAttendance(Long labourerId, LocalDate date, String status, Long siteId) {
        Labourer labourer = labourerRepository.findById(labourerId)
                .orElseThrow(() -> new RuntimeException("Labourer not found"));
        
        Optional<Attendance> existing = repository.findByLabourerAndAttendanceDate(labourer, date);
        Attendance attendance = existing.orElse(new Attendance());
        
        attendance.setLabourer(labourer);
        attendance.setAttendanceDate(date);
        attendance.setStatus(status);
        attendance.setDailyWageAtTime(labourer.getDailyWage());
        
        // Handle Site Linkage (Simplified for UI/Demo)
        // In a full implementation, SiteService would be used.
        
        return repository.save(attendance);
    }

    public Double calculatePayroll(Long labourerId, LocalDate start, LocalDate end) {
        Labourer labourer = labourerRepository.findById(labourerId)
                .orElseThrow(() -> new RuntimeException("Labourer not found"));
        
        List<Attendance> records = repository.findByLabourerAndAttendanceDateBetween(labourer, start, end);
        
        return records.stream().mapToDouble(rec -> {
            if ("PRESENT".equalsIgnoreCase(rec.getStatus())) {
                return rec.getDailyWageAtTime();
            } else if ("HALF_DAY".equalsIgnoreCase(rec.getStatus())) {
                return rec.getDailyWageAtTime() / 2;
            }
            return 0.0;
        }).sum();
    }
}
