import { ChangeDetectorRef, Component, ViewEncapsulation, OnInit, OnDestroy } from '@angular/core';
import { routes } from 'src/app/core/core.index';
import dayGridPlugin from '@fullcalendar/daygrid';
import interactionPlugin from '@fullcalendar/interaction';
import timeGridPlugin from '@fullcalendar/timegrid';
import {
  CalendarOptions,
  DateSelectArg,
  EventClickArg,
  EventInput,
} from '@fullcalendar/core';
import listPlugin from '@fullcalendar/list';
import { Subject } from 'rxjs';
import { takeUntil, catchError } from 'rxjs/operators';
import { of } from 'rxjs';

import { Evenement } from "../../../../model/evenement.model";
import { EvenementService } from "../../../../core/service/evenement/evenement.service";
import {CategorieEvenement, ColorCategorie} from "../../../../model/categorieEvenement.model";
import {MatDialog} from "@angular/material/dialog";
import {EventDialogComponent} from "./event-dialog/event-dialog.component";
import {FormBuilder, FormGroup} from "@angular/forms";
import {Validators} from "ngx-editor";
import {ResponseDto} from "../../../../model/reponseDto.model";
import {PermissionDto} from "../../../../model/permissionDto.model";
import { NotificationService } from '../../../../shared/notification/notification.service';

@Component({
  selector: 'app-calendar',
  templateUrl: './calendar.component.html',
  styleUrls: ['./calendar.component.scss'],
  encapsulation: ViewEncapsulation.None,
})
export class CalendarComponent implements OnInit, OnDestroy {
  public routes = routes;

  calendarOptions!: CalendarOptions;
  currentEvents: Evenement[] = [];
  calendarVisible = true;
  isLoading = false;
  error: string | null = null;

  private destroy$ = new Subject<void>();

  // Mapping des couleurs enum vers des couleurs hex/CSS
  private colorMapping: { [key in ColorCategorie]: string } = {
    [ColorCategorie.ROUGE]: '#dc3545',
    [ColorCategorie.BLEU]: '#0d6efd',
    [ColorCategorie.VERT]: '#198754',
    [ColorCategorie.JAUNE]: '#ffc107'
  };
   permissionEvenement!: PermissionDto;

  constructor(private evenementService: EvenementService,
    private changeDetector: ChangeDetectorRef,
    private dialog: MatDialog,
    private fb: FormBuilder,
    private notification: NotificationService) {}

