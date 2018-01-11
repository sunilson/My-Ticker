import { LivetickerComment } from './../models/livetickerComment';
import { LivetickerDataService } from './../services/liveticker-data-service.service';
import { Component, OnInit, OnDestroy } from '@angular/core';
import { DialogComponent, DialogService } from "ng2-bootstrap-modal";

export interface AlertModel {
  title: string;
  message: string;
  userID: string;
}

@Component({
  selector: 'confirm',
  templateUrl: './comment-dialog.component.html',
  styleUrls: ['./comment-dialog.component.css']
})

export class CommentDialogComponent extends DialogComponent<AlertModel, boolean> implements AlertModel, OnInit, OnDestroy {
  title: string;
  message: string;
  userID: string;
  status: string = "loading";
  subscription;
  livetickerComments: LivetickerComment[] = [];
  constructor(dialogService: DialogService, private dataService: LivetickerDataService) {
    super(dialogService);
  }

  ngOnInit() {
    this.subscription = this.dataService.getLivetickerComments(this.message, this.userID).then(obs => {
      this.subscription = obs.subscribe(result => {
        if (result.val()) {
          this.status = "finished";
          for (let key in result.val().comments) {
            let comment = result.val().comments[key];
            this.livetickerComments.push(new LivetickerComment(comment.content, comment.timestamp, comment.userName, comment.profilePicture));
          }
        }
      })
    });
  }

  ngOnDestroy() {
    this.subscription.unsubscribe();
  }
}
