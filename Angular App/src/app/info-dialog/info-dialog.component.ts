import { Component, OnInit } from '@angular/core';
import { DialogComponent, DialogService } from 'ng2-bootstrap-modal';

export interface AlertModel {
  title: string;
  status: string;
  message: string;
}


@Component({
  selector: 'alert',
  templateUrl: './info-dialog.component.html',
  styleUrls: ['./info-dialog.component.css']
})
export class InfoDialogComponent extends DialogComponent<AlertModel, null> implements AlertModel {
  title: string;
  message: string;
  status: string;
  constructor(dialogService: DialogService) {
    super(dialogService);
  }
}
