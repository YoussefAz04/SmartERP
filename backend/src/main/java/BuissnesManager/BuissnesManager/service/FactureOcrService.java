package BuissnesManager.BuissnesManager.service;

import BuissnesManager.BuissnesManager.entity.ZoneSelectionneeDeDocument;
import BuissnesManager.BuissnesManager.repository.ZoneSelectionneeDeDocumentRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extracts facture (achat / vente) header fields from an uploaded PDF using
 * the zone templates persisted via the PDF zone selector
 * (table {@code zone_selectionnee_de_document}, column {@code typeDeDocument}).
 *
 * <p>For each saved zone whose {@code typeDeDocument} matches the requested
 * facture type, the corresponding rectangle is OCR'd on the uploaded PDF and
 * stored in the response keyed by the zone {@code type}
 * (e.g. {@code montantHT}, {@code montantTVA}, {@code date}, …).</p>
 */
@Service
public class FactureOcrService {

    public static final String TYPE_FACTURE_ACHAT = "FACTURE_ACHAT";
    public static final String TYPE_FACTURE_VENTE = "FACTURE_VENTE";

    private static final List<DateTimeFormatter> DATE_FORMATS = List.of(
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("d/M/yyyy"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("dd.MM.yyyy")
    );

    private static final Pattern AMOUNT_PATTERN = Pattern.compile(
            "[-+]?\\d{1,3}(?:[\\s\\u00A0.,]\\d{3})*(?:[.,]\\d+)?|[-+]?\\d+(?:[.,]\\d+)?");

    @Autowired
    private ZoneSelectionneeDeDocumentRepo zoneRepo;

    @Autowired
    private OcrPdfService ocrPdfService;

    /**
     * Run OCR on every saved zone for the given facture type and return a map
     * keyed by zone {@code type} (e.g. {@code montantHT} → "1234,56").
     */
    public ExtractionResult extractFromPdf(MultipartFile pdf, String typeDeDocument) throws Exception {
        List<ZoneSelectionneeDeDocument> zones = zoneRepo.findAllByTypeDeDocument(typeDeDocument);
        if (zones.isEmpty()) {
            return new ExtractionResult(new LinkedHashMap<>(), new ArrayList<>(),
                    "Aucun gabarit de zones n'est défini pour " + typeDeDocument
                            + ". Définissez d'abord les zones via le sélecteur de zones PDF.");
        }

        File tempFile = File.createTempFile("facture_ocr_", ".pdf");
        try {
            Files.write(tempFile.toPath(), pdf.getBytes());

            Map<String, String> rawByType = new LinkedHashMap<>();
            List<Map<String, Object>> details = new ArrayList<>();
            for (ZoneSelectionneeDeDocument z : zones) {
                Map<String, Object> detail = new HashMap<>();
                detail.put("type", z.getType());
                detail.put("pageNumber", z.getPageNumber());
                try {
                    String text = ocrPdfService.extractZoneFromPdf(
                            tempFile,
                            z.getX(), z.getY(), z.getWidth(), z.getHeight(),
                            z.getCanvasWidth(), z.getCanvasHeight(),
                            z.getPageNumber()
                    );
                    String clean = text == null ? "" : text.trim();
                    rawByType.putIfAbsent(z.getType(), clean);
                    detail.put("text", clean);
                    detail.put("success", true);
                } catch (Exception e) {
                    detail.put("text", "");
                    detail.put("success", false);
                    detail.put("error", e.getMessage());
                }
                details.add(detail);
            }
            return new ExtractionResult(rawByType, details, null);
        } finally {
            //noinspection ResultOfMethodCallIgnored
            tempFile.delete();
        }
    }

    /** Try to parse a French/European numeric amount, returning {@code null} if nothing usable was found. */
    public static BigDecimal parseAmount(String raw) {
        if (raw == null) return null;
        Matcher m = AMOUNT_PATTERN.matcher(raw);
        if (!m.find()) return null;
        String token = m.group();
        // Normalize: drop spaces / non-breaking spaces, then convert decimal comma to dot.
        // If both '.' and ',' are present, the rightmost separator is treated as the decimal separator
        // and the other one (thousands grouping) is dropped.
        String normalized = token.replace("\u00A0", "").replace(" ", "");
        int lastDot = normalized.lastIndexOf('.');
        int lastComma = normalized.lastIndexOf(',');
        if (lastDot >= 0 && lastComma >= 0) {
            char decimal = lastComma > lastDot ? ',' : '.';
            char grouping = decimal == ',' ? '.' : ',';
            normalized = normalized.replace(String.valueOf(grouping), "");
            if (decimal == ',') {
                normalized = normalized.replace(',', '.');
            }
        } else if (lastComma >= 0) {
            // single ',' → decimal separator
            normalized = normalized.replace(',', '.');
        }
        try {
            return new BigDecimal(normalized);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** Try to parse a date string in any of the common French formats. Returns ISO ({@code yyyy-MM-dd}) or {@code null}. */
    public static String parseDateIso(String raw) {
        if (raw == null) return null;
        // Find the first numeric date-looking substring
        Matcher m = Pattern.compile("\\d{1,4}[/.\\-]\\d{1,2}[/.\\-]\\d{1,4}").matcher(raw);
        if (!m.find()) return null;
        String candidate = m.group();
        for (DateTimeFormatter f : DATE_FORMATS) {
            try {
                LocalDate date = LocalDate.parse(candidate, f);
                return date.toString();
            } catch (Exception ignored) {
                // try next format
            }
        }
        return null;
    }

    /** Lightweight container for extraction results. */
    public static class ExtractionResult {
        private final Map<String, String> rawByType;
        private final List<Map<String, Object>> details;
        private final String warning;

        public ExtractionResult(Map<String, String> rawByType, List<Map<String, Object>> details, String warning) {
            this.rawByType = rawByType;
            this.details = details;
            this.warning = warning;
        }

        public Map<String, String> getRawByType() { return rawByType; }
        public List<Map<String, Object>> getDetails() { return details; }
        public String getWarning() { return warning; }
    }

    /** Map raw extracted strings to a typed payload for facture-achat (DTO-shape map). */
    public Map<String, Object> toFactureAchatPayload(ExtractionResult r) {
        Map<String, String> raw = r.getRawByType();
        Map<String, Object> payload = new LinkedHashMap<>();
        BigDecimal montantHT = parseAmount(raw.get("montantHT"));
        BigDecimal montantTVA = parseAmount(raw.get("montantTVA"));
        BigDecimal montantTTC = parseAmount(raw.get("montantTTC"));
        BigDecimal remise = parseAmount(raw.get("remise"));
        payload.put("referance", lower(raw.get("referanc"), raw.get("referance")));
        payload.put("dateAchat", firstNonBlank(parseDateIso(raw.get("date")), raw.get("date")));
        payload.put("montantHT", montantHT);
        payload.put("montantTVA", montantTVA);
        payload.put("montantTTC", montantTTC);
        if (montantTTC != null) payload.put("montantTotal", montantTTC.doubleValue());
        payload.put("remise", remise);
        payload.put("modePaiement", trim(raw.get("modePaiement")));
        payload.put("statut", trim(raw.get("statut")));
        payload.put("description", trim(raw.get("description")));

        Map<String, Object> ligne = ligneFromRaw(raw);
        if (!ligne.isEmpty()) {
            payload.put("ligne", ligne);
        }
        payload.put("details", r.getDetails());
        if (r.getWarning() != null) payload.put("warning", r.getWarning());
        return payload;
    }

    /** Map raw extracted strings to the legacy facture-de-vente form shape (matches add-facture-de-vente form). */
    public Map<String, Object> toFactureDeVentePayload(ExtractionResult r) {
        Map<String, String> raw = r.getRawByType();
        Map<String, Object> payload = new LinkedHashMap<>();
        BigDecimal montantHT = parseAmount(raw.get("montantHT"));
        BigDecimal montantTVA = parseAmount(raw.get("montantTVA"));
        BigDecimal montantTTC = parseAmount(raw.get("montantTTC"));
        BigDecimal remise = parseAmount(raw.get("remise"));
        payload.put("referance", lower(raw.get("referanc"), raw.get("referance")));
        // The legacy add-facture-de-vente form uses dateAchat as the date control name.
        payload.put("dateAchat", firstNonBlank(parseDateIso(raw.get("date")), raw.get("date")));
        payload.put("montantHT", montantHT);
        payload.put("montantTVA", montantTVA);
        payload.put("montantTTC", montantTTC);
        if (montantTTC != null) payload.put("montantTotal", montantTTC.doubleValue());
        payload.put("remise", remise);
        payload.put("modePaiement", trim(raw.get("modePaiement")));
        payload.put("statut", trim(raw.get("statut")));
        payload.put("description", trim(raw.get("description")));

        Map<String, Object> detail = ligneFromRaw(raw);
        if (!detail.isEmpty()) {
            payload.put("detaisFactureDeVente", detail);
        }
        payload.put("details", r.getDetails());
        if (r.getWarning() != null) payload.put("warning", r.getWarning());
        return payload;
    }

    private static Map<String, Object> ligneFromRaw(Map<String, String> raw) {
        Map<String, Object> ligne = new LinkedHashMap<>();
        BigDecimal qte = parseAmount(raw.get("quantite"));
        BigDecimal puht = parseAmount(raw.get("prixUnitaireHT"));
        BigDecimal tva = parseAmount(raw.get("tva"));
        String article = trim(raw.get("article"));
        if (article != null && !article.isEmpty()) ligne.put("article", article);
        if (qte != null) ligne.put("quantite", qte);
        if (puht != null) ligne.put("prixUnitaireHT", puht);
        if (tva != null) ligne.put("tva", tva);
        return ligne;
    }

    private static String trim(String s) { return s == null ? null : s.trim(); }
    private static String firstNonBlank(String a, String b) {
        if (a != null && !a.trim().isEmpty()) return a;
        if (b != null && !b.trim().isEmpty()) return b.trim();
        return null;
    }
    /** Return the first non-blank value (kept lower-case label intentional for clarity). */
    private static String lower(String a, String b) {
        return firstNonBlank(a, b);
    }

    // referenced to keep Locale import non-unused if any future formatter needs it
    @SuppressWarnings("unused")
    private static final Locale LOCALE = Locale.FRANCE;
}
