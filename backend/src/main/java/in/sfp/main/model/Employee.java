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
    private String userRole; // ADMIN or EMPLOYEE

    @Column(nullable = false)
    private String password;

    @com.fasterxml.jackson.annotation.JsonProperty(access = com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY)
    @Lob
    @Column(name = "employee_image", columnDefinition = "LONGBLOB")
    private byte[] employeeImage;

    private boolean hasImage;

    @com.fasterxml.jackson.annotation.JsonProperty("hasImage")
    public boolean isHasImage() {
        return employeeImage != null && employeeImage.length > 0;
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

    public Employee(Long id, String name, String email, String phone, String role, String userRole, String password, byte[] employeeImage) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.userRole = userRole;
        this.password = password;
        this.employeeImage = employeeImage;
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

    public byte[] getEmployeeImage() {
        return employeeImage;
    }

    public void setEmployeeImage(byte[] employeeImage) {
        this.employeeImage = employeeImage;
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
}
