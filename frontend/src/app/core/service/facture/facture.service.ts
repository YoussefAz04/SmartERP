import { Injectable } from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {FactureAchat} from "../../../model/FactureAchat.model";
import { environment } from '../../../../environments/environment';
import {jsPDF} from "jspdf";
import autoTable from "jspdf-autotable";
import {EntrepriseService} from "../entreprise/entreprise.service";
import {Entreprise} from "../../../model/entreprise.model";
import {ResponseDto} from "../../../model/reponseDto.model";

@Injectable({
  providedIn: 'root'
})
export class FactureService {

  private apiUrl = `${environment.apiUrl}/facture`;
  private entreprise!: Entreprise;

  constructor(private http: HttpClient,
              private entrepriseService: EntrepriseService) { }

  getAllFactureAchat(): Observable<FactureAchat[]> {
  return this.http.get<FactureAchat[]>(`${this.apiUrl}/getAllFactureAchat`);
}

  getFactureAchatById(id: number): Observable<FactureAchat> {
    const url = `${this.apiUrl}/getFactureAchatById/${id}`;
    return this.http.get<FactureAchat>(url);
  }

  /** Récupérer l'historique des factures d'achat d'un fournisseur. */
  getFacturesByFournisseur(fournisseurId: number): Observable<FactureAchat[]> {
    return this.http.get<FactureAchat[]>(`${this.apiUrl}/fournisseur/${fournisseurId}`);
  }

  updateFactureAchat(id: number, facture: FactureAchat): Observable<FactureAchat> {
    return this.http.put<FactureAchat>(`${this.apiUrl}/updateFactureAchat/${id}`, facture);
  }

  ajouterFacture(idDocument: number): Observable<any> {
    return this.http.post(`${this.apiUrl}/${idDocument}`, {});
  }

