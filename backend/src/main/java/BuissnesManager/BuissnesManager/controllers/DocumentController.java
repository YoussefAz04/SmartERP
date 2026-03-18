package BuissnesManager.BuissnesManager.controllers;

import BuissnesManager.BuissnesManager.entity.CategorieDocument;
import BuissnesManager.BuissnesManager.entity.Document;
import BuissnesManager.BuissnesManager.entity.FileChemin;
import BuissnesManager.BuissnesManager.entity.ZoneSelectionneeDeDocument;
import BuissnesManager.BuissnesManager.service.CloudinaryService;
import BuissnesManager.BuissnesManager.service.DocumentService;
import BuissnesManager.BuissnesManager.service.FileCheminService;

import BuissnesManager.BuissnesManager.service.OcrPdfService;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("document")
public class DocumentController {
    @Autowired
    private DocumentService documentService;
    @Autowired
    private CloudinaryService cloudinaryService ;
    @Autowired
    private FileCheminService fileCheminService ;


//    @PostMapping("add/{categorieId}")
//    public ResponseEntity<?> ajouterDocument(@RequestBody Document document,
//                                                    @PathVariable Long categorieId) {
//        Document saved = documentService.ajouterDocument(document, categorieId);
//        return ResponseEntity.ok(saved);
//    }
    @PostMapping("ajouterCategorieDocument")
    public ResponseEntity<?> ajouterCategorieDocument(@RequestBody CategorieDocument categorieDocument
                                                   ) {
        CategorieDocument saved = documentService.addCategorieDocument(categorieDocument);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("getDocumentById/{id}")
    public ResponseEntity<?> getDocument(@PathVariable Long id) {
        Document document = documentService.getDocumentById(id);
        return ResponseEntity.ok(document);
    }
    @GetMapping("getAllDocument")
    public ResponseEntity<?> getAllDocument() {
        return ResponseEntity.ok(documentService.getAllDocument());
    }

    @DeleteMapping("supprimerDocument/{id}")
    public ResponseEntity<?> supprimerDocument(@PathVariable Long id) {
        documentService.supprimerDocument(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("ajouterZone/{documentId}")
    public ResponseEntity<?> ajouterZone(
                                                                  @RequestBody ZoneSelectionneeDeDocument zone) {
        ZoneSelectionneeDeDocument savedZone = documentService.ajouterZone( zone);
        return ResponseEntity.ok(savedZone);
    }


    @PostMapping(value = "/upload-file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadFile(@RequestPart("file") MultipartFile file) {
        try {

            String urlCloud = cloudinaryService.uploadFile(file);
            FileChemin fileChemin = new FileChemin();
            fileChemin.setUrLPdf(urlCloud);
            return ResponseEntity.ok(fileCheminService.addFileChemin(fileChemin));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/ajouterDocument/{categorieId}")
    public ResponseEntity<Document> ajouterDocument(
            @RequestPart("document") Document document,
            @PathVariable Long categorieId) {
        Document savedDoc = documentService.ajouterDocument(document, categorieId);
        return ResponseEntity.ok(savedDoc);
    }


    @PutMapping("/{idDocument}/affecter-chemin/{idFile}")
    public ResponseEntity<?> affecterChemin(
            @PathVariable Long idDocument,
            @PathVariable Long idFile) {
        try {
            documentService.affectationDeChemin(idFile, idDocument);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
    @GetMapping("/getAllCategorieDocument")
    public ResponseEntity<?> getAllCategorieDocument() {
        return ResponseEntity.ok(documentService.getAllCategorieDocument());
    }
    @PutMapping("/updateDocument/{id}")
    public ResponseEntity<?> updateDocument(@PathVariable Long id, @RequestBody Document document) {
        documentService.update(id, document);
        return ResponseEntity.ok().build();
    }


    @DeleteMapping("deleteDocument/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long id) {
        try {
            documentService.deleteDocument(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/document/{id}/download")
    public ResponseEntity<InputStreamResource> downloadPdf(@PathVariable Long id) throws IOException {
        File pdfFile = documentService.downloadPdfFromUrl(id);
        InputStreamResource resource = new InputStreamResource(new FileInputStream(pdfFile));

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=document.pdf")
                .body(resource);
    }


    @GetMapping("/downloadPdfAndScanner/{id}")
    public ResponseEntity<InputStreamResource> downloadPdfAndScanner(@PathVariable Long id) throws Exception {
        File pdfFile = documentService.downloadPdfFromUrl(id);

//        String text = ocrService.extractTextFromScannedPdf(pdfFile); // si PDF texte


        List<ZoneSelectionneeDeDocument> zones = documentService.getZonesDeDocument(id);
        for (ZoneSelectionneeDeDocument zone : zones) {
            String text = ocrService.extractZoneFromPdf(
                    pdfFile,
                    zone.getX(),
                    zone.getY(),
                    zone.getWidth(),
                    zone.getHeight(),
                    zone.getCanvasWidth(),
                    zone.getCanvasHeight(),
                    zone.getPageNumber()
            );
         System.out.println("Zone " + zone.getType() + " : " + text);
        }

        InputStreamResource resource = new InputStreamResource(new FileInputStream(pdfFile));
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=document.pdf")
                .body(resource);
    }




    private final OcrPdfService ocrService;

    public DocumentController(OcrPdfService ocrService) {
        this.ocrService = ocrService;
    }
    @PostMapping("/addImage")
    public ResponseEntity<String> getImageToString(
            @RequestParam("multipartFile") MultipartFile multipartFile) throws TesseractException {
        return ResponseEntity.ok(ocrService.getImageString(multipartFile));
    }




}
