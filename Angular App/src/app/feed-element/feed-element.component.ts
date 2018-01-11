import { Liveticker } from './../liveticker';
import { Component, OnInit, Input } from '@angular/core';

@Component({
  selector: 'app-feed-element',
  templateUrl: './feed-element.component.html',
  styleUrls: ['./feed-element.component.css']
})
export class FeedElementComponent implements OnInit {

  @Input() liveticker: Liveticker;

  constructor() { }

  ngOnInit() {
  }

  clicked() {
    alert(this.liveticker.title);
  }
}
