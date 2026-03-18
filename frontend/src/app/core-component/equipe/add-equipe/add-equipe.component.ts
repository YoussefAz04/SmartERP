import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from "@angular/forms";
import { EquipeService } from "../../../core/service/equipe/equipe.service";
import {Router, RouterLink} from "@angular/router";
import { routes } from "../../../core/helpers/routes";

@Component({
  selector: 'app-add-equipe',
  standalone: true,
  imports: [
    RouterLink,
    ReactiveFormsModule
  ],
  templateUrl: './add-equipe.component.html',
  styleUrl: './add-equipe.component.scss'
})
export class AddEquipeComponent implements OnInit {
  equipeForm!: FormGroup;

  constructor(private equipeService: EquipeService, private fb: FormBuilder,private router :Router) {}

  ngOnInit() {
    this.equipeForm = this.fb.group({
      nom: ['', Validators.required],
      description: ['', Validators.required],
      dateCreation: ['', Validators.required]
    });
  }

  onSubmit() {
    if (this.equipeForm.valid) {
      this.equipeService.createEquipe(this.equipeForm.value).subscribe(data => {
        console.log('Ajouté avec succès', data);
        this.router.navigate(['/equipe/listEquipe'])
      });
    }
  }

  protected readonly routes = routes;
}
