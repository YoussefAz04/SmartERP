import { ComponentFixture, TestBed } from '@angular/core/testing';
import { convertToParamMap, provideRouter } from '@angular/router';
import { of } from 'rxjs';

import { EditClientComponent } from './edit-client.component';
import { ClientService } from '../../../core/service/client/client.service';
import { NotificationService } from '../../../shared/notification/notification.service';
import { ActivatedRoute } from '@angular/router';

describe('EditClientComponent', () => {
  let component: EditClientComponent;
  let fixture: ComponentFixture<EditClientComponent>;
  let clientServiceSpy: jasmine.SpyObj<ClientService>;
  let notificationSpy: jasmine.SpyObj<NotificationService>;

  beforeEach(async () => {
    clientServiceSpy = jasmine.createSpyObj<ClientService>('ClientService', ['getAllCategorieClients', 'getClientById', 'updateClient', 'addClient']);
    notificationSpy = jasmine.createSpyObj<NotificationService>('NotificationService', ['error', 'success', 'warning']);
    clientServiceSpy.getAllCategorieClients.and.returnValue(of([]));
    clientServiceSpy.getClientById.and.returnValue(of({
      id: 1,
      nom: 'Test',
      prenom: 'Client',
      email: 'test@example.com',
      telephone: '12345678',
      adresse: 'Test address',
      secteurActivite: 'Test sector',
      categorieClient: { id: 1, nom: 'VIP', description: 'VIP' }
    }));
    clientServiceSpy.updateClient.and.returnValue(of({
      nom: 'Test',
      prenom: 'Client',
      email: 'test@example.com',
      telephone: '12345678',
      adresse: 'Test address',
      secteurActivite: 'Test sector'
    }));
    clientServiceSpy.addClient.and.returnValue(of({
      nom: 'Test',
      prenom: 'Client',
      email: 'test@example.com',
      telephone: '12345678',
      adresse: 'Test address',
      secteurActivite: 'Test sector'
    }));

    await TestBed.configureTestingModule({
      imports: [EditClientComponent],
      providers: [
        provideRouter([]),
        { provide: ClientService, useValue: clientServiceSpy },
        { provide: NotificationService, useValue: notificationSpy },
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              paramMap: convertToParamMap({ id: '1' })
            }
          }
        }
      ]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(EditClientComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
