import { ImageComponent } from './image/image.component';
import { CommentsComponent } from './comments/comments.component';
import { FeedComponent } from './feed/feed.component';
import { HomeComponent } from './home/home.component';
import { LivetickerComponent } from './liveticker/liveticker.component';
import { Routes, RouterModule } from "@angular/router";

const APP_ROUTES: Routes = [
    { path: '', component: HomeComponent },
    { path: 'liveticker/:id', component: LivetickerComponent },
    { path: 'liveticker/:id/image/:imageid', component: ImageComponent },
    { path: 'liveticker/:id/comments', component: CommentsComponent },
    { path: 'feed', component: FeedComponent }
];

export const routing = RouterModule.forRoot(APP_ROUTES);