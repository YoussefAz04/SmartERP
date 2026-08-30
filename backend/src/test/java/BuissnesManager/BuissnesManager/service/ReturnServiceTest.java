package BuissnesManager.BuissnesManager.service;

import BuissnesManager.BuissnesManager.entity.*;
import BuissnesManager.BuissnesManager.repository.*;
import BuissnesManager.BuissnesManager.security.EnterpriseSecurityUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ReturnServiceTest {
    @Mock private ReturnDocumentRepository returnRepository;
    @Mock private ReturnLineRepository returnLineRepository;
    @Mock private CreditNoteRepository creditNoteRepository;
    @Mock private FactureDeVenteRepo salesInvoiceRepository;
    @Mock private FactureAchatRepo purchaseInvoiceRepository;
    @Mock private LigneCommandeVenteRepository salesLineRepository;
    @Mock private LigneCommandeAchatRepository purchaseLineRepository;
    @Mock private StockRepository stockRepository;
    @Mock private StockMouvementRepo stockMovementRepository;
    @Mock private EnterpriseSecurityUtil enterpriseSecurityUtil;
    @Mock private FinancialService financialService;
    @Mock private AuditLogService auditLogService;

    private ReturnService service;
    private ReturnDocument document;
    private Stock stock;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new ReturnService(returnRepository, returnLineRepository, creditNoteRepository,
                salesInvoiceRepository, purchaseInvoiceRepository, salesLineRepository,
                purchaseLineRepository, stockRepository, stockMovementRepository,
                enterpriseSecurityUtil, financialService, auditLogService);

        Entreprise entreprise = new Entreprise();
        entreprise.setId(1L);
        Article article = new Article();
        article.setId(30L);
        article.setNom("Keyboard");
        article.setEntreprise(entreprise);
        stock = new Stock();
        stock.setId(40L);
        stock.setEntreprise(entreprise);
        stock.setArticleAchat(article);
        stock.setQuantiteDisponible(new BigDecimal("10"));
        stock.setQuantiteVendue(new BigDecimal("10"));
        stock.setQuantiteEntree(new BigDecimal("20"));

        CommandeVente order = new CommandeVente();
        order.setId(60L);
        FactureDeVente invoice = new FactureDeVente();
        invoice.setId(50L);
        invoice.setReferance("FV-0050");
        invoice.setEntreprise(entreprise);
        invoice.setCommandeVente(order);
        LigneCommandeVente orderLine = new LigneCommandeVente();
        orderLine.setId(70L);

        ReturnLine line = new ReturnLine();
        line.setArticle(article);
        line.setSalesOrderLine(orderLine);
        line.setQuantity(new BigDecimal("3"));
        line.setUnitPriceHT(new BigDecimal("100"));
        line.setTaxRate(new BigDecimal("19"));
        line.setAmountHT(new BigDecimal("300.00"));
        line.setAmountTotal(new BigDecimal("357.00"));

        document = new ReturnDocument();
        document.setId(80L);
        document.setReference("RET-C-TEST");
        document.setReturnType(ReturnType.CUSTOMER_RETURN);
        document.setStatus(ReturnStatus.DRAFT);
        document.setReturnDate(LocalDate.now());
        document.setReason("Defective item");
        document.setAmountHT(new BigDecimal("300.00"));
        document.setAmountTax(new BigDecimal("57.00"));
        document.setAmountTotal(new BigDecimal("357.00"));
        document.setEntreprise(entreprise);
        document.setSalesInvoice(invoice);
        document.setLines(List.of(line));
        line.setReturnDocument(document);

        when(enterpriseSecurityUtil.getContextEnterpriseId()).thenReturn(1L);
        when(enterpriseSecurityUtil.getCurrentUser()).thenReturn(Optional.empty());
        when(returnRepository.findByIdForUpdate(80L)).thenReturn(Optional.of(document));
        when(returnRepository.save(any(ReturnDocument.class))).thenAnswer(i -> i.getArgument(0));
        when(creditNoteRepository.save(any(CreditNote.class))).thenAnswer(i -> i.getArgument(0));
        when(stockRepository.findByArticleAndEntrepriseForUpdate(30L, 1L)).thenReturn(Optional.of(stock));
    }

    @Test
    void customerReturnRestoresStockAndIssuesSalesCreditNote() {
        var response = service.validate(80L);

        assertEquals(ReturnStatus.VALIDATED, response.status());
        assertEquals(new BigDecimal("13"), stock.getQuantiteDisponible());
        assertEquals(new BigDecimal("7"), stock.getQuantiteVendue());
        ArgumentCaptor<StockMouvement> movement = ArgumentCaptor.forClass(StockMouvement.class);
        verify(stockMovementRepository).save(movement.capture());
        assertEquals(TypeMouvement.RETOUR_CLIENT, movement.getValue().getTypeMouvement());
        assertEquals(3, movement.getValue().getQuantite());
        ArgumentCaptor<CreditNote> credit = ArgumentCaptor.forClass(CreditNote.class);
        verify(creditNoteRepository).save(credit.capture());
        assertEquals(FinancialDocumentType.SALES, credit.getValue().getDocumentType());
        assertEquals(new BigDecimal("357.00"), credit.getValue().getAmountTotal());
        verify(financialService).recalculateInvoice(FinancialDocumentType.SALES, 50L);
    }
}
