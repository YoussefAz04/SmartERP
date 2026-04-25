package BuissnesManager.BuissnesManager.controllers;

import BuissnesManager.BuissnesManager.entity.ZoneSelectionneeDeDocument;
import BuissnesManager.BuissnesManager.repository.ZoneSelectionneeDeDocumentRepo;
import BuissnesManager.BuissnesManager.service.OcrPdfService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/zones")
public class PdfZoneController {


    @Autowired
    private ZoneSelectionneeDeDocumentRepo zoneSelectionneeDeDocumentRepo ;

    @Autowired
    private OcrPdfService ocrPdfService;


    @PostMapping("/save-all")
    public ResponseEntity<?> saveZones(@RequestBody List<ZoneSelectionneeDeDocument> zones) {
        List<ZoneSelectionneeDeDocument> saved = zoneSelectionneeDeDocumentRepo.saveAll(zones);
        return ResponseEntity.ok(saved);
    }

    /**
     * Extract OCR text from the supplied zones drawn on the supplied PDF.
     * Body shape: { "pdfBase64": "...", "zones": [ {type,x,y,width,height,pageNumber,canvasWidth,canvasHeight,...}, ... ] }
     * Response shape: { "results": [ {type, text, pageNumber, success, error?}, ... ] }
     */
    @PostMapping("/extract")
    public ResponseEntity<?> extractZones(@RequestBody Map<String, Object> payload) {
        String pdfBase64 = (String) payload.get("pdfBase64");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> zones = (List<Map<String, Object>>) payload.get("zones");

        Map<String, Object> response = new HashMap<>();
        List<Map<String, Object>> results = new ArrayList<>();

        if (pdfBase64 == null || pdfBase64.isEmpty() || zones == null || zones.isEmpty()) {
            response.put("results", results);
            return ResponseEntity.badRequest().body(response);
        }

        File tempFile = null;
        try {
            // pdfBase64 may be a data URL prefixed string
            String cleanBase64 = pdfBase64;
            int commaIdx = cleanBase64.indexOf(',');
            if (cleanBase64.startsWith("data:") && commaIdx > 0) {
                cleanBase64 = cleanBase64.substring(commaIdx + 1);
            }
            byte[] pdfBytes = Base64.getDecoder().decode(cleanBase64);

            tempFile = File.createTempFile("zone_extract_", ".pdf");
            Files.write(tempFile.toPath(), pdfBytes);

            for (Map<String, Object> zone : zones) {
                Map<String, Object> result = new HashMap<>();
                String type = asString(zone.get("type"));
                int pageNumber = asInt(zone.get("pageNumber"), 1);
                result.put("type", type);
                result.put("zoneType", type); // kept for frontend backward-compat
                result.put("pageNumber", pageNumber);
                try {
                    String text = ocrPdfService.extractZoneFromPdf(
                            tempFile,
                            asFloat(zone.get("x"), 0f),
                            asFloat(zone.get("y"), 0f),
                            asFloat(zone.get("width"), 0f),
                            asFloat(zone.get("height"), 0f),
                            asFloat(zone.get("canvasWidth"), 1f),
                            asFloat(zone.get("canvasHeight"), 1f),
                            pageNumber
                    );
                    String trimmed = text == null ? "" : text.trim();
                    result.put("text", trimmed);
                    result.put("content", trimmed); // kept for frontend backward-compat
                    result.put("success", true);
                } catch (Exception e) {
                    result.put("text", "");
                    result.put("content", "");
                    result.put("success", false);
                    result.put("error", e.getMessage());
                }
                results.add(result);
            }

            response.put("results", results);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("results", results);
            response.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        } finally {
            if (tempFile != null && tempFile.exists()) {
                //noinspection ResultOfMethodCallIgnored
                tempFile.delete();
            }
        }
    }

    private static String asString(Object o) {
        return o == null ? "" : o.toString();
    }

    private static int asInt(Object o, int def) {
        if (o instanceof Number) return ((Number) o).intValue();
        if (o == null) return def;
        try { return Integer.parseInt(o.toString().trim()); } catch (NumberFormatException e) { return def; }
    }

    private static float asFloat(Object o, float def) {
        if (o instanceof Number) return ((Number) o).floatValue();
        if (o == null) return def;
        try { return Float.parseFloat(o.toString().trim()); } catch (NumberFormatException e) { return def; }
    }
}

