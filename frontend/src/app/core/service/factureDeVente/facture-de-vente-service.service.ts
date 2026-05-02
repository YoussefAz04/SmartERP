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
    const caract = this.entreprise.caracteristiqueFactureEntreprise;
    const headerR = caract?.colorHeaderR ?? 46;
    const headerG = caract?.colorHeaderG ?? 125;
    const headerB = caract?.colorHeaderB ?? 50;
    doc.setFillColor(headerR, headerG, headerB);
    doc.rect(0, 0, 210, 20, 'F');

    doc.setTextColor(255, 255, 255);
    doc.setFontSize(20);
    doc.setFont('helvetica', 'bold');
    doc.text("FACTURE DE VENTE", 105, 15, {align: 'center'});
    doc.setTextColor(0, 0, 0);




    doc.setFontSize(10);
    doc.setFont('bold', 'normal');
    const invoiceDetails = [
      `N° Facture: ${factureData.referance || "F-2025-001"}`,
      `Date: ${factureData.dateAchat || new Date().toLocaleDateString("fr-FR")}`,
      `Statut: ${factureData.statut || "En attente"}`,
      `Paiement: ${factureData.modePaiement || "—"}`
    ];
    if (factureData.commandeVente?.reference) {
      invoiceDetails.push(`Bon de Commande: ${factureData.commandeVente.reference}`);
    }

    invoiceDetails.forEach((detail, index) => {
      doc.text(detail, 140, 40 + (index * 7));
    });

    doc.setFontSize(12);
    doc.setFont('bold', 'bold');
    doc.text("FACTURÉ À:", 20, 70);



    doc.setFontSize(10);
    doc.setFont('bold', 'normal');
    const clientLines: string[] = [];
    const fullName = [factureData.client?.nom, factureData.client?.prenom].filter(Boolean).join(' ');
    clientLines.push(fullName || "Nom du Client");
    if (factureData.client?.adresse) clientLines.push(factureData.client.adresse);
    clientLines.push(factureData.client?.email || "email@example.com");
    clientLines.push(factureData.client?.telephone || "Téléphone");
    if (factureData.client?.secteurActivite) {
      clientLines.push(`Secteur: ${factureData.client.secteurActivite}`);
    }

    clientLines.forEach((info, index) => {
      doc.text(info, 20, 80 + (index * 7));
    });

    // Separator line after client info
    doc.setLineWidth(0.5);
    doc.line(20, 80 + clientLines.length * 7 + 3, 190, 80 + clientLines.length * 7 + 3);



    if (factureData.description) {
      const descStartY = 80 + clientLines.length * 7 + 10;
      doc.setFontSize(10);
      doc.text("Description:", 20, descStartY);
      const splitDesc = doc.splitTextToSize(factureData.description, 170);
      doc.text(splitDesc, 20, descStartY + 7);
    }

    const rawDetails = factureData.detaisFactureDeVente;
    const details = Array.isArray(rawDetails) ? rawDetails : (rawDetails ? [rawDetails] : []);

    const tableStartY = 80 + clientLines.length * 7 + (factureData.description ? 20 : 5);

    const rows = details.map((d, i) => {
      const qte = d.quantite || 1;
      const prix = d.prixUnitaireHT || 0;
      const tva = d.tva || 0;
      const totalHT = qte * prix;
      const totalTTC = totalHT + totalHT * (tva / 100);
      return [
        d.article || `Article ${i + 1}`,
        qte.toString(),
        prix.toFixed(2) + " DT",
        `${tva}%`,
        totalHT.toFixed(2) + " DT",
        totalTTC.toFixed(2) + " DT",
      ];
    });

    autoTable(doc, {
      startY: tableStartY,
      head: [["Article", "Qté", "Prix HT", "TVA %", "Montant HT", "Total TTC"]],
      body: rows.length > 0 ? rows : [['—', '—', '—', '—', '—', '—']],
      theme: "grid",
      headStyles: {
        fillColor: [caract?.fillColorR || 0, caract?.fillColorG || 0, caract?.fillColorB || 0],
        textColor: [caract?.textColorR || 255, caract?.textColorG || 255, caract?.textColorB || 255]
      },
      styles: { fontSize: 9 },
    });

    const finalY = (doc as any).lastAutoTable.finalY + 10;

    const montantHT = factureData.montantHT || 0;
    const montantTVA = factureData.montantTVA || 0;
    const remise = factureData.remise || 0;
    const montantTotal = factureData.montantTotal || factureData.montantTTC || 0;

    doc.setFontSize(10);
    doc.text("Sous-total HT:", 130, finalY);
    doc.text(montantHT.toFixed(2) + " DT", 190, finalY, { align: "right" });

    doc.text("TVA:", 130, finalY + 7);
    doc.text(montantTVA.toFixed(2) + " DT", 190, finalY + 7, { align: "right" });

    let summaryOffset = 14;
    if (remise > 0) {
      doc.text("Remise:", 130, finalY + summaryOffset);
      doc.text("-" + remise.toFixed(2) + " DT", 190, finalY + summaryOffset, { align: "right" });
      summaryOffset += 7;
    }

    doc.setFontSize(12);
    doc.setFont('helvetica', 'bold');
    doc.text("TOTAL TTC:", 130, finalY + summaryOffset);
    doc.text(montantTotal.toFixed(2) + " DT", 190, finalY + summaryOffset, { align: "right" });
    doc.setFont('helvetica', 'normal');

    if (caract?.colorFooterR && caract?.colorFooterG && caract?.colorFooterB) {
      doc.setFillColor(caract.colorFooterR, caract.colorFooterG, caract.colorFooterB);
    } else {
      doc.setFillColor(46, 125, 50);
    }
    doc.rect(0, 275, 210, 20, 'F');
    doc.setTextColor(255, 255, 255);
    doc.setFontSize(8);
    const nif = this.entreprise.numeroIdentificationFiscale || '';
    const rc = this.entreprise.registreCommerce || '';
    if (nif || rc) {
      doc.text(`NIF: ${nif}  —  RC: ${rc}`, 20, 285);
    } else {
      doc.text(`${this.entreprise.nom}`, 20, 285);
    }
    doc.text(caract?.messageFooter || "Merci pour votre confiance !", 190, 285, { align: "right" });
    doc.setTextColor(0, 0, 0);



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

  /** Récupérer l'historique des factures de vente (achats) d'un client. */
  getFacturesByClient(clientId: number): Observable<FactureVente[]> {
    return this.http.get<FactureVente[]>(`${this.baseUrl}/client/${clientId}`);
  }




}
