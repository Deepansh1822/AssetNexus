package in.sfp.main.service.serviceimpl;

import in.sfp.main.model.Asset;
import in.sfp.main.service.PdfService;
import org.springframework.stereotype.Service;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.oned.Code128Writer;

import java.io.ByteArrayOutputStream;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

@Service
public class PdfServiceImpl implements PdfService {

    @Override
    public byte[] generateAssetIdentityPdf(Asset asset) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, baos);
            document.open();

            // Add Header
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 24);
            Paragraph title = new Paragraph("ASSET IDENTITY", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(new Paragraph(" ")); // Spacer

            // Generate Barcode Image using ZXing
            String assetTag = asset.getAssetTag() != null ? asset.getAssetTag() : "000000";
            Code128Writer writer = new Code128Writer();
            BitMatrix bitMatrix = writer.encode(assetTag, BarcodeFormat.CODE_128, 500, 200);
            BufferedImage bcImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
            
            ByteArrayOutputStream bcBaos = new ByteArrayOutputStream();
            ImageIO.write(bcImage, "png", bcBaos);
            Image pdfImage = Image.getInstance(bcBaos.toByteArray());
            pdfImage.setAlignment(Element.ALIGN_CENTER);
            pdfImage.scaleToFit(400, 150);
            document.add(pdfImage);

            // Add Tag ID Text
            Font tagFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20);
            Paragraph tagText = new Paragraph("ID: " + assetTag, tagFont);
            tagText.setAlignment(Element.ALIGN_CENTER);
            document.add(tagText);
            
            document.close();
            System.out.println("PDF generation completed successfully.");
            return baos.toByteArray();
        } catch (Exception e) {
            System.err.println("Fatal error in PdfServiceImpl: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