  deleteFactureAchat(id: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/deleteFactureAchat/${id}`);
  }

  /**
   * Importer une facture d'achat depuis un PDF en utilisant les zones OCR
   * sauvegardées (typeDeDocument = "FACTURE_ACHAT").
   */
  importFromPdf(file: File): Observable<Record<string, unknown>> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<Record<string, unknown>>(`${this.apiUrl}/import-pdf`, formData);
  }

  /** Importer + sauvegarder en une étape la facture d'achat extraite. */
  saveFromPdf(file: File): Observable<Record<string, unknown>> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<Record<string, unknown>>(`${this.apiUrl}/save-from-pdf`, formData);
  }

  generateFactureAchatPDF(factureData: FactureAchat) {
    const doc = new jsPDF();
    const item = localStorage.getItem('myLSkey');
    if (!item) { return; }

    const currentUser: ResponseDto = JSON.parse(decodeURIComponent(escape(atob(item))));
    if (currentUser?.entreprise?.id) {
      this.entrepriseService.getEntrepriseById(currentUser.entreprise.id).subscribe(data => {
        this.entreprise = data;
        this.buildFactureAchatContent(doc, factureData);
        doc.save(`facture-achat-${factureData.referance || 'achat'}.pdf`);
      });
    }
  }

  private buildFactureAchatContent(doc: jsPDF, factureData: FactureAchat) {
    const caract = this.entreprise.caracteristiqueFactureEntreprise;
    const headerR = caract?.colorHeaderR ?? 41;
    const headerG = caract?.colorHeaderG ?? 128;
    const headerB = caract?.colorHeaderB ?? 185;

    // ── Header bar ────────────────────────────────────────────────────────────
    doc.setFillColor(headerR, headerG, headerB);
    doc.rect(0, 0, 210, 28, 'F');
    doc.setTextColor(255, 255, 255);
    doc.setFontSize(22);
    doc.setFont('helvetica', 'bold');
    doc.text("FACTURE D'ACHAT", 105, 18.5, { align: 'center' });
    doc.setTextColor(0, 0, 0);

    // ── Company info panel (left column) ──────────────────────────────────────
    doc.setFillColor(248, 249, 250);
    doc.rect(12, 33, 86, 52, 'F');
    doc.setFontSize(13);
    doc.setFont('helvetica', 'bold');
    doc.text(this.entreprise.nom || '', 17, 43);
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
    if (factureData.commandeAchat?.reference) {
      invoiceDetails.push(`Bon de Commande: ${factureData.commandeAchat.reference}`);
    }
    invoiceDetails.forEach((detail, index) => {
      doc.text(detail, 117, 43 + index * 7);
    });

    // ── Section heading: FOURNISSEUR ──────────────────────────────────────────
    doc.setFillColor(headerR, headerG, headerB);
    doc.rect(12, 89, 86, 8, 'F');
    doc.setTextColor(255, 255, 255);
    doc.setFontSize(10.5);
    doc.setFont('helvetica', 'bold');
    doc.text('FOURNISSEUR:', 17, 95);
    doc.setTextColor(0, 0, 0);

    // ── Fournisseur info ──────────────────────────────────────────────────────
    doc.setFontSize(9.5);
    doc.setFont('helvetica', 'normal');
    const fournisseurLines: string[] = [
      factureData.fournisseur?.nom || 'Nom du Fournisseur',
      factureData.fournisseur?.adresse || '',
      factureData.fournisseur?.email || '',
      factureData.fournisseur?.telephone || ''
    ];
    const villePays = [factureData.fournisseur?.ville, factureData.fournisseur?.pays].filter(Boolean).join(', ');
    if (villePays) { fournisseurLines.push(villePays); }
    if (factureData.fournisseur?.numeroIdentificationFiscale) {
      fournisseurLines.push(`NIF: ${factureData.fournisseur.numeroIdentificationFiscale}`);
    }
    if (factureData.fournisseur?.registreCommerce) {
      fournisseurLines.push(`RC: ${factureData.fournisseur.registreCommerce}`);
    }
    const filteredLines = fournisseurLines.filter(l => l.trim() !== '');
    filteredLines.forEach((line, index) => {
      doc.text(line, 17, 102 + index * 7);
    });

    // ── Separator ─────────────────────────────────────────────────────────────
    const separatorY = 102 + filteredLines.length * 7 + 6;
    doc.setDrawColor(200, 200, 200);
    doc.setLineWidth(0.4);
    doc.line(12, separatorY, 198, separatorY);

    // ── Line items table ──────────────────────────────────────────────────────
    const rawDetails = factureData.detaisFactureAchat;
    const detailsArray = Array.isArray(rawDetails) ? rawDetails : (rawDetails ? [rawDetails] : []);
    let rows: string[][] = detailsArray.map((d, i) => {
      const qte = d.quantite || 1;
      const prix = d.prixUnitaireHT || 0;
      const tva = d.tva || 0;
      const totalHT = qte * prix;
      const totalTTC = totalHT * (1 + tva / 100);
      return [
        d.article || `Article ${i + 1}`,
        qte.toString(),
        prix.toFixed(2) + ' DT',
        `${tva}%`,
        totalHT.toFixed(2) + ' DT',
        totalTTC.toFixed(2) + ' DT'
      ];
    });

    // Fallback to lignes (LigneFactureAchat) when no DetaisFactureAchat is available
    if (rows.length === 0 && Array.isArray(factureData.lignes) && factureData.lignes.length > 0) {
      rows = factureData.lignes.map((l, i) => {
        const qte = Number(l.quantite) || 1;
        const prix = Number(l.prixUnitaireHT) || 0;
        const tva = Number(l.tva) || 0;
        const totalHT = l.montantHT != null ? Number(l.montantHT) : qte * prix;
        const totalTTC = l.montantTTC != null ? Number(l.montantTTC) : totalHT * (1 + tva / 100);
        return [
          l.designation || `Article ${i + 1}`,
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
      startY: separatorY + 6,
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

    if (factureData.description) {
      const descY = finalY + summaryOffset + 18;
      doc.setFontSize(9);
      doc.setTextColor(80, 80, 80);
      doc.text('Notes:', 17, descY);
      const splitDesc = doc.splitTextToSize(factureData.description, 90);
      doc.text(splitDesc, 17, descY + 6);
      doc.setTextColor(0, 0, 0);
    }

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
      doc.text(this.entreprise.nom || '', 17, 283);
    }
    doc.text(
      caract?.messageFooter || 'Merci pour votre collaboration !',
      193, 283, { align: 'right' }
    );
    doc.setTextColor(0, 0, 0);
  }

}
