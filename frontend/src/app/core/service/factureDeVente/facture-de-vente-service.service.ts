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
    const hR = caract?.colorHeaderR ?? 46;
    const hG = caract?.colorHeaderG ?? 125;
    const hB = caract?.colorHeaderB ?? 50;
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
    doc.text('FACTURE DE VENTE', W / 2, 21, { align: 'center' });

    doc.setFontSize(8.5);
    doc.setFont('helvetica', 'normal');
    doc.setTextColor(215, 240, 215);
    doc.text(String(this.entreprise.nom || ''), W / 2, 30, { align: 'center' });
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
    doc.text(String(this.entreprise.nom || ''), lX + 4, pTop + 17);

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
      { label: 'N° Facture', value: factureData.referance || '—' },
      { label: 'Date',       value: factureData.dateAchat || new Date().toLocaleDateString('fr-FR') },
      { label: 'Statut',     value: factureData.statut || 'En attente' },
      { label: 'Paiement',   value: factureData.modePaiement || '—' }
    ];
    if (factureData.commandeVente?.reference) {
      invoiceFields.push({ label: 'Bon de commande', value: factureData.commandeVente.reference });
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

    // ── FACTURÉ À SECTION ────────────────────────────────────────────────────
    const secTop = pTop + pH + 10;
    doc.setFillColor(hR, hG, hB);
    doc.rect(mg, secTop, W - 2 * mg, 9, 'F');
    doc.setTextColor(255, 255, 255);
    doc.setFontSize(9.5);
    doc.setFont('helvetica', 'bold');
    doc.text('FACTURÉ À', mg + 5, secTop + 6.5);
    doc.setTextColor(0, 0, 0);

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

    const boxH = Math.max(28, clientLines.length * 7 + 10);

    doc.setFillColor(250, 251, 253);
    doc.setDrawColor(215, 225, 215);
    doc.setLineWidth(0.3);
    doc.rect(mg, secTop + 9, W - 2 * mg, boxH, 'FD');
    // accent left bar
    doc.setFillColor(hR, hG, hB);
    doc.rect(mg, secTop + 9, 3, boxH, 'F');

    doc.setFont('helvetica', 'bold');
    doc.setFontSize(9.5);
    doc.setTextColor(25, 25, 25);
    doc.text(clientLines[0] || '', mg + 8, secTop + 17);
    doc.setFont('helvetica', 'normal');
    doc.setFontSize(9);
    doc.setTextColor(70, 70, 70);
    clientLines.slice(1).forEach((info, i) => {
      doc.text(info, mg + 8, secTop + 24 + i * 7);
    });

    // ── SEPARATOR ────────────────────────────────────────────────────────────
    const separatorY = secTop + 9 + boxH + 8;
    doc.setDrawColor(200, 215, 200);
    doc.setLineWidth(0.5);
    doc.line(mg, separatorY, W - mg, separatorY);
    doc.setLineWidth(0.2);

    // ── DESCRIPTION ──────────────────────────────────────────────────────────
    let tableStartY = separatorY + 8;
    if (factureData.description) {
      doc.setFontSize(8.5);
      doc.setTextColor(80, 80, 80);
      doc.setFont('helvetica', 'bold');
      doc.text('Description :', mg, tableStartY + 4);
      doc.setFont('helvetica', 'normal');
      const splitDesc = doc.splitTextToSize(factureData.description, W - 2 * mg);
      doc.text(splitDesc, mg, tableStartY + 11);
      tableStartY += 11 + splitDesc.length * 5 + 6;
      doc.setTextColor(0, 0, 0);
    }

    // ── LINE ITEMS TABLE ─────────────────────────────────────────────────────
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

    const fillR = caract?.fillColorR ?? hR;
    const fillG = caract?.fillColorG ?? hG;
    const fillB = caract?.fillColorB ?? hB;
    const txtR = caract?.textColorR ?? 255;
    const txtG = caract?.textColorG ?? 255;
    const txtB = caract?.textColorB ?? 255;

    autoTable(doc, {
      startY: tableStartY,
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
      alternateRowStyles: { fillColor: [246, 250, 246] },
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

    doc.setFillColor(248, 251, 248);
    doc.setDrawColor(215, 235, 215);
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
      doc.text(String(this.entreprise.nom || ''), mg, 283);
    }
    doc.text(caract?.messageFooter || 'Merci pour votre confiance !', W - mg, 283, { align: 'right' });
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
