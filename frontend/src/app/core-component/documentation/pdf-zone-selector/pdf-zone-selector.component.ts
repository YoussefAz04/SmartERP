import { Component, ElementRef, OnInit, ViewChild } from '@angular/core';
import { environment } from '../../../../environments/environment';
import { HttpClient } from "@angular/common/http";
import { NgClass, NgForOf, NgIf } from "@angular/common";
import { FormsModule } from "@angular/forms";
import { PDFDocument } from 'pdf-lib';

interface Zone {
  type: string;
  x: number;
  y: number;
  width: number;
  height: number;
  pageNumber: number;
  typeDeDocument: string;
  canvasWidth?: number;
  canvasHeight?: number;
  pdfWidth?: number;
  pdfHeight?: number;
}

interface ZoneExtractionRequest {
  pdfBase64: string;
  zones: Zone[];
}

@Component({
  selector: 'app-pdf-zone-selector',
  standalone: true,
  imports: [NgIf, FormsModule, NgForOf, NgClass],
  templateUrl:'./pdf-zone-selector.component.html',
  styleUrl:'pdf-zone-selector.component.scss'
})
export class PdfZoneSelectorComponent implements OnInit {
  @ViewChild('pdfCanvas', { static: false }) canvasRef!: ElementRef<HTMLCanvasElement>;
  @ViewChild('pdfViewer', { static: false }) viewerRef!: ElementRef<HTMLDivElement>;

  // Supprimé selectedZoneType car on le sélectionne après
  typeDeDocument: string = '';

  zones: Zone[] = [];
  extractionResults: any[] = [];

  isSelecting: boolean = false;
  selectionStart = { x: 0, y: 0 };
  currentSelection = { x: 0, y: 0, width: 0, height: 0 };

  // Nouvelles propriétés pour la modal de sélection de type
  showZoneTypeModal: boolean = false;
  tempZoneType: string = '';
  pendingZone: Zone | null = null;

  pdfDocument: PDFDocument | null = null;
  currentPage: number = 1;
  totalPages: number = 0;
  pdfBase64: string = '';
  pdfLoaded: boolean = false;
  isLoading: boolean = false;

  // Canvas et page info
  private canvasScale = 1.5;
  private pageImages: string[] = [];
  private isDragging = false;
  // Nouvelles propriétés pour stocker les dimensions réelles des pages PDF
  private pageDimensions: { width: number, height: number }[] = [];

  constructor(private http: HttpClient) {}

  ngOnInit() {
    console.log('PDF Zone Selector initialisé');
    document.addEventListener('mousemove', this.onDocumentMouseMove.bind(this));
    document.addEventListener('mouseup', this.onDocumentMouseUp.bind(this));
  }

  ngOnDestroy() {
    document.removeEventListener('mousemove', this.onDocumentMouseMove.bind(this));
    document.removeEventListener('mouseup', this.onDocumentMouseUp.bind(this));
  }

  async onFileSelected(event: any) {
    const file = event.target.files[0];
    if (file && file.type === 'application/pdf') {
      this.isLoading = true;
      try {
        const arrayBuffer = await this.fileToArrayBuffer(file);
        this.pdfBase64 = this.arrayBufferToBase64(arrayBuffer);

        // Charger le PDF avec pdf-lib
        this.pdfDocument = await PDFDocument.load(arrayBuffer);
        this.totalPages = this.pdfDocument.getPageCount();

        // Extraire les dimensions réelles des pages
        await this.extractPageDimensions();

        // Convertir toutes les pages en images
        await this.convertPagesToImages(arrayBuffer);

        // Afficher la première page
        await this.renderPage(1);

        this.pdfLoaded = true;
        this.zones = [];
        this.extractionResults = [];

      } catch (error) {
        console.error('Erreur lors du chargement du PDF:', error);
        alert('Erreur lors du chargement du PDF. Veuillez vérifier le fichier.');
      } finally {
        this.isLoading = false;
      }
    } else {
      alert('Veuillez sélectionner un fichier PDF valide');
    }
  }

