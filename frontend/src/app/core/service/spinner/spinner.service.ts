import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class SpinnerService {
  private _loading = new BehaviorSubject<boolean>(false);
  public readonly loading$ = this._loading.asObservable();

  // Counter for in-flight operations. The spinner stays visible while at
  // least one is active and is hidden synchronously when the count hits 0.
  // This avoids both flicker between back-to-back requests and the 400ms
  // tax that a deferred hide adds to every HTTP call.
  private active = 0;

  show(): void {
    this.active++;
    if (!this._loading.value) {
      this._loading.next(true);
    }
  }

  hide(): void {
    if (this.active > 0) {
      this.active--;
    }
    if (this.active === 0 && this._loading.value) {
      this._loading.next(false);
    }
  }
}
