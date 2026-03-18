import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class SettingsService {
  // Theme Mode
  public themeMode: BehaviorSubject<string> = new BehaviorSubject<string>(
    localStorage.getItem('themeMode') || 'light_mode'
  );

  // Layout Mode
  public layoutMode: BehaviorSubject<string> = new BehaviorSubject<string>(
    localStorage.getItem('layoutMode') || 'default_mode'
  );

  // Navigation Color
  public navigationColor: BehaviorSubject<string> = new BehaviorSubject<string>(
    localStorage.getItem('navigationColor') || 'light_color'
  );

  public changeThemeMode(theme: string): void {
    this.themeMode.next(theme);
    localStorage.setItem('themeMode', theme);
  }
  public changeLayoutMode(layout: string): void {
    this.layoutMode.next(layout);
    localStorage.setItem('layoutMode', layout);
  }
  public changeNavigationColor(color: string): void {
    this.navigationColor.next(color);
    localStorage.setItem('navigationColor', color);
  }
}

