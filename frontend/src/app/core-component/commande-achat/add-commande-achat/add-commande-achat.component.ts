import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, FormArray, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, ActivatedRoute, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { CommandeAchatService } from '../../../core/service/commande-achat/commande-achat.service';
import { ArticleAchatService } from '../../../core/service/article-achat/article-achat.service';
import { FournisseurService } from '../../../core/service/fournisseur/fournisseur.service';
import { ArticleAchat } from '../../../model/article-achat.model';
import { Fournisseur } from '../../../model/fournisseur.model';

@Component({
  selector: 'app-add-commande-achat',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './add-commande-achat.component.html',
  styleUrl: './add-commande-achat.component.scss'
})
export class AddCommandeAchatComponent implements OnInit {
  commandeForm!: FormGroup;
  fournisseurs: Fournisseur[] = [];
  articles: ArticleAchat[] = [];
  isLoading = false;
  submitted = false;
  isEditMode = false;
  commandeId: number | null = null;

  constructor(
    private commandeAchatService: CommandeAchatService,
    private articleAchatService: ArticleAchatService,
    private fournisseurService: FournisseurService,
    private fb: FormBuilder,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.initializeForm();
    this.loadFournisseurs();
    this.loadArticles();

    this.commandeId = this.route.snapshot.params['id'] ? Number(this.route.snapshot.params['id']) : null;
    if (this.commandeId) {
      this.isEditMode = true;
      this.loadCommande(this.commandeId);
    }
  }

  initializeForm(): void {
    this.commandeForm = this.fb.group({
      reference: ['', [Validators.required]],
      dateCommande: ['', [Validators.required]],
      dateLivraisonPrevue: [''],
      fournisseurId: [null],
      note: [''],
      lignes: this.fb.array([])
    });
  }

  get lignesArray(): FormArray {
    return this.commandeForm.get('lignes') as FormArray;
  }

  addLigne(): void {
    const ligneGroup = this.fb.group({
      articleAchatId: [null],
      quantite: [1, [Validators.required, Validators.min(0.01)]],
      prixUnitaireHT: [0, [Validators.required, Validators.min(0)]],
      tva: [20],
      montantHT: [{ value: 0, disabled: true }],
      montantTTC: [{ value: 0, disabled: true }]
    });

    ligneGroup.get('quantite')?.valueChanges.subscribe(() => this.calculateLigne(ligneGroup));
    ligneGroup.get('prixUnitaireHT')?.valueChanges.subscribe(() => this.calculateLigne(ligneGroup));
    ligneGroup.get('tva')?.valueChanges.subscribe(() => this.calculateLigne(ligneGroup));

    this.lignesArray.push(ligneGroup);
  }

  removeLigne(index: number): void {
    this.lignesArray.removeAt(index);
  }

  calculateLigne(ligneGroup: FormGroup): void {
    const quantite = ligneGroup.get('quantite')?.value || 0;
    const prix = ligneGroup.get('prixUnitaireHT')?.value || 0;
    const tva = ligneGroup.get('tva')?.value || 0;
    const montantHT = quantite * prix;
    const montantTTC = montantHT * (1 + tva / 100);
    ligneGroup.get('montantHT')?.setValue(montantHT.toFixed(2), { emitEvent: false });
    ligneGroup.get('montantTTC')?.setValue(montantTTC.toFixed(2), { emitEvent: false });
  }

  get totalHT(): number {
    let total = 0;
    this.lignesArray.controls.forEach(ctrl => total += parseFloat(ctrl.get('montantHT')?.value || 0));
    return total;
  }

  get totalTVA(): number {
    let total = 0;
    this.lignesArray.controls.forEach(ctrl => {
      const ht = parseFloat(ctrl.get('montantHT')?.value || 0);
      const ttc = parseFloat(ctrl.get('montantTTC')?.value || 0);
      total += ttc - ht;
    });
    return total;
  }

  get totalTTC(): number {
    let total = 0;
    this.lignesArray.controls.forEach(ctrl => total += parseFloat(ctrl.get('montantTTC')?.value || 0));
    return total;
  }

  loadFournisseurs(): void {
    this.fournisseurService.getAllFournisseurs().subscribe({
      next: (data) => { this.fournisseurs = data; },
      error: (err) => console.error('Erreur chargement fournisseurs:', err)
    });
  }

