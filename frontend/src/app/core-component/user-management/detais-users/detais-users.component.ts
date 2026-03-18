import {Component, OnInit} from '@angular/core';
import {UserRequest} from "../../../model/user-request.model";
import {UserService} from "../../../auth/service/user/user.service";
import {ActivatedRoute, Router} from "@angular/router";
import {UserDTO} from "../../../model/userDTO.model";
import {FormsModule} from "@angular/forms";
import {ResponseDto} from "../../../model/reponseDto.model";
import {PermissionDto} from "../../../model/permissionDto.model";
import {NgIf} from "@angular/common";

@Component({
  selector: 'app-detais-users',
  standalone: true,
  imports: [
    FormsModule,
    NgIf
  ],
  templateUrl: './detais-users.component.html',
  styleUrl: './detais-users.component.scss'
})
export class DetaisUsersComponent implements OnInit{
  user!: UserDTO ;
  idUser! : any ;
  permissionUsers!: PermissionDto;

  constructor(private userService : UserService,private activateRouter : ActivatedRoute,private router : Router) {

  }
  ngOnInit() {
    this.idUser = this.activateRouter.snapshot.params['id'];
    this.userService.getUserById(this.idUser).subscribe(data =>{
      this.user = data ;

    })

  }


  onSubmit() {
    this.userService.updateUser(this.idUser,this.user).subscribe(data =>{
      console.log("dapartement modifier" , data);
      this.router.navigate(['/user-management/users'])
    })
  }


  getUserAuthData(){
    const item = localStorage.getItem('myLSkey');
    let currentUser: ResponseDto | null = null;

    if (item) {
      currentUser = JSON.parse(atob(item));
      console.log('User connecté:', currentUser?.name);

      console.log('Token:', currentUser?.token);
      console.log('Permissions:', currentUser?.permission);
      currentUser?.permission.map(per =>{
        if (per.moduleTitle.includes("Entreprises")){
          this.permissionUsers = per ;
          console.log("permission Entreprise" ,this.permissionUsers);
        }
      })
      console.log('Entreprise:', currentUser?.entreprise.nom);
    }
  }

}
