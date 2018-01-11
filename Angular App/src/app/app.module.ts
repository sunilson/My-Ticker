import { MessagingService } from './services/messaging-service.service';
import { CommentDialogComponent } from './comment-dialog/comment-dialog.component';
import { LivetickerPostService } from './services/liveticker-post-service.service';
import { RefreshCommentsService } from './services/refresh-comments.service';
import { BootstrapModalModule } from 'ng2-bootstrap-modal';
import { LivetickerControlService } from './services/liveticker-control.service';
import { AuthService } from './services/auth-service.service';
import { routing } from './app.routing';
import { LivetickerDataService } from './services/liveticker-data-service.service';
import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpModule } from '@angular/http';
import { AppComponent } from './app.component';
import { LivetickerComponent } from './liveticker/liveticker.component';
import { LivetickerElementComponent } from './liveticker-element/liveticker-element.component';
import { AngularFireModule } from "angularfire2";
import { environment } from "../environments/environment";
import { AngularFireDatabaseModule } from "angularfire2/database";
import { AngularFireAuthModule } from "angularfire2/auth";
import { LivetickerListDataService } from './services/liveticker-list-data-service.service';
import { HomeComponent } from './home/home.component';
import { FeedComponent } from './feed/feed.component';
import { FeedElementComponent } from './feed-element/feed-element.component';
import { HeaderComponent } from './header/header.component';
import { LoadingDirective } from './loading.directive';
import { LoadingComponent } from './loading/loading.component';
import { LivetickerCommentComponent } from './liveticker-comment/liveticker-comment.component';
import { ShareDialogComponent } from './share-dialog/share-dialog.component';
import { InfoDialogComponent } from './info-dialog/info-dialog.component';
import { ImageDialogComponent } from './image-dialog/image-dialog.component';
import { CommentsComponent } from './comments/comments.component';
import { SimpleNotificationsModule } from 'angular2-notifications';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { LazyLoadImageModule } from 'ng-lazyload-image';
import { ImageComponent } from './image/image.component';

@NgModule({
  declarations: [
    AppComponent,
    LivetickerComponent,
    LivetickerElementComponent,
    HomeComponent,
    FeedComponent,
    FeedElementComponent,
    HeaderComponent,
    LoadingDirective,
    LoadingComponent,
    CommentDialogComponent,
    LivetickerCommentComponent,
    ShareDialogComponent,
    InfoDialogComponent,
    ImageDialogComponent,
    CommentsComponent,
    ImageComponent
  ],
  imports: [
    BootstrapModalModule,
    BrowserModule,
    FormsModule,
    HttpModule,
    BrowserAnimationsModule,
    SimpleNotificationsModule.forRoot(),
    AngularFireModule.initializeApp(environment.firebase),
    AngularFireDatabaseModule, // imports firebase/database, only needed for database features
    AngularFireAuthModule,
    LazyLoadImageModule,
    routing
  ],

  entryComponents: [
    CommentDialogComponent, ShareDialogComponent, InfoDialogComponent
  ],
  providers: [RefreshCommentsService, LivetickerListDataService, LivetickerDataService, AuthService, LivetickerControlService, LivetickerPostService, MessagingService],
  bootstrap: [AppComponent],
})
export class AppModule { }
