import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { FormsModule } from '@angular/forms';
import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';

import { LoaderComponent } from './common-component/loader/loader.component';
import { SharedModule } from './shared/shared.module';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { ErpHeaderComponent } from './components/header/header.component';



@NgModule({
  declarations: [AppComponent, LoaderComponent, ErpHeaderComponent],
  imports: [BrowserModule, AppRoutingModule, SharedModule, BrowserAnimationsModule, FormsModule],
  bootstrap: [AppComponent],
})
export class AppModule {}
