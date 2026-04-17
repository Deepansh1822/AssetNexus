package in.sfp.main.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "labourers")
public class Labourer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String personnelId;

    @Column(nullable = false)
    private String trade;

    @Column(nullable = false)
    private String skillLevel;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false)
    private Double dailyWage;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(unique = true)
    private String email;

    @com.fasterxml.jackson.annotation.JsonIgnore
    private String password;

    @Column(nullable = false)
    private String userRole = "LABOURER"; // ADMIN, SITE_MANAGER, LABOURER

    @Column(nullable = false)
    private String status = "AVAILABLE"; // AVAILABLE, ACTIVE, DISPOSED

    @Column(nullable = false)
    private String paymentType = "DAILY"; // DAILY, FIXED

    private String currentSite; // Nullable if in AVAILABLE

    private String companyName;

    private LocalDate registrationDate = LocalDate.now();

    @Lob
    @Column(name = "image_data", length = 10000000)
    private byte[] imageData;

    private boolean hasImage = false;

    private String documentType; // AADHAR, PAN, VOTER_ID, etc.
    private String documentNumber;
    
    @Lob
    @Column(name = "document_file", length = 10000000)
    private byte[] documentFile;

    public Labourer() {}

    // Getters and Setters
    public String getPaymentType() { return paymentType; }
    public void setPaymentType(String paymentType) { this.paymentType = paymentType; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getUserRole() { return userRole; }
    public void setUserRole(String userRole) { this.userRole = userRole; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }


    public String getPersonnelId() { return personnelId; }
    public void setPersonnelId(String personnelId) { this.personnelId = personnelId; }

    public String getTrade() { return trade; }
    public void setTrade(String trade) { this.trade = trade; }

    public String getSkillLevel() { return skillLevel; }
    public void setSkillLevel(String skillLevel) { this.skillLevel = skillLevel; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public Double getDailyWage() { return dailyWage; }
    public void setDailyWage(Double dailyWage) { this.dailyWage = dailyWage; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCurrentSite() { return currentSite; }
    public void setCurrentSite(String currentSite) { this.currentSite = currentSite; }

    public LocalDate getRegistrationDate() { return registrationDate; }
    public void setRegistrationDate(LocalDate registrationDate) { this.registrationDate = registrationDate; }

    public byte[] getImageData() { return imageData; }
    public void setImageData(byte[] imageData) { this.imageData = imageData; }

    public boolean isHasImage() { return hasImage; }
    public void setHasImage(boolean hasImage) { this.hasImage = hasImage; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getDocumentType() { return documentType; }
    public void setDocumentType(String documentType) { this.documentType = documentType; }

    public String getDocumentNumber() { return documentNumber; }
    public void setDocumentNumber(String documentNumber) { this.documentNumber = documentNumber; }

    public byte[] getDocumentFile() { return documentFile; }
    public void setDocumentFile(byte[] documentFile) { this.documentFile = documentFile; }
}
