import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { StockService } from '../../../core/service/stock/stock.service';
import { StockMouvementService } from '../../../core/service/stock-mouvement/stock-mouvement.service';
import { Stock } from '../../../model/stock.model';
import { StockMouvementDto, TypeMouvement } from '../../../model/stock-mouvement.model';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-adjust-stock',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './adjust-stock.component.html',
  styleUrl: './adjust-stock.component.scss'
})
export class AdjustStockComponent implements OnInit {
  adjustForm!: FormGroup;
  stocks: Stock[] = [];
  selectedStock: Stock | null = null;
  isLoading = false;
  isSubmitting = false;
  submitted = false;

  get quantiteActuelle(): number {
    return this.selectedStock?.quantiteDisponible || 0;
  }

  get quantiteNouvelle(): number {
    const type = this.adjustForm?.get('typeMouvement')?.value as TypeMouvement;
    const qty = +(this.adjustForm?.get('quantite')?.value || 0);
    if (!type || !qty) return this.quantiteActuelle;
    if (type === 'ENTREE') return this.quantiteActuelle + qty;
    if (type === 'SORTIE') return this.quantiteActuelle - qty;
    if (type === 'AJUSTEMENT') return qty;
    return this.quantiteActuelle;
  }

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private stockService: StockService,
    private mouvementService: StockMouvementService
  ) {}

  ngOnInit(): void {
    this.initForm();
    this.loadStocks();
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.adjustForm.patchValue({ stockId: +id });
      this.loadStockById(+id);
    }
  }

  initForm(): void {
    this.adjustForm = this.fb.group({
      stockId: [null, Validators.required],
      typeMouvement: ['ENTREE', Validators.required],
      quantite: [null, [Validators.required, Validators.min(1)]],
      localisation: [''],
      motif: ['', Validators.required],
      referenceCommande: [''],
      dateMouvement: [new Date().toISOString().substring(0, 16)]
    });

    this.adjustForm.get('stockId')?.valueChanges.subscribe(id => {
      if (id) this.loadStockById(+id);
    });
  }

  loadStocks(): void {
    this.isLoading = true;
    this.stockService.getAllStocks().subscribe({
      next: (data) => {
        this.stocks = data;
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Erreur chargement stocks:', err);
        this.isLoading = false;
      }
    });
  }

  loadStockById(id: number): void {
    const existing = this.stocks.find(s => s.id === id);
    if (existing) {
      this.selectedStock = existing;
      this.adjustForm.patchValue({ localisation: existing.localisation || '' });
    } else {
      this.stockService.getStockById(id).subscribe({
        next: (data) => {
          this.selectedStock = data;
          this.adjustForm.patchValue({ localisation: data.localisation || '' });
        },
        error: (err) => console.error('Erreur chargement stock:', err)
      });
    }
  }

  isFieldInvalid(field: string): boolean {
    const f = this.adjustForm.get(field);
    return !!(f && f.invalid && (f.dirty || f.touched || this.submitted));
  }

  getStockLabel(stock: Stock): string {
    const name = stock.articleAchat?.nom || `Stock #${stock.id}`;
    const qty = stock.quantiteDisponible || 0;
    const loc = stock.localisation ? ` (${stock.localisation})` : '';
    return `${name}${loc} - Qté: ${qty}`;
  }

  onSubmit(): void {
    this.submitted = true;
    if (this.adjustForm.invalid) return;

    const type = this.adjustForm.get('typeMouvement')?.value as TypeMouvement;
    const qty = +this.adjustForm.get('quantite')?.value;
    const newQty = this.quantiteNouvelle;

    if (type === 'SORTIE' && newQty < 0) {
      Swal.fire('Erreur', 'La quantité ne peut pas être négative après la sortie.', 'error');
      return;
    }

    const isLargeAdjustment = Math.abs(qty) > 100 || (type === 'SORTIE' && qty > this.quantiteActuelle * 0.5);

    const performSubmit = () => {
      this.isSubmitting = true;
      const dto: StockMouvementDto = {
        stockId: +this.adjustForm.get('stockId')?.value,
        typeMouvement: type,
        quantite: qty,
        motif: this.adjustForm.get('motif')?.value,
        referenceCommande: this.adjustForm.get('referenceCommande')?.value || undefined,
        dateMouvement: this.adjustForm.get('dateMouvement')?.value
      };

      const localisation = this.adjustForm.get('localisation')?.value || undefined;
      this.mouvementService.createMovement(dto).subscribe({
        next: () => {
          const updatedQty = newQty;
          this.stockService.updateStock(dto.stockId, { quantiteDisponible: updatedQty, localisation }).subscribe({
            next: () => {
              this.isSubmitting = false;
              Swal.fire('Succès', 'Stock ajusté avec succès!', 'success').then(() => {
                this.router.navigate(['/stock/view', dto.stockId]);
              });
            },
            error: () => {
              this.isSubmitting = false;
              Swal.fire('Succès partiel', 'Mouvement créé mais erreur lors de la mise à jour du stock.', 'warning');
            }
          });
        },
        error: (err) => {
          this.isSubmitting = false;
          Swal.fire('Erreur', "Impossible de créer le mouvement de stock.", 'error');
        }
      });
    };

    if (isLargeAdjustment) {
      Swal.fire({
        title: 'Confirmer l\'ajustement',
        html: `Vous êtes sur le point de <strong>${type}</strong> <strong>${qty}</strong> unités.<br>
               Nouvelle quantité: <strong>${newQty}</strong>`,
        icon: 'warning',
        showCancelButton: true,
        confirmButtonText: 'Confirmer',
        cancelButtonText: 'Annuler'
      }).then(result => {
        if (result.isConfirmed) performSubmit();
      });
    } else {
      performSubmit();
    }
  }

  cancel(): void {
    const stockId = this.adjustForm.get('stockId')?.value;
    if (stockId) {
      this.router.navigate(['/stock/view', stockId]);
    } else {
      this.router.navigate(['/stock/list']);
    }
  }
}
