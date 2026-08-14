import { Injectable } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { BehaviorSubject } from 'rxjs';

export type AppLanguage = 'en' | 'fr';

export interface LanguageOption {
  code: AppLanguage;
  label: string;
  nativeLabel: string;
}

@Injectable({ providedIn: 'root' })
export class LanguageService {
  private readonly storageKey = 'smart_erp_language';
  private readonly fallbackLanguage: AppLanguage = 'en';
  private readonly supportedLanguages: AppLanguage[] = ['en', 'fr'];

  readonly languages: LanguageOption[] = [
    { code: 'en', label: 'English', nativeLabel: 'English' },
    { code: 'fr', label: 'French', nativeLabel: 'Français' },
  ];

  private readonly currentLanguageSubject =
    new BehaviorSubject<AppLanguage>(this.fallbackLanguage);

  readonly currentLanguage$ = this.currentLanguageSubject.asObservable();

  constructor(private translate: TranslateService) {}

  init(): void {
    this.translate.addLangs(this.supportedLanguages);
    this.translate.setDefaultLang(this.fallbackLanguage);
    this.use(this.getInitialLanguage());
  }

  use(language: string): void {
    const nextLanguage = this.normalizeLanguage(language);
    localStorage.setItem(this.storageKey, nextLanguage);
    this.currentLanguageSubject.next(nextLanguage);
    this.translate.use(nextLanguage);
    document.documentElement.lang = nextLanguage;
  }

  get currentLanguage(): AppLanguage {
    return this.currentLanguageSubject.value;
  }

  private getInitialLanguage(): AppLanguage {
    const storedLanguage = localStorage.getItem(this.storageKey);
    return this.normalizeLanguage(storedLanguage || navigator.language);
  }

  private normalizeLanguage(language: string): AppLanguage {
    const shortCode = (language || '').toLowerCase().split('-')[0];
    return this.supportedLanguages.includes(shortCode as AppLanguage)
      ? (shortCode as AppLanguage)
      : this.fallbackLanguage;
  }
}
