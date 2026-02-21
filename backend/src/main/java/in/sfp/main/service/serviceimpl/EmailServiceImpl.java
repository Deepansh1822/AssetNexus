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

    @Override
    public void sendContactFormEmail(String senderName, String senderEmail, String subject, String priority, String message) {
        try {
            // --- 1. Notification email to admin ---
            MimeMessage adminMail = mailSender.createMimeMessage();
            MimeMessageHelper adminHelper = new MimeMessageHelper(adminMail, true, "UTF-8");
            adminHelper.setFrom(fromEmail);
            adminHelper.setTo("deepanshshakya669@gmail.com");
            adminHelper.setReplyTo(senderEmail);
            adminHelper.setSubject("[AssetNexus Contact] " + subject + " [" + priority.toUpperCase() + "]");

            String priorityColor = switch (priority.toLowerCase()) {
                case "urgent" -> "#ef4444";
                case "high"   -> "#f97316";
                case "medium" -> "#f59e0b";
                default       -> "#10b981";
            };

            // Build HTML via concatenation — avoids % format-specifier conflicts with CSS values
            String receivedAt = java.time.LocalDateTime.now()
                    .format(java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a"));

            String htmlBody =
                "<div style=\"font-family:'Segoe UI',Arial,sans-serif;max-width:600px;margin:0 auto;background:#f8fafc;border-radius:16px;overflow:hidden;border:1px solid #e2e8f0;\">"
                + "<div style=\"background:linear-gradient(135deg,#6366f1,#818cf8);padding:2rem;text-align:center;\">"
                +   "<h1 style=\"color:white;margin:0;font-size:1.5rem;\">&#128236; New Contact Form Submission</h1>"
                +   "<p style=\"color:rgba(255,255,255,0.85);margin:0.5rem 0 0;font-size:0.9rem;\">AssetNexus &mdash; Contact Us Page</p>"
                + "</div>"
                + "<div style=\"padding:2rem;\">"
                +   "<table style=\"width:100%;border-collapse:collapse;font-size:0.9rem;\">"
                +     "<tr>"
                +       "<td style=\"padding:0.6rem 0;color:#64748b;width:130px;font-weight:600;\">From</td>"
                +       "<td style=\"padding:0.6rem 0;color:#1e293b;\">" + senderName + " &lt;<a href=\"mailto:" + senderEmail + "\" style=\"color:#6366f1;\">" + senderEmail + "</a>&gt;</td>"
                +     "</tr>"
                +     "<tr style=\"border-top:1px solid #f1f5f9;\">"
                +       "<td style=\"padding:0.6rem 0;color:#64748b;font-weight:600;\">Subject</td>"
                +       "<td style=\"padding:0.6rem 0;color:#1e293b;\">" + subject + "</td>"
                +     "</tr>"
                +     "<tr style=\"border-top:1px solid #f1f5f9;\">"
                +       "<td style=\"padding:0.6rem 0;color:#64748b;font-weight:600;\">Priority</td>"
                +       "<td style=\"padding:0.6rem 0;\">"
                +         "<span style=\"background:" + priorityColor + "33;color:" + priorityColor + ";padding:0.2rem 0.75rem;border-radius:20px;font-size:0.8rem;font-weight:700;text-transform:uppercase;\">" + priority + "</span>"
                +       "</td>"
                +     "</tr>"
                +     "<tr style=\"border-top:1px solid #f1f5f9;\">"
                +       "<td style=\"padding:0.6rem 0;color:#64748b;font-weight:600;\">Received</td>"
                +       "<td style=\"padding:0.6rem 0;color:#1e293b;\">" + receivedAt + "</td>"
                +     "</tr>"
                +   "</table>"
                +   "<div style=\"margin-top:1.5rem;background:white;border-radius:12px;padding:1.5rem;border:1px solid #e2e8f0;\">"
                +     "<p style=\"margin:0 0 0.5rem;font-weight:700;color:#1e293b;font-size:0.85rem;text-transform:uppercase;letter-spacing:0.05em;\">Message</p>"
                +     "<p style=\"margin:0;color:#475569;line-height:1.7;white-space:pre-wrap;\">" + message + "</p>"
                +   "</div>"
                +   "<div style=\"margin-top:1.5rem;text-align:center;\">"
                +     "<a href=\"mailto:" + senderEmail + "\" style=\"display:inline-block;background:#6366f1;color:white;padding:0.75rem 2rem;border-radius:10px;text-decoration:none;font-weight:600;font-size:0.9rem;\">Reply to " + senderName + "</a>"
                +   "</div>"
                + "</div>"
                + "<div style=\"background:#f1f5f9;padding:1rem 2rem;text-align:center;font-size:0.75rem;color:#94a3b8;\">"
                +   "Sent via AssetNexus Contact Form &bull; "
                +   "<a href=\"https://deepansh-react-portfolio.netlify.app/\" style=\"color:#6366f1;text-decoration:none;\">deepansh-react-portfolio.netlify.app</a>"
                + "</div>"
                + "</div>";

            adminHelper.setText(htmlBody, true);
            mailSender.send(adminMail);

            // --- 2. Auto-reply to the sender ---
            MimeMessage replyMail = mailSender.createMimeMessage();
            MimeMessageHelper replyHelper = new MimeMessageHelper(replyMail, false, "UTF-8");
            replyHelper.setFrom(fromEmail);
            replyHelper.setTo(senderEmail);
            replyHelper.setSubject("We received your message - AssetNexus Support");
            replyHelper.setText(
                "Hi " + senderName + ",\n\n" +
                "Thank you for reaching out to AssetNexus! We have received your message and will get back to you within 24 business hours.\n\n" +
                "Your message details:\n" +
                "  Subject  : " + subject + "\n" +
                "  Priority : " + priority.toUpperCase() + "\n\n" +
                "If your issue is urgent, please reply directly to this email.\n\n" +
                "Regards,\n" +
                "Deepansh Shakya\n" +
                "AssetNexus - Asset Management System\n" +
                "https://deepansh-react-portfolio.netlify.app/",
                false
            );
            mailSender.send(replyMail);

        } catch (Exception e) {
            System.err.println("Failed to send contact form email: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Email sending failed: " + e.getMessage());
        }
    }

}
