import { RefreshCommentsService } from './../services/refresh-comments.service';
import { NotificationsService } from 'angular2-notifications';
import { LivetickerPostService } from './../services/liveticker-post-service.service';
import { LivetickerControlService } from './../services/liveticker-control.service';
import { ActivatedRoute, Params } from '@angular/router';
import { AuthService } from './../services/auth-service.service';
import { LivetickerDataService } from './../services/liveticker-data-service.service';
import { LivetickerComment } from './../models/livetickerComment';
import { Component, OnInit, Input } from '@angular/core';

@Component({
  selector: 'app-comments',
  templateUrl: './comments.component.html',
  styleUrls: ['./comments.component.css']
})
export class CommentsComponent implements OnInit {

  @Input() livetickerID: string;
  user: firebase.User;
  subscription;
  status: string = "";
  commentValue: string = "";
  addingComment: boolean = false;
  livetickerComments: LivetickerComment[] = [];

  constructor(private refreshCommentsService: RefreshCommentsService, private service: NotificationsService, private controlService: LivetickerControlService, private dataService: LivetickerDataService, private activatedRoute: ActivatedRoute, private authService: AuthService, private postService: LivetickerPostService) { }

  ngOnInit() {
    this.activatedRoute.params.subscribe((param: Params) => {
      this.livetickerID = '-' + param['id'];
      this.controlService.livetickerIDChange(this.livetickerID, "comments");
      this.authService.getCurrentUserObservable().subscribe(result => {
        this.user = result;
        this.dataService.getLivetickerCommentUpdates(this.livetickerID).subscribe(result => {
          this.refreshComments();
        });
      });

      this.refreshCommentsService.refreshComments.subscribe(result => {
        this.refreshComments();
      });
    });
  }

  sendComment(comment: string) {
    if (comment && comment.length > 0) {
      this.addingComment = true;
      this.service.info(
        'In progress!',
        'Adding your comment...',
        {
          timeOut: 5000,
          showProgressBar: true,
          pauseOnHover: true,
          clickToClose: true,
          maxLength: 0
        }
      );
      this.postService.addComment(this.livetickerID, this.user.uid, comment).then(obs => {
        obs.subscribe(result => {
          if (result.val()) {
            if (result.val().state === "success") {
              this.service.success(
                'Done!',
                'Comment was added!',
                {
                  timeOut: 5000,
                  showProgressBar: true,
                  pauseOnHover: true,
                  clickToClose: true,
                  maxLength: 0
                }
              );
              this.refreshComments();
              this.commentValue = null;
              this.addingComment = false;
            } else if (result.val().errorDetails && result.val().errorDetails === "spam") {
              this.addingComment = false;
              this.service.error(
                'Error!',
                'You need to wait 30 seconds before posting another comment!',
                {
                  timeOut: 5000,
                  showProgressBar: true,
                  pauseOnHover: true,
                  clickToClose: true,
                  maxLength: 0
                }
              );
            } else {
              this.addingComment = false;
              this.service.error(
                'Error!',
                'Failed to add new comment!',
                {
                  timeOut: 5000,
                  showProgressBar: true,
                  pauseOnHover: true,
                  clickToClose: true,
                  maxLength: 0
                }
              );
            }
          }
        });
      });
    } else {
      this.addingComment = false;
      this.service.error(
        'Error!',
        'Comment was empty!',
        {
          timeOut: 5000,
          showProgressBar: true,
          pauseOnHover: true,
          clickToClose: true,
          maxLength: 0
        }
      );
    }
  }

  refreshComments() {
    if (this.status !== "loading") {
      this.status = "loading";
      this.livetickerComments = [];

      if (this.subscription && !this.subscription.closed) {
        this.subscription.unsubscribe;
      }

      this.subscription = this.dataService.getLivetickerComments(this.livetickerID, this.user.uid).then(obs => {
        this.subscription = obs.subscribe(result => {
          if (result.val()) {
            for (let key in result.val().comments) {
              let comment = result.val().comments[key];
              this.livetickerComments.push(new LivetickerComment(comment.content, comment.timestamp, comment.userName, comment.profilePicture));
            }

            this.livetickerComments.reverse();

            if (this.livetickerComments.length == 0) {
              this.status = "empty";
            } else {
              this.status = "finished";
            }
          }
        })
      });
    }
  }
}
