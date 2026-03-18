import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import { TacheService } from '../../../core/service/taches/tache.service';
import { Tache, PrioriteDeTache, StatutTache } from '../../../model/tache.model';
import {NgForOf} from "@angular/common";
import {CategorieProjet} from "../../../model/CategorieProjet.model";
import {CategorieTache} from "../../../model/categorieTache.model";
import {CategorieTacheService} from "../../../core/service/categorieTache/categorie-tache.service";
import {ActivatedRoute, Router, RouterLink} from "@angular/router";
import {routes} from "../../../core/helpers/routes";

@Component({
  selector: 'app-add-tache',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    NgForOf,
    RouterLink
  ],
  templateUrl: './add-tache.component.html',
  styleUrl: './add-tache.component.scss'
})
export class AddTacheComponent implements OnInit {
  tacheForm: FormGroup;
  priorites = Object.values(PrioriteDeTache);
  statuts = Object.values(StatutTache);

  categorieTache : CategorieTache[] = [] ;
  idProjet !: number ;

  ngOnInit() {
   this.categorieTacheService.getAllCategories().subscribe(data => {
     this.categorieTache = data ;
   })
    this.idProjet = this.activatedRouter.snapshot.params['id'];
   this.getAllTache();
  }

  constructor(private fb: FormBuilder, private tacheService: TacheService,private categorieTacheService : CategorieTacheService,private activatedRouter : ActivatedRoute, private  router : Router) {
    this.tacheForm = this.fb.group({
      titre: ['', Validators.required],
      description: [''],
      dateDebut: ['', Validators.required],
      dateFin: ['', Validators.required],
      statut: [StatutTache.A_FAIRE, Validators.required],
      priorite: [PrioriteDeTache.MOYENNE, Validators.required],
      progres: [0, Validators.required],
      duree: [0, Validators.required],
      predecesseur: ['', Validators.required],
      idCategorieTache : ['',Validators.required]

    });
  }

  listTachePredecesseur : Tache [] =[] ;
  getAllTache(){
    this.tacheService.getAllTachesByProjet(this.idProjet).subscribe(data => {
      this.listTachePredecesseur = data ;
    })
  }


  onSubmit() {
    if (this.tacheForm.valid) {
      const tache: Tache = this.tacheForm.value;
      this.tacheService.addTache(tache, this.tacheForm.value.idCategorieTache, this.idProjet).subscribe({
        next: (res) => {
          console.log('Tâche ajoutée avec succès', res);
          this.router.navigate(['/projet/listTachesProjet'])
        },
        error: (err) => console.error('Erreur lors de l’ajout de la tâche', err)
      });
    }
  }


  onCheckboxChange(event: any) {
    const currentValue = this.tacheForm.get('predecesseur')?.value || '';
    let selectedIds: number[] = currentValue ? currentValue.split(',').map((id: string) => +id) : [];

    const id: number = +event.target.value;

    if (event.target.checked) {
      if (!selectedIds.includes(id)) {
        selectedIds.push(id);
      }
    } else {
      selectedIds = selectedIds.filter((val: number) => val !== id);
    }

    this.tacheForm.get('predecesseur')?.setValue(selectedIds.join(','));
  }

  protected readonly routes = routes;
}
