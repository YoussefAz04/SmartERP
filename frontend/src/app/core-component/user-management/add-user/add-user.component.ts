import { Component, OnInit } from '@angular/core';
import {
  FormBuilder,
  FormGroup,
  Validators,
  ReactiveFormsModule,

  FormsModule
} from "@angular/forms";
import { MatOption } from "@angular/material/autocomplete";
import { MatSelect } from "@angular/material/select";
import {NgClass, NgForOf, NgIf} from "@angular/common";
import { UserService } from "../../../auth/service/user/user.service";
import { EntrepriseService } from "../../../core/service/entreprise/entreprise.service";
import { Entreprise } from "../../../model/entreprise.model";
import { UserRequest } from "../../../model/user-request.model";
import {Router, RouterLink} from "@angular/router";
import {routes} from "../../../core/helpers/routes";

@Component({
  selector: 'app-add-user',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatOption,
    MatSelect,
    NgClass,
    NgForOf,
    FormsModule,
    NgIf,
    RouterLink
  ],
  templateUrl: './add-user.component.html',
  styleUrl: './add-user.component.scss'
})
export class AddUserComponent implements OnInit {
  addUserForm!: FormGroup;
  entreprises: Entreprise[] = [];
  passwordVisible = false;
  confirmPasswordVisible = false;

  constructor(
    private fb: FormBuilder,
    private entrepriseService: EntrepriseService,
    private userService: UserService,
    private router : Router
  ) {}

  ngOnInit(): void {
    this.initForm();
    this.loadEntreprises();
  }

  initForm() {
    this.addUserForm = this.fb.group(
      {
        username: ['', Validators.required],
        phone: ['', Validators.required],
        email: ['', [Validators.required, Validators.email]],
        entrepriseId: [null, Validators.required],
        password: ['', [Validators.required, Validators.minLength(6)]],
      },
    );
  }

  // passwordsMatchValidator(form: FormGroup) {
  //   const password = form.get('password')?.value;
  //   const confirmPassword = form.get('confirmPassword')?.value;
  //   return password === confirmPassword ? null : form.get('confirmPassword')?.setErrors({ passwordMismatch: true });
  // }

  loadEntreprises() {
    this.entrepriseService.getAllEntreprises().subscribe((data) => {
      this.entreprises = data;
      console.log("liste de entreprise");
    });
  }

  // togglePassword(field: 'password' | 'confirmPassword') {
  //   if (field === 'password') {
  //     this.passwordVisible = !this.passwordVisible;
  //   } else {
  //     this.confirmPasswordVisible = !this.confirmPasswordVisible;
  //   }
  // }

  onSubmit() {
    if (this.addUserForm.invalid) {
      this.addUserForm.markAllAsTouched();
      console.log("form invalid");
      return;
    }

    const formValue = this.addUserForm.value;

    const user: UserRequest = {
      username: formValue.username,
      email: formValue.email,
      phone: formValue.phone,
      password: formValue.password,
    };

    const idEntreprise = formValue.entrepriseId;
    console.log("id de entreprise selected", idEntreprise);

    this.userService.registerUser(user, idEntreprise,0).subscribe(data =>{
      console.log('Utilisateur ajouté avec succès:', data);
      this.router.navigate(['/user-management/users'])
      this.addUserForm.reset();
    });
  }

  protected readonly routes = routes;
}
