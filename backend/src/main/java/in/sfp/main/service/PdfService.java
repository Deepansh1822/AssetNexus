package in.sfp.main.service;

import in.sfp.main.model.Asset;

public interface PdfService {
    byte[] generateAssetIdentityPdf(Asset asset);
}
