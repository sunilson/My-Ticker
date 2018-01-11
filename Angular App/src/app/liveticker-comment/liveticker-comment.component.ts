import { LivetickerDataService } from './../services/liveticker-data-service.service';
import { LivetickerComment } from './../models/livetickerComment';
import { Component, OnInit, Input } from '@angular/core';

@Component({
  selector: 'app-liveticker-comment',
  templateUrl: './liveticker-comment.component.html',
  styleUrls: ['./liveticker-comment.component.css']
})
export class LivetickerCommentComponent implements OnInit {

  @Input() livetickerComment: LivetickerComment;

  constructor(private dataService: LivetickerDataService) { }

  ngOnInit() {
  }

  getTime() {
    return this.dataService.formatTime(this.livetickerComment.timestamp);
  }

  getDate() {
    return this.dataService.formatDate(this.livetickerComment.timestamp);
  }
}
