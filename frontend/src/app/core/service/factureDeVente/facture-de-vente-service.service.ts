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
    if (!item) { return; }
    const currentUser: ResponseDto = JSON.parse(decodeURIComponent(escape(atob(item))));
    if (currentUser?.entreprise.id) {
      this.entrepriseService.getEntrepriseById(currentUser.entreprise.id).subscribe(data => {
        this.entreprise = data;
        this.buildFactureContent(doc, factureData);
        doc.save(`facture-${factureData.referance || 'vente'}.pdf`);
      });
    }
  }

  private buildFactureContent(doc: jsPDF, factureData: FactureVente) {
    const caract = this.entreprise.caracteristiqueFactureEntreprise;
    const headerR = caract?.colorHeaderR ?? 46;
    const headerG = caract?.colorHeaderG ?? 125;
    const headerB = caract?.colorHeaderB ?? 50;

    // ── Header bar ────────────────────────────────────────────────────────────
    doc.setFillColor(headerR, headerG, headerB);
    doc.rect(0, 0, 210, 28, 'F');
    doc.setTextColor(255, 255, 255);
    doc.setFontSize(22);
    doc.setFont('helvetica', 'bold');
    doc.text('FACTURE DE VENTE', 105, 18.5, { align: 'center' });
    doc.setTextColor(0, 0, 0);

    // ── Company info panel (left column) ──────────────────────────────────────
    doc.setFillColor(248, 249, 250);
    doc.rect(12, 33, 86, 52, 'F');
    doc.setFontSize(13);
    doc.setFont('helvetica', 'bold');
    doc.text(String(this.entreprise.nom || ''), 17, 43);
    doc.setFontSize(9);
    doc.setFont('helvetica', 'normal');
    const companyLines = [
      this.entreprise.email || '',
      this.entreprise.telephone || '',
      this.entreprise.adresse || '',
      this.entreprise.siteWeb || ''
    ].filter(l => l.trim() !== '');
    companyLines.forEach((line, i) => {
      doc.text(line, 17, 51 + i * 7);
    });

    // ── Invoice details panel (right column) ──────────────────────────────────
    doc.setFillColor(248, 249, 250);
    doc.rect(112, 33, 86, 52, 'F');
    doc.setFontSize(9);
    doc.setFont('helvetica', 'normal');
    const invoiceDetails = [
      `N° Facture: ${factureData.referance || '—'}`,
      `Date: ${factureData.dateAchat || new Date().toLocaleDateString('fr-FR')}`,
      `Statut: ${factureData.statut || 'En attente'}`,
      `Paiement: ${factureData.modePaiement || '—'}`
    ];
    if (factureData.commandeVente?.reference) {
      invoiceDetails.push(`Bon de Commande: ${factureData.commandeVente.reference}`);
    }
    invoiceDetails.forEach((detail, index) => {
      doc.text(detail, 117, 43 + index * 7);
    });

    // ── Section heading: FACTURÉ À ────────────────────────────────────────────
    doc.setFillColor(headerR, headerG, headerB);
    doc.rect(12, 89, 86, 8, 'F');
    doc.setTextColor(255, 255, 255);
    doc.setFontSize(10.5);
    doc.setFont('helvetica', 'bold');
    doc.text('FACTURÉ À:', 17, 95);
    doc.setTextColor(0, 0, 0);

    // ── Client info ───────────────────────────────────────────────────────────
    doc.setFontSize(9.5);
    doc.setFont('helvetica', 'normal');
    const clientLines: string[] = [];
    const fullName = [factureData.client?.nom, factureData.client?.prenom].filter(Boolean).join(' ');
    if (fullName) { clientLines.push(fullName); }
    if (factureData.client?.adresse) { clientLines.push(factureData.client.adresse); }
    if (factureData.client?.email) { clientLines.push(factureData.client.email); }
    if (factureData.client?.telephone) { clientLines.push(factureData.client.telephone); }
    if (factureData.client?.secteurActivite) {
      clientLines.push(`Secteur: ${factureData.client.secteurActivite}`);
    }
    if (clientLines.length === 0) { clientLines.push('Client non renseigné'); }
    clientLines.forEach((info, index) => {
      doc.text(info, 17, 102 + index * 7);
    });

    // ── Separator ─────────────────────────────────────────────────────────────
    const separatorY = 102 + clientLines.length * 7 + 6;
    doc.setDrawColor(200, 200, 200);
    doc.setLineWidth(0.4);
    doc.line(12, separatorY, 198, separatorY);

    // ── Description ───────────────────────────────────────────────────────────
    let tableStartY = separatorY + 6;
    if (factureData.description) {
      doc.setFontSize(9);
      doc.setTextColor(80, 80, 80);
      doc.text('Description:', 17, tableStartY + 4);
      const splitDesc = doc.splitTextToSize(factureData.description, 170);
      doc.text(splitDesc, 17, tableStartY + 11);
      tableStartY += 11 + splitDesc.length * 5 + 4;
      doc.setTextColor(0, 0, 0);
    }

    // ── Line items table ──────────────────────────────────────────────────────
    const rawDetails = factureData.detaisFactureDeVente;
    const details = Array.isArray(rawDetails) ? rawDetails : (rawDetails ? [rawDetails] : []);
    let rows: string[][] = details.map((d, i) => {
      const qte = d.quantite || 1;
      const prix = d.prixUnitaireHT || 0;
      const tva = d.tva || 0;
      const totalHT = qte * prix;
      const totalTTC = totalHT + totalHT * (tva / 100);
      return [
        d.article || `Article ${i + 1}`,
        qte.toString(),
        prix.toFixed(2) + ' DT',
        `${tva}%`,
        totalHT.toFixed(2) + ' DT',
        totalTTC.toFixed(2) + ' DT'
      ];
    });

    // Fallback to commandeVente.lignes when no DetaisFactureDeVente is available
    const commandeLignes = factureData.commandeVente?.lignes;
    if (rows.length === 0 && Array.isArray(commandeLignes) && commandeLignes.length > 0) {
      rows = commandeLignes.map((l, i) => {
        const qte = Number(l.quantite) || 1;
        const prix = Number(l.prixUnitaireHT) || 0;
        const tva = Number(l.tva) || 0;
        const totalHT = l.montantHT != null ? Number(l.montantHT) : qte * prix;
        const totalTTC = l.montantTTC != null ? Number(l.montantTTC) : totalHT + totalHT * (tva / 100);
        return [
          l.articleVente?.nom || `Article ${i + 1}`,
          qte.toString(),
          prix.toFixed(2) + ' DT',
          `${tva}%`,
          totalHT.toFixed(2) + ' DT',
          totalTTC.toFixed(2) + ' DT'
        ];
      });
    }

    const fillR = caract?.fillColorR ?? headerR;
    const fillG = caract?.fillColorG ?? headerG;
    const fillB = caract?.fillColorB ?? headerB;
    const txtR = caract?.textColorR ?? 255;
    const txtG = caract?.textColorG ?? 255;
    const txtB = caract?.textColorB ?? 255;

    autoTable(doc, {
      startY: tableStartY,
      head: [['Article', 'Qté', 'Prix HT', 'TVA %', 'Montant HT', 'Total TTC']],
      body: rows.length > 0 ? rows : [['—', '—', '—', '—', '—', '—']],
      margin: { left: 12, right: 12 },
      theme: 'grid',
      headStyles: {
        fillColor: [fillR, fillG, fillB],
        textColor: [txtR, txtG, txtB],
        fontStyle: 'bold',
        fontSize: 9
      },
      styles: { fontSize: 9 },
      alternateRowStyles: { fillColor: [252, 252, 252] }
    });

    const finalY = (doc as any).lastAutoTable.finalY + 8;
    const montantHT = factureData.montantHT || 0;
    const montantTVA = factureData.montantTVA || 0;
    const remise = factureData.remise || 0;
    const montantTotal = factureData.montantTotal || factureData.montantTTC || 0;
    const hasRemise = remise > 0;

    // ── Totals ────────────────────────────────────────────────────────────────
    doc.setFontSize(9.5);
    doc.setFont('helvetica', 'normal');
    doc.setTextColor(60, 60, 60);
    doc.text('Sous-total HT:', 117, finalY + 2);
    doc.text(montantHT.toFixed(2) + ' DT', 196, finalY + 2, { align: 'right' });
    doc.text('TVA:', 117, finalY + 10);
    doc.text(montantTVA.toFixed(2) + ' DT', 196, finalY + 10, { align: 'right' });

    let summaryOffset = 18;
    if (hasRemise) {
      doc.text('Remise:', 117, finalY + summaryOffset);
      doc.text('-' + remise.toFixed(2) + ' DT', 196, finalY + summaryOffset, { align: 'right' });
      summaryOffset += 8;
    }

    // TOTAL TTC — colored accent row
    doc.setDrawColor(180, 180, 180);
    doc.setLineWidth(0.4);
    doc.line(112, finalY + summaryOffset - 3, 198, finalY + summaryOffset - 3);
    doc.setFillColor(headerR, headerG, headerB);
    doc.rect(112, finalY + summaryOffset - 3, 86, 11, 'F');
    doc.setTextColor(255, 255, 255);
    doc.setFontSize(11);
    doc.setFont('helvetica', 'bold');
    doc.text('TOTAL TTC:', 117, finalY + summaryOffset + 4.5);
    doc.text(montantTotal.toFixed(2) + ' DT', 196, finalY + summaryOffset + 4.5, { align: 'right' });
    doc.setTextColor(0, 0, 0);
    doc.setFont('helvetica', 'normal');

    // ── Footer bar ────────────────────────────────────────────────────────────
    const footerR = caract?.colorFooterR ?? headerR;
    const footerG = caract?.colorFooterG ?? headerG;
    const footerB = caract?.colorFooterB ?? headerB;
    doc.setFillColor(footerR, footerG, footerB);
    doc.rect(0, 272, 210, 25, 'F');
    doc.setTextColor(255, 255, 255);
    doc.setFontSize(8);
    const nif = this.entreprise.numeroIdentificationFiscale || '';
    const rc = this.entreprise.registreCommerce || '';
    if (nif || rc) {
      doc.text(`NIF: ${nif}  —  RC: ${rc}`, 17, 283);
    } else {
      doc.text(String(this.entreprise.nom || ''), 17, 283);
    }
    doc.text(caract?.messageFooter || 'Merci pour votre confiance !', 193, 283, { align: 'right' });
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
