package BuissnesManager.BuissnesManager.service;

import BuissnesManager.BuissnesManager.entity.CategorieDocument;
import BuissnesManager.BuissnesManager.entity.Document;
import BuissnesManager.BuissnesManager.entity.FileChemin;
import BuissnesManager.BuissnesManager.entity.ZoneSelectionneeDeDocument;
import BuissnesManager.BuissnesManager.repository.CategorieDocumentRepo;
import BuissnesManager.BuissnesManager.repository.DocumentRepo;
import BuissnesManager.BuissnesManager.repository.FileCheminRepo;
import BuissnesManager.BuissnesManager.repository.ZoneSelectionneeDeDocumentRepo;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripperByArea;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.multipart.MultipartFile;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.*;
import java.util.List;

@Service
public class DocumentService {
    @Autowired
    private DocumentRepo documentRepo;
    @Autowired
    private ZoneSelectionneeDeDocumentRepo zoneSelectionneeDeDocumentRepo;
    @Autowired
    private CategorieDocumentRepo categorieDocumentRepo;
    @Autowired
    private CloudinaryService cloudinaryService;
    @Autowired
    private FileCheminRepo fileCheminRepo;
    @Autowired
    private OcrPdfService ocrPdfService;


    public Document ajouterDocument(Document document, Long categorieId) {
        CategorieDocument categorie = categorieDocumentRepo.findById(categorieId)
                .orElseThrow(() -> new RuntimeException("Catégorie introuvable"));
        document.setCategorieDocument(categorie);
        document.setDateAjout(LocalDateTime.now());
        return documentRepo.save(document);
    }

    public List<Document> getAllDocument() {
        return documentRepo.findAll();
    }

    public Document ajouterDocumentCloud(MultipartFile fichier, String nom, String type, Long categorieId) {
        CategorieDocument categorie = categorieDocumentRepo.findById(categorieId)
                .orElseThrow(() -> new RuntimeException("Catégorie introuvable"));

        String urlCloud = cloudinaryService.uploadFile(fichier);

        Document document = new Document();
        document.setNom(nom);
        document.setType(type);
        document.setCategorieDocument(categorie);
        document.setDateAjout(LocalDateTime.now());

        return documentRepo.save(document);
    }


    @Transactional
    public Document getDocumentById(Long id) {
        return documentRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Document introuvable"));
    }

    public void supprimerDocument(Long id) {
        documentRepo.deleteById(id);
    }

    public ZoneSelectionneeDeDocument ajouterZone(ZoneSelectionneeDeDocument zone) {

        return zoneSelectionneeDeDocumentRepo.save(zone);
    }

    public CategorieDocument addCategorieDocument(CategorieDocument categorieDocument) {
        return categorieDocumentRepo.save(categorieDocument);
    }


    public void affectationDeChemin(Long idFile, Long idDocument) {
        Document document = documentRepo.findById(idDocument)
                .orElseThrow(() -> new RuntimeException("Document non trouvé"));
        FileChemin fileChemin = fileCheminRepo.findById(idFile)
                .orElseThrow(() -> new RuntimeException("FileChemin non trouvé"));

        document.setFileChemin(fileChemin);
        documentRepo.save(document);
    }

    public List<CategorieDocument> getAllCategorieDocument() {
        return categorieDocumentRepo.findAll();
    }


    public Document update(Long idDocument, Document document) {
        Document existingDocument = documentRepo.findById(idDocument)
                .orElseThrow(() -> new RuntimeException("Document non trouvé"));

        existingDocument.setNom(document.getNom());
        existingDocument.setType(document.getType());


        return documentRepo.save(existingDocument);
    }


    @Transactional
    public void deleteDocument(Long idDocument) {
        Document existingDocument = documentRepo.findById(idDocument)
                .orElseThrow(() -> new RuntimeException("Document non trouvé"));
        if (existingDocument.getFileChemin() != null) {
            Long idCheminFile = existingDocument.getFileChemin().getId();
            existingDocument.setFileChemin(null);
            documentRepo.save(existingDocument);
            fileCheminRepo.deleteById(idCheminFile);
        }

        documentRepo.deleteById(idDocument);
    }


    public File downloadPdfFromUrl(Long idDocument) throws IOException {
        Document document = documentRepo.findById(idDocument)
                .orElseThrow(() -> new FileNotFoundException("Document not found with ID: " + idDocument));

        String urlString = document.getFileChemin().getUrLPdf();
        if (urlString == null || urlString.trim().isEmpty()) {
            throw new IllegalArgumentException("URL PDF is null or empty for document ID: " + idDocument);
        }

        URL url;
        try {
            url = new URL(urlString);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid URL format: " + urlString, e);
        }

        File tempFile = File.createTempFile("doc_", ".pdf");
        tempFile.deleteOnExit();

        try (InputStream in = url.openStream();
             OutputStream out = new FileOutputStream(tempFile)) {

            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            if (tempFile.exists()) {
                tempFile.delete();
            }
            throw new IOException("Failed to download PDF from URL: " + urlString, e);
        }

        return tempFile;
    }

    public List<ZoneSelectionneeDeDocument> getZonesDeDocument(Long idDocument) throws IOException{
        Document document = documentRepo.findById(idDocument)
                .orElseThrow(() -> new FileNotFoundException("Document not found with ID: " + idDocument));


        return zoneSelectionneeDeDocumentRepo.findAllByTypeDeDocument(document.getType());
    }












}