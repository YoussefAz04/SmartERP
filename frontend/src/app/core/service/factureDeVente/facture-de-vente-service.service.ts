import { Injectable } from '@angular/core';
import { environment } from '../../../../environments/environment';
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {FactureVente} from "../../../model/FactureDeVente.model";
import {jsPDF} from "jspdf";
import {ResponseDto} from "../../../model/reponseDto.model";
import {EntrepriseService} from "../entreprise/entreprise.service";
import {Entreprise} from "../../../model/entreprise.model";
import autoTable from "jspdf-autotable";
import { NotificationService } from '../../../shared/notification/notification.service';

@Injectable({
  providedIn: 'root'
})
export class FactureDeVenteServiceService {

  private baseUrl = `${environment.apiUrl}/factures-vente`;

  constructor(private http: HttpClient,
              private entrepriseService: EntrepriseService,
    private notification: NotificationService) {
  }

  // Ajouter une facture
  ajouterFacture(facture: FactureVente): Observable<FactureVente> {
    return this.http.post<FactureVente>(`${this.baseUrl}/ajouterFacture`, facture);
  }

  // Récupérer toutes les factures
  getAllFactures(): Observable<FactureVente[]> {
    return this.http.get<[]>(`${this.baseUrl}/getAllFactures`);
  }

  // Récupérer une facture par ID
  getFactureById(id: number): Observable<FactureVente> {
    return this.http.get<FactureVente>(`${this.baseUrl}/getFactureById/${id}`);
  }

  // Mettre à jour une facture
  updateFacture(id: number, facture: FactureVente): Observable<FactureVente> {
    return this.http.put<FactureVente>(`${this.baseUrl}/updateFacture/${id}`, facture);
  }

