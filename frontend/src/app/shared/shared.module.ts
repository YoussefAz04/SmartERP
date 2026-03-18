import { NgModule } from '@angular/core';
import { HttpClientModule, HTTP_INTERCEPTORS } from '@angular/common/http';
import { NgApexchartsModule } from 'ng-apexcharts';
import { DataService } from '../core/core.index';
import { SpinnerInterceptor } from '../core/interceptor/spinner/spinner.interceptor';
import { MaterialModule } from './material/material.module';
import { FeatherIconModule } from './feather/feather.module';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { CarouselModule } from 'ngx-owl-carousel-o';
import { CustomPaginationModule } from './custom-pagination/custom-pagination.module';
import { FullCalendarModule } from '@fullcalendar/angular';
import { NgxbootstrapModule } from 'src/app/shared/ngx-bootstrap/ngxbootstrap.module';
import { DragDropModule } from '@angular/cdk/drag-drop';
import { NgChartsModule } from 'ng2-charts';
import { NgxEditorModule } from 'ngx-editor';
import { PopoverModule } from 'ngx-bootstrap/popover';
import { MatTooltipModule } from '@angular/material/tooltip';
import { ToastModule } from 'primeng/toast';
import {
  BsDatepickerModule,
  BsDatepickerConfig,
} from 'ngx-bootstrap/datepicker';
import { SettingsSidebarModule } from '../core-component/settings/common/settings-sidebar/settings-sidebar.module';
import { NgxMaskModule } from 'ngx-mask';
import { NgxDropzoneModule } from 'ngx-dropzone';
import { MatStepperModule } from '@angular/material/stepper';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatChipsModule } from '@angular/material/chips';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { LightgalleryModule } from 'lightgallery/angular'; 
import { NgxMatIntlTelInputComponent } from 'ngx-mat-intl-tel-input';
import { CountUpModule } from 'ngx-countup';
import { TimepickerActions, TimepickerModule } from 'ngx-bootstrap/timepicker';
import { DatePipe } from '@angular/common';
import { FeatherModule } from 'angular-feather';
import { Tool } from 'angular-feather/icons';

const icons = {
  Tool
};
@NgModule({
  imports: [
    FeatherIconModule,
    MaterialModule,
    HttpClientModule,
    NgApexchartsModule,
    FormsModule,
    CarouselModule,
    CustomPaginationModule,
    DragDropModule,
    NgxMaskModule.forRoot({
      showMaskTyped: false,
    }),
    NgChartsModule.forRoot(),
    FullCalendarModule,
    NgxbootstrapModule,
    NgxEditorModule,
    PopoverModule,
    MatTooltipModule,
    ToastModule,
    BsDatepickerModule.forRoot(),
    SettingsSidebarModule,
    NgxMaskModule.forRoot({
      showMaskTyped: false,
    }),
    NgxDropzoneModule,
    MatStepperModule,
    MatFormFieldModule,
    MatChipsModule,
    MatAutocompleteModule,
    ReactiveFormsModule,
    ReactiveFormsModule,
    LightgalleryModule,
    NgxMatIntlTelInputComponent,
    CountUpModule,
    TimepickerModule,
    
    FeatherModule.pick(icons)

  ],
  exports: [
    MaterialModule,
    HttpClientModule,
    FeatherIconModule,
    NgApexchartsModule,
    FormsModule,
    CarouselModule,
    CustomPaginationModule,
    DragDropModule,
    NgChartsModule,
    FullCalendarModule,
    NgxbootstrapModule,
    NgxMaskModule,
    NgxEditorModule,
    PopoverModule,
    MatTooltipModule,
    ToastModule,
    BsDatepickerModule,
    SettingsSidebarModule,
    NgxMaskModule,
    NgxDropzoneModule,
    MatStepperModule,
    MatFormFieldModule,
    MatChipsModule,
    MatAutocompleteModule,
    ReactiveFormsModule,
    ReactiveFormsModule,
    LightgalleryModule,
    NgxMatIntlTelInputComponent,
    CountUpModule,
    TimepickerModule,
    FeatherModule
  ],
  providers: [
    DataService,
    DatePipe,
    TimepickerActions,
    BsDatepickerConfig,
    { provide: HTTP_INTERCEPTORS, useClass: SpinnerInterceptor, multi: true },
  ],
})
export class sharedModule {}
