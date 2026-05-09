import { Component, OnDestroy } from '@angular/core';
import {
  NavigationEnd,
  NavigationStart,
  Router,
  Event as RouterEvent,
} from '@angular/router';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { SpinnerService } from './core/core.index';


@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss'],
})
export class AppComponent implements OnDestroy {
  title = 'template';
  public page = '';

  private readonly destroy$ = new Subject<void>();

  constructor(private router: Router, private spinner: SpinnerService) {
    // Single subscription to router events – the previous code subscribed
    // twice and did the same work for every navigation, doubling overhead.
    this.router.events
      .pipe(takeUntil(this.destroy$))
      .subscribe((event: RouterEvent) => {
        if (event instanceof NavigationStart) {
          const URL = event.url.split('/');
          this.page = URL[1];
          this.spinner.show();
        }
        if (event instanceof NavigationEnd) {
          this.spinner.hide();
        }
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
