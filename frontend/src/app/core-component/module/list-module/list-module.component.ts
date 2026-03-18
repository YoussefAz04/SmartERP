import {Component, OnInit} from '@angular/core';
import {ModuleTittle} from "../../../model/moduleTittle.model";
import {ModuleService} from "../../../core/service/Module/module.service";
import {Router} from "@angular/router";
import {Module} from "../../../model/module.model";
import {MatTooltip} from "@angular/material/tooltip";
import {NgForOf} from "@angular/common";

@Component({
  selector: 'app-list-module',
  standalone: true,
  imports: [
    MatTooltip,
    NgForOf
  ],
  templateUrl: './list-module.component.html',
  styleUrl: './list-module.component.scss'
})
export class ListModuleComponent implements OnInit{
  moduleListe : Module[] = [] ;
  constructor(private moduleService : ModuleService,private router : Router) {

  }
  ngOnInit() {
    this.moduleService.getAllModules().subscribe(data => {
      this.moduleListe = data ;
    })
  }


  addModule() {
    this.router.navigate(['/module/addModule']);
  }

  editModule(id: number) {

  }

  deleteModule(id: number) {

  }
}
