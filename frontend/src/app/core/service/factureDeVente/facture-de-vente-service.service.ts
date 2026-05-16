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

  // Mettre à jour uniquement le statut d'une facture
  updateStatut(id: number, statut: string): Observable<FactureVente> {
    return this.http.patch<FactureVente>(`${this.baseUrl}/${id}/statut`, { statut });
  }

  deleteFacture(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/deleteFacture/${id}`);
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

  generateMultipleFactureVentePDF(factures: FactureVente[]) {
    if (factures.length === 0) { return; }
    const doc = new jsPDF();
    const item = localStorage.getItem('myLSkey');
    if (!item) { return; }
    const currentUser: ResponseDto = JSON.parse(decodeURIComponent(escape(atob(item))));
    if (currentUser?.entreprise.id) {
      this.entrepriseService.getEntrepriseById(currentUser.entreprise.id).subscribe(data => {
        this.entreprise = data;
        factures.forEach((facture, index) => {
          if (index > 0) { doc.addPage(); }
          this.buildFactureContent(doc, facture);
        });
        doc.save('Factures_Vente.pdf');
      });
    }
  }

  private buildFactureContent(doc: jsPDF, factureData: FactureVente) {
    const caract = this.entreprise.caracteristiqueFactureEntreprise;

    // ── COLOR PALETTE ────────────────────────────────────────────────────────
    const hR = caract?.colorHeaderR ?? 20;
    const hG = caract?.colorHeaderG ?? 89;
    const hB = caract?.colorHeaderB ?? 55;
    const dR = Math.max(0, Math.round(hR * 0.62));
    const dG = Math.max(0, Math.round(hG * 0.62));
    const dB = Math.max(0, Math.round(hB * 0.62));
    const fillR = caract?.fillColorR ?? hR;
    const fillG = caract?.fillColorG ?? hG;
    const fillB = caract?.fillColorB ?? hB;
    const txtR = caract?.textColorR ?? 255;
    const txtG = caract?.textColorG ?? 255;
    const txtB = caract?.textColorB ?? 255;
    const footerR = caract?.colorFooterR ?? dR;
    const footerG = caract?.colorFooterG ?? dG;
    const footerB = caract?.colorFooterB ?? dB;

    const W  = 210;
    const mg = 14;
    const rX = 110;
    const rW = 86;
    const referance = factureData.referance || '—';
    const statut  = (factureData.statut || '').toUpperCase();
    const dateStr = factureData.dateAchat || new Date().toLocaleDateString('fr-FR');

    // ── STATUS WATERMARK ─────────────────────────────────────────────────────
    const watermarkLabel =
      statut.includes('PAYEE')   ? 'PAYÉE'    :
      statut.includes('ANNUL')   ? 'ANNULÉE'  :
      statut.includes('PARTIEL') ? 'PARTIELLE': null;
    if (watermarkLabel) {
      doc.setFontSize(64);
      doc.setFont('helvetica', 'bold');
      doc.setTextColor(
        Math.min(255, hR + 188),
        Math.min(255, hG + 162),
        Math.min(255, hB + 175)
      );
      doc.text(watermarkLabel, W / 2, 178, { align: 'center', angle: 45 });
      doc.setTextColor(0, 0, 0);
    }

    // ── HEADER BAND (0 – 52 mm) ───────────────────────────────────────────────
    doc.setFillColor(hR, hG, hB);
    doc.rect(0, 0, W, 52, 'F');

    // Diagonal accent — mid-tone parallelogram
    doc.setFillColor(
      Math.min(255, Math.round(hR * 0.80 + 8)),
      Math.min(255, Math.round(hG * 0.80 + 8)),
      Math.min(255, Math.round(hB * 0.80 + 8))
    );
    doc.lines([[28, 0], [50, 52], [-28, 0]], 122, 0, [1, 1], 'F', true);

    // Diagonal accent — darker triangle (right edge)
    doc.setFillColor(dR, dG, dB);
    doc.lines([[58, 0], [0, 52]], 152, 0, [1, 1], 'F', true);

    // Bottom accent strip
    doc.setFillColor(dR, dG, dB);
    doc.rect(0, 49.5, W, 2.5, 'F');

    // Title
    doc.setTextColor(255, 255, 255);
    doc.setFontSize(28);
    doc.setFont('helvetica', 'bold');
    doc.text('FACTURE DE VENTE', mg, 25);

    // Company name subtitle
    doc.setFontSize(9);
    doc.setFont('helvetica', 'normal');
    doc.setTextColor(195, 230, 210);
    doc.text(String(this.entreprise.nom || ''), mg, 35);

    // Sector subtitle
    if (this.entreprise.secteurActivite) {
      doc.setFontSize(7.5);
      doc.setTextColor(165, 210, 185);
      doc.text(this.entreprise.secteurActivite, mg, 43);
    }

    // Invoice reference white box
    doc.setFillColor(255, 255, 255);
    doc.roundedRect(W - 62, 6, 55, 38, 3, 3, 'F');

    doc.setFontSize(6.5);
    doc.setFont('helvetica', 'normal');
    doc.setTextColor(140, 150, 165);
    doc.text('FACTURE N°', W - 34.5, 14, { align: 'center' });

    const refDisplay = (factureData.referance || '—').length > 18
      ? referance.substring(0, 18) + '…'
      : referance;
    doc.setFontSize(11);
    doc.setFont('helvetica', 'bold');
    doc.setTextColor(hR, hG, hB);
    doc.text(refDisplay, W - 34.5, 23, { align: 'center' });

    doc.setFontSize(7);
    doc.setFont('helvetica', 'normal');
    doc.setTextColor(100, 110, 125);
    doc.text(dateStr, W - 34.5, 30, { align: 'center' });

    // Status badge
    let sR = 39, sG = 174, sB = 96;
    if (statut.includes('ATTENTE'))  { sR = 194; sG = 146; sB = 0; }
    else if (statut.includes('ANNUL'))   { sR = 185; sG = 52; sB = 43; }
    else if (statut.includes('PARTIEL')) { sR = 205; sG = 102; sB = 20; }
    doc.setFillColor(sR, sG, sB);
    doc.roundedRect(W - 58, 33, 47, 8, 2, 2, 'F');
    doc.setTextColor(255, 255, 255);
    doc.setFontSize(6.5);
    doc.setFont('helvetica', 'bold');
    const statutLabelsV: Record<string, string> = {
      'PAYEE': 'PAYÉE', 'EN_ATTENTE': 'EN ATTENTE',
      'ANNULEE': 'ANNULÉE', 'PARTIELLEMENT_PAYEE': 'PART. PAYÉE'
    };
    const statutDisplay = statutLabelsV[factureData.statut || ''] || statut;
    doc.text(statutDisplay, W - 34.5, 38.2, { align: 'center' });
    doc.setTextColor(0, 0, 0);

    // ── INFO PANELS (57 – 108 mm) ─────────────────────────────────────────────
    const pTop = 57;
    const pH   = 48;
    const lX   = mg;
    const lW   = 88;

    // LEFT PANEL — ÉMETTEUR
    doc.setFillColor(247, 250, 248);
    doc.setDrawColor(hR, hG, hB);
    doc.setLineWidth(0.3);
    doc.roundedRect(lX, pTop, lW, pH, 2, 2, 'FD');
    doc.setFillColor(hR, hG, hB);
    doc.roundedRect(lX, pTop, lW, 9, 2, 2, 'F');
    doc.rect(lX, pTop + 5, lW, 4, 'F');
    doc.setTextColor(255, 255, 255);
    doc.setFontSize(7);
    doc.setFont('helvetica', 'bold');
    doc.text('ÉMETTEUR', lX + 5, pTop + 6.5);

    doc.setTextColor(22, 40, 28);
    doc.setFontSize(10.5);
    doc.setFont('helvetica', 'bold');
    doc.text(String(this.entreprise.nom || ''), lX + 5, pTop + 17);

    const telEntreprise = this.entreprise.telephone;
    const emLines = [
      this.entreprise.adresse   || '',
      this.entreprise.email     || '',
      telEntreprise ? `Tél : ${telEntreprise}` : '',
      this.entreprise.siteWeb   || ''
    ].filter(l => l.trim() !== '');
    doc.setFontSize(8);
    doc.setFont('helvetica', 'normal');
    doc.setTextColor(62, 85, 70);
    emLines.slice(0, 4).forEach((line, i) => {
      doc.text(line, lX + 5, pTop + 25 + i * 6.5);
    });

    // RIGHT PANEL — INFORMATIONS FACTURE
    doc.setFillColor(247, 250, 248);
    doc.setDrawColor(hR, hG, hB);
    doc.setLineWidth(0.3);
    doc.roundedRect(rX, pTop, rW, pH, 2, 2, 'FD');
    doc.setFillColor(hR, hG, hB);
    doc.roundedRect(rX, pTop, rW, 9, 2, 2, 'F');
    doc.rect(rX, pTop + 5, rW, 4, 'F');
    doc.setTextColor(255, 255, 255);
    doc.setFontSize(7);
    doc.setFont('helvetica', 'bold');
    doc.text('INFORMATIONS FACTURE', rX + 5, pTop + 6.5);

    const invoiceFields: Array<{ label: string; value: string }> = [
      { label: 'N° Facture', value: referance },
      { label: 'Date',       value: dateStr },
      { label: 'Paiement',   value: factureData.modePaiement || '—' }
    ];
    if (factureData.commandeVente?.reference) {
      invoiceFields.push({ label: 'Bon de commande', value: factureData.commandeVente.reference });
    }
    invoiceFields.forEach(({ label, value }, i) => {
      const y = pTop + 17 + i * 8;
      doc.setFontSize(7);
      doc.setFont('helvetica', 'normal');
      doc.setTextColor(110, 130, 118);
      doc.text(label, rX + 5, y);
      doc.setFont('helvetica', 'bold');
      doc.setTextColor(22, 40, 28);
      const valStr = String(value);
      const valDisplay = valStr.length > 22 ? valStr.substring(0, 22) + '…' : valStr;
      doc.text(valDisplay, rX + rW - 5, y, { align: 'right' });
      if (i < invoiceFields.length - 1) {
        doc.setDrawColor(210, 228, 218);
        doc.setLineWidth(0.15);
        doc.line(rX + 5, y + 3, rX + rW - 5, y + 3);
      }
    });

    doc.setTextColor(0, 0, 0);
    doc.setLineWidth(0.2);

    // ── FACTURÉ À SECTION ─────────────────────────────────────────────────────
    const secTop = pTop + pH + 7;

    doc.setFillColor(hR, hG, hB);
    doc.roundedRect(mg, secTop, W - 2 * mg, 9, 2, 2, 'F');
    doc.rect(mg, secTop + 4, W - 2 * mg, 5, 'F');
    doc.setTextColor(255, 255, 255);
    doc.setFontSize(8);
    doc.setFont('helvetica', 'bold');
    doc.text('FACTURÉ À', mg + 6, secTop + 6.5);
    doc.setTextColor(0, 0, 0);

    const clientLines: string[] = [];
    const fullName = [factureData.client?.nom, factureData.client?.prenom].filter(Boolean).join(' ');
    if (fullName)                          { clientLines.push(fullName); }
    if (factureData.client?.adresse)       { clientLines.push(factureData.client.adresse); }
    if (factureData.client?.email)         { clientLines.push(factureData.client.email); }
    if (factureData.client?.telephone)     { clientLines.push(`Tél : ${factureData.client.telephone}`); }
    if (factureData.client?.secteurActivite) {
      clientLines.push(`Secteur : ${factureData.client.secteurActivite}`);
    }
    if (clientLines.length === 0)          { clientLines.push('Client non renseigné'); }

    const boxH = Math.max(28, clientLines.length * 6.5 + 12);
    doc.setFillColor(250, 252, 251);
    doc.setDrawColor(205, 228, 215);
    doc.setLineWidth(0.25);
    doc.roundedRect(mg, secTop + 9, W - 2 * mg, boxH, 2, 2, 'FD');
    doc.setFillColor(hR, hG, hB);
    doc.roundedRect(mg, secTop + 9, 4, boxH, 2, 2, 'F');
    doc.rect(mg + 2, secTop + 9, 2, boxH, 'F');

    doc.setFont('helvetica', 'bold');
    doc.setFontSize(10);
    doc.setTextColor(22, 40, 28);
    doc.text(clientLines[0], mg + 10, secTop + 18);
    doc.setFont('helvetica', 'normal');
    doc.setFontSize(8.5);
    doc.setTextColor(58, 82, 68);
    clientLines.slice(1).forEach((info, i) => {
      doc.text(info, mg + 10, secTop + 25.5 + i * 6.5);
    });

    // ── DESCRIPTION ──────────────────────────────────────────────────────────
    const tableStartY0 = secTop + 9 + boxH + 10;
    let tableStartY = tableStartY0;

    if (factureData.description) {
      doc.setFillColor(247, 251, 248);
      doc.setDrawColor(205, 228, 215);
      doc.setLineWidth(0.25);
      doc.roundedRect(mg, tableStartY0 - 4, W - 2 * mg, 20, 2, 2, 'FD');
      doc.setFontSize(7.5);
      doc.setFont('helvetica', 'bold');
      doc.setTextColor(hR, hG, hB);
      doc.text('DESCRIPTION :', mg + 5, tableStartY0 + 3);
      doc.setFont('helvetica', 'normal');
      doc.setTextColor(58, 82, 68);
      const splitDesc = doc.splitTextToSize(factureData.description, W - 2 * mg - 10);
      doc.text(splitDesc, mg + 5, tableStartY0 + 10);
      tableStartY = tableStartY0 + 11 + splitDesc.length * 5 + 8;
      doc.setTextColor(0, 0, 0);
    }

    // ── LINE ITEMS TABLE ──────────────────────────────────────────────────────
    doc.setFontSize(8);
    doc.setFont('helvetica', 'bold');
    doc.setTextColor(hR, hG, hB);
    doc.text('DÉTAIL DES ARTICLES', mg, tableStartY - 3);
    doc.setTextColor(0, 0, 0);

    const rawDetails = factureData.detaisFactureDeVente;
    const details = Array.isArray(rawDetails) ? rawDetails : (rawDetails ? [rawDetails] : []);
    let rows: string[][] = details.map((d, i) => {
      const qte  = d.quantite || 1;
      const prix = d.prixUnitaireHT || 0;
      const tva  = d.tva || 0;
      const totalHT  = qte * prix;
      const totalTTC = totalHT + totalHT * (tva / 100);
      return [
        String(i + 1),
        d.article || `Article ${i + 1}`,
        qte.toString(),
        prix.toFixed(2) + ' DT',
        `${tva}%`,
        totalHT.toFixed(2) + ' DT',
        totalTTC.toFixed(2) + ' DT'
      ];
    });

    const commandeLignes = factureData.commandeVente?.lignes;
    if (rows.length === 0 && Array.isArray(commandeLignes) && commandeLignes.length > 0) {
      rows = commandeLignes.map((l, i) => {
        const qte  = Number(l.quantite) || 1;
        const prix = Number(l.prixUnitaireHT) || 0;
        const tva  = Number(l.tva) || 0;
        const totalHT  = l.montantHT  != null ? Number(l.montantHT)  : qte * prix;
        const totalTTC = l.montantTTC != null ? Number(l.montantTTC) : totalHT + totalHT * (tva / 100);
        return [
          String(i + 1),
          l.articleVente?.nom || `Article ${i + 1}`,
          qte.toString(),
          prix.toFixed(2) + ' DT',
          `${tva}%`,
          totalHT.toFixed(2) + ' DT',
          totalTTC.toFixed(2) + ' DT'
        ];
      });
    }

    autoTable(doc, {
      startY: tableStartY,
      head: [['#', 'Désignation', 'Qté', 'Prix HT', 'TVA', 'Montant HT', 'Total TTC']],
      body: rows.length > 0 ? rows : [['—', '—', '—', '—', '—', '—', '—']],
      margin: { left: mg, right: mg },
      theme: 'grid',
      headStyles: {
        fillColor: [fillR, fillG, fillB],
        textColor: [txtR, txtG, txtB],
        fontStyle: 'bold',
        fontSize: 8.5,
        cellPadding: 4,
        halign: 'center'
      },
      bodyStyles: {
        cellPadding: 3.5,
        textColor: [32, 48, 38],
        fontSize: 8.5
      },
      alternateRowStyles: { fillColor: [242, 250, 245] },
      columnStyles: {
        0: { halign: 'center', cellWidth: 10 },
        2: { halign: 'center', cellWidth: 14 },
        3: { halign: 'right',  cellWidth: 24 },
        4: { halign: 'center', cellWidth: 14 },
        5: { halign: 'right',  cellWidth: 26 },
        6: { halign: 'right',  cellWidth: 26 }
      }
    });

    const finalY = (doc as any).lastAutoTable.finalY;
    const montantHT    = factureData.montantHT    || 0;
    const montantTVA   = factureData.montantTVA   || 0;
    const remise       = factureData.remise        || 0;
    const montantTotal = factureData.montantTotal  || factureData.montantTTC || 0;
    const hasRemise    = remise > 0;

    // ── TOTALS BOX ────────────────────────────────────────────────────────────
    const tX   = rX;
    const tW   = rW;
    const rowH = 9;
    const numSummaryRows = hasRemise ? 3 : 2;
    let rowY = finalY + 10;

    doc.setFillColor(242, 250, 245);
    doc.setDrawColor(195, 228, 210);
    doc.setLineWidth(0.3);
    doc.roundedRect(tX, rowY - 4, tW, numSummaryRows * rowH + 8, 2, 2, 'FD');

    doc.setFontSize(8.5);
    doc.setFont('helvetica', 'normal');
    doc.setTextColor(90, 118, 102);
    doc.text('Sous-total HT :', tX + 6, rowY + 2.5);
    doc.setFont('helvetica', 'bold');
    doc.setTextColor(22, 40, 28);
    doc.text(montantHT.toFixed(2) + ' DT', tX + tW - 6, rowY + 2.5, { align: 'right' });

    rowY += rowH;
    doc.setFont('helvetica', 'normal');
    doc.setTextColor(90, 118, 102);
    doc.text('TVA :', tX + 6, rowY + 2.5);
    doc.setFont('helvetica', 'bold');
    doc.setTextColor(22, 40, 28);
    doc.text(montantTVA.toFixed(2) + ' DT', tX + tW - 6, rowY + 2.5, { align: 'right' });

    if (hasRemise) {
      rowY += rowH;
      doc.setFont('helvetica', 'normal');
      doc.setTextColor(90, 118, 102);
      doc.text('Remise :', tX + 6, rowY + 2.5);
      doc.setFont('helvetica', 'bold');
      doc.setTextColor(175, 38, 38);
      doc.text('-' + remise.toFixed(2) + ' DT', tX + tW - 6, rowY + 2.5, { align: 'right' });
    }

    rowY += rowH + 4;
    doc.setFillColor(hR, hG, hB);
    doc.roundedRect(tX, rowY - 3, tW, 14, 2, 2, 'F');
    doc.setTextColor(255, 255, 255);
    doc.setFontSize(11);
    doc.setFont('helvetica', 'bold');
    doc.text('TOTAL TTC', tX + 6, rowY + 6.5);
    doc.text(montantTotal.toFixed(2) + ' DT', tX + tW - 6, rowY + 6.5, { align: 'right' });
    doc.setTextColor(0, 0, 0);
    doc.setFont('helvetica', 'normal');

    // ── NOTES ─────────────────────────────────────────────────────────────────
    if (factureData.description) {
      const notesY = finalY + 10;
      doc.setFillColor(246, 251, 248);
      doc.setDrawColor(200, 228, 212);
      doc.setLineWidth(0.25);
      doc.roundedRect(mg, notesY - 4, tX - mg - 6, numSummaryRows * rowH + 8, 2, 2, 'FD');
      doc.setFontSize(7.5);
      doc.setFont('helvetica', 'bold');
      doc.setTextColor(hR, hG, hB);
      doc.text('NOTES :', mg + 5, notesY + 3);
      doc.setFont('helvetica', 'normal');
      doc.setTextColor(58, 82, 68);
      const splitDesc = doc.splitTextToSize(factureData.description, tX - mg - 16);
      doc.text(splitDesc, mg + 5, notesY + 10);
      doc.setTextColor(0, 0, 0);
    }

    // ── FOOTER BAR (268 – 297 mm) ─────────────────────────────────────────────
    doc.setFillColor(footerR, footerG, footerB);
    doc.rect(0, 268, W, 29, 'F');
    doc.setFillColor(
      Math.min(255, footerR + 32),
      Math.min(255, footerG + 28),
      Math.min(255, footerB + 22)
    );
    doc.rect(0, 268, W, 1.5, 'F');

    doc.setTextColor(255, 255, 255);
    const nif = this.entreprise.numeroIdentificationFiscale || '';
    const rc  = this.entreprise.registreCommerce || '';
    const footerMsg = caract?.messageFooter || 'Merci pour votre confiance !';

    doc.setFontSize(8);
    doc.setFont('helvetica', 'bold');
    if (nif || rc) {
      doc.text(`NIF : ${nif}   •   RC : ${rc}`, mg, 277);
    } else {
      doc.text(String(this.entreprise.nom || ''), mg, 277);
    }
    doc.setFont('helvetica', 'normal');
    if (this.entreprise.adresse) {
      doc.setFontSize(7.5);
      doc.text(this.entreprise.adresse, mg, 284);
    }

    if (this.entreprise.siteWeb) {
      doc.setFontSize(7.5);
      doc.setTextColor(185, 228, 205);
      doc.text(this.entreprise.siteWeb, W / 2, 277, { align: 'center' });
      doc.setTextColor(255, 255, 255);
    }

    doc.setFontSize(8);
    doc.setFont('helvetica', 'bold');
    doc.text(footerMsg, W - mg, 277, { align: 'right' });
    doc.setFont('helvetica', 'normal');
    doc.setFontSize(6.5);
    doc.setTextColor(188, 225, 205);
    doc.text(
      `Document généré le ${new Date().toLocaleString('fr-FR')}`,
      W - mg, 284, { align: 'right' }
    );
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
