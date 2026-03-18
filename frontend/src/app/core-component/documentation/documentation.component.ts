// documentation.component.ts
import { Component, ViewChild, ElementRef, AfterViewInit } from '@angular/core';
import { PdfViewerModule } from "ng2-pdf-viewer";
import {DecimalPipe, NgForOf, NgIf, NgStyle} from "@angular/common";
import { FormsModule } from "@angular/forms";
import { HttpClient, HttpErrorResponse } from "@angular/common/http";
import { catchError, finalize } from 'rxjs/operators';
import { throwError } from 'rxjs';

interface SelectedZone {
  id: string;
  x: number;
  y: number;
  width: number;
  height: number;
  type: string;
  color: string;
  pageNumber?: number;
}

interface ZoneExtractionResult {
  type: string;
  text: string;
  success: boolean;
  error?: string;
}

@Component({
  selector: 'app-documentation',
  standalone: true,
  imports: [
    PdfViewerModule,
    NgForOf,
    NgStyle,
    NgIf,
    FormsModule,
    DecimalPipe
  ],
  templateUrl: './documentation.component.html',
  styleUrl: './documentation.component.scss'
})
export class DocumentationComponent implements AfterViewInit {
  @ViewChild('pdfContainer') pdfContainer!: ElementRef;

  // PDF properties
  pdfSrc = '/assets/autorisation_travail_format_original_italien.pdf';
  pdfLoaded = false;
  pdfError = false;
  errorMessage = '';
  currentPage = 1;
  totalPages = 0;
  zoomLevel = 1;
  pdfDocument: any = null;

  // Selection properties
  selecting = false;
  startX = 0;
  startY = 0;
  currentX = 0;
  currentY = 0;
  boxStyle: any = {};

  // Zone management
  zoneTypes = [
    { label: 'Nom', value: 'nom', color: '#ff6b6b' },
    { label: 'Prénom', value: 'prenom', color: '#ff8c42' },
    { label: 'Date de naissance', value: 'date_naissance', color: '#4ecdc4' },
    { label: 'Date du document', value: 'date_document', color: '#45b7d1' },
    { label: 'Signature', value: 'signature', color: '#6c5ce7' },
    { label: 'Adresse', value: 'adresse', color: '#96ceb4' },
    { label: 'Téléphone', value: 'telephone', color: '#feca57' },
    { label: 'Email', value: 'email', color: '#ff9ff3' },
    { label: 'Code postal', value: 'code_postal', color: '#fd79a8' },
    { label: 'Ville', value: 'ville', color: '#55a3ff' },
    { label: 'Numéro de document', value: 'numero_document', color: '#00cec9' },
    { label: 'Autre', value: 'autre', color: '#a55eea' }
  ];

  selectedZones: SelectedZone[] = [];
  showZoneTypeDialog = false;
  pendingZone: Omit<SelectedZone, 'type' | 'color' | 'id' | 'pageNumber'> | null = null;
  selectedZoneType = '';

  // File handling
  selectedPdfFile: File | null = null;
  isProcessing = false;
  extractionResults: ZoneExtractionResult[] = [];

  // UI state
  showResults = false;
  showFileUpload = false;

  constructor(private http: HttpClient) {}

  ngAfterViewInit() {
    // Add keyboard event listener to document
    document.addEventListener('keydown', this.onKeyDown.bind(this));
  }

  ngOnDestroy() {
    document.removeEventListener('keydown', this.onKeyDown.bind(this));
  }

  // File handling methods
  onFileSelected(event: any) {
    const file = event.target.files[0];
    if (file && file.type === 'application/pdf') {
      this.selectedPdfFile = file;
      this.pdfSrc = URL.createObjectURL(file);
      this.clearAllZones();
      this.showFileUpload = false;
    } else {
      alert('Veuillez sélectionner un fichier PDF valide.');
    }
  }

  // Selection methods
  startSelection(event: MouseEvent) {
    if (this.showZoneTypeDialog || this.isProcessing) return;

    this.selecting = true;
    const container = this.getPdfContainer(event.target as HTMLElement);
    if (!container) return;

    const rect = container.getBoundingClientRect();
    this.startX = event.clientX - rect.left;
    this.startY = event.clientY - rect.top;

    this.boxStyle = {
      left: `${this.startX}px`,
      top: `${this.startY}px`,
      width: '0px',
      height: '0px',
      border: '2px dashed #007bff',
      backgroundColor: 'rgba(0, 123, 255, 0.1)',
      position: 'absolute',
      pointerEvents: 'none',
      zIndex: 1000
    };

    event.preventDefault();
  }

