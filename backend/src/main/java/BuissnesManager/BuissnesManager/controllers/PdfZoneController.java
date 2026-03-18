package BuissnesManager.BuissnesManager.controllers;

import BuissnesManager.BuissnesManager.entity.ZoneRequest;
import BuissnesManager.BuissnesManager.entity.ZoneResponse;
import BuissnesManager.BuissnesManager.entity.ZoneSelectionneeDeDocument;
import BuissnesManager.BuissnesManager.repository.ZoneSelectionneeDeDocumentRepo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.text.PDFTextStripperByArea;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("api/zones")
public class PdfZoneController {


    @Autowired
    private ZoneSelectionneeDeDocumentRepo zoneSelectionneeDeDocumentRepo ;


    @PostMapping("/save-all")
    public ResponseEntity<?> saveZones(@RequestBody List<ZoneSelectionneeDeDocument> zones) {
        List<ZoneSelectionneeDeDocument> saved = zoneSelectionneeDeDocumentRepo.saveAll(zones);
        return ResponseEntity.ok(saved);
    }
}

