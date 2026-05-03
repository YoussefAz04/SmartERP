import { Injectable } from '@angular/core';
import Swal, { SweetAlertIcon } from 'sweetalert2';

/**
 * Central notification service providing modern, aesthetic toasts and dialogs.
 *
 * Replaces native browser `alert()` / `confirm()` popups across the application
 * with styled SweetAlert2 notifications (toasts for feedback, modal for confirms).
 */
@Injectable({ providedIn: 'root' })
export class NotificationService {
  private toast = Swal.mixin({
    toast: true,
    position: 'top-end',
    showConfirmButton: false,
    showCloseButton: true,
    closeButtonHtml: '&times;',
    timer: 3000,
    timerProgressBar: true,
    customClass: {
      popup: 'smart-toast',
      title: 'smart-toast-title',
      htmlContainer: 'smart-toast-body',
      timerProgressBar: 'smart-toast-progress',
      closeButton: 'smart-toast-close'
    },
    didOpen: (el) => {
      el.addEventListener('mouseenter', Swal.stopTimer);
      el.addEventListener('mouseleave', Swal.resumeTimer);
    }
  });

  /** Show a success toast. */
  success(message: string, title?: string): void {
    this.fire('success', this.clean(message), title);
  }

  /** Show an error toast. */
  error(message: string, title?: string): void {
    this.fire('error', this.clean(message), title);
  }

  /** Show a warning toast. */
  warning(message: string, title?: string): void {
    this.fire('warning', this.clean(message), title);
  }

  /** Show an informational toast. */
  info(message: string, title?: string): void {
    this.fire('info', this.clean(message), title);
  }

  /**
   * Show a toast whose icon is auto-detected from common prefix symbols
   * (✓, ✅ → success; ✗, ❌ → error; ⚠ → warning). Falls back to info.
   */
  show(message: string, title?: string): void {
    const trimmed = (message ?? '').trim();
    let icon: SweetAlertIcon = 'info';
    if (/^(✓|✔|✅)/.test(trimmed)) icon = 'success';
    else if (/^(✗|✘|❌)/.test(trimmed)) icon = 'error';
    else if (/^(⚠)/.test(trimmed)) icon = 'warning';
    this.fire(icon, this.clean(trimmed), title);
  }

  /**
   * Show a modern confirmation dialog. Resolves to `true` when the user
   * confirms, `false` otherwise.
   */
  confirm(
    message: string,
    options: {
      title?: string;
      confirmText?: string;
      cancelText?: string;
      icon?: SweetAlertIcon;
    } = {}
  ): Promise<boolean> {
    return Swal.fire({
      title: options.title ?? 'Confirmer',
      text: message,
      icon: options.icon ?? 'question',
      showCancelButton: true,
      confirmButtonText: options.confirmText ?? 'Confirmer',
      cancelButtonText: options.cancelText ?? 'Annuler',
      reverseButtons: true,
      focusCancel: true,
      buttonsStyling: false,
      customClass: {
        popup: 'smart-dialog',
        title: 'smart-dialog-title',
        htmlContainer: 'smart-dialog-body',
        confirmButton: 'smart-dialog-confirm',
        cancelButton: 'smart-dialog-cancel'
      }
    }).then((res) => !!res.isConfirmed);
  }

  private fire(icon: SweetAlertIcon, text: string, title?: string): void {
    this.toast.fire({ icon, title: title ?? '', text });
  }

  /** Strip leading decoration symbols that used to be part of legacy alerts. */
  private clean(message: string): string {
    return (message ?? '').replace(/^\s*(✓|✔|✅|✗|✘|❌|⚠)\s*/, '').trim();
  }
}
