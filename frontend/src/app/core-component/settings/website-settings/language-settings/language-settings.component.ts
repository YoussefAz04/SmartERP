import { Component, OnDestroy, OnInit } from '@angular/core';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { SidebarService, routes } from 'src/app/core/core.index';
import {
  AppLanguage,
  LanguageOption,
  LanguageService,
} from 'src/app/core/service/language/language.service';

@Component({
  selector: 'app-language-settings',
  templateUrl: './language-settings.component.html',
  styleUrl: './language-settings.component.scss',
})
export class LanguageSettingsComponent implements OnInit, OnDestroy {
  public routes = routes;
  public isCollapsed = false;
  public selectedLanguage: AppLanguage = 'en';
  public readonly languages: LanguageOption[];

  private readonly destroy$ = new Subject<void>();

  constructor(
    private sidebar: SidebarService,
    private languageService: LanguageService
  ) {
    this.languages = this.languageService.languages;
  }

  ngOnInit(): void {
    this.selectedLanguage = this.languageService.currentLanguage;
    this.languageService.currentLanguage$
      .pipe(takeUntil(this.destroy$))
      .subscribe((language) => {
        this.selectedLanguage = language;
      });
  }

  changeLanguage(language: AppLanguage): void {
    this.languageService.use(language);
  }

  toggleCollapse(): void {
    this.sidebar.toggleCollapse();
    this.isCollapsed = !this.isCollapsed;
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
