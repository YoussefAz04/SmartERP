import { AfterViewInit, Component } from '@angular/core';
import { OwlOptions } from 'ngx-owl-carousel-o';
import { SidebarService } from 'src/app/core/service/sidebar/sidebar.service';
import { videocall } from 'src/app/shared/model/page.model';

export interface videocallModel {
  img: string;
  name: string;
}
@Component({
  selector: 'app-video-call',
  templateUrl: './video-call.component.html',
  styleUrl: './video-call.component.scss',
})
export class VideoCallComponent implements AfterViewInit {
  isAddMeetingClassVisible = false;
  isAddPaticipantClassVisible = false;
  public videocall: videocall[] = [];
  public sidebarData: Array<videocallModel> = [];
  toggleAddMeetingClass() {
    this.isAddMeetingClassVisible = !this.isAddMeetingClassVisible;
    this.isAddPaticipantClassVisible = false;
  }
  toggleAddPaticipantClass() {
    this.isAddPaticipantClassVisible = !this.isAddPaticipantClassVisible;
    this.isAddMeetingClassVisible = false;
  }
  customOptions: OwlOptions = {
    items: 5,
    loop: true,
    margin: 24,
    nav: true,
    dots: false,
    autoplay: true,

    smartSpeed: 1000,
    navText: [
      '<i class="fa fa-angle-left" data-bs-toggle="tooltip" title="fa fa-angle-left"></i>',
      '<i class="fa fa-angle-right" data-bs-toggle="tooltip" title="fa fa-angle-right"></i>',
    ],
    responsive: {
      0: {
        items: 1,
      },
      500: {
        items: 1,
      },
      768: {
        items: 5,
      },
      991: {
        items: 5,
      },
      1200: {
        items: 4,
      },
      1401: {
        items: 4,
      },
    },
  };
  constructor(private sidebar: SidebarService) {
    this.sidebarData = this.sidebar.videocall;
  }
  isCollapsed: boolean = false;
  toggleCollapse() {
    this.sidebar.toggleCollapse();
    this.isCollapsed = !this.isCollapsed;
  }

  isMicOn = true;
  toggleMicrophone(): void {
    this.isMicOn = !this.isMicOn;
  }
  isMuted8 = false;
  isvideo9 = false;
  isMuteButtonClicked = false;
  usersArray = [
    {
      name: 'Maybelle',
      isMuted: false,
      isVideo: false,
      imageSrc: 'assets/img/users/user-02.jpg',
    },
    {
      name: 'Benjamin',
      isMuted: false,
      isVideo: false,
      imageSrc: 'assets/img/users/user-03.jpg',
    },
    {
      name: 'Kaitlin',
      isMuted: false,
      isVideo: false,
      imageSrc: 'assets/img/users/user-04.jpg',
    },
    {
      name: 'Alwin',
      isMuted: false,
      isVideo: false,
      imageSrc: 'assets/img/users/user-05.jpg',
    },
    {
      name: 'Freda',
      isMuted: false,
      isVideo: false,
      imageSrc: 'assets/img/users/user-06.jpg',
    },
    {
      name: 'John Doe',
      isMuted: false,
      isVideo: false,
      imageSrc: 'assets/img/users/user-08.jpg',
    },
    {
      name: 'John Blair',
      isMuted: false,
      isVideo: false,
      imageSrc: 'assets/img/users/user-08.jpg',
    },
    {
      name: 'Joseph Collins',
      isMuted: false,
      isVideo: false,
      imageSrc: 'assets/img/users/user-09.jpg',
    },
  ];

  toggleMute8() {
    this.isMuted8 = !this.isMuted8;
  }
  toggleVideo(index: number) {
    this.usersArray[index].isVideo = !this.usersArray[index].isVideo;
  }
  toggleVideo9() {
    this.isvideo9 = !this.isvideo9;
  }
  isMicView: boolean = false;

  isMutedArray: boolean[] = [
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
  ];
  toggleMute(userIndex: number) {
    this.isMutedArray[userIndex] = !this.isMutedArray[userIndex];
  }
  toggleMicView() {
    this.isMicView = !this.isMicView;
  }
  public isVideoClass = false;
  isActive: boolean = false;

  toggleVideoAvatar() {
    this.isActive = !this.isActive;
  }
  toggleChatVisibility() {
    this.isAddMeetingClassVisible = !this.isAddMeetingClassVisible;
  }

  public ngAfterViewInit(): void {
    window.dispatchEvent(new Event('resize'));
  }
  switchToChatRoom() {
    this.isAddMeetingClassVisible = true;
    this.isAddPaticipantClassVisible = false;
  }
  switchToAddParty() {
    this.isAddMeetingClassVisible = false;
    this.isAddPaticipantClassVisible = true;
  }
  closeChat() {
    this.isAddMeetingClassVisible = false;
    this.isAddPaticipantClassVisible = false;
  }
}
