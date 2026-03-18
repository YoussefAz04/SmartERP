import {Component, OnInit} from '@angular/core';
import {Module} from "../../../model/module.model";
import {ModuleService} from "../../../core/service/Module/module.service";
import {Router} from "@angular/router";
import {SousModule} from "../../../model/sousModule.model";
import {NgForOf, NgIf} from "@angular/common";

@Component({
  selector: 'app-list-sous-module',
  standalone: true,
  imports: [
    NgIf,
    NgForOf
  ],
  templateUrl: './list-sous-module.component.html',
  styleUrl: './list-sous-module.component.scss'
})
export class ListSousModuleComponent implements OnInit{
  sousModuleListe : SousModule[] = [] ;
  constructor(private moduleService : ModuleService,private router : Router) {

  }
  ngOnInit() {
    this.moduleService.getAllSousModule().subscribe(data => {
      this.sousModuleListe = data ;
    })
  }


  addModule() {

    this.router.navigate(['module/addSousModule']);

  }

  editModule(id: number) {

  }

  deleteModule(id: number) {

  }
}
