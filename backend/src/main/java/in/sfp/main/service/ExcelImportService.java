package in.sfp.main.service;

import in.sfp.main.model.Employee;
import in.sfp.main.repo.EmployeeRepo;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ExcelImportService {

    private final EmployeeRepo employeeRepo;
    private final PasswordEncoder passwordEncoder;

    public ExcelImportService(EmployeeRepo employeeRepo, PasswordEncoder passwordEncoder) {
        this.employeeRepo = employeeRepo;
        this.passwordEncoder = passwordEncoder;
    }

    public Map<String, Object> importEmployeesFromExcel(MultipartFile file) throws IOException {
        List<Employee> successfulEmployees = new ArrayList<>();
        List<Map<String, String>> errors = new ArrayList<>();
        int rowNumber = 0;

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            // Skip header row
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                rowNumber = i + 1; // Excel rows are 1-indexed
                Row row = sheet.getRow(i);
                
                if (row == null || isRowEmpty(row)) {
                    continue;
                }

                try {
                    Employee employee = parseEmployeeFromRow(row, rowNumber);
                    
                    // Validate employee
                    String validationError = validateEmployee(employee, rowNumber);
                    if (validationError != null) {
                        errors.add(createError(rowNumber, validationError));
                        continue;
                    }

                    // Check for duplicate email
                    if (employeeRepo.findByEmail(employee.getEmail()).isPresent()) {
                        errors.add(createError(rowNumber, "Email already exists: " + employee.getEmail()));
                        continue;
                    }

                    // Encode password
                    employee.setPassword(passwordEncoder.encode(employee.getPassword()));
                    
                    // Save employee
                    Employee saved = employeeRepo.save(employee);
                    successfulEmployees.add(saved);

                } catch (Exception e) {
                    errors.add(createError(rowNumber, "Error processing row: " + e.getMessage()));
                }
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("success", successfulEmployees.size());
        result.put("failed", errors.size());
        result.put("employees", successfulEmployees);
        result.put("errors", errors);
        
        return result;
    }

    private Employee parseEmployeeFromRow(Row row, int rowNumber) {
        Employee employee = new Employee();
        
        // Column mapping:
        // 0: Name, 1: Email, 2: Phone, 3: Role (Job Title), 4: Department, 5: User Role (ADMIN/EMPLOYEE), 6: Password
        employee.setName(getCellValueAsString(row.getCell(0)));
        employee.setEmail(getCellValueAsString(row.getCell(1)));
        employee.setPhone(getCellValueAsString(row.getCell(2)));
        employee.setRole(getCellValueAsString(row.getCell(3)));
        employee.setDepartment(getCellValueAsString(row.getCell(4)));
        
        String userRole = getCellValueAsString(row.getCell(5));
        employee.setUserRole(userRole != null && !userRole.isEmpty() ? userRole.toUpperCase() : "EMPLOYEE");
        
        String password = getCellValueAsString(row.getCell(6));
        employee.setPassword(password != null && !password.isEmpty() ? password : "password123");
        
        return employee;
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    // Convert numeric to string, removing decimal if it's a whole number
                    double numericValue = cell.getNumericCellValue();
                    if (numericValue == (long) numericValue) {
                        return String.valueOf((long) numericValue);
                    } else {
                        return String.valueOf(numericValue);
                    }
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }

    private boolean isRowEmpty(Row row) {
        for (int cellNum = 0; cellNum < 7; cellNum++) {
            Cell cell = row.getCell(cellNum);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                String value = getCellValueAsString(cell);
                if (value != null && !value.trim().isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    private String validateEmployee(Employee employee, int rowNumber) {
        if (employee.getName() == null || employee.getName().trim().isEmpty()) {
            return "Name is required";
        }
        if (employee.getEmail() == null || employee.getEmail().trim().isEmpty()) {
            return "Email is required";
        }
        if (!employee.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            return "Invalid email format";
        }
        if (employee.getPhone() == null || employee.getPhone().trim().isEmpty()) {
            return "Phone is required";
        }
        if (employee.getRole() == null || employee.getRole().trim().isEmpty()) {
            return "Role is required";
        }
        if (employee.getDepartment() == null || employee.getDepartment().trim().isEmpty()) {
            return "Department is required";
        }
        if (!employee.getUserRole().equals("ADMIN") && !employee.getUserRole().equals("EMPLOYEE")) {
            return "User Role must be either ADMIN or EMPLOYEE";
        }
        return null;
    }

    private Map<String, String> createError(int rowNumber, String message) {
        Map<String, String> error = new HashMap<>();
        error.put("row", String.valueOf(rowNumber));
        error.put("message", message);
        return error;
    }
}
