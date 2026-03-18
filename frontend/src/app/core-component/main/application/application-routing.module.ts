import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { CallHistoryComponent } from './call/call-history/call-history.component';
import { ApplicationComponent } from './application.component';
import { FileArchivedComponent } from './file/file-archived/file-archived.component';
import { FileDocumentComponent } from './file/file-document/file-document.component';
import { FileFavouritesComponent } from './file/file-favourites/file-favourites.component';
import { FileManagerComponent } from './file/file-manager/file-manager.component';
import { FileRecentComponent } from './file/file-recent/file-recent.component';
import { FileSharedComponent } from './file/file-shared/file-shared.component';
import { AudioCallComponent } from './call/audio-call/audio-call.component';
import { VideoCallComponent } from './call/video-call/video-call.component';
import { ChatComponent } from './chat/chat.component';
import { TodoComponent } from './todo/todo.component';
import { NotesComponent } from './notes/notes.component';
import { FileManagerDeletedComponent } from './file/file-manager-deleted/file-manager-deleted.component';

const routes: Routes = [
  {
    path: '',
    component: ApplicationComponent,
    children: [
      {
        path: 'chat',
        component: ChatComponent,
      },
      {
        path: 'calendar',
        loadChildren: () =>
          import('./calendar/calendar.module').then((m) => m.CalendarModule),
      },
      {
        path: 'email',
        loadChildren: () =>
          import('./email/email.module').then((m) => m.EmailModule),
      },
      {
        path: 'call-history',
        component: CallHistoryComponent,
      },
      {
        path: 'file-archived',
        component: FileArchivedComponent,
      },
      {
        path: 'file-document',
        component: FileDocumentComponent,
      },
      {
        path: 'file-favourites',
        component: FileFavouritesComponent,
      },
      {
        path: 'file-manager',
        component: FileManagerComponent,
      },
      {
        path: 'file-manager-deleted',
        component: FileManagerDeletedComponent,
      },
      {
        path: 'file-recent',
        component: FileRecentComponent,
      },
      {
        path: 'file-shared',
        component: FileSharedComponent,
      },
      {
        path: 'audio-call',
        component: AudioCallComponent,
      },
      {
        path: 'video-call',
        component: VideoCallComponent,
      },
      {
        path: 'todo',
        component: TodoComponent,
      },
      {
        path: 'notes',
        component: NotesComponent,
      },
    ],
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class ApplicationRoutingModule {}
