import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';

import { AddClientComponent } from './add-client.component';
import { ClientService } from '../../../core/service/client/client.service';
import { NotificationService } from '../../../shared/notification/notification.service';

describe('AddClientComponent', () => {
  let component: AddClientComponent;
  let fixture: ComponentFixture<AddClientComponent>;
  let clientServiceSpy: jasmine.SpyObj<ClientService>;
  let notificationSpy: jasmine.SpyObj<NotificationService>;

  beforeEach(async () => {
    clientServiceSpy = jasmine.createSpyObj<ClientService>('ClientService', ['getAllCategorieClients', 'addClient']);
    notificationSpy = jasmine.createSpyObj<NotificationService>('NotificationService', ['error', 'success', 'warning']);
    clientServiceSpy.getAllCategorieClients.and.returnValue(of([]));
    clientServiceSpy.addClient.and.returnValue(of({
      nom: 'Test',
      prenom: 'Client',
      email: 'test@example.com',
      telephone: '12345678',
      adresse: 'Test address',
      secteurActivite: 'Test sector'
    }));

    await TestBed.configureTestingModule({
      imports: [AddClientComponent],
      providers: [
        provideRouter([]),
        { provide: ClientService, useValue: clientServiceSpy },
        { provide: NotificationService, useValue: notificationSpy }
      ]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(AddClientComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
