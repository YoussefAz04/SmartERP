import {Component, OnInit} from '@angular/core';
import {ModuleService} from "../../../core/service/Module/module.service";
import {FormBuilder, FormGroup, ReactiveFormsModule} from "@angular/forms";
import {Validators} from "ngx-editor";
import {RouterLink} from "@angular/router";
import {routes} from "../../../core/helpers/routes";

@Component({
  selector: 'app-add-module-tittle',
  standalone: true,
  imports: [
    RouterLink,
    ReactiveFormsModule
  ],
  templateUrl: './add-module-tittle.component.html',
  styleUrl: './add-module-tittle.component.scss'
})
export class AddModuleTittleComponent implements OnInit{
  moduleTittleForm!: FormGroup;
  constructor(private moduleTittleService : ModuleService ,private  fb: FormBuilder ) {

  }

  ngOnInit(): void {
    this.moduleTittleForm = this.fb.group({
      moduleTittle: ['', Validators.required],
    });
  }

  createModuleTittle(){
    if (this.moduleTittleForm.valid){
      this.moduleTittleService.addModuleTittle(this.moduleTittleForm.value).subscribe(data =>{
          console.log('ajout avec succes', data);
      })
    }
  }


  protected readonly routes = routes;
}
