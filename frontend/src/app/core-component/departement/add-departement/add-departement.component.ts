import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, ReactiveFormsModule} from "@angular/forms";
import {DepartementService} from "../../../core/service/departement/departement.service";
import {EntrepriseService} from "../../../core/service/entreprise/entreprise.service";
import {Router, RouterLink} from "@angular/router";
import {Validators} from "ngx-editor";
import {Entreprise} from "../../../model/entreprise.model";
import {NgForOf} from "@angular/common";
import {routes} from "../../../core/helpers/routes";

@Component({
  selector: 'app-add-departement',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    NgForOf,
    RouterLink
  ],
  templateUrl: './add-departement.component.html',
  styleUrl: './add-departement.component.scss'
})
export class AddDepartementComponent implements OnInit{

  departementFormGroup!: FormGroup;
  entreprises: Entreprise[] = [];
  constructor(
    private fb: FormBuilder,
    private departementService: DepartementService,
    private entrepriseService: EntrepriseService,
    private router: Router
  ) {}


  ngOnInit(): void {
    this.departementFormGroup = this.fb.group({
      nom: ['', Validators.required],
      description: [''],
      responsable: ['', Validators.required],
      email: ['', [Validators.required]],
      telephone: [''],
      nombreEmployes: [0, [Validators.required]],
      entrepriseId: [null, Validators.required]
    });

    this.entrepriseService.getAllEntreprises().subscribe(data => {
      this.entreprises = data;
    });
  }

  createDepartement() {
    const formData = this.departementFormGroup.value;
    const entrepriseId = formData.entrepriseId;

    this.departementService.createDepartement(formData, entrepriseId).subscribe(() => {
      this.router.navigate(['/departement']);
    });
  }

  protected readonly routes = routes;
}
