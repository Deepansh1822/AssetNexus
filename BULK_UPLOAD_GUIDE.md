# Employee Bulk Upload Feature

## Overview
The Employee Bulk Upload feature allows administrators to import multiple employees at once using an Excel spreadsheet (.xlsx format). This significantly speeds up the onboarding process when adding many employees to the system.

## How to Use

### Step 1: Access the Bulk Upload Feature
1. Navigate to the **Employees** page
2. Click the **"Bulk Upload"** button (with Excel icon) in the top-right corner of the employee table

### Step 2: Download the Template
1. In the bulk upload modal, click **"Download Sample Template"**
2. This will download a CSV file with sample data that you can open in Excel
3. Save the file as `.xlsx` format after editing

### Step 3: Prepare Your Excel File
Your Excel file must have the following columns **in this exact order**:

| Column # | Column Name | Required | Description | Example |
|----------|-------------|----------|-------------|---------|
| 1 | Name | ✅ Yes | Full name of the employee | John Doe |
| 2 | Email | ✅ Yes | Valid email address (must be unique) | john.doe@company.com |
| 3 | Phone | ✅ Yes | Phone number | 1234567890 |
| 4 | Role | ✅ Yes | Job title/position | Software Engineer |
| 5 | Department | ✅ Yes | Department name | IT |
| 6 | User Role | ✅ Yes | Either "ADMIN" or "EMPLOYEE" | EMPLOYEE |
| 7 | Password | ❌ No | Login password (defaults to "password123" if empty) | mypassword |

**Important Notes:**
- The first row should contain column headers
- All required fields must be filled
- Email addresses must be unique (duplicates will be rejected)
- User Role must be exactly "ADMIN" or "EMPLOYEE" (case-insensitive)
- If password is not provided, it defaults to "password123"

### Step 4: Upload the File
1. Click **"Choose File"** and select your prepared Excel file
2. Click **"Upload & Import"**
3. Wait for the processing to complete

### Step 5: Review Results
After processing, you'll see:
- **Success count**: Number of employees successfully imported
- **Failed count**: Number of rows that failed validation
- **Error details**: Specific errors for each failed row (if any)

## Example Excel Data

```
Name          | Email                    | Phone      | Role              | Department | User Role | Password
John Doe      | john.doe@company.com     | 1234567890 | Software Engineer | IT         | EMPLOYEE  | password123
Jane Smith    | jane.smith@company.com   | 0987654321 | HR Manager        | HR         | ADMIN     | admin123
Bob Johnson   | bob.johnson@company.com  | 5551234567 | Accountant        | Finance    | EMPLOYEE  |
```

## Common Errors and Solutions

| Error | Cause | Solution |
|-------|-------|----------|
| "Email already exists" | Duplicate email in database | Use a unique email address |
| "Name is required" | Empty name field | Fill in the name column |
| "Invalid email format" | Malformed email | Use proper email format (user@domain.com) |
| "User Role must be either ADMIN or EMPLOYEE" | Invalid role value | Use only "ADMIN" or "EMPLOYEE" |
| "Please upload an Excel file (.xlsx)" | Wrong file format | Convert to .xlsx format |

## Technical Details

### Backend Implementation
- **Service**: `ExcelImportService.java`
- **Controller**: `EmployeeController.java` (`/api/employees/bulk-import`)
- **Dependencies**: Apache POI 5.2.5 for Excel processing

### Validation Rules
1. All required fields must be non-empty
2. Email must match regex pattern: `^[A-Za-z0-9+_.-]+@(.+)$`
3. Email must be unique in the database
4. User Role must be "ADMIN" or "EMPLOYEE"
5. Passwords are automatically encrypted using BCrypt

### Security
- Only administrators can access the bulk upload feature
- All passwords are encrypted before storage
- File uploads are validated for correct format
- Each row is validated independently to prevent partial imports from corrupting data

## Tips for Best Results
1. **Start small**: Test with 2-3 employees first
2. **Check duplicates**: Ensure no duplicate emails in your file
3. **Use the template**: Always start with the downloaded template
4. **Save as .xlsx**: Make sure to save in Excel format, not CSV
5. **Review errors**: If some rows fail, fix them and re-upload just those rows
