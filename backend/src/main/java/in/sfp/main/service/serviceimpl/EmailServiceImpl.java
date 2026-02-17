package in.sfp.main.service.serviceimpl;

import in.sfp.main.model.Asset;
import in.sfp.main.model.Employee;
import in.sfp.main.model.MaintenanceRequest;
import in.sfp.main.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.util.ByteArrayDataSource;


@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Autowired
    private in.sfp.main.service.PdfService pdfService;

    @Override
    public void sendAssetAssignmentEmail(Employee employee, Asset asset, String notes) {
        if (employee.getEmail() == null || employee.getEmail().isEmpty()) return;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(employee.getEmail());
        message.setSubject("Asset Assigned: " + asset.getName());
        message.setText("Dear " + employee.getName() + ",\n\n" +
                "The following asset has been assigned to you:\n" +
                "Asset Name: " + asset.getName() + "\n" +
                "Asset Tag: " + (asset.getAssetTag() != null ? asset.getAssetTag() : "N/A") + "\n" +
                "Assigned Date: " + java.time.LocalDate.now() + "\n" +
                "Notes: " + (notes != null ? notes : "No additional information provided.") + "\n\n" +
                "Please ensure proper care and maintenance of the asset.\n\n" +
                "Regards,\n" +
                "Assets Management System");
        
        try {
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Failed to send assignment email: " + e.getMessage());
        }
    }

    @Override
    public void sendAssetReturnEmail(Employee employee, Asset asset, String notes) {
        if (employee.getEmail() == null || employee.getEmail().isEmpty()) return;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(employee.getEmail());
        message.setSubject("Asset Returned: " + asset.getName());
        message.setText("Dear " + employee.getName() + ",\n\n" +
                "The following asset has been successfully returned:\n" +
                "Asset Name: " + asset.getName() + "\n" +
                "Asset Tag: " + (asset.getAssetTag() != null ? asset.getAssetTag() : "N/A") + "\n" +
                "Return Date: " + java.time.LocalDate.now() + "\n" +
                "Return Log: " + (notes != null ? notes : "N/A") + "\n\n" +
                "Thank you.\n\n" +
                "Regards,\n" +
                "Assets Management System");

        try {
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Failed to send return email: " + e.getMessage());
        }
    }

    @Override
    public void sendAssetDisposalEmail(Employee employee, Asset asset, String reason) {
        if (employee == null || employee.getEmail() == null || employee.getEmail().isEmpty()) return;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(employee.getEmail());
        message.setSubject("Asset Disposed: " + asset.getName());
        message.setText("Dear " + employee.getName() + ",\n\n" +
                "An asset previously assigned to you has been marked as DISPOSED:\n" +
                "Asset Name: " + asset.getName() + "\n" +
                "Asset Tag: " + (asset.getAssetTag() != null ? asset.getAssetTag() : "N/A") + "\n" +
                "Reason: " + (reason != null ? reason : "Obsolete / Beyond repair") + "\n\n" +
                "This asset is no longer in active inventory.\n\n" +
                "Regards,\n" +
                "Assets Management System");

        try {
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Failed to send disposal email: " + e.getMessage());
        }
    }

    @Override
    public void sendMaintenanceStatusUpdateEmail(MaintenanceRequest request) {
        Employee employee = request.getRequestedBy();
        if (employee == null || employee.getEmail() == null || employee.getEmail().isEmpty()) return;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(employee.getEmail());
        message.setSubject("Maintenance Update: #" + request.getId() + " - " + request.getStatus());
        message.setText("Dear " + employee.getName() + ",\n\n" +
                "The status of your maintenance request #" + request.getId() + " has been updated.\n\n" +
                "Asset: " + request.getAsset().getName() + "\n" +
                "New Status: " + request.getStatus().name().replace('_', ' ') + "\n" +
                "Technician: " + (request.getAssignedTechnician() != null ? request.getAssignedTechnician() : "To be assigned") + "\n" +
                "Admin Notes: " + (request.getAdminNotes() != null ? request.getAdminNotes() : "N/A") + "\n\n" +
                "You can view more details on your maintenance dashboard.\n\n" +
                "Regards,\n" +
                "Assets Management System");

        try {
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Failed to send maintenance update email: " + e.getMessage());
        }
    }

    @Override
    public void sendAssetDetailsEmail(String recipientEmail, Asset asset) {
        if (recipientEmail == null || recipientEmail.isEmpty()) return;

        MimeMessage mimeMessage = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
            helper.setFrom(fromEmail);
            helper.setTo(recipientEmail);
            helper.setSubject("Asset Identity: " + asset.getName() + " [" + asset.getAssetTag() + "]");
            
            StringBuilder text = new StringBuilder();
            text.append("The following asset static identification has been shared with you.\n\n");
            text.append("Please find the attached Barcode Identity PDF for the same.\n\n");
            text.append("Asset Details:\n");
            text.append("Name: ").append(asset.getName()).append("\n");
            text.append("Tag: ").append(asset.getAssetTag() != null ? asset.getAssetTag() : "N/A").append("\n");
            
            helper.setText(text.toString());

            // Generate PDF using PdfService
            byte[] pdfBytes = pdfService.generateAssetIdentityPdf(asset);
            if (pdfBytes != null) {
                String assetTag = asset.getAssetTag() != null ? asset.getAssetTag() : "000000";
                helper.addAttachment("Asset_Identity_" + assetTag + ".pdf", new ByteArrayDataSource(pdfBytes, "application/pdf"));
            }

            mailSender.send(mimeMessage);
        } catch (Exception e) {
            System.err.println("Failed to send asset details email with PDF: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
