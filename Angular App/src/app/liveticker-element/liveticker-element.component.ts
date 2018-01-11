import { LivetickerDataService } from './../services/liveticker-data-service.service';
import { Liveticker } from './../liveticker';
import { LivetickerElement } from './../models/livetickerElement';
import { Component, OnInit, Input, OnChanges } from '@angular/core';

@Component({
  selector: 'app-liveticker-element',
  templateUrl: './liveticker-element.component.html',
  styleUrls: ['./liveticker-element.component.css']
})
export class LivetickerElementComponent implements OnInit, OnChanges {

  @Input() livetickerElement: LivetickerElement;

  constructor(private dataService: LivetickerDataService) {

  }

  getDate() {
    return this.dataService.formatDate(this.livetickerElement.timestamp);
  }

  getTime() {
    return this.dataService.formatTime(this.livetickerElement.timestamp);
  }

  ngOnInit() {
    if (this.livetickerElement.type === "state") {
      switch (this.livetickerElement.content) {
        case "started":
          this.livetickerElement.content = "Started Liveticker";
          break;
        case "created":
          this.livetickerElement.content = "Created Liveticker";
          break;
        case "finished":
          this.livetickerElement.content = "Finished Liveticker";
          break;
      }
    }
  }

  ngOnChanges() {
    //console.log(this.livetickerElement);
  }

}
