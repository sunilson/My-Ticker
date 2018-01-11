import { LivetickerListDataService } from './../services/liveticker-list-data-service.service';
import { Liveticker } from './../liveticker';
import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-feed',
  templateUrl: './feed.component.html',
  styleUrls: ['./feed.component.css']
})
export class FeedComponent implements OnInit {

  livetickers: Liveticker[] = [];

  constructor(private dataListService: LivetickerListDataService) { }

  ngOnInit() {
    this.dataListService.getLivetickerList().subscribe(result => {
      console.log(result);
      this.livetickers = [];
      for (let i = 0; i < result.length; i++) {
        //this.livetickers.push(new Liveticker(result[i].$key, result[i].authorID, result[i].title, result[i].status, result[i].state));
      }
    });
  }
}
