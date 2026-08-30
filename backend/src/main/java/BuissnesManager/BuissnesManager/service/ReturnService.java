package BuissnesManager.BuissnesManager.service;

import BuissnesManager.BuissnesManager.dto.FinancialDtos.*;
import BuissnesManager.BuissnesManager.entity.*;
import BuissnesManager.BuissnesManager.repository.*;
import BuissnesManager.BuissnesManager.security.EnterpriseSecurityUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class ReturnService {
    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

    private final ReturnDocumentRepository returnRepository;
    private final ReturnLineRepository returnLineRepository;
    private final CreditNoteRepository creditNoteRepository;
    private final FactureDeVenteRepo salesInvoiceRepository;
    private final FactureAchatRepo purchaseInvoiceRepository;
    private final LigneCommandeVenteRepository salesLineRepository;
    private final LigneCommandeAchatRepository purchaseLineRepository;
    private final StockRepository stockRepository;
    private final StockMouvementRepo stockMovementRepository;
    private final EnterpriseSecurityUtil enterpriseSecurityUtil;
    private final FinancialService financialService;
    private final AuditLogService auditLogService;

    public ReturnService(ReturnDocumentRepository returnRepository,
                         ReturnLineRepository returnLineRepository,
                         CreditNoteRepository creditNoteRepository,
                         FactureDeVenteRepo salesInvoiceRepository,
                         FactureAchatRepo purchaseInvoiceRepository,
                         LigneCommandeVenteRepository salesLineRepository,
                         LigneCommandeAchatRepository purchaseLineRepository,
                         StockRepository stockRepository,
                         StockMouvementRepo stockMovementRepository,
                         EnterpriseSecurityUtil enterpriseSecurityUtil,
                         FinancialService financialService,
                         AuditLogService auditLogService) {
        this.returnRepository = returnRepository;
        this.returnLineRepository = returnLineRepository;
        this.creditNoteRepository = creditNoteRepository;
        this.salesInvoiceRepository = salesInvoiceRepository;
        this.purchaseInvoiceRepository = purchaseInvoiceRepository;
        this.salesLineRepository = salesLineRepository;
        this.purchaseLineRepository = purchaseLineRepository;
        this.stockRepository = stockRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.enterpriseSecurityUtil = enterpriseSecurityUtil;
        this.financialService = financialService;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public ReturnResponse createDraft(ReturnRequest request) {
        Long enterpriseId = requireEnterpriseId();
        ReturnDocument document = new ReturnDocument();
        document.setReference(referenceFor(request.returnType()));
        document.setReturnType(request.returnType());
        document.setStatus(ReturnStatus.DRAFT);
        document.setReturnDate(request.returnDate());
        document.setReason(request.reason().trim());
        document.setCreatedBy(enterpriseSecurityUtil.getCurrentUser().orElse(null));

        if (request.returnType() == ReturnType.CUSTOMER_RETURN) {
            FactureDeVente invoice = requireSalesInvoice(request.invoiceId(), enterpriseId);
            if (invoice.getCommandeVente() == null) throw new IllegalArgumentException("Invoice has no sales order");
            document.setSalesInvoice(invoice);
            document.setEntreprise(invoice.getEntreprise());
            document.setLines(buildSalesLines(document, invoice, request.lines()));
        } else {
            FactureAchat invoice = requirePurchaseInvoice(request.invoiceId(), enterpriseId);
            if (invoice.getCommandeAchat() == null) throw new IllegalArgumentException("Invoice has no purchase order");
            document.setPurchaseInvoice(invoice);
            document.setEntreprise(invoice.getEntreprise());
            document.setLines(buildPurchaseLines(document, invoice, request.lines()));
        }
        calculateTotals(document);
        ReturnDocument saved = returnRepository.save(document);
        auditLogService.log("RETURN_DRAFT_CREATED", "Return draft created: " + saved.getReference());
        return toResponse(saved);
    }

    @Transactional
    public ReturnResponse validate(Long id) {
        ReturnDocument document = requireReturnForUpdate(id);
        if (document.getStatus() != ReturnStatus.DRAFT) {
            throw new IllegalStateException("Only a draft return can be validated");
        }
        for (ReturnLine line : document.getLines()) {
            applyStockMovement(document, line);
        }
        document.setStatus(ReturnStatus.VALIDATED);
        ReturnDocument saved = returnRepository.save(document);

        CreditNote creditNote = new CreditNote();
        creditNote.setReference("CN-" + saved.getReference().substring(4));
        creditNote.setDocumentType(saved.getReturnType() == ReturnType.CUSTOMER_RETURN
                ? FinancialDocumentType.SALES : FinancialDocumentType.PURCHASE);
        creditNote.setStatus(CreditNoteStatus.ISSUED);
        creditNote.setIssueDate(LocalDate.now());
        creditNote.setAmountHT(saved.getAmountHT());
        creditNote.setAmountTax(saved.getAmountTax());
        creditNote.setAmountTotal(saved.getAmountTotal());
        creditNote.setReturnDocument(saved);
        creditNote.setEntreprise(saved.getEntreprise());
        CreditNote issued = creditNoteRepository.save(creditNote);
        saved.setCreditNote(issued);

        FinancialDocumentType type = issued.getDocumentType();
        Long invoiceId = type == FinancialDocumentType.SALES
                ? saved.getSalesInvoice().getId() : saved.getPurchaseInvoice().getId();
        financialService.recalculateInvoice(type, invoiceId);
        auditLogService.log("RETURN_VALIDATED", "Return validated: " + saved.getReference()
                + ", credit note=" + issued.getReference() + ", amount=" + issued.getAmountTotal());
        return toResponse(saved);
    }

    @Transactional
    public ReturnResponse cancelDraft(Long id) {
        ReturnDocument document = requireReturnForUpdate(id);
        if (document.getStatus() != ReturnStatus.DRAFT) {
            throw new IllegalStateException("A validated return cannot be cancelled");
        }
        document.setStatus(ReturnStatus.CANCELLED);
        ReturnDocument saved = returnRepository.save(document);
        auditLogService.log("RETURN_CANCELLED", "Return draft cancelled: " + saved.getReference());
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ReturnResponse> getReturns() {
        return returnRepository.findByEntrepriseIdOrderByCreatedAtDesc(requireEnterpriseId())
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<EligibleReturnLine> getEligibleLines(ReturnType type, Long invoiceId) {
        Long enterpriseId = requireEnterpriseId();
        if (type == ReturnType.CUSTOMER_RETURN) {
            FactureDeVente invoice = requireSalesInvoice(invoiceId, enterpriseId);
            if (invoice.getCommandeVente() == null) return List.of();
            return salesLineRepository.findByCommandeVenteId(invoice.getCommandeVente().getId()).stream()
                    .map(line -> eligible(line, returnLineRepository.sumActiveBySalesOrderLine(line.getId())))
                    .filter(line -> line.returnableQuantity().compareTo(ZERO) > 0).toList();
        }
        FactureAchat invoice = requirePurchaseInvoice(invoiceId, enterpriseId);
        if (invoice.getCommandeAchat() == null) return List.of();
        return purchaseLineRepository.findByCommandeAchatId(invoice.getCommandeAchat().getId()).stream()
                .map(line -> eligible(line, returnLineRepository.sumActiveByPurchaseOrderLine(line.getId())))
                .filter(line -> line.returnableQuantity().compareTo(ZERO) > 0).toList();
    }

    private List<ReturnLine> buildSalesLines(ReturnDocument document, FactureDeVente invoice,
                                             List<ReturnLineRequest> requests) {
        Set<Long> seen = new HashSet<>();
        List<ReturnLine> result = new ArrayList<>();
        for (ReturnLineRequest request : requests) {
            if (!seen.add(request.orderLineId())) throw new IllegalArgumentException("Duplicate order line");
            LigneCommandeVente orderLine = salesLineRepository.findById(request.orderLineId())
                    .orElseThrow(() -> new NoSuchElementException("Sales order line not found"));
            if (!orderLine.getCommandeVente().getId().equals(invoice.getCommandeVente().getId())) {
                throw new IllegalArgumentException("Order line does not belong to the invoice");
            }
            Article article = orderLine.getArticleVente() != null ? orderLine.getArticleVente().getArticleAchat() : null;
            result.add(buildLine(document, article, orderLine.getQuantite(),
                    returnLineRepository.sumActiveBySalesOrderLine(orderLine.getId()),
                    orderLine.getPrixUnitaireHT(), orderLine.getTva(), request.quantity(), orderLine, null));
        }
        return result;
    }

    private List<ReturnLine> buildPurchaseLines(ReturnDocument document, FactureAchat invoice,
                                                List<ReturnLineRequest> requests) {
        Set<Long> seen = new HashSet<>();
        List<ReturnLine> result = new ArrayList<>();
        for (ReturnLineRequest request : requests) {
            if (!seen.add(request.orderLineId())) throw new IllegalArgumentException("Duplicate order line");
            LigneCommandeAchat orderLine = purchaseLineRepository.findById(request.orderLineId())
                    .orElseThrow(() -> new NoSuchElementException("Purchase order line not found"));
            if (!orderLine.getCommandeAchat().getId().equals(invoice.getCommandeAchat().getId())) {
                throw new IllegalArgumentException("Order line does not belong to the invoice");
            }
            result.add(buildLine(document, orderLine.getArticle(), orderLine.getQuantite(),
                    returnLineRepository.sumActiveByPurchaseOrderLine(orderLine.getId()),
                    orderLine.getPrixUnitaireHT(), orderLine.getTva(), request.quantity(), null, orderLine));
        }
        return result;
    }

    private ReturnLine buildLine(ReturnDocument document, Article article, BigDecimal ordered,
                                 BigDecimal alreadyReturned, BigDecimal unitPrice, BigDecimal taxRate,
                                 BigDecimal requested, LigneCommandeVente salesLine,
                                 LigneCommandeAchat purchaseLine) {
        if (article == null) throw new IllegalArgumentException("Order line has no stock article");
        requireWholePositiveQuantity(requested);
        BigDecimal available = value(ordered).subtract(value(alreadyReturned));
        if (requested.compareTo(available) > 0) {
            throw new IllegalArgumentException("Return quantity exceeds the remaining returnable quantity");
        }
        BigDecimal price = value(unitPrice);
        BigDecimal tax = value(taxRate);
        BigDecimal amountHT = requested.multiply(price).setScale(2, RoundingMode.HALF_UP);
        BigDecimal amountTotal = amountHT.multiply(BigDecimal.ONE.add(tax.movePointLeft(2)))
                .setScale(2, RoundingMode.HALF_UP);
        ReturnLine line = new ReturnLine();
        line.setReturnDocument(document);
        line.setArticle(article);
        line.setSalesOrderLine(salesLine);
        line.setPurchaseOrderLine(purchaseLine);
        line.setQuantity(requested);
        line.setUnitPriceHT(price);
        line.setTaxRate(tax);
        line.setAmountHT(amountHT);
        line.setAmountTotal(amountTotal);
        return line;
    }

    private void applyStockMovement(ReturnDocument document, ReturnLine line) {
        Stock stock = stockRepository.findByArticleAndEntrepriseForUpdate(
                        line.getArticle().getId(), document.getEntreprise().getId())
                .orElseThrow(() -> new IllegalStateException("No stock record for " + line.getArticle().getNom()));
        BigDecimal available = value(stock.getQuantiteDisponible());
        BigDecimal quantity = line.getQuantity();
        TypeMouvement movementType;
        int movementQuantity;

        if (document.getReturnType() == ReturnType.CUSTOMER_RETURN) {
            stock.setQuantiteDisponible(available.add(quantity));
            stock.setQuantiteVendue(value(stock.getQuantiteVendue()).subtract(quantity).max(BigDecimal.ZERO));
            movementType = TypeMouvement.RETOUR_CLIENT;
            movementQuantity = quantity.intValueExact();
        } else {
            if (available.compareTo(quantity) < 0) {
                throw new IllegalStateException("Insufficient available stock for supplier return: " + line.getArticle().getNom());
            }
            stock.setQuantiteDisponible(available.subtract(quantity));
            stock.setQuantiteEntree(value(stock.getQuantiteEntree()).subtract(quantity).max(BigDecimal.ZERO));
            movementType = TypeMouvement.RETOUR_FOURNISSEUR;
            movementQuantity = -quantity.intValueExact();
        }
        stockRepository.save(stock);

        StockMouvement movement = new StockMouvement();
        movement.setTypeMouvement(movementType);
        movement.setQuantite(movementQuantity);
        movement.setReferenceCommande(document.getReference());
        movement.setMotif(document.getReason());
        movement.setDateMouvement(LocalDateTime.now());
        movement.setStock(stock);
        movement.setUtilisateur(enterpriseSecurityUtil.getCurrentUser().orElse(null));
        stockMovementRepository.save(movement);
    }

    private void calculateTotals(ReturnDocument document) {
        BigDecimal ht = document.getLines().stream().map(ReturnLine::getAmountHT).reduce(ZERO, BigDecimal::add);
        BigDecimal total = document.getLines().stream().map(ReturnLine::getAmountTotal).reduce(ZERO, BigDecimal::add);
        document.setAmountHT(ht);
        document.setAmountTotal(total);
        document.setAmountTax(total.subtract(ht));
    }

    private EligibleReturnLine eligible(LigneCommandeVente line, BigDecimal returned) {
        Article article = line.getArticleVente() != null ? line.getArticleVente().getArticleAchat() : null;
        BigDecimal ordered = value(line.getQuantite());
        BigDecimal returnedValue = value(returned);
        return new EligibleReturnLine(line.getId(), article != null ? article.getId() : null,
                article != null ? article.getNom() : "-", ordered, returnedValue,
                ordered.subtract(returnedValue).max(ZERO), value(line.getPrixUnitaireHT()), value(line.getTva()));
    }

    private EligibleReturnLine eligible(LigneCommandeAchat line, BigDecimal returned) {
        BigDecimal ordered = value(line.getQuantite());
        BigDecimal returnedValue = value(returned);
        return new EligibleReturnLine(line.getId(), line.getArticle() != null ? line.getArticle().getId() : null,
                line.getArticle() != null ? line.getArticle().getNom() : "-", ordered, returnedValue,
                ordered.subtract(returnedValue).max(ZERO), value(line.getPrixUnitaireHT()), value(line.getTva()));
    }

    private ReturnResponse toResponse(ReturnDocument document) {
        boolean sales = document.getReturnType() == ReturnType.CUSTOMER_RETURN;
        Long invoiceId = sales ? document.getSalesInvoice().getId() : document.getPurchaseInvoice().getId();
        String invoiceReference = sales ? document.getSalesInvoice().getReferance() : document.getPurchaseInvoice().getReferance();
        List<ReturnLineResponse> lines = document.getLines().stream().map(line -> new ReturnLineResponse(
                line.getId(), sales ? line.getSalesOrderLine().getId() : line.getPurchaseOrderLine().getId(),
                line.getArticle().getId(), line.getArticle().getNom(), line.getQuantity(), line.getUnitPriceHT(),
                line.getTaxRate(), line.getAmountHT(), line.getAmountTotal())).toList();
        return new ReturnResponse(document.getId(), document.getReference(), document.getReturnType(),
                document.getStatus(), document.getReturnDate(), document.getReason(), invoiceId, invoiceReference,
                document.getAmountHT(), document.getAmountTax(), document.getAmountTotal(),
                document.getCreditNote() != null ? document.getCreditNote().getReference() : null,
                lines, document.getCreatedAt());
    }

    private ReturnDocument requireReturnForUpdate(Long id) {
        Long enterpriseId = requireEnterpriseId();
        return returnRepository.findByIdForUpdate(id)
                .filter(r -> r.getEntreprise().getId().equals(enterpriseId))
                .orElseThrow(() -> new NoSuchElementException("Return not found"));
    }

    private FactureDeVente requireSalesInvoice(Long id, Long enterpriseId) {
        return salesInvoiceRepository.findById(id)
                .filter(f -> f.getEntreprise() != null && enterpriseId.equals(f.getEntreprise().getId()))
                .orElseThrow(() -> new NoSuchElementException("Sales invoice not found"));
    }

    private FactureAchat requirePurchaseInvoice(Long id, Long enterpriseId) {
        return purchaseInvoiceRepository.findById(id)
                .filter(f -> f.getEntreprise() != null && enterpriseId.equals(f.getEntreprise().getId()))
                .orElseThrow(() -> new NoSuchElementException("Purchase invoice not found"));
    }

    private void requireWholePositiveQuantity(BigDecimal quantity) {
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0
                || quantity.stripTrailingZeros().scale() > 0) {
            throw new IllegalArgumentException("Return quantity must be a positive whole number");
        }
    }

    private String referenceFor(ReturnType type) {
        return "RET-" + (type == ReturnType.CUSTOMER_RETURN ? "C-" : "S-")
                + LocalDate.now().toString().replace("-", "") + "-"
                + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
    }

    private Long requireEnterpriseId() {
        Long id = enterpriseSecurityUtil.getContextEnterpriseId();
        if (id == null) throw new IllegalStateException("Enterprise context required");
        return id;
    }

    private BigDecimal value(BigDecimal value) {
        return value == null ? ZERO : value;
    }
}
