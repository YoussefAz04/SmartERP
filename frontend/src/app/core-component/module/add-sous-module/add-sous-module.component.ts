import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from "@angular/forms";
import {ModuleService} from "../../../core/service/Module/module.service";
import { routes } from 'src/app/core/core.index';
import {RouterLink} from "@angular/router";
import {Module} from "../../../model/module.model";
import {NgForOf} from "@angular/common";

@Component({
  selector: 'app-add-sous-module',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    RouterLink,
    NgForOf
  ],
  templateUrl: './add-sous-module.component.html',
  styleUrl: './add-sous-module.component.scss'
})
export class AddSousModuleComponent implements OnInit{
  sousModuleForm!: FormGroup;
  constructor(private sousModuleService : ModuleService ,private  fb: FormBuilder ) {

  }

  ngOnInit(): void {
    this.sousModuleForm = this.fb.group({
      titreSousModule: ['', Validators.required],
      link: ['', Validators.required],
      idModule :['',Validators.required]
    });
    this.getAllModule() ;
  }

  createSousModule(){
    if (this.sousModuleForm.valid){
      this.sousModuleService.createSousModule(this.sousModuleForm.value,this.sousModuleForm.value.idModule).subscribe(data =>{
        console.log('ajout avec succes', data);
      })
    }
  }


  modules : Module[ ] =[];
  getAllModule(){
    this.sousModuleService.getAllModules().subscribe(data =>{
      this.modules = data ;
    })
  }


  protected readonly routes = routes;
}