  // Nouvelle méthode pour extraire les dimensions réelles des pages PDF
  private async extractPageDimensions(): Promise<void> {
    if (!this.pdfDocument) return;

    this.pageDimensions = [];
    for (let i = 0; i < this.totalPages; i++) {
      const page = this.pdfDocument.getPage(i);
      const { width, height } = page.getSize();
      this.pageDimensions.push({ width, height });
    }
  }

  private fileToArrayBuffer(file: File): Promise<ArrayBuffer> {
    return new Promise((resolve, reject) => {
      const reader = new FileReader();
      reader.onload = () => resolve(reader.result as ArrayBuffer);
      reader.onerror = reject;
      reader.readAsArrayBuffer(file);
    });
  }

  private arrayBufferToBase64(buffer: ArrayBuffer): string {
    let binary = '';
    const bytes = new Uint8Array(buffer);
    for (let i = 0; i < bytes.byteLength; i++) {
      binary += String.fromCharCode(bytes[i]);
    }
    return btoa(binary);
  }

  private async convertPagesToImages(pdfBuffer: ArrayBuffer): Promise<void> {
    const pdfjsLib = (window as any).pdfjsLib;
    if (!pdfjsLib) {
      await this.loadPdfJs();
    }

    try {
      const pdf = await (window as any).pdfjsLib.getDocument({ data: pdfBuffer }).promise;
      this.pageImages = [];

      for (let i = 1; i <= this.totalPages; i++) {
        const page = await pdf.getPage(i);
        const viewport = page.getViewport({ scale: this.canvasScale });

        const canvas = document.createElement('canvas');
        const context = canvas.getContext('2d')!;
        canvas.height = viewport.height;
        canvas.width = viewport.width;

        await page.render({
          canvasContext: context,
          viewport: viewport
        }).promise;

        this.pageImages.push(canvas.toDataURL());
      }
    } catch (error) {
      console.error('Erreur conversion pages:', error);
      this.pageImages = Array(this.totalPages).fill('');
    }
  }

  private async loadPdfJs(): Promise<void> {
    return new Promise((resolve, reject) => {
      const script = document.createElement('script');
      script.src = 'https://cdnjs.cloudflare.com/ajax/libs/pdf.js/3.11.174/pdf.min.js';
      script.onload = () => {
        (window as any).pdfjsLib.GlobalWorkerOptions.workerSrc =
          'https://cdnjs.cloudflare.com/ajax/libs/pdf.js/3.11.174/pdf.worker.min.js';
        resolve();
      };
      script.onerror = reject;
      document.head.appendChild(script);
    });
  }

  async renderPage(pageNumber: number) {
    if (!this.canvasRef || !this.pageImages[pageNumber - 1]) return;

    const canvas = this.canvasRef.nativeElement;
    const context = canvas.getContext('2d')!;

    if (this.pageImages[pageNumber - 1]) {
      const img = new Image();
      img.onload = () => {
        canvas.width = img.width;
        canvas.height = img.height;
        context.clearRect(0, 0, canvas.width, canvas.height);
        context.drawImage(img, 0, 0);

        // Redessiner les zones après le rendu de la page
        this.redrawZones();
      };
      img.src = this.pageImages[pageNumber - 1];
    } else {
      canvas.width = 595 * this.canvasScale;
      canvas.height = 842 * this.canvasScale;
      context.fillStyle = 'white';
      context.fillRect(0, 0, canvas.width, canvas.height);
      context.fillStyle = 'black';
      context.font = '20px Arial';
      context.textAlign = 'center';
      context.fillText(`Page ${pageNumber}`, canvas.width/2, canvas.height/2);
    }

    this.currentPage = pageNumber;
  }

  private redrawZones() {
    if (!this.canvasRef) return;

    const canvas = this.canvasRef.nativeElement;
    const context = canvas.getContext('2d')!;

    const currentPageZones = this.getCurrentPageZones();
    currentPageZones.forEach(zone => {
      context.strokeStyle = this.getZoneColor(zone.type);
      context.lineWidth = 2;
      context.strokeRect(zone.x, zone.y, zone.width, zone.height);

      // Ajouter un fond semi-transparent
      context.fillStyle = this.getZoneColor(zone.type) + '20';
      context.fillRect(zone.x, zone.y, zone.width, zone.height);

      // Ajouter un label pour identifier la zone
      context.fillStyle = this.getZoneColor(zone.type);
      context.font = '12px Arial';
      context.fillText(zone.type, zone.x + 2, zone.y - 2);
    });
  }