  drawSelection(event: MouseEvent) {
    if (!this.selecting || this.showZoneTypeDialog) return;

    const container = this.getPdfContainer(event.target as HTMLElement);
    if (!container) return;

    const rect = container.getBoundingClientRect();
    this.currentX = event.clientX - rect.left;
    this.currentY = event.clientY - rect.top;

    const x = Math.min(this.startX, this.currentX);
    const y = Math.min(this.startY, this.currentY);
    const width = Math.abs(this.currentX - this.startX);
    const height = Math.abs(this.currentY - this.startY);

    this.boxStyle = {
      ...this.boxStyle,
      left: `${x}px`,
      top: `${y}px`,
      width: `${width}px`,
      height: `${height}px`
    };
  }

  endSelection(event: MouseEvent) {
    if (!this.selecting || this.showZoneTypeDialog) return;

    this.selecting = false;

    const x = parseInt(this.boxStyle.left, 10);
    const y = parseInt(this.boxStyle.top, 10);
    const width = parseInt(this.boxStyle.width, 10);
    const height = parseInt(this.boxStyle.height, 10);

    // Only create zone if it has meaningful dimensions
    if (width > 10 && height > 10) {
      this.pendingZone = { x, y, width, height };
      this.showZoneTypeDialog = true;
      this.selectedZoneType = '';
    } else {
      this.boxStyle = {};
    }
  }

  // Zone management methods
  confirmZoneType() {
    if (!this.pendingZone || !this.selectedZoneType) return;

    const zoneTypeConfig = this.zoneTypes.find(type => type.value === this.selectedZoneType);
    if (!zoneTypeConfig) return;

    const newZone: SelectedZone = {
      id: this.generateId(),
      ...this.pendingZone,
      type: this.selectedZoneType,
      color: zoneTypeConfig.color,
      pageNumber: this.currentPage
    };

    this.selectedZones.push(newZone);
    this.closeZoneTypeDialog();
  }

  cancelZoneSelection() {
    this.closeZoneTypeDialog();
  }

  private closeZoneTypeDialog() {
    this.showZoneTypeDialog = false;
    this.pendingZone = null;
    this.selectedZoneType = '';
    this.boxStyle = {};
  }

  deleteZone(zoneId: string) {
    this.selectedZones = this.selectedZones.filter(zone => zone.id !== zoneId);
  }

  clearAllZones() {
    if (this.selectedZones.length === 0) return;

    if (confirm('Êtes-vous sûr de vouloir supprimer toutes les zones ?')) {
      this.selectedZones = [];
      this.extractionResults = [];
      this.showResults = false;
    }
  }

  getZoneStyle(zone: SelectedZone) {
    return {
      position: 'absolute',
      left: `${zone.x}px`,
      top: `${zone.y}px`,
      width: `${zone.width}px`,
      height: `${zone.height}px`,
      border: `2px solid ${zone.color}`,
      backgroundColor: `${zone.color}20`,
      pointerEvents: 'auto',
      zIndex: 999,
      cursor: 'pointer'
    };
  }

  // Backend communication
  sendZonesToBackend() {
    if (!this.selectedPdfFile && !this.pdfSrc.startsWith('blob:')) {
      alert('Veuillez d\'abord sélectionner un fichier PDF.');
      return;
    }

    if (this.selectedZones.length === 0) {
      alert('Veuillez d\'abord sélectionner au moins une zone.');
      return;
    }

    this.isProcessing = true;
    this.extractionResults = [];

    const formData = new FormData();

    if (this.selectedPdfFile) {
      formData.append('file', this.selectedPdfFile);
    } else {
      // If using default PDF, we need to fetch it first
      this.fetchDefaultPdfAndExtract();
      return;
    }

    // Convert zones to the format expected by backend
    const backendZones = this.selectedZones.map(zone => ({
      id: zone.id,
      x: Math.round(zone.x / this.zoomLevel),
      y: Math.round(zone.y / this.zoomLevel),
      width: Math.round(zone.width / this.zoomLevel),
      height: Math.round(zone.height / this.zoomLevel),
      type: zone.type
    }));

    formData.append('zones', JSON.stringify(backendZones));

    this.http.post<ZoneExtractionResult[]>('http://localhost:8020/api/pdf/extract-zones', formData)
      .pipe(
        catchError((error: HttpErrorResponse) => {
          console.error('Erreur lors de l\'extraction:', error);
          let errorMessage = 'Erreur lors de l\'extraction des zones.';

          if (error.status === 0) {
            errorMessage = 'Impossible de contacter le serveur. Vérifiez que le backend est démarré.';
          } else if (error.status >= 400 && error.status < 500) {
            errorMessage = 'Erreur dans la requête. Vérifiez les données envoyées.';
          } else if (error.status >= 500) {
            errorMessage = 'Erreur serveur lors du traitement du PDF.';
          }

          alert(errorMessage);
          return throwError(() => error);
        }),
        finalize(() => {
          this.isProcessing = false;
        })
      )
      .subscribe({
        next: (results) => {
          this.extractionResults = results;
          this.showResults = true;
          console.log('Résultats d\'extraction:', results);
        },
        error: (error) => {
          console.error('Erreur:', error);
        }
      });
  }

