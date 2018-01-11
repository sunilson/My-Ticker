import { ShareDialogComponent } from './../share-dialog/share-dialog.component';
import { CommentDialogComponent } from './.././comment-dialog/comment-dialog.component';
import { AuthService } from './../services/auth-service.service';
import { LivetickerPostService } from './../services/liveticker-post-service.service';
import { DialogService } from 'ng2-bootstrap-modal';
import { LivetickerControlService } from './../services/liveticker-control.service';
import { User } from './../models/user';
import { Liveticker } from './../liveticker';
import { LivetickerElement } from './../models/livetickerElement';
import { FirebaseObjectObservable } from 'angularfire2/database';
import { LivetickerDataService } from './../services/liveticker-data-service.service';
import { LivetickerListDataService } from './../services/liveticker-list-data-service.service';
import { FirebaseListObservable } from 'angularfire2/database';
import { Component, OnInit, Output, EventEmitter, OnDestroy } from '@angular/core';
import { ActivatedRoute, Params, Router } from "@angular/router";
import { Modal, BSModalContext } from 'angular2-modal/plugins/bootstrap';
import * as firebase from 'firebase';

@Component({
  selector: 'app-liveticker',
  templateUrl: './liveticker.component.html',
  styleUrls: ['./liveticker.component.css']
})


export class LivetickerComponent implements OnInit, OnDestroy {

  livetickerElements: LivetickerElement[] = [];
  liveticker: Liveticker;
  author: User;
  livetickerID: string;
  status: string = "loading";
  viewerCount: number = 0;
  user: firebase.User;
  parentRouter: Router;
  liked: boolean = false;

  constructor(private router: Router, private dataService: LivetickerDataService, private activatedRoute: ActivatedRoute, private livetickerControlService: LivetickerControlService, private dialogService: DialogService, private postService: LivetickerPostService, private authService: AuthService) {
  }

  ngOnDestroy() {
    this.postService.unregisterAsViewer(this.livetickerID, this.user.uid);
  }

  ngOnInit() {
    this.activatedRoute.params.subscribe((param: Params) => {
      this.livetickerID = '-' + param['id'];

      this.livetickerControlService.livetickerIDChange(this.livetickerID, "liveticker");

      //Get Liveticker and setup the rest
      this.dataService.getLiveticker(this.livetickerID).subscribe(result => {
        if (result.val()) {
          this.liveticker = result.val();

          //Emmit Liveticker
          this.livetickerControlService.livetickerChange(this.liveticker);

          //Register as viewer
          this.livetickerControlService.userChanged.subscribe(result => {
            this.user = result;

            //Get Liked Listener
            this.dataService.getLikedLiveticker(this.livetickerID, this.user.uid).subscribe(result => {
              this.liked = result.val();
            });
          });

          //Get Viewer Count
          this.dataService.getLivetickerViewerCount(this.livetickerID).subscribe(result => {
            this.viewerCount = result.val();
          });

          //Get Author
          this.dataService.getLivetickerAuthor(this.liveticker.authorID).subscribe(result => {
            this.author = new User(result.val().userName, result.val().userName, result.val().profilePicture, result.val().titlePicture);
            this.status = "finished";
          });
        } else {
          //Liveticker can't be loaded
          this.status = "error";
        }
      });

      //Get the Events of the Liveticker
      this.dataService.getLivetickerContents(this.livetickerID).subscribe(result => {
        this.livetickerElements = [];
        for (let i = result.length - 1; i >= 0; i--) {
          if (result[i].type === "text") {
            this.livetickerElements.push(new LivetickerElement(result[i].content, result[i].timestamp, result[i].type));
          } else if (result[i].type === "image") {
            this.livetickerElements.push(new LivetickerElement(result[i].content, result[i].timestamp, result[i].type, result[i].thumbnail));
          } else {
            this.livetickerElements.push(new LivetickerElement(result[i].content, result[i].timestamp, result[i].type));
          }
        }
      });
    });
  }

  like() {
    if (!this.liked) {
      this.postService.addLike(this.livetickerID, this.user.uid);
    } else {
      this.postService.removeLike(this.livetickerID, this.user.uid);
    }
  }
}
