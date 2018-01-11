import { AuthService } from './services/auth-service.service';
import { Component, OnInit } from '@angular/core';
import { AngularFireDatabase, FirebaseListObservable, FirebaseObjectObservable } from 'angularfire2/database';
import { Observable } from 'rxjs/Observable';
import * as firebase from 'firebase/app';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit {

  constructor(public authService: AuthService) {

  }

  ngOnInit() {
    this.authService.getCurrentUserObservable().subscribe(user => {
      if (!user) {
        this.authService.loginAnonymous().then(value => {
          console.log("logged in anonymously");
        });
      }
    });
  }

  clicked() {
    this.authService.logOut();
  }
}