  private fetchDefaultPdfAndExtract() {
    fetch(this.pdfSrc)
      .then(response => response.blob())
      .then(blob => {
        this.selectedPdfFile = new File([blob], 'document.pdf', { type: 'application/pdf' });
        this.sendZonesToBackend();
      })
      .catch(error => {
        console.error('Erreur lors du chargement du PDF par défaut:', error);
        alert('Erreur lors du chargement du fichier PDF.');
        this.isProcessing = false;
      });
  }

  // PDF event handlers
  onPdfLoadComplete(pdf: any) {
    this.pdfLoaded = true;
    this.pdfError = false;
    this.pdfDocument = pdf;
    this.totalPages = pdf.numPages;
    console.log('PDF loaded successfully:', pdf);
  }

  onPdfLoadError(error: any) {
    this.pdfError = true;
    this.pdfLoaded = false;
    this.errorMessage = 'Erreur lors du chargement du PDF. Vérifiez que le fichier existe et est accessible.';
    console.error('PDF loading error:', error);
  }

  onPdfLoadProgress(progress: any) {
    console.log('PDF loading progress:', progress);
  }

  // Navigation methods
  previousPage() {
    if (this.currentPage > 1) {
      this.currentPage--;
      this.clearCurrentPageZones();
    }
  }

  nextPage() {
    if (this.currentPage < this.totalPages) {
      this.currentPage++;
      this.clearCurrentPageZones();
    }
  }

  private clearCurrentPageZones() {
    // Optionally clear zones when changing pages, or filter by page
    // this.selectedZones = this.selectedZones.filter(zone => zone.pageNumber !== this.currentPage);
  }

  // Zoom methods
  zoomIn() {
    if (this.zoomLevel < 3) {
      this.zoomLevel += 0.25;
    }
  }

  zoomOut() {
    if (this.zoomLevel > 0.5) {
      this.zoomLevel -= 0.25;
    }
  }

  resetZoom() {
    this.zoomLevel = 1;
  }

  // Utility methods
  private getPdfContainer(element: HTMLElement): HTMLElement | null {
    return element.closest('.pdf-container') as HTMLElement;
  }

  private generateId(): string {
    return Date.now().toString(36) + Math.random().toString(36).substr(2);
  }

  trackByZoneId(index: number, zone: SelectedZone): string {
    return zone.id;
  }

  onKeyDown(event: KeyboardEvent) {
    if (event.key === 'Escape') {
      if (this.selecting) {
        this.selecting = false;
        this.boxStyle = {};
      }
      if (this.showZoneTypeDialog) {
        this.cancelZoneSelection();
      }
      if (this.showResults) {
        this.showResults = false;
      }
    }

    // Keyboard shortcuts
    if (event.ctrlKey || event.metaKey) {
      switch (event.key) {
        case 'z':
          event.preventDefault();
          if (this.selectedZones.length > 0) {
            this.selectedZones.pop();
          }
          break;
        case 'a':
          event.preventDefault();
          this.clearAllZones();
          break;
        case 'e':
          event.preventDefault();
          if (this.selectedZones.length > 0) {
            this.sendZonesToBackend();
          }
          break;
      }
    }
  }

  // Export/Import functionality
  exportZones() {
    if (this.selectedZones.length === 0) {
      alert('Aucune zone à exporter.');
      return;
    }

    const dataStr = JSON.stringify(this.selectedZones, null, 2);
    const dataUri = 'data:application/json;charset=utf-8,'+ encodeURIComponent(dataStr);

    const exportFileDefaultName = 'zones_pdf.json';

    const linkElement = document.createElement('a');
    linkElement.setAttribute('href', dataUri);
    linkElement.setAttribute('download', exportFileDefaultName);
    linkElement.click();
  }

  importZones(event: any) {
    const file = event.target.files[0];
    if (file && file.type === 'application/json') {
      const reader = new FileReader();
      reader.onload = (e) => {
        try {
          const zones = JSON.parse(e.target?.result as string);
          if (Array.isArray(zones)) {
            this.selectedZones = zones;
            console.log('Zones importées avec succès');
          } else {
            alert('Format de fichier invalide.');
          }
        } catch (error) {
          alert('Erreur lors de la lecture du fichier JSON.');
        }
      };
      reader.readAsText(file);
    } else {
      alert('Veuillez sélectionner un fichier JSON valide.');
    }
  }

  getZoneTypeLabel(type: string): string {
    const zoneType = this.zoneTypes.find(zt => zt.value === type);
    return zoneType ? zoneType.label : type;
  }
}
