import { Component } from '@angular/core';
import { jsPDF } from "jspdf";

@Component({
  selector: 'app-generate-pdf-facture-de-vente',
  standalone: true,
  imports: [],
  templateUrl: './generate-pdf-facture-de-vente.component.html',
  styleUrl: './generate-pdf-facture-de-vente.component.scss'
})
export class GeneratePdfFactureDeVenteComponent {
  generatePDF() {
    const doc = new jsPDF();

    doc.text("Hello world!", 10, 10);
    doc.save("facture-de-vente.pdf");
  }
}