  loadArticles(): void {
    this.articleAchatService.getAllArticlesAchat().subscribe({
      next: (data) => { this.articles = data; },
      error: (err) => console.error('Erreur chargement articles:', err)
    });
  }

  onArticleChange(index: number): void {
    const ligneGroup = this.lignesArray.at(index) as FormGroup;
    const articleId = ligneGroup.get('articleAchatId')?.value;
    if (articleId) {
      const article = this.articles.find(a => a.id === Number(articleId));
      if (article && article.prixUnitaire) {
        ligneGroup.get('prixUnitaireHT')?.setValue(article.prixUnitaire);
        this.calculateLigne(ligneGroup);
      }
    }
  }

  loadCommande(id: number): void {
    this.commandeAchatService.getCommandeById(id).subscribe({
      next: (commande) => {
        this.commandeForm.patchValue({
          reference: commande.reference,
          dateCommande: commande.dateCommande,
          dateLivraisonPrevue: commande.dateLivraisonPrevue,
          fournisseurId: commande.fournisseur?.id,
          note: commande.note
        });
        if (commande.lignes) {
          commande.lignes.forEach(ligne => {
            const ligneGroup = this.fb.group({
              articleAchatId: [ligne.articleAchat?.id],
              quantite: [ligne.quantite, [Validators.required, Validators.min(0.01)]],
              prixUnitaireHT: [ligne.prixUnitaireHT, [Validators.required, Validators.min(0)]],
              tva: [ligne.tva || 20],
              montantHT: [{ value: ligne.montantHT, disabled: true }],
              montantTTC: [{ value: ligne.montantTTC, disabled: true }]
            });
            ligneGroup.get('quantite')?.valueChanges.subscribe(() => this.calculateLigne(ligneGroup));
            ligneGroup.get('prixUnitaireHT')?.valueChanges.subscribe(() => this.calculateLigne(ligneGroup));
            ligneGroup.get('tva')?.valueChanges.subscribe(() => this.calculateLigne(ligneGroup));
            this.lignesArray.push(ligneGroup);
          });
        }
      },
      error: (err) => console.error('Erreur chargement commande:', err)
    });
  }

  isFieldInvalid(fieldName: string): boolean {
    const field = this.commandeForm.get(fieldName);
    return !!(field && field.invalid && (field.dirty || field.touched || this.submitted));
  }

  onCancel(): void {
    this.router.navigate(['/commande-achat/list']);
  }

  onSubmit(): void {
    this.submitted = true;
    if (this.commandeForm.valid) {
      this.isLoading = true;
      const formValue = this.commandeForm.getRawValue();
      const commandeData: any = {
        reference: formValue.reference,
        dateCommande: formValue.dateCommande,
        dateLivraisonPrevue: formValue.dateLivraisonPrevue || null,
        fournisseurId: formValue.fournisseurId ? Number(formValue.fournisseurId) : null,
        note: formValue.note,
        montantHT: this.totalHT,
        montantTVA: this.totalTVA,
        montantTTC: this.totalTTC,
        statut: 'EN_ATTENTE',
        lignes: formValue.lignes.map((l: any) => ({
          articleAchatId: l.articleAchatId ? Number(l.articleAchatId) : null,
          quantite: l.quantite,
          prixUnitaireHT: l.prixUnitaireHT,
          tva: l.tva,
          montantHT: parseFloat(l.montantHT || 0),
          montantTTC: parseFloat(l.montantTTC || 0)
        }))
      };

      const request = this.isEditMode && this.commandeId
        ? this.commandeAchatService.updateCommande(this.commandeId, commandeData)
        : this.commandeAchatService.createCommande(commandeData);

      request.subscribe({
        next: () => {
          this.isLoading = false;
          alert(this.isEditMode ? '✅ Commande modifiée avec succès!' : '✅ Commande créée avec succès!');
          this.router.navigate(['/commande-achat/list']);
        },
        error: (err: any) => {
          this.isLoading = false;
          alert('❌ Erreur: ' + (err.error?.message || err.message || 'Une erreur est survenue'));
        }
      });
    } else {
      alert('⚠ Veuillez remplir correctement tous les champs obligatoires');
    }
  }
}
