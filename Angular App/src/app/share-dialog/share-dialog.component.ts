import { LivetickerComment } from './../models/livetickerComment';
import { LivetickerDataService } from './../services/liveticker-data-service.service';
import { Component, OnInit, OnDestroy } from '@angular/core';
import { DialogComponent, DialogService } from "ng2-bootstrap-modal";

export interface AlertModel {
  title: string;
  message: string;
}

@Component({
  selector: 'alert',
  templateUrl: './share-dialog.component.html',
  styleUrls: ['./share-dialog.component.css']
})

export class ShareDialogComponent extends DialogComponent<AlertModel, boolean> implements AlertModel, OnInit, OnDestroy {
  title: string;
  message: string;

  constructor(dialogService: DialogService, private dataService: LivetickerDataService) {
    super(dialogService);
  }

  ngOnInit() {

  }
}