  deleteFacture(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/deleteFacture/${id}`);
  }

  /**
   * Importer une facture de vente depuis un PDF en utilisant les zones OCR
   * sauvegardées (typeDeDocument = "FACTURE_VENTE"). La réponse correspond à
   * la structure du formulaire d'ajout (dateAchat, montantHT, ...,
   * detaisFactureDeVente).
   */
  importFromPdf(file: File): Observable<Record<string, unknown>> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<Record<string, unknown>>(`${this.baseUrl}/import-pdf`, formData);
  }





  entreprise !: Entreprise;

  generateFactureVentePDF(factureData: FactureVente) {
    const doc = new jsPDF();

    const item = localStorage.getItem('myLSkey');
    let currentUser: ResponseDto | null = null;

    if (item) {
      currentUser = JSON.parse(decodeURIComponent(escape(atob(item))));

      if (currentUser?.entreprise.id) {
        this.entrepriseService.getEntrepriseById(currentUser.entreprise.id).subscribe(data => {
          this.entreprise = data;

          doc.setFontSize(15);
          doc.text(String(this.entreprise.nom), 20, 35);

          doc.setFontSize(10);
          doc.text(String(this.entreprise.email), 20, 42);
          doc.text(String(this.entreprise.telephone), 20, 49);
          doc.text(String(this.entreprise.adresse), 20, 56);
          doc.text(String(this.entreprise.siteWeb), 20, 63);

          this.buildFactureContent(doc, factureData);

          doc.save(`facture-${factureData.referance || 'vente'}.pdf`);
        });
      }
    }
  }


  private buildFactureContent(doc: jsPDF, factureData: FactureVente) {
    if (this.entreprise.caracteristiqueFactureEntreprise?.colorHeaderR && this.entreprise.caracteristiqueFactureEntreprise?.colorHeaderB && this.entreprise.caracteristiqueFactureEntreprise?.colorHeaderG)
    doc.setFillColor(this.entreprise.caracteristiqueFactureEntreprise?.colorHeaderR, this.entreprise.caracteristiqueFactureEntreprise?.colorHeaderG, this.entreprise.caracteristiqueFactureEntreprise?.colorHeaderB);
    doc.rect(0, 0, 210, 20, 'F');


    doc.setFontSize(20);
    doc.setFont( 'bold');
    doc.text("FACTURE DE VENTE", 105, 15, {align: 'center'});




    doc.setFontSize(10);
    doc.setFont('bold', 'normal');
    const invoiceDetails = [
      `N° Facture: ${factureData.referance || "F-2025-001"}`,
      `Date: ${factureData.dateAchat || new Date().toLocaleDateString("fr-FR")}`,
      `Statut: ${factureData.statut || "En attente"}`
    ];

    invoiceDetails.forEach((detail, index) => {
      doc.text(detail, 140, 40 + (index * 7));
    });

    doc.setFontSize(12);
    doc.setFont('bold', 'bold');
    doc.text("FACTURÉ À:", 20, 70);



    doc.setFontSize(10);
    doc.setFont('bold', 'normal');
    const clientInfo = [
      factureData.client?.nom || "Nom du Client",
      factureData.client?.adresse || "Adresse du Client",
      factureData.client?.email || "email@example.com",
      factureData.client?.telephone || "Téléphone"
    ];

    clientInfo.forEach((info, index) => {
      doc.text(info, 20, 80 + (index * 7));
    });

    // Separator line after client info
    doc.setLineWidth(0.5);
    doc.line(20, 110, 190, 110);



    if (factureData.description) {
      doc.setFontSize(10);
      doc.text("Description:", 20, 200);
      const splitDesc = doc.splitTextToSize(factureData.description, 170);
      doc.text(splitDesc, 20, 210);
    }

    const details = Array.isArray(factureData.detaisFactureDeVente)
      ? factureData.detaisFactureDeVente
      : [factureData.detaisFactureDeVente];

    const rows = details.map((d, i) => {
      const qte = d.quantite || 1;
      const prix = d.prixUnitaireHT || 0;
      const tva = d.tva || 20;
      const totalHT = qte * prix;
      const totalTTC = totalHT + totalHT * (tva / 100);
      return [
        d.article || `Article ${i + 1}`,
        qte.toString(),
        prix.toFixed(2) + " DT",
        `${tva}%`,
        totalTTC.toFixed(2) + " DT",
      ];
    });


    autoTable(doc, {
      startY: factureData.description ? 140 : 110,
      head: [["Article", "Qté", "Prix HT", "TVA %", "Total TTC"]],
      body: rows,
      theme: "grid",
      headStyles: {

        fillColor: [this.entreprise.caracteristiqueFactureEntreprise?.fillColorR || 0, this.entreprise.caracteristiqueFactureEntreprise?.fillColorG || 0, this.entreprise.caracteristiqueFactureEntreprise?.fillColorB || 0], textColor: [this.entreprise.caracteristiqueFactureEntreprise?.textColorR || 0, this.entreprise.caracteristiqueFactureEntreprise?.textColorG || 0, this.entreprise.caracteristiqueFactureEntreprise?.textColorB || 0] },
      styles: { fontSize: 10 },
    });

    const totalHT = details.reduce((sum, d) => sum + (d.quantite || 1) * (d.prixUnitaireHT || 0), 0);
    const totalTVA = details.reduce((sum, d) => sum + ((d.quantite || 1) * (d.prixUnitaireHT || 0)) * ((d.tva || 20) / 100), 0);
    const totalTTC = totalHT + totalTVA;

    const finalY = (doc as any).lastAutoTable.finalY + 10;

    doc.setFontSize(10);
    doc.text("Sous-total HT:", 140, finalY);
    doc.text(totalHT.toFixed(2) + " DT", 180, finalY, { align: "right" });

    doc.text("TVA:", 140, finalY + 7);
    doc.text(totalTVA.toFixed(2) + " DT", 180, finalY + 7, { align: "right" });

    doc.setFontSize(12);
    doc.text("TOTAL TTC:", 140, finalY + 14);
    doc.text(totalTTC.toFixed(2) + " DT", 180, finalY + 14, { align: "right" });



    if (this.entreprise.caracteristiqueFactureEntreprise?.colorFooterR && this.entreprise.caracteristiqueFactureEntreprise?.colorFooterG && this.entreprise.caracteristiqueFactureEntreprise?.colorFooterB)
      doc.setFillColor(this.entreprise.caracteristiqueFactureEntreprise?.colorFooterR, this.entreprise.caracteristiqueFactureEntreprise?.colorFooterG,  this.entreprise.caracteristiqueFactureEntreprise?.colorFooterB);
    doc.rect(0, 275, 210, 20, 'F'); // rectangle en bas
    doc.setTextColor(255, 255, 255);
    doc.setFontSize(8);
    doc.text("SIRET: 123 456 789 00012 - TVA: FR12345678901", 20, 285);
    doc.text(this.entreprise.caracteristiqueFactureEntreprise?.messageFooter || "Merci pour votre confiance !", 140, 285);



  }




  sendEmailFactureVente(factureData: FactureVente) {
    const item = localStorage.getItem('myLSkey');
    if (!item) return;

    const currentUser: ResponseDto = JSON.parse(decodeURIComponent(escape(atob(item))));

    if (currentUser?.entreprise.id) {
      this.entrepriseService.getEntrepriseById(currentUser.entreprise.id).subscribe(data => {
        this.entreprise = data;

        const doc = new jsPDF();

        doc.setFontSize(15);
        doc.text(String(this.entreprise.nom), 20, 35);

        doc.setFontSize(10);
        doc.text(String(this.entreprise.email), 20, 42);
        doc.text(String(this.entreprise.telephone), 20, 49);
        doc.text(String(this.entreprise.adresse), 20, 56);
        doc.text(String(this.entreprise.siteWeb), 20, 63);

        this.buildFactureContent(doc, factureData);

        // Transformer en blob
        const pdfBlob = doc.output("blob");

        const formData = new FormData();
        formData.append("file", pdfBlob, `facture-${factureData.referance || 'vente'}.pdf`);
        formData.append("to", factureData.client?.email || "sayarim558@gmail.com");
        formData.append("subject", "Votre facture de vente");
        formData.append("text", "Veuillez trouver ci-joint votre facture.");

        this.http.post(`${this.baseUrl}/send-facture`, formData).subscribe({
          next: () => this.notification.success("Facture envoyée avec succès !"),
          error: (err) => console.error("Erreur envoi email:", err)
        });
      });
    }
  }








  affecterClient(idFacture: number, idClient: number): Observable<string> {
    return this.http.put<string>(`${this.baseUrl}/affecterFactureClient/${idFacture}/${idClient}`, null);
  }




}
