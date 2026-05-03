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
    const hR = caract?.colorHeaderR ?? 41;
    const hG = caract?.colorHeaderG ?? 128;
    const hB = caract?.colorHeaderB ?? 185;
    // Darker shade for header accent strip
    const dR = Math.max(0, hR - 30);
    const dG = Math.max(0, hG - 30);
    const dB = Math.max(0, hB - 30);

    const W  = 210;
    const mg = 12;

    // ── HEADER (0 – 40 mm) ───────────────────────────────────────────────────
    doc.setFillColor(hR, hG, hB);
    doc.rect(0, 0, W, 38, 'F');
    // darker accent strip at bottom of header
    doc.setFillColor(dR, dG, dB);
    doc.rect(0, 35.5, W, 2.5, 'F');

    doc.setTextColor(255, 255, 255);
    doc.setFontSize(26);
    doc.setFont('helvetica', 'bold');
    doc.text("FACTURE D'ACHAT", W / 2, 21, { align: 'center' });

    doc.setFontSize(8.5);
    doc.setFont('helvetica', 'normal');
    doc.setTextColor(215, 230, 245);
    doc.text(this.entreprise.nom || '', W / 2, 30, { align: 'center' });
    doc.setTextColor(0, 0, 0);

    // ── INFO PANELS (43 – 95 mm) ─────────────────────────────────────────────
    const pTop = 43;
    const pH   = 52;
    const lX   = mg;
    const lW   = 86;
    const rX   = 112;
    const rW   = 86;

    // Left panel — company info
    doc.setFillColor(248, 250, 252);
    doc.setDrawColor(hR, hG, hB);
    doc.setLineWidth(0.35);
    doc.rect(lX, pTop, lW, pH, 'FD');
    doc.setFillColor(hR, hG, hB);
    doc.rect(lX, pTop, lW, 8, 'F');
    doc.setTextColor(255, 255, 255);
    doc.setFontSize(7);
    doc.setFont('helvetica', 'bold');
    doc.text('ÉMETTEUR', lX + 4, pTop + 5.5);

    doc.setTextColor(25, 25, 25);
    doc.setFontSize(11.5);
    doc.setFont('helvetica', 'bold');
    doc.text(this.entreprise.nom || '', lX + 4, pTop + 17);

    doc.setFontSize(8.5);
    doc.setFont('helvetica', 'normal');
    doc.setTextColor(70, 70, 70);
    const companyLines = [
      this.entreprise.email || '',
      this.entreprise.telephone || '',
      this.entreprise.adresse || '',
      this.entreprise.siteWeb || ''
    ].filter(l => l.trim() !== '');
    companyLines.slice(0, 4).forEach((line, i) => {
      doc.text(line, lX + 4, pTop + 25 + i * 7);
    });

    // Right panel — invoice details
    doc.setFillColor(248, 250, 252);
    doc.setDrawColor(hR, hG, hB);
    doc.setLineWidth(0.35);
    doc.rect(rX, pTop, rW, pH, 'FD');
    doc.setFillColor(hR, hG, hB);
    doc.rect(rX, pTop, rW, 8, 'F');
    doc.setTextColor(255, 255, 255);
    doc.setFontSize(7);
    doc.setFont('helvetica', 'bold');
    doc.text('DÉTAILS FACTURE', rX + 4, pTop + 5.5);

    const invoiceFields: Array<{ label: string; value: string }> = [
      { label: 'N° Facture',  value: factureData.referance || '—' },
      { label: 'Date',        value: factureData.dateAchat || new Date().toLocaleDateString('fr-FR') },
      { label: 'Statut',      value: factureData.statut || 'En attente' },
      { label: 'Paiement',    value: factureData.modePaiement || '—' }
    ];
    if (factureData.commandeAchat?.reference) {
      invoiceFields.push({ label: 'Bon de commande', value: factureData.commandeAchat.reference });
    }
    invoiceFields.forEach(({ label, value }, i) => {
      const y = pTop + 17 + i * 7.5;
      doc.setFontSize(7.5);
      doc.setFont('helvetica', 'normal');
      doc.setTextColor(130, 130, 130);
      doc.text(label, rX + 4, y);
      doc.setFont('helvetica', 'bold');
      doc.setTextColor(25, 25, 25);
      doc.text(String(value), rX + rW - 4, y, { align: 'right' });
    });

    doc.setTextColor(0, 0, 0);
    doc.setLineWidth(0.2);

    // ── FOURNISSEUR SECTION ──────────────────────────────────────────────────
    const secTop = pTop + pH + 10;
    doc.setFillColor(hR, hG, hB);
    doc.rect(mg, secTop, W - 2 * mg, 9, 'F');
    doc.setTextColor(255, 255, 255);
    doc.setFontSize(9.5);
    doc.setFont('helvetica', 'bold');
    doc.text('FOURNISSEUR', mg + 5, secTop + 6.5);
    doc.setTextColor(0, 0, 0);

    const fournisseurLines: string[] = [
      factureData.fournisseur?.nom || 'Nom du Fournisseur',
      factureData.fournisseur?.adresse || '',
      factureData.fournisseur?.email || '',
      factureData.fournisseur?.telephone || ''
    ];
    const villePays = [factureData.fournisseur?.ville, factureData.fournisseur?.pays]
      .filter(Boolean).join(', ');
    if (villePays) { fournisseurLines.push(villePays); }
    if (factureData.fournisseur?.numeroIdentificationFiscale) {
      fournisseurLines.push(`NIF: ${factureData.fournisseur.numeroIdentificationFiscale}`);
    }
    if (factureData.fournisseur?.registreCommerce) {
      fournisseurLines.push(`RC: ${factureData.fournisseur.registreCommerce}`);
    }
    const filteredLines = fournisseurLines.filter(l => l.trim() !== '');
    const boxH = Math.max(28, filteredLines.length * 7 + 10);

    doc.setFillColor(250, 251, 253);
    doc.setDrawColor(215, 225, 235);
    doc.setLineWidth(0.3);
    doc.rect(mg, secTop + 9, W - 2 * mg, boxH, 'FD');
    // accent left bar
    doc.setFillColor(hR, hG, hB);
    doc.rect(mg, secTop + 9, 3, boxH, 'F');

    doc.setFont('helvetica', 'bold');
    doc.setFontSize(9.5);
    doc.setTextColor(25, 25, 25);
    doc.text(filteredLines[0] || '', mg + 8, secTop + 17);
    doc.setFont('helvetica', 'normal');
    doc.setFontSize(9);
    doc.setTextColor(70, 70, 70);
    filteredLines.slice(1).forEach((line, i) => {
      doc.text(line, mg + 8, secTop + 24 + i * 7);
    });

    // ── SEPARATOR ────────────────────────────────────────────────────────────
    const separatorY = secTop + 9 + boxH + 8;
    doc.setDrawColor(200, 210, 220);
    doc.setLineWidth(0.5);
    doc.line(mg, separatorY, W - mg, separatorY);
    doc.setLineWidth(0.2);

    // ── LINE ITEMS TABLE ─────────────────────────────────────────────────────
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

    const fillR = caract?.fillColorR ?? hR;
    const fillG = caract?.fillColorG ?? hG;
    const fillB = caract?.fillColorB ?? hB;
    const txtR = caract?.textColorR ?? 255;
    const txtG = caract?.textColorG ?? 255;
    const txtB = caract?.textColorB ?? 255;

    autoTable(doc, {
      startY: separatorY + 8,
      head: [['Article', 'Qté', 'Prix HT', 'TVA %', 'Montant HT', 'Total TTC']],
      body: rows.length > 0 ? rows : [['—', '—', '—', '—', '—', '—']],
      margin: { left: mg, right: mg },
      theme: 'grid',
      headStyles: {
        fillColor: [fillR, fillG, fillB],
        textColor: [txtR, txtG, txtB],
        fontStyle: 'bold',
        fontSize: 9,
        cellPadding: 4
      },
      bodyStyles: {
        cellPadding: 3.5,
        textColor: [40, 40, 40],
        fontSize: 9
      },
      alternateRowStyles: { fillColor: [246, 249, 252] },
      columnStyles: {
        1: { halign: 'center' },
        2: { halign: 'right' },
        3: { halign: 'center' },
        4: { halign: 'right' },
        5: { halign: 'right' }
      }
    });

    const finalY = (doc as any).lastAutoTable.finalY + 10;
    const montantHT = factureData.montantHT || 0;
    const montantTVA = factureData.montantTVA || 0;
    const remise = factureData.remise || 0;
    const montantTotal = factureData.montantTotal || factureData.montantTTC || 0;
    const hasRemise = remise > 0;

    // ── TOTALS BOX ───────────────────────────────────────────────────────────
    const tX   = rX;
    const tW   = rW;
    const rowH = 8;
    const numSummaryRows = hasRemise ? 3 : 2;
    let rowY = finalY;

    doc.setFillColor(248, 250, 252);
    doc.setDrawColor(215, 225, 235);
    doc.setLineWidth(0.3);
    doc.rect(tX, rowY - 3, tW, numSummaryRows * rowH + 6, 'FD');

    doc.setFontSize(9);
    doc.setFont('helvetica', 'normal');
    doc.setTextColor(100, 100, 100);
    doc.text('Sous-total HT :', tX + 5, rowY + 2.5);
    doc.setFont('helvetica', 'bold');
    doc.setTextColor(30, 30, 30);
    doc.text(montantHT.toFixed(2) + ' DT', tX + tW - 5, rowY + 2.5, { align: 'right' });

    rowY += rowH;
    doc.setFont('helvetica', 'normal');
    doc.setTextColor(100, 100, 100);
    doc.text('TVA :', tX + 5, rowY + 2.5);
    doc.setFont('helvetica', 'bold');
    doc.setTextColor(30, 30, 30);
    doc.text(montantTVA.toFixed(2) + ' DT', tX + tW - 5, rowY + 2.5, { align: 'right' });

    if (hasRemise) {
      rowY += rowH;
      doc.setFont('helvetica', 'normal');
      doc.setTextColor(100, 100, 100);
      doc.text('Remise :', tX + 5, rowY + 2.5);
      doc.setFont('helvetica', 'bold');
      doc.setTextColor(180, 40, 40);
      doc.text('-' + remise.toFixed(2) + ' DT', tX + tW - 5, rowY + 2.5, { align: 'right' });
    }

    // TOTAL TTC accent row
    rowY += rowH + 4;
    doc.setFillColor(hR, hG, hB);
    doc.rect(tX, rowY - 2, tW, 12, 'F');
    doc.setTextColor(255, 255, 255);
    doc.setFontSize(10.5);
    doc.setFont('helvetica', 'bold');
    doc.text('TOTAL TTC', tX + 5, rowY + 6.5);
    doc.text(montantTotal.toFixed(2) + ' DT', tX + tW - 5, rowY + 6.5, { align: 'right' });
    doc.setTextColor(0, 0, 0);
    doc.setFont('helvetica', 'normal');

    // ── NOTES ────────────────────────────────────────────────────────────────
    if (factureData.description) {
      doc.setFontSize(8.5);
      doc.setTextColor(80, 80, 80);
      doc.setFont('helvetica', 'bold');
      doc.text('Notes :', mg, finalY + 2);
      doc.setFont('helvetica', 'normal');
      const splitDesc = doc.splitTextToSize(factureData.description, tX - mg - 6);
      doc.text(splitDesc, mg, finalY + 9);
      doc.setTextColor(0, 0, 0);
    }

    // ── FOOTER BAR ───────────────────────────────────────────────────────────
    const footerR = caract?.colorFooterR ?? hR;
    const footerG = caract?.colorFooterG ?? hG;
    const footerB = caract?.colorFooterB ?? hB;
    doc.setFillColor(footerR, footerG, footerB);
    doc.rect(0, 272, W, 25, 'F');
    doc.setTextColor(255, 255, 255);
    doc.setFontSize(8);
    const nif = this.entreprise.numeroIdentificationFiscale || '';
    const rc  = this.entreprise.registreCommerce || '';
    if (nif || rc) {
      doc.text(`NIF: ${nif}  |  RC: ${rc}`, mg, 283);
    } else {
      doc.text(this.entreprise.nom || '', mg, 283);
    }
    doc.text(
      caract?.messageFooter || 'Merci pour votre collaboration !',
      W - mg, 283, { align: 'right' }
    );
    doc.setTextColor(0, 0, 0);
  }

}
