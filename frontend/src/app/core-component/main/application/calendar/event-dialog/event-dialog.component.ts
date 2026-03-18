import {Component, Inject} from '@angular/core';
import {
  MAT_DIALOG_DATA,
  MatDialogActions,
  MatDialogContent,
  MatDialogRef,
  MatDialogTitle
} from "@angular/material/dialog";
import {MatFormField, MatLabel} from "@angular/material/form-field";
import {FormsModule} from "@angular/forms";
import {MatInput} from "@angular/material/input";
import {MatOption, MatSelect} from "@angular/material/select";
import {MatButton} from "@angular/material/button";
import {NgForOf, NgIf} from "@angular/common";

@Component({
  selector: 'app-event-dialog',
  standalone: true,
  imports: [
    MatDialogContent,
    MatLabel,
    FormsModule,
    MatInput,
    MatFormField,
    MatSelect,
    MatOption,
    MatDialogActions,
    MatButton,
    MatDialogTitle,
    NgForOf,
    NgIf
  ],
  templateUrl: './event-dialog.component.html',
  styleUrl: './event-dialog.component.scss'
})
export class EventDialogComponent {
  title = '';
  selectedCategorieId: number | null = null;
  categories: any[];

  constructor(
    public dialogRef: MatDialogRef<EventDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any
  ) {
    this.categories = data.categories;
  }

  onCancel(): void {
    this.dialogRef.close();
  }

  onSave(): void {
    if (this.title && this.selectedCategorieId !== null) {
      this.dialogRef.close({ title: this.title, idCategorie: this.selectedCategorieId });
    }
  }
}

