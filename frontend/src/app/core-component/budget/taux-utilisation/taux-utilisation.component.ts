import {Component, OnInit} from '@angular/core';
import {DepenseService} from "../../../core/service/depense/depense.service";
import {DecimalPipe, NgClass, NgIf} from "@angular/common";
import {ProjetService} from "../../../core/service/projet/projet.service";
import {ActivatedRoute} from "@angular/router";

@Component({
  selector: 'app-taux-utilisation',
  standalone: true,
  imports: [
    NgIf,
    DecimalPipe,
    NgClass
  ],
  templateUrl: './taux-utilisation.component.html',
  styleUrl: './taux-utilisation.component.scss'
})
export class TauxUtilisationComponent implements OnInit{
  idProjet = 2;
  taux: number | null = null;



  constructor(private depenseService: DepenseService,private projetService : ProjetService,private activateRouter : ActivatedRoute) {}

  ngOnInit(): void {
    this.idProjet = this.activateRouter.snapshot.params['id'];
    this.loadTaux();
  }

  loadTaux() {
    this.depenseService.getTauxUtilisation(this.idProjet).subscribe({
      next: (data) => this.taux = data,
      error: (err) => console.error("Erreur chargement taux d'utilisation", err)
    });
  }
}
