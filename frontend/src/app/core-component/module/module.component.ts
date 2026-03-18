import {Component, OnInit} from '@angular/core';
import {ModuleService} from "../../core/service/Module/module.service";
import {ModuleTittle} from "../../model/moduleTittle.model";
import {DatePipe, NgForOf, NgIf} from "@angular/common";
import {MatTooltip} from "@angular/material/tooltip";
import {Router} from "@angular/router";
import {routes} from "../../core/helpers/routes";

@Component({
  selector: 'app-module',
  standalone: true,
  imports: [
    DatePipe,
    MatTooltip,
    NgForOf,
    NgIf
  ],
  templateUrl: './module.component.html',
  styleUrl: './module.component.scss'
})
export class ModuleComponent implements  OnInit{

  moduleTittleListe : ModuleTittle[] = [] ;
  constructor(private moduleService : ModuleService,private router : Router) {

  }
  ngOnInit() {
    this.moduleService.getAllModulesTittle().subscribe(data => {
      this.moduleTittleListe = data ;
    })
  }


  editModule(id: number) {
    return id ;
  }

  deleteModule(id: number) {
    this.moduleService.deleteModuleTittle(id).subscribe(() =>{
      console.log("delete est succes");
    }) ;
  }



  addModule() {
     this.router.navigate(['/module/addModuleTittle'])
  }

  protected readonly routes = routes;

  affecterModule(id : any) {
    this.router.navigate(['/module/listModuleForAffecter',id]);
  }
}
