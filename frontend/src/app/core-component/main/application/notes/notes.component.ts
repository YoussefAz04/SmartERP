import { Component } from '@angular/core';
import { OwlOptions } from 'ngx-owl-carousel-o';
import { SidebarService } from 'src/app/core/core.index';

@Component({
  selector: 'app-notes',
  templateUrl: './notes.component.html',
  styleUrl: './notes.component.scss',
})
export class NotesComponent {
  constructor(private sidebar: SidebarService) {}
  isCollapsed: boolean = false;
  toggleCollapse() {
    this.sidebar.toggleCollapse();
    this.isCollapsed = !this.isCollapsed;
  }
  selectedValue1 = '';
  selectedValue2 = '';
  selectedValue3 = '';
  selectedValue4 = '';
  selectedValue5 = '';
  selectedValue6 = '';
  selectedValue7 = '';
  selectedValue8 = '';
  selectedValue9 = '';
  public appSidebar = true;

  toggleChange() {
    this.appSidebar = !this.appSidebar;
  }
  public notesSlider: OwlOptions = {
    loop: true,
    margin: 24,
    dots: false,
    nav: true,
    smartSpeed: 2000,
    navText: [
      '<i class="fas fa-chevron-left"></i>',
      '<i class="fas fa-chevron-right"></i>',
    ],
    responsive: {
      0: {
        items: 3,
      },
      768: {
        items: 3,
      },
      1300: {
        items: 3,
      },
    },
  };
}
