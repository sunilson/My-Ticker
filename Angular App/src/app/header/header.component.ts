import { RefreshCommentsService } from './../services/refresh-comments.service';
import { LivetickerPostService } from './../services/liveticker-post-service.service';
import { AuthService } from './../services/auth-service.service';
import { LivetickerDataService } from './../services/liveticker-data-service.service';
import { Liveticker } from './../liveticker';
import { MessagingService } from './../services/messaging-service.service';
import { InfoDialogComponent } from './../info-dialog/info-dialog.component';
import { ShareDialogComponent } from './../share-dialog/share-dialog.component';
import { DialogService } from 'ng2-bootstrap-modal';
import { LivetickerControlService } from './../services/liveticker-control.service';
import { Component, OnInit, Input } from '@angular/core';
import { ActivatedRoute, Params, Router } from "@angular/router";
import { Location } from "@angular/common";
import * as firebase from 'firebase';

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.css']
})
export class HeaderComponent implements OnInit {

  livetickerID: string;
  title: string = "My Ticker";
  status: string = "No status yet!";
  state: string = "";
  description: string = "";
  user: firebase.User;
  notificationState: boolean = false;
  liveticker: Liveticker;
  currentRoute: string;

  constructor(private refreshComments: RefreshCommentsService, private location: Location, private postService: LivetickerPostService, private authService: AuthService, private dataService: LivetickerDataService, private activatedRoute: ActivatedRoute, private livetickerControlService: LivetickerControlService, private dialogService: DialogService, private messaingService: MessagingService, private livetickerDataService: LivetickerDataService) {
  }

  ngOnInit() {
    this.livetickerControlService.livetickerIDChanged.subscribe(result => {
      this.livetickerID = result[0];
      this.currentRoute = result[1];

      if (this.livetickerID) {
        this.dataService.getLiveticker(this.livetickerID).subscribe(result => {
          if (result.val()) {
            this.liveticker = new Liveticker(this.livetickerID, result.val().authorID, result.val().title, result.val().status, result.val().description, result.val().state, result.val().commentCount, result.val().likeCount);
            //Emmit Title changed event
            this.livetickerControlService.livetickerChange(this.liveticker);

            this.title = this.liveticker.title;
            if (this.liveticker.status.length > 0) {
              this.status = this.liveticker.status;
            }
            this.state = this.liveticker.state;
            this.description = this.liveticker.description;

            this.authService.getCurrentUserObservable().subscribe(user => {
              this.user = user;
              this.livetickerControlService.userChange(this.user);
              this.postService.registerAsViewer(this.livetickerID, user.uid);
              this.livetickerDataService.getLivetickerNotification(this.user.uid, this.livetickerID).subscribe(result => {
                if (result.val()) {
                  this.messaingService.requestNotificationPermission(this.user.uid);
                  this.notificationState = true;
                } else {
                  this.notificationState = false;
                }
              });
            });
          }
        });
      }
    });
  }

  refresh() {
    this.refreshComments.refresh();
  }

  share() {
    let disposable = this.dialogService.addDialog(ShareDialogComponent, {
      title: 'Share dialog',
      message: this.livetickerID.substring(1)
    }, {
        closeByClickingOutside: true
      })
  }

  info() {
    let disposable = this.dialogService.addDialog(InfoDialogComponent, {
      title: this.title,
      message: this.description,
      status: this.status
    }, {
        closeByClickingOutside: true
      })
  }

  livetickerIDForUrl() {
    return this.livetickerID.substring(1);
  }

  toggleNotifications() {
    this.messaingService.toggleNotifications(this.user.uid, this.livetickerID, this.notificationState);
  }
}
