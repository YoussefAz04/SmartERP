import { DOCUMENT } from '@angular/common';
import { Inject, Injectable } from '@angular/core';
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

  constructor(@Inject(DOCUMENT) private document: Document) {
    this.applyThemeMode(this.themeMode.value);
  }

  public changeThemeMode(theme: string): void {
    this.themeMode.next(theme);
    localStorage.setItem('themeMode', theme);
    this.applyThemeMode(theme);
  }
  public changeLayoutMode(layout: string): void {
    this.layoutMode.next(layout);
    localStorage.setItem('layoutMode', layout);
  }
  public changeNavigationColor(color: string): void {
    this.navigationColor.next(color);
    localStorage.setItem('navigationColor', color);
  }

  private applyThemeMode(theme: string): void {
    const isDark = theme === 'dark_mode';
    const body = this.document.body;
    const root = this.document.documentElement;

    body.classList.toggle('dark-mode', isDark);
    body.classList.toggle('dark-select', isDark);
    root.classList.toggle('dark-mode', isDark);
    root.dataset['theme'] = isDark ? 'dark' : 'light';
  }
}

