import { Component } from '@angular/core';
import { Router } from '@angular/router';
import {
  SettingsService,
  routes,
} from 'src/app/core/core.index';

@Component({
  selector: 'app-layout',
  templateUrl: './layout.component.html',
  styleUrls: ['./layout.component.scss'],
})
export class LayoutComponent {
  public routes = routes;
  showPreview = false;
  themeMode: string = 'light_mode';
  layoutMode: string = 'default_mode';
  navigationColor: string = 'light_color';


  constructor(private settings: SettingsService, private router: Router) {
    this.settings.themeMode.subscribe((res: string) => {
      this.themeMode = res;
    });
    this.settings.layoutMode.subscribe((res: string) => {
      this.layoutMode = res;
    });
    this.settings.navigationColor.subscribe((res: string) => {
      this.navigationColor = res;
    });
  }

  public changeThemeMode(theme: string): void {
    this.settings.themeMode.next(theme);
    localStorage.setItem('themeMode', theme);
  }
  public changeLayoutMode(layout: string): void {
    this.settings.layoutMode.next(layout);
    localStorage.setItem('layoutMode', layout);
  }
  public changeNavigationColor(color: string): void {
    this.settings.navigationColor.next(color);
    localStorage.setItem('navigationColor', color);
  }

  resetAllMode(){
    this.settings.changeThemeMode('light_mode');
    this.settings.changeLayoutMode('default_mode');
    this.settings.changeNavigationColor('light_color');
  }
  setRtlFormat(): void {
    window.location.href =
      'https://dreamspos.dreamstechnologies.com/angular/template-rtl';
  }
  
}
