import {Component, OnInit} from '@angular/core';
import {ResponseDto} from "../../../model/reponseDto.model";
import {UserService} from "../../../auth/service/user/user.service";
import {UserDTO} from "../../../model/userDTO.model";

@Component({
  selector: 'app-profile',
  templateUrl: './profile.component.html',
  styleUrl: './profile.component.scss'
})
export class ProfileComponent implements OnInit{
  public password : boolean[] = [false];
  userId !: number | undefined ;
  user !: UserDTO ;

  constructor(private userService : UserService) {
  }

  ngOnInit() {
    this.getUserAuthId();
  }

  public togglePassword(index: number){
    this.password[index] = !this.password[index]
  }




  getUserAuthId(){
    const item = localStorage.getItem('myLSkey');
    let currentUser: ResponseDto | null = null;

    if (item) {
      currentUser = JSON.parse(atob(item));
      this.userId = currentUser?.userId ;
    }
    if (this.userId != null){
      this.userService.getUserById(this.userId).subscribe(data =>{
          this.user = data ;
      })
    }

  }

}