  previousPage() {
    if (this.currentPage > 1) {
      this.renderPage(this.currentPage - 1);
    }
  }

  nextPage() {
    if (this.currentPage < this.totalPages) {
      this.renderPage(this.currentPage + 1);
    }
  }

  getCurrentPageZones(): Zone[] {
    return this.zones.filter(zone => zone.pageNumber === this.currentPage);
  }

  startSelection(event: MouseEvent) {
    if (!this.canvasRef || this.showZoneTypeModal) return;

    event.preventDefault();

    const rect = this.canvasRef.nativeElement.getBoundingClientRect();

    this.selectionStart = {
      x: event.clientX - rect.left,
      y: event.clientY - rect.top
    };

    this.isSelecting = true;
    this.isDragging = true;

    this.currentSelection = {
      x: this.selectionStart.x,
      y: this.selectionStart.y,
      width: 0,
      height: 0
    };
  }

  updateSelection(event: MouseEvent) {
    if (!this.isSelecting || !this.isDragging) return;
    event.preventDefault();
    this.onDocumentMouseMove(event);
  }

  private onDocumentMouseMove(event: MouseEvent) {
    if (!this.isSelecting || !this.canvasRef) return;

    const rect = this.canvasRef.nativeElement.getBoundingClientRect();
    const currentX = event.clientX - rect.left;
    const currentY = event.clientY - rect.top;

    // Limiter les coordonnées aux limites du canvas
    const canvas = this.canvasRef.nativeElement;
    const boundedX = Math.max(0, Math.min(currentX, canvas.width));
    const boundedY = Math.max(0, Math.min(currentY, canvas.height));

    this.currentSelection = {
      x: Math.min(this.selectionStart.x, boundedX),
      y: Math.min(this.selectionStart.y, boundedY),
      width: Math.abs(boundedX - this.selectionStart.x),
      height: Math.abs(boundedY - this.selectionStart.y)
    };
  }

  endSelection(event: MouseEvent) {
    this.onDocumentMouseUp(event);
  }

  private onDocumentMouseUp(event: MouseEvent) {
    if (!this.isSelecting || !this.canvasRef) return;

    this.isSelecting = false;
    this.isDragging = false;

    const minSize = 5;
    if (this.currentSelection.width > minSize && this.currentSelection.height > minSize) {
      const canvas = this.canvasRef.nativeElement;
      const pageIndex = this.currentPage - 1;

      // Créer une zone temporaire sans type
      this.pendingZone = {
        type: '', // Sera rempli après sélection du type
        x: this.currentSelection.x,
        y: this.currentSelection.y,
        width: this.currentSelection.width,
        height: this.currentSelection.height,
        pageNumber: this.currentPage,
        typeDeDocument: this.typeDeDocument,
        // Stocker les dimensions pour la conversion backend
        canvasWidth: canvas.width,
        canvasHeight: canvas.height,
        pdfWidth: this.pageDimensions[pageIndex]?.width || 595,
        pdfHeight: this.pageDimensions[pageIndex]?.height || 842
      };

      // Ouvrir la modal pour sélectionner le type
      this.tempZoneType = '';
      this.showZoneTypeModal = true;

      console.log('Zone créée en attente de type:', this.pendingZone);
    }

    this.currentSelection = { x: 0, y: 0, width: 0, height: 0 };
  }

  // Nouvelle méthode pour confirmer la création de zone
  confirmZoneCreation() {
    if (!this.pendingZone || !this.tempZoneType) return;

    // Assigner le type à la zone
    this.pendingZone.type = this.tempZoneType;

    // Ajouter la zone à la liste finale
    this.zones.push(this.pendingZone);
    console.log('Zone confirmée et ajoutée:', this.pendingZone);

    // Nettoyer les variables temporaires
    this.pendingZone = null;
    this.tempZoneType = '';
    this.showZoneTypeModal = false;

    // Redessiner les zones
    this.redrawZones();
  }

  // Nouvelle méthode pour annuler la création de zone
  cancelZoneCreation() {
    console.log('Création de zone annulée');
    this.pendingZone = null;
    this.tempZoneType = '';
    this.showZoneTypeModal = false;
  }

