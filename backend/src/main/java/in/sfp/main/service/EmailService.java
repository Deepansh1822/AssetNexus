package in.sfp.main.service;

import in.sfp.main.model.Asset;
import in.sfp.main.model.Employee;
import in.sfp.main.model.MaintenanceRequest;

public interface EmailService {
    void sendAssetAssignmentEmail(Employee employee, Asset asset, String notes);
    void sendAssetReturnEmail(Employee employee, Asset asset, String notes);
    void sendAssetDisposalEmail(Employee employee, Asset asset, String reason);
    void sendMaintenanceStatusUpdateEmail(MaintenanceRequest request);
    void sendAssetDetailsEmail(String recipientEmail, Asset asset);
}
