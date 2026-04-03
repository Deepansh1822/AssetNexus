package in.sfp.main.controllers;

import in.sfp.main.model.Employee;
import in.sfp.main.service.EmployeeService;
import in.sfp.main.service.ExcelImportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;
    
    @Autowired
    private ExcelImportService excelImportService;

    @GetMapping
    public List<Employee> getAllEmployees() {
        return employeeService.getAllEmployees();
    }

    @GetMapping("/branches")
    public List<String> getDistinctBranches() {
        return employeeService.getDistinctBranchNames();
    }

    @GetMapping("/{id}")
    public Employee getEmployeeById(@PathVariable Long id) {
        return employeeService.getEmployeeById(id);
    }

    @GetMapping("/by-role")
    public List<Employee> getByRole(@RequestParam String role) {
        return employeeService.findByUserRole(role);
    }

    @PostMapping
    public Employee createEmployee(@RequestBody Employee employee, 
                                   @RequestParam(required = false, defaultValue = "false") boolean invite,
                                   jakarta.servlet.http.HttpServletRequest request) {
        if (invite) {
            String scheme = request.getScheme();
            String serverName = request.getServerName();
            int serverPort = request.getServerPort();
            String contextPath = request.getContextPath();
            String setupLinkBase = scheme + "://" + serverName + ":" + serverPort + contextPath + "/reset-password.html";
            
            employeeService.registerWithInvite(employee, setupLinkBase);
            return employee; // Note: JPA might not populate everything immediately, but we return the object
        }
        return employeeService.saveEmployee(employee);
    }

    @PutMapping("/{id}")
    public Employee updateEmployee(@PathVariable Long id, @RequestBody Employee employee) {
        employee.setId(id);
        return employeeService.saveEmployee(employee);
    }

    @DeleteMapping("/{id}")
    public void deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
    }

    @GetMapping("/{id}/image")
    public ResponseEntity<byte[]> getEmployeeImage(@PathVariable Long id) {
        Employee employee = employeeService.getEmployeeById(id);
        if (employee != null && employee.getImageData() != null) {
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_PNG) // Or detect from data if possible
                    .body(employee.getImageData());
        }
        return ResponseEntity.<byte[]>notFound().build();
    }
    
    @PostMapping("/{id}/image")
    public ResponseEntity<?> uploadEmployeeImage(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        try {
            Employee employee = employeeService.getEmployeeById(id);
            if (employee == null) return ResponseEntity.notFound().build();
            
            employee.setImageData(file.getBytes());
            employee.setHasImage(true);
            employeeService.saveEmployee(employee);
            return ResponseEntity.ok(Map.of("message", "Profile image synchronized successfully!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Image sync failed: " + e.getMessage()));
        }
    }

    @PostMapping("/bulk-import")
    public ResponseEntity<?> bulkImportEmployees(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Please upload a file"));
            }
            
            String filename = file.getOriginalFilename();
            if (filename == null || !filename.endsWith(".xlsx")) {
                return ResponseEntity.badRequest().body(Map.of("message", "Please upload an Excel file (.xlsx)"));
            }
            
            Map<String, Object> result = excelImportService.importEmployeesFromExcel(file);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Error processing file: " + e.getMessage()));
        }
    }
}