  ngOnInit(): void {
    this.initializeCalendar();
    this.loadEvents();
    this.getAllCategorie() ;
    this.getUserAuthData();

    this.categoryForm = this.fb.group({
      titre: ['', Validators.required],
      colorCategorie: ['', Validators.required]
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private initializeCalendar(): void {
    this.calendarOptions = {
      plugins: [interactionPlugin, dayGridPlugin, timeGridPlugin, listPlugin],
      headerToolbar: {
        left: 'prev,next today',
        center: 'title',
        right: 'dayGridMonth,timeGridWeek,timeGridDay,listWeek',
      },
      initialView: 'dayGridMonth',
      events: [],
      editable: true,
      selectable: true,
      selectMirror: true,
      dayMaxEvents: true,
      weekends: true,
      select: this.handleDateSelect.bind(this),
      eventClick: this.handleEventClick.bind(this),
      loading: this.handleLoading.bind(this),
      // Configuration pour améliorer l'affichage des couleurs
      eventDisplay: 'block',
      eventBackgroundColor: 'transparent',
      eventBorderColor: 'transparent',
    };
  }

  loadEvents(): void {
    this.isLoading = true;
    this.error = null;

    this.evenementService.getAllEvenement()
      .pipe(
        takeUntil(this.destroy$),
        catchError(error => {
          console.error('Error loading events:', error);
          this.error = 'Failed to load events. Please try again.';
          return of([]);
        })
      )
      .subscribe(events => {
        this.currentEvents = events;
        const fullCalendarEvents: EventInput[] = events.map(event => {
          // Conversion de la couleur enum vers couleur CSS
          const categoryColor = event.categorieEvenement?.colorCategorie
            ? this.getColorFromEnum(event.categorieEvenement.colorCategorie)
            : '#6c757d'; // Couleur par défaut (gris) si pas de catégorie

          return {
            id: event.id?.toString(),
            title: event.title,
            start: event.start,
            end: event.end,
            allDay: event.allDay,
            backgroundColor: categoryColor,
            borderColor: categoryColor,
            textColor: this.getContrastColor(categoryColor),
            // Ajouter des données personnalisées pour le template
            extendedProps: {
              categoryTitle: event.categorieEvenement?.titre || 'Sans catégorie',
              categoryColor: event.categorieEvenement?.colorCategorie || null
            }
          };
        });

        this.calendarOptions = {
          ...this.calendarOptions,
          events: fullCalendarEvents
        };

        this.isLoading = false;
        this.changeDetector.detectChanges();
      });
  }

  /**
   * Convertit une couleur enum en couleur CSS
   */
  private getColorFromEnum(colorEnum: ColorCategorie): string {
    return this.colorMapping[colorEnum] || '#6c757d';
  }

  /**
   * Détermine la couleur du texte (blanc ou noir) en fonction de la couleur de fond
   */
  private getContrastColor(hexColor: string): string {
    // Supprimer le # si présent
    const hex = hexColor.replace('#', '');

    // Convertir en RGB
    const r = parseInt(hex.substr(0, 2), 16);
    const g = parseInt(hex.substr(2, 2), 16);
    const b = parseInt(hex.substr(4, 2), 16);

    // Calculer la luminosité
    const luminance = (0.299 * r + 0.587 * g + 0.114 * b) / 255;

    // Retourner blanc pour les couleurs sombres, noir pour les couleurs claires
    return luminance > 0.5 ? '#000000' : '#ffffff';
  }

  handleDateSelect(selectInfo: DateSelectArg): void {
    this.evenementService.getAllCategorie().subscribe(categories => {
      const dialogRef = this.dialog.open(EventDialogComponent, {
        width: '400px',
        data: { categories }
      });

      dialogRef.afterClosed().subscribe(result => {
        if (result?.title && result?.idCategorie) {
          const newEvent: Evenement = {
            title: result.title,
            start: new Date(selectInfo.start).toISOString(),
            end: new Date(selectInfo.end).toISOString(),
            allDay: selectInfo.allDay
          };

          this.createEvent(newEvent, result.idCategorie);
        }

        selectInfo.view.calendar.unselect();
      });
    });
  }
  handleEventClick(clickInfo: EventClickArg): void {
    const categoryInfo = clickInfo.event.extendedProps['categoryTitle']
      ? `\nCatégorie: ${clickInfo.event.extendedProps['categoryTitle']}`
      : '';

    this.notification.confirm(`Supprimer l'événement '${clickInfo.event.title}' ?${categoryInfo}`).then(ok => {
      if (ok) {
        const id = clickInfo.event.id;
        if (id) {
          this.deleteEvent(+id);
        }
      }
    });
  }

  handleLoading(isLoading: boolean): void {
    this.isLoading = isLoading;
  }

  private createEvent(event: Evenement, idCategorie: number): void {
    if (this.permissionEvenement.create)
    this.evenementService.saveEvenement(event, idCategorie)
      .pipe(
        takeUntil(this.destroy$),
        catchError(error => {
          console.error('Erreur création événement:', error);
          this.notification.error('Erreur lors de la création');
          return of(null);
        })
      )
      .subscribe(result => {
        if (result) this.loadEvents();
      });
  }

  private deleteEvent(id: number): void {
    this.evenementService.deleteEvenement(id)
      .pipe(
        takeUntil(this.destroy$),
        catchError(error => {
          console.error('Error deleting event:', error);
          this.notification.error('Erreur lors de la suppression de l\'événement');
          return of(null);
        })
      )
      .subscribe(result => {
        if (result !== null) {
          this.loadEvents();
        }
      });
  }

  // Additional utility methods
  refreshCalendar(): void {
    this.loadEvents();
  }

  changeView(view: string): void {
    this.calendarOptions = {
      ...this.calendarOptions,
      initialView: view as any
    };
  }


  getAvailableColors(): Array<{enum: ColorCategorie, hex: string, name: string}> {
    return [
      { enum: ColorCategorie.ROUGE, hex: this.colorMapping[ColorCategorie.ROUGE], name: 'ROUGE' },
      { enum: ColorCategorie.BLEU, hex: this.colorMapping[ColorCategorie.BLEU], name: 'BLEU' },
      { enum: ColorCategorie.VERT, hex: this.colorMapping[ColorCategorie.VERT], name: 'VERT' },
      { enum: ColorCategorie.JAUNE, hex: this.colorMapping[ColorCategorie.JAUNE], name: 'JAUNE' }
    ];
  }

  categoryForm!: FormGroup;

  onSubmit(): void {
    if (this.categoryForm.invalid) return;

    const formValue = this.categoryForm.value;

    console.log('Form values:', formValue);

    this.evenementService.createCategorieEvenement(formValue).subscribe({
      next: res => {
        console.log('Catégorie ajoutée:', res);
        this.categoryForm.reset();
        this.getAllCategorie();
      },
      error: err => console.error('Erreur:', err)
    });
  }


  categorieList : CategorieEvenement[] =[];
  getAllCategorie(){
    this.evenementService.getAllCategorie().subscribe(data =>{
      this.categorieList =data ;
    })
  }

  public mapEnumToColor(color: ColorCategorie): string {
    return this.colorMapping[color] || '#6c757d';
  }


  getUserAuthData(){
    const item = localStorage.getItem('myLSkey');
    let currentUser: ResponseDto | null = null;

    if (item) {
      currentUser = JSON.parse(decodeURIComponent(escape(atob(item))));
      console.log('User connecté:', currentUser?.name);

      console.log('Token:', currentUser?.token);
      console.log('Permissions:', currentUser?.permission);
      currentUser?.permission.map(per =>{
        if (per.moduleTitle == "Evenement"){
          this.permissionEvenement = per ;
          console.log("permission Evenement" ,this.permissionEvenement);
        }
      })
      console.log('Taches:', currentUser?.entreprise.nom);
    }
  }

}
