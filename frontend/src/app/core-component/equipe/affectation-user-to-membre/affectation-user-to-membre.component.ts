import {Component, OnInit} from '@angular/core';
import {UserRequest} from "../../../model/user-request.model";
import {UserService} from "../../../auth/service/user/user.service";
import {MembreService} from "../../../core/service/membre/membre.service";
import {CheckboxModule} from "primeng/checkbox";
import {DatePipe, NgForOf} from "@angular/common";
import {FormsModule} from "@angular/forms";
import {ActivatedRoute, Router} from "@angular/router";
import {MatTooltip} from "@angular/material/tooltip";

@Component({
  selector: 'app-affectation-user-to-membre',
  standalone: true,
  imports: [
    CheckboxModule,
    NgForOf,
    FormsModule,
    MatTooltip,
    DatePipe
  ],
  templateUrl: './affectation-user-to-membre.component.html',
  styleUrl: './affectation-user-to-membre.component.scss'
})
export class AffectationUserToMembreComponent  implements OnInit{
  userlist : UserRequest[] = [];
  userSelected: UserRequest[] = [];
  idMembre !: any ;

  constructor(private userService : UserService,
              private membreService : MembreService,
              private routerActivted : ActivatedRoute,
              private  router : Router) {
  }

  ngOnInit() {
    this.idMembre = this.routerActivted.snapshot.params['id'];
    this.userService.getAllUsers().subscribe(data =>{
      this.userlist = data ;
    })
  }


  affecterToMembre() {
    this.membreService.affecterUserToMembre(this.userSelected,this.idMembre).subscribe(()=>{
      console.log("affected Users To membre with succes");
      this.router.navigate(['/equipe/listMembre']);
    })
  }
}
