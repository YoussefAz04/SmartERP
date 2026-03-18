import {Component, OnInit} from '@angular/core';
import {Module} from "../../../model/module.model";
import {ModuleService} from "../../../core/service/Module/module.service";
import {CheckboxModule} from "primeng/checkbox";
import {FormsModule} from "@angular/forms";
import {NgForOf} from "@angular/common";
import {ActivatedRoute} from "@angular/router";

@Component({
  selector: 'app-list-module-affecter',
  standalone: true,
  imports: [
    CheckboxModule,
    FormsModule,
    NgForOf
  ],
  templateUrl: './list-module-affecter.component.html',
  styleUrl: './list-module-affecter.component.scss'
})
export class ListModuleAffecterComponent implements  OnInit{
  moduleAffecterListe : Module[] = [] ;
  selectModule : Module[]= [] ;
  idModule !: number ;


  constructor(private moduleService : ModuleService,
              private routerActivated : ActivatedRoute) {
  }
  ngOnInit() {
    this.idModule = this.routerActivated.snapshot.params['id'];
    this.moduleService.getAllModulesNotAffecter(this.idModule).subscribe(data => {
      this.moduleAffecterListe = data ;
      console.log(this.moduleAffecterListe);
    })
  }


  affecterModule() {
    console.log(this.selectModule);
    this.moduleService.affecterModulesAuModuleTittle(this.selectModule,203).subscribe(()=>{
      console.log("affected module");
    })
  }
}
