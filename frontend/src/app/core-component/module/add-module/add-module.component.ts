import {Component, OnInit} from '@angular/core';
import {ModuleService} from "../../../core/service/Module/module.service";
import {FormBuilder, FormGroup, ReactiveFormsModule} from "@angular/forms";
import {Validators} from "ngx-editor";
import {PaginatorModule} from "primeng/paginator";
import {routes} from "../../../core/helpers/routes";
import {RouterLink} from "@angular/router";
import {ModuleTittle} from "../../../model/moduleTittle.model";
import {NgForOf} from "@angular/common";

@Component({
  selector: 'app-add-module',
  standalone: true,
  imports: [
    PaginatorModule,
    ReactiveFormsModule,
    RouterLink,
    NgForOf
  ],
  templateUrl: './add-module.component.html',
  styleUrl: './add-module.component.scss'
})
export class AddModuleComponent implements OnInit{

  moduleForm!: FormGroup;
  constructor(private moduleTittleService : ModuleService ,private  fb: FormBuilder ) {

  }

  ngOnInit(): void {
    this.moduleForm = this.fb.group({
      title: ['', Validators.required],
      status: [false],
      idModuleTitre : ['',Validators.required]
    });
    this.getAllTitreModule() ;
  }

  modules : ModuleTittle[] =[] ;
  getAllTitreModule(){
    this.moduleTittleService.getAllModulesTittle().subscribe(data =>{
      this.modules = data ;
    })
  }

  createModule(){
    if (this.moduleForm.valid){
      this.moduleTittleService.addModuleWithTitreModule(this.moduleForm.value,this.moduleForm.value.idModuleTitre).subscribe(data =>{
        console.log('ajout avec succes', data);
      })
    }
  }

  protected readonly routes = routes;
}
