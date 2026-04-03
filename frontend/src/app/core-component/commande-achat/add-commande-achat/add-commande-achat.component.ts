import { Component, OnInit } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { FormBuilder, FormGroup, FormArray, Validators, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { CommandeAchatService } from '../../../core/service/commande-achat/commande-achat.service';
import { FournisseurService } from '../../../core/service/fournisseur/fournisseur.service';
import { ArticleAchatService } from '../../../core/service/article-achat/article-achat.service';
import { EntrepriseService } from '../../../core/service/entreprise/entreprise.service';

@Component({
  selector: 'app-add-commande-achat',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './add-commande-achat.component.html',
  styleUrl: './add-commande-achat.component.scss'
})
export class AddCommandeAchatComponent implements OnInit {

  commandeForm!: FormGroup;
  isEditMode = false;
  isLoading = false;
  errorMessage = '';
  fournisseurs: any[] = [];
  articles: any[] = [];
  entreprises: any[] = [];
  
  currentEntrepriseId: number | null = null;
  selectedEntrepriseName: string = '';
  totalHT = 0;
  totalTVA = 0;
  totalTTC = 0;

  constructor(
    private fb: FormBuilder,
    private commandeAchatService: CommandeAchatService,
    private fournisseurService: FournisseurService,
    private articleAchatService: ArticleAchatService,
    private entrepriseService: EntrepriseService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.initializeForm();
    
    // ✅ LOAD ENTERPRISES FIRST, THEN EVERYTHING ELSE
    this.loadEntreprises(() => {
      this.getEntrepriseId();
      this.loadFournisseurs();
      this.loadArticles();
      this.checkEditMode();
      
      this.lignesArray.valueChanges.subscribe(() => {
        this.calculateTotals();
      });
    });
  }

  initializeForm(): void {
    this.commandeForm = this.fb.group({
      reference: ['', Validators.required],
      dateCommande: [new Date().toISOString().split('T')[0], Validators.required],
      dateLivraisonPrevue: ['', Validators.required],
      fournisseurId: [null, Validators.required],
      statut: ['EN_ATTENTE'],
      note: [''],
      lignes: this.fb.array([])
    });
  }

  // ✅ LOAD ENTERPRISES WITH CALLBACK
  loadEntreprises(callback?: () => void): void {
    this.entrepriseService.getAllEntreprises().subscribe({
      next: (data: any) => {
        this.entreprises = data;
        console.log('✅ Enterprises loaded:', this.entreprises);
        
        // Auto-select first enterprise if none selected
        if (this.entreprises.length > 0 && !this.currentEntrepriseId) {
          this.changeEnterprise(this.entreprises[0].id);
        }
        
        // Call callback after loading
        if (callback) callback();
      },
      error: (err: any) => {
        console.error('Error loading enterprises:', err);
        this.errorMessage = 'Erreur lors du chargement des entreprises';
        // Still call callback on error
        if (callback) callback();
      }
    });
  }

  getEntrepriseId(): void {
    let enterpriseId = localStorage.getItem('enterpriseId');
    
    if (!enterpriseId) {
      const myLSkey = localStorage.getItem('myLSkey');
      if (myLSkey) {
        try {
          const decoded = JSON.parse(atob(myLSkey));
          enterpriseId = decoded.enterpriseId?.toString();
          console.log('📦 Decoded token:', decoded);
        } catch (e) {
          console.error('❌ Failed to decode myLSkey:', e);
        }
      }
    }

    if (enterpriseId) {
      this.currentEntrepriseId = parseInt(enterpriseId);
      console.log('✅ Enterprise ID loaded:', this.currentEntrepriseId);
      this.updateSelectedEnterpriseName();
    } else {
      console.warn('⚠️ No enterprise ID found in localStorage');
    }
  }

  checkEditMode(): void {
    this.route.params.subscribe((params: any) => {
      if (params['id']) {
        this.isEditMode = true;
        this.loadCommande(params['id']);
      }
    });
  }

  loadCommande(id: number): void {
    this.commandeAchatService.getCommandeById(id).subscribe({
      next: (commande: any) => {
        this.populateForm(commande);
      },
      error: (err: any) => {
        console.error('Error loading commande:', err);
        this.errorMessage = 'Erreur lors du chargement de la commande';
      }
    });
  }

  populateForm(commande: any): void {
    this.commandeForm.patchValue({
      reference: commande.reference,
      dateCommande: commande.dateCommande,
      dateLivraisonPrevue: commande.dateLivraisonPrevue,
      fournisseurId: commande.fournisseurId,
      statut: commande.statut,
      note: commande.note
    });

    if (commande.lignes && commande.lignes.length > 0) {
      const lignesArray = this.lignesArray;
      commande.lignes.forEach((ligne: any) => {
        lignesArray.push(this.createLigneGroup(ligne));
      });
      this.calculateTotals();
    }
  }

  loadFournisseurs(): void {
    this.fournisseurService.getAllFournisseurs().subscribe({
      next: (data: any) => {
        this.fournisseurs = data;
        console.log('✅ Fournisseurs loaded:', this.fournisseurs);
      },
      error: (err: any) => {
        console.error('Error loading fournisseurs:', err);
      }
    });
  }

  loadArticles(): void {
    this.articleAchatService.getAllArticlesAchat().subscribe({
      next: (data: any) => {
        this.articles = data;
        console.log('✅ Articles loaded:', this.articles);
      },
      error: (err: any) => {
        console.error('Error loading articles:', err);
      }
    });
  }

  get lignesArray(): FormArray {
    return this.commandeForm.get('lignes') as FormArray;
  }

  createLigneGroup(data?: any): FormGroup {
    const montantHT = data ? (data.quantite || 1) * (data.prixUnitaireHT || 0) : 0;
    const montantTTC = data ? montantHT * (1 + (data.tva || 0) / 100) : 0;

    return this.fb.group({
      articleAchatId: [data?.articleAchat?.id || data?.articleAchatId || null, Validators.required],
      quantite: [data?.quantite || 1, [Validators.required, Validators.min(1)]],
      prixUnitaireHT: [data?.prixUnitaireHT || 0, [Validators.required, Validators.min(0)]],
      tva: [data?.tva || 0, [Validators.required, Validators.min(0)]],
      montantHT: [{ value: montantHT, disabled: true }],
      montantTTC: [{ value: montantTTC, disabled: true }]
    });
  }

  addLigne(): void {
    this.lignesArray.push(this.createLigneGroup());
  }

  removeLigne(index: number): void {
    this.lignesArray.removeAt(index);
    this.calculateTotals();
  }

  onArticleChange(index: number): void {
    const ligneControl = this.lignesArray.at(index);
    const articleId = ligneControl.get('articleAchatId')?.value;

    if (articleId) {
      const article = this.articles.find((a: any) => a.id === articleId);
      if (article) {
        ligneControl.patchValue({
          prixUnitaireHT: article.prixAchat || article.prixUnitaire || 0,
          tva: article.tva || 0
        });
      } else {
        ligneControl.patchValue({
          prixUnitaireHT: 0
        });
      }
    } else {
      ligneControl.patchValue({
        prixUnitaireHT: 0
      });
    }
  } else {
    console.warn(`❌ No article ID selected`);
    ligneControl.patchValue({
      prixUnitaireHT: 0  // ← DEFAULT TO 0 IF NULL
    });
  }
  
  this.calculateLineAmount(index);
  this.calculateTotals();
}
  compareNumbers(a: any, b: any): boolean {
  return a === b;
}

  calculateLineAmount(index: number): void {
    const ligneControl = this.lignesArray.at(index);
    const quantite = parseFloat(ligneControl.get('quantite')?.value || 0);
    const prixUnitaire = parseFloat(ligneControl.get('prixUnitaireHT')?.value || 0);
    const tva = parseFloat(ligneControl.get('tva')?.value || 0);

    const montantHT = quantite * prixUnitaire;
    const montantTTC = montantHT * (1 + tva / 100);

    ligneControl.patchValue({
      montantHT: Number(montantHT.toFixed(2)),
      montantTTC: Number(montantTTC.toFixed(2))
    }, { emitEvent: false });
  }

  calculateTotals(): void {
    let totalHT = 0;
    let totalTTC = 0;

    this.lignesArray.controls.forEach((control: any, index: number) => {
      this.calculateLineAmount(index);
      const ht = parseFloat(control.get('montantHT')?.value || 0);
      const ttc = parseFloat(control.get('montantTTC')?.value || 0);
      totalHT += ht;
      totalTTC += ttc;
    });

    this.totalHT = Number(totalHT.toFixed(2));
    this.totalTVA = Number((totalTTC - totalHT).toFixed(2));
    this.totalTTC = Number(totalTTC.toFixed(2));

    console.log('💰 Totals calculated:', { totalHT: this.totalHT, totalTVA: this.totalTVA, totalTTC: this.totalTTC });
  }

  isFieldInvalid(fieldName: string): boolean {
    const field = this.commandeForm.get(fieldName);
    return !!(field && field.invalid && (field.dirty || field.touched));
  }

  changeEnterprise(enterpriseId: number): void {
    this.currentEntrepriseId = enterpriseId;
    localStorage.setItem('enterpriseId', enterpriseId.toString());
    this.updateSelectedEnterpriseName();
    console.log('✅ Enterprise changed to:', enterpriseId);
  }

  updateSelectedEnterpriseName(): void {
    const selected = this.entreprises.find(e => e.id === this.currentEntrepriseId);
    this.selectedEntrepriseName = selected ? selected.nom : 'Chargement...';
  }

 onSubmit(): void {
  console.log('🔄 Form submit clicked');
  console.log('Form valid?', this.commandeForm.valid);
  console.log('currentEntrepriseId:', this.currentEntrepriseId);
  console.log('Lines count:', this.lignesArray.length);

  // ✅ STEP 1: Validate form structure
  if (!this.commandeForm.valid) {
    this.errorMessage = '❌ Veuillez remplir tous les champs obligatoires';
    console.error('❌ Form not valid');
    return;
  }

  // ✅ STEP 2: Check enterprise is selected
  if (!this.currentEntrepriseId) {
    this.errorMessage = '❌ Sélectionnez une entreprise avant de créer la commande';
    console.error('❌ No enterprise ID');
    return;
  }

  // ✅ STEP 3: Check at least one line exists
  if (this.lignesArray.length === 0) {
    this.errorMessage = '❌ Au moins une ligne de commande est requise';
    console.error('❌ No lines');
    return;
  }

  // ✅ STEP 4: VALIDATE EACH LINE COMPLETELY
  for (let i = 0; i < this.lignesArray.length; i++) {
    const ligne = this.lignesArray.at(i);
    const articleId = ligne.get('articleAchatId')?.value;
    const quantite = ligne.get('quantite')?.value;
    const prix = ligne.get('prixUnitaireHT')?.value;
    const tva = ligne.get('tva')?.value;
    
    // Validate article is selected
    if (!articleId || articleId === '' || articleId === null || isNaN(articleId)) {
      this.errorMessage = `❌ Ligne ${i + 1}: Sélectionnez un article SVP`;
      console.error(`❌ Line ${i + 1} - Invalid article ID:`, articleId);
      return;
    }
    
    // Validate quantity
    const quantiteNum = parseFloat(quantite);
    if (!quantite || isNaN(quantiteNum) || quantiteNum <= 0) {
      this.errorMessage = `❌ Ligne ${i + 1}: Quantité doit être > 0`;
      console.error(`❌ Line ${i + 1} - Invalid quantity:`, quantite);
      return;
    }
    
    // Validate price
    const prixNum = parseFloat(prix);
    if (prix === undefined || prix === null || isNaN(prixNum) || prixNum < 0) {
      this.errorMessage = `❌ Ligne ${i + 1}: Prix unitaire invalide`;
      console.error(`❌ Line ${i + 1} - Invalid price:`, prix);
      return;
    }
    
    // Validate TVA
    const tvaNum = parseFloat(tva || 0);
    if (isNaN(tvaNum) || tvaNum < 0 || tvaNum > 100) {
      this.errorMessage = `❌ Ligne ${i + 1}: TVA doit être entre 0 et 100`;
      console.error(`❌ Line ${i + 1} - Invalid TVA:`, tva);
      return;
    }
  }

  console.log('✅ All validations passed. Building payload...');
  
  const formData = this.commandeForm.getRawValue();
  const payload = {
    reference: formData.reference,
    dateCommande: formData.dateCommande,
    dateLivraisonPrevue: formData.dateLivraisonPrevue,
    fournisseurId: parseInt(formData.fournisseurId),
    statut: formData.statut,
    note: formData.note,
    montantHT: this.totalHT,
    montantTVA: this.totalTVA,
    montantTTC: this.totalTTC,
    entrepriseId: this.currentEntrepriseId,
    lignes: formData.lignes.map((ligne: any, index: number) => {
      const articleId = parseInt(ligne.articleAchatId);
      console.log(`Line ${index}: articleId = ${articleId}, isNaN = ${isNaN(articleId)}`);
      
      if (isNaN(articleId)) {
        throw new Error(`Line ${index + 1}: Invalid article ID`);
      }
      
      return {
        articleAchatId: articleId,
        quantite: parseFloat(ligne.quantite),
        prixUnitaireHT: parseFloat(ligne.prixUnitaireHT),
        tva: parseFloat(ligne.tva || 0),
        montantHT: parseFloat(ligne.montantHT || 0),
        montantTTC: parseFloat(ligne.montantTTC || 0)
      };
    })
  };

  console.log('📝 Payload ready:', payload);
  this.isLoading = true;
  this.errorMessage = '';

  console.log('🚀 Sending to backend...');
  this.commandeAchatService.createCommande(payload).subscribe({
    next: (response: any) => {
      console.log('✅✅✅ SUCCESS! Response:', response);
      this.isLoading = false;
      alert('✅ Commande créée avec succès!');
      this.router.navigate(['/commande-achat/list']);
    },
    error: (err: any) => {
      console.error('❌❌❌ ERROR! Response:', err);
      this.isLoading = false;
      const errorMsg = err?.error?.message || err?.message || 'Erreur lors de la création';
      this.errorMessage = errorMsg;
      alert('❌ ' + errorMsg);
    }
  });
}

  onCancel(): void {
    this.cancel();
  }

  cancel(): void {
    this.router.navigate(['/commande-achat/list']);
  }
}