  get selectionWidth(): number {
    return Math.abs(this.currentSelection.width);
  }

  get selectionHeight(): number {
    return Math.abs(this.currentSelection.height);
  }

  deleteZone(index: number) {
    const currentPageZones = this.getCurrentPageZones();
    if (index < 0 || index >= currentPageZones.length) return;

    const zoneToDelete = currentPageZones[index];

    const globalIndex = this.zones.findIndex(zone =>
      zone.x === zoneToDelete.x &&
      zone.y === zoneToDelete.y &&
      zone.pageNumber === zoneToDelete.pageNumber &&
      zone.type === zoneToDelete.type &&
      zone.width === zoneToDelete.width &&
      zone.height === zoneToDelete.height
    );

    if (globalIndex !== -1) {
      this.zones.splice(globalIndex, 1);
      console.log('Zone supprimée:', zoneToDelete);
      this.redrawZones();
    }
  }

  clearZones() {
    this.zones = [];
    this.extractionResults = [];
    this.pendingZone = null;
    this.showZoneTypeModal = false;
    this.renderPage(this.currentPage);
  }

  async saveZonesToBackend() {
    if (this.zones.length === 0) {
      alert('Aucune zone à sauvegarder');
      return;
    }

    this.isLoading = true;

    try {
      const response = await this.http.post(`${environment.apiUrl}/api/zones/save-all`, this.zones).toPromise();
      console.log('Réponse du serveur:', response);
      alert(`${this.zones.length} zone(s) enregistrée(s) avec succès dans la base de données.`);
    } catch (error) {
      console.error('Erreur lors de la sauvegarde des zones :', error);
      alert('Échec de l\'enregistrement. Vérifiez que le serveur backend est démarré sur localhost:8020');
    } finally {
      this.isLoading = false;
    }
  }

  async extractZonesContent() {
    if (this.zones.length === 0) {
      alert('Aucune zone à extraire');
      return;
    }

    this.isLoading = true;

    try {
      const requestData: ZoneExtractionRequest = {
        pdfBase64: this.pdfBase64,
        zones: this.zones
      };

      const response: any = await this.http.post(`${environment.apiUrl}/api/zones/extract`, requestData).toPromise();

      this.extractionResults = response.results || [];

      if (this.extractionResults.length > 0) {
        alert(`Contenu extrait de ${this.extractionResults.length} zone(s)`);
        console.log('Résultats d\'extraction:', this.extractionResults);
      }

    } catch (error) {
      console.error('Erreur lors de l\'extraction :', error);
      alert('Échec de l\'extraction. Vérifiez le serveur backend.');
    } finally {
      this.isLoading = false;
    }
  }

  getZoneColor(type: string): string {
    const colors: { [key: string]: string } = {
      'montantHT': '#3498db',
      'montantTVA': '#e74c3c',
      'montantTTC': '#9b59b6',
      'date': '#f39c12',
      'modePaiement': '#2ecc71',
      'statut': '#e67e22',
      'description': '#34495e',
      'quantite': '#16a085',
      'remise': '#8e44ad',
      'prixUnitaireHT': '#27ae60',
      'tva': '#c0392b',
      'article': '#2980b9',
      'referanc': '#7f8c8d'
    };
    return colors[type] || '#95a5a6';
  }

  // Nouvelle méthode utilitaire pour déboguer les coordonnées
  debugZoneCoordinates(zone: Zone) {
    if (!zone.canvasWidth || !zone.canvasHeight || !zone.pdfWidth || !zone.pdfHeight) {
      console.warn('Dimensions manquantes pour la zone:', zone);
      return;
    }

    const scaleX = zone.pdfWidth / zone.canvasWidth;
    const scaleY = zone.pdfHeight / zone.canvasHeight;

    const pdfCoords = {
      x: zone.x * scaleX,
      y: zone.y * scaleY,
      width: zone.width * scaleX,
      height: zone.height * scaleY
    };

    console.log('Zone canvas:', { x: zone.x, y: zone.y, width: zone.width, height: zone.height });
    console.log('Zone PDF:', pdfCoords);
    console.log('Échelles:', { scaleX, scaleY });
  }
}
