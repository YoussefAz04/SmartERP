import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-custom-pagination',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="pagination-wrapper">
      <div class="dataTables_info">
        Affichage des résultats
      </div>
      <div class="dataTables_paginate">
        <ul class="pagination">
          <li class="paginate_button page-item previous">
            <button class="page-link" type="button" (click)="onPrevious()">Précédent</button>
          </li>
          <li class="paginate_button page-item active">
            <button class="page-link" type="button">1</button>
          </li>
          <li class="paginate_button page-item next">
            <button class="page-link" type="button" (click)="onNext()">Suivant</button>
          </li>
        </ul>
      </div>
    </div>
  `,
  styles: [`
    .pagination-wrapper {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 10px 0;
    }
  `]
})
export class CustomPaginationComponent {
  @Input() currentPage = 1;
  @Input() totalPages = 1;
  @Output() pageChange = new EventEmitter<number>();

  onPrevious(): void {
    if (this.currentPage > 1) {
      this.pageChange.emit(this.currentPage - 1);
    }
  }

  onNext(): void {
    if (this.currentPage < this.totalPages) {
      this.pageChange.emit(this.currentPage + 1);
    }
  }
}
