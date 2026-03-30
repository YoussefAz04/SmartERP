import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';

import { LoaderComponent } from './common-component/loader/loader.component';
import { SharedModule } from './shared/shared.module';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';



@NgModule({
  declarations: [AppComponent, LoaderComponent],
  imports: [BrowserModule, AppRoutingModule, SharedModule, BrowserAnimationsModule],
  bootstrap: [AppComponent],
})
export class AppModule {}
