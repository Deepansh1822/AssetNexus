package in.sfp.main.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "employees")
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false)
    private String role; // Job title

    @Column(nullable = false)
    private String userRole; // ADMIN, EMPLOYEE, or SITE_MANAGER

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String department;

    private String branchName;

    private String companyName;

    @Column(unique = true)
    private String systemId;

    @com.fasterxml.jackson.annotation.JsonProperty(access = com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY)
    @Lob
    @Column(name = "employee_image", columnDefinition = "LONGBLOB")
    private byte[] imageData;

    private boolean hasImage;
    private boolean active = true;
    private String status = "ACTIVE"; // ACTIVE, DISABLED, DISPOSED

    @com.fasterxml.jackson.annotation.JsonProperty("hasImage")
    public boolean isHasImage() {
        return imageData != null && imageData.length > 0;
    }

    public void setHasImage(boolean hasImage) {
        this.hasImage = hasImage;
    }

    // Getters and Setters
    public byte[] getImageData() {
        return imageData;
    }

    public void setImageData(byte[] imageData) {
        this.imageData = imageData;
    }

    @OneToMany(mappedBy = "employee")
    @JsonIgnoreProperties("employee")
    private List<Asset> assets;

    @OneToMany(mappedBy = "employee")
    @JsonIgnoreProperties("employee")
    private List<AssetTracking> assetTracking;

    // Constructors
    public Employee() {
    }

    public Employee(Long id, String name, String email, String phone, String role, String userRole, String password, byte[] imageData, String department, String branchName, String companyName, String systemId) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.userRole = userRole;
        this.password = password;
        this.imageData = imageData;
        this.department = department;
        this.branchName = branchName;
        this.companyName = companyName;
        this.systemId = systemId;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }


    public String getUserRole() {
        return userRole;
    }

    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<Asset> getAssets() {
        return assets;
    }

    public void setAssets(List<Asset> assets) {
        this.assets = assets;
    }

    public List<AssetTracking> getAssetTracking() {
        return assetTracking;
    }

    public void setAssetTracking(List<AssetTracking> assetTracking) {
        this.assetTracking = assetTracking;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getSystemId() {
        return systemId;
    }

    public void setSystemId(String systemId) {
        this.systemId = systemId;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
