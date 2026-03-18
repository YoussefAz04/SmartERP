import {Component, ElementRef, OnInit, ViewChild} from '@angular/core';
import {ProjetService} from "../../../core/service/projet/projet.service";
import {ActivatedRoute, Router, RouterLink} from "@angular/router";
import {Projet} from "../../../model/projet.model";
import {routes} from "../../../core/helpers/routes";
import {DatePipe, NgForOf, NgIf} from "@angular/common";
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from "@angular/forms";
import {PrioriteDeTache, StatutTache, Tache} from "../../../model/tache.model";
import {CategorieTache} from "../../../model/categorieTache.model";
import {TacheService} from "../../../core/service/taches/tache.service";
import {CategorieTacheService} from "../../../core/service/categorieTache/categorie-tache.service";
import {MatTooltip} from "@angular/material/tooltip";
import {ResponseDto} from "../../../model/reponseDto.model";
import {PermissionDto} from "../../../model/permissionDto.model";


@Component({
  selector: 'app-detais-projet',
  standalone: true,
  imports: [
    RouterLink,
    NgIf,
    DatePipe,
    NgForOf,
    ReactiveFormsModule,
    MatTooltip
  ],
  templateUrl: './detais-projet.component.html',
  styleUrl: './detais-projet.component.scss'
})
export class DetaisProjetComponent implements OnInit{



  idProjet !: number ;
  projetDetais !: Projet ;


  tacheForm !: FormGroup;
  priorites = Object.values(PrioriteDeTache);
  statuts = Object.values(StatutTache);

  categorieTache : CategorieTache[] = [] ;
  permissionProjet!: PermissionDto;

  constructor(private fb: FormBuilder,private projetService : ProjetService,
              private activateRouter : ActivatedRoute,
              private tacheService: TacheService,
              private categorieTacheService : CategorieTacheService,
              private router :Router) {
  }
  ngOnInit() {
    this.idProjet = this.activateRouter.snapshot.params['id'];
    this.projetService.getProjetById(this.idProjet).subscribe(data =>{
      this.projetDetais = data ;


    })


    this.categorieTacheService.getAllCategories().subscribe(data => {
      this.categorieTache = data ;
    })
    this.getUserAuthData() ;

    this.tacheForm = this.fb.group({
      titre: ['', Validators.required],
      description: [''],
      dateDebut: ['', Validators.required],
      dateFin: ['', Validators.required],
      statut: [StatutTache.A_FAIRE, Validators.required],
      priorite: [PrioriteDeTache.MOYENNE, Validators.required],
      idCategorieTache : ['',Validators.required]

    });
  }


  onSubmit() {
    if (this.tacheForm.valid) {
      const tache: Tache = this.tacheForm.value;
      this.tacheService.addTache(tache, this.tacheForm.value.idCategorieTache, this.idProjet).subscribe({
        next: (res) => {
          console.log('Tâche ajoutée avec succès', res);
          this.projetService.getProjetById(this.idProjet).subscribe(data =>{
            this.projetDetais = data ;
          })


        },
        error: (err) => console.error('Erreur lors de l’ajout de la tâche', err)
      });
    }
  }

  protected readonly routes = routes;

  getTachesParStatut(status: string): Tache[] {
    return this.projetDetais.taches?.filter(tache =>
      tache.statut.includes(status)
    ) || [];
  }

  updateProjet() {
    this.router.navigate(['/projet/updateProjet', this.idProjet]);
  }


  updateTache(id: number | undefined) {
    this.router.navigate(['/projet/updateTache', id]);
  }

  detaisTache(id: number | undefined) {
    this.router.navigate(['projet/detaisTache',id]);
  }

  getUserAuthData(){
    const item = localStorage.getItem('myLSkey');
    let currentUser: ResponseDto | null = null;

    if (item) {
      currentUser = JSON.parse(atob(item));
      console.log('User connecté:', currentUser?.name);

      console.log('Token:', currentUser?.token);
      console.log('Permissions:', currentUser?.permission);
      currentUser?.permission.map(per =>{
        if (per.moduleTitle == "Projet"){
          this.permissionProjet = per ;
          console.log("permission Projet" ,this.permissionProjet);
        }
      })
      console.log('Projet:', currentUser?.entreprise.nom);
    }
  }
}
