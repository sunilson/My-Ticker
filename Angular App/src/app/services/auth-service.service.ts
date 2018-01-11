import { Injectable } from '@angular/core';
import { AngularFireAuth } from 'angularfire2/auth';
import { Observable } from 'rxjs/Observable';
import * as firebase from 'firebase/app';

@Injectable()
export class AuthService {

  userObservable: Observable<firebase.User>;
  user: firebase.User;

  constructor(public auth: AngularFireAuth) {
    this.userObservable = auth.authState;
    this.userObservable.subscribe(result => {
      this.user = result;
    });
  }

  loginAnonymous() {
    return this.auth.auth.signInAnonymously();
  }

  getCurrentUserObservable() {
    return this.userObservable;
  }

  getCurrentUser() {
    return this.user;
  }

  logOut() {
    return this.auth.auth.signOut();
  }
}
