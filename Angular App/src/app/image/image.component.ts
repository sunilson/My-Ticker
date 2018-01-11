import { LivetickerControlService } from './../services/liveticker-control.service';
import { ActivatedRoute, Params } from '@angular/router';
import { Component, OnInit, HostListener, ViewChild, ElementRef } from '@angular/core';

@Component({
  selector: 'app-image',
  templateUrl: './image.component.html',
  styleUrls: ['./image.component.css']
})
export class ImageComponent implements OnInit {

  @ViewChild('image') imageView: ElementRef;
  livetickerID: string;
  width;
  height;
  resizingHeight: boolean;

  constructor(private activatedRoute: ActivatedRoute, private livetickerControlService: LivetickerControlService) { }

  ngOnInit() {
    this.activatedRoute.params.subscribe((param: Params) => {
      this.livetickerID = '-' + param['id'];
      this.livetickerControlService.livetickerIDChange(this.livetickerID, "image");
    });
  }


  @HostListener('window:resize', ['$event'])
  onResize(event) {
    let imageWidth = this.imageView.nativeElement.width;
    let imageHeight = this.imageView.nativeElement.height;

    if (event.target.innerWidth < imageWidth) {
      this.width = event.target.innerWidth + "px";
      this.height = "auto";
    } else {
      this.height = event.target.innerHeight - 70 + "px";
      this.width = "auto";
    }
  }
}
