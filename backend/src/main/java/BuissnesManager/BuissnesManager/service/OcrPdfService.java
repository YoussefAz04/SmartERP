package BuissnesManager.BuissnesManager.service;


import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class OcrPdfService {

    private final Tesseract tesseract;

    public OcrPdfService(Tesseract tesseract) {
        this.tesseract = tesseract;
    }

    public String getImageString(MultipartFile multipartFile) throws TesseractException {
        try {
            // Lecture directe depuis le flux du fichier, sans l'enregistrer
            File tempFile = File.createTempFile("ocr_", multipartFile.getOriginalFilename());
            multipartFile.transferTo(tempFile);

            String text = tesseract.doOCR(tempFile);

            tempFile.delete();
            return text;

        } catch (IOException e) {
            throw new RuntimeException("Erreur lors du traitement du fichier", e);
        }
    }


    public String extractTextFromScannedPdf(File pdfFile) throws Exception {
        StringBuilder result = new StringBuilder();
        try (PDDocument document = PDDocument.load(pdfFile)) {
            PDFRenderer renderer = new PDFRenderer(document);
            for (int page = 0; page < document.getNumberOfPages(); page++) {
                BufferedImage image = renderer.renderImageWithDPI(page, 300); // haute résolution
                String text = tesseract.doOCR(image);
                result.append(text).append("\n");
            }
        }
        return result.toString();
    }


    public String extractZoneFromPdf(File pdfFile, float x, float y, float width, float height,
                                     float canvasWidth, float canvasHeight,
                                     int pageNumber) throws Exception {

        try (PDDocument document = PDDocument.load(pdfFile)) {
            PDFRenderer renderer = new PDFRenderer(document);

            int totalPages = document.getNumberOfPages();
            int pageIndex = pageNumber - 1;

            if (pageIndex < 0 || pageIndex >= totalPages) {
                throw new IllegalArgumentException("Page " + pageNumber + " invalide, max : " + totalPages);
            }

            float dpi = 300;
            BufferedImage pageImage = renderer.renderImageWithDPI(pageIndex, dpi);

            float scaleX = pageImage.getWidth() / canvasWidth;
            float scaleY = pageImage.getHeight() / canvasHeight;

            int imgX = Math.round(x * scaleX);
            int imgY = Math.round(y * scaleY);
            int imgWidth = Math.round(width * scaleX);
            int imgHeight = Math.round(height * scaleY);

            imgX = Math.max(0, Math.min(imgX, pageImage.getWidth() - 1));
            imgY = Math.max(0, Math.min(imgY, pageImage.getHeight() - 1));
            imgWidth = Math.min(imgWidth, pageImage.getWidth() - imgX);
            imgHeight = Math.min(imgHeight, pageImage.getHeight() - imgY);

            BufferedImage croppedImage = pageImage.getSubimage(imgX, imgY, imgWidth, imgHeight);
            return tesseract.doOCR(croppedImage);
        }
    }

}

