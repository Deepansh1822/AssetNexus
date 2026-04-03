package in.sfp.main.controllers;

import in.sfp.main.model.Attendance;
import in.sfp.main.service.AttendanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    @Autowired
    private AttendanceService service;

    @GetMapping("/by-date")
    public List<Attendance> getByDate(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return service.getAttendanceByDate(date);
    }

    @GetMapping("/range")
    public List<Attendance> getByRange(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
                                         @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return service.getAttendanceByRange(start, end);
    }

    @PostMapping("/mark")
    public org.springframework.http.ResponseEntity<?> mark(@RequestParam Long labourerId, 
                             @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                             @RequestParam String status,
                             @RequestParam(required = false) Long siteId) {
        try {
            return org.springframework.http.ResponseEntity.ok(service.markAttendance(labourerId, date, status, siteId));
        } catch (RuntimeException e) {
            return org.springframework.http.ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/payroll")
    public Double getPayroll(@RequestParam Long labourerId, 
                              @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start, 
                              @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return service.calculatePayroll(labourerId, start, end);
    }
}
