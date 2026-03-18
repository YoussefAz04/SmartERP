package BuissnesManager.BuissnesManager.service;

import BuissnesManager.BuissnesManager.entity.*;
import BuissnesManager.BuissnesManager.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

@Service
public class FactureService {

    @Autowired
    private FactureAchatRepo factureAchatRepo ;
    @Autowired
    private FactureDeVenteRepo factureDeVenteRepo ;
    @Autowired
    private DetaisFactureAchatRepo detaisFactureAchatRepo ;
    @Autowired
    private DetaisFactureDeVenteRepo detaisFactureDeVenteRepo ;
    @Autowired
    private DocumentService documentService;
    @Autowired
    private OcrPdfService ocrPdfService ;
    @Autowired
    private DocumentRepo documentRepo ;



    public FactureAchat AjouterFactureAchat(Long idDocument) throws Exception {

        Document document = documentRepo.findById(idDocument)
                .orElseThrow(() -> new FileNotFoundException("Document not found with ID: " + idDocument));

        FactureAchat factureAchat = new FactureAchat();
        DetaisFactureAchat detaisFactureAchat = new DetaisFactureAchat() ;

        if (document.getType().equalsIgnoreCase("FACTURE D'ACHAT")) {
            List<ZoneSelectionneeDeDocument> zones = documentService.getZonesDeDocument(document.getId());
            File pdfFile = documentService.downloadPdfFromUrl(document.getId());
            factureAchat.setCheminFichier(document.getFileChemin().getUrLPdf());

            for (ZoneSelectionneeDeDocument zone : zones) {
               String text = ocrPdfService.extractZoneFromPdf(
                        pdfFile,
                        zone.getX(),
                        zone.getY(),
                        zone.getWidth(),
                        zone.getHeight(),
                        zone.getCanvasWidth(),
                        zone.getCanvasHeight(),
                        zone.getPageNumber()
                );
                if (zone.getType().equalsIgnoreCase("montantTotal")) {
                    factureAchat.setMontantTotal(Double.parseDouble(text));
                } else if (zone.getType().equalsIgnoreCase("montantTTC")) {
                    factureAchat.setMontantTTC(Double.parseDouble(text));
                }else if (zone.getType().equalsIgnoreCase("montantTVA")){
                    factureAchat.setMontantTVA(Double.parseDouble(text.replace(",", ".")));
                }else if (zone.getType().equalsIgnoreCase("dateAchat")){
                    factureAchat.setDateAchat(text);
                }else if (zone.getType().equalsIgnoreCase("remise")){
                    factureAchat.setRemise(Double.parseDouble(text));
                }else if (zone.getType().equalsIgnoreCase("montantHT")){
                    factureAchat.setMontantHT(Double.parseDouble(text));
                }else if (zone.getType().equalsIgnoreCase("quantite")){
                    detaisFactureAchat.setQuantite(Integer.parseInt(text.trim()));
                }else if (zone.getType().equalsIgnoreCase("article")){
                    detaisFactureAchat.setArticle(text);
                }else if (zone.getType().equalsIgnoreCase("tva")){
                    detaisFactureAchat.setTva(Double.parseDouble(text.replace(",", ".")));
                }else if (zone.getType().equalsIgnoreCase("Referanc")){
                    factureAchat.setReferance(text);
                }
            }
        }

        factureAchatRepo.save(factureAchat);
        detaisFactureAchat.setFactureAchat(factureAchat);
        detaisFactureAchatRepo.save(detaisFactureAchat);
        return factureAchat ;
    }

public List<FactureAchat> getAllFactureAchat(){
        return factureAchatRepo.findAll() ;
}
public FactureAchat getById(Long id){
   return  this.factureAchatRepo.findById(id).get();
}

    public FactureAchat updateFactureAchat(Long id, FactureAchat updatedFacture) {
        return factureAchatRepo.findById(id)
                .map(existingFacture -> {
                    existingFacture.setDateAchat(updatedFacture.getDateAchat());
                    existingFacture.setReferance(updatedFacture.getReferance());
                    existingFacture.setMontantTotal(updatedFacture.getMontantTotal());
                    existingFacture.setMontantHT(updatedFacture.getMontantHT());
                    existingFacture.setMontantTVA(updatedFacture.getMontantTVA());
                    existingFacture.setMontantTTC(updatedFacture.getMontantTTC());
                    existingFacture.setRemise(updatedFacture.getRemise());
                    existingFacture.setModePaiement(updatedFacture.getModePaiement());
                    existingFacture.setStatut(updatedFacture.getStatut());
                    existingFacture.setDescription(updatedFacture.getDescription());
                    existingFacture.setCheminFichier(updatedFacture.getCheminFichier());
                    //
                    return factureAchatRepo.save(existingFacture);
                })
                .orElseThrow(() -> new RuntimeException("FactureAchat non trouvée avec id " + id));
    }











public FactureDeVente ajouterFactureDeVente(FactureDeVente factureDeVente){
 return factureDeVenteRepo.save(factureDeVente);
}










}
