# My Ticker Cloud functions

These are the cloud functions used for the server side code of the My Ticker Android and Web app.

The apps can be found here: 
<a href="https://github.com/sunilson/My-Ticker-Android">https://github.com/sunilson/My-Ticker-Android</a>
<a href="https://github.com/sunilson/My-Ticker-Angular-App">https://github.com/sunilson/My-Ticker-Angular-App</a>

## Firebase

The cloud functions are created with the Firebase platform, which itself uses Google Cloud Functions. Those functions are stand-alone functions that work in a Node.js enviroment directly on Firebase and can be triggered via HTTPS or realtime database events.

## Usage

The functions are used to sanitize/validate user input, send out notifications, clean the queue system and handling viewer/comment/like counts.

More infos about the queue system can be found in the readme of the Android app: <a href="https://github.com/sunilson/My-Ticker-Android">https://github.com/sunilson/My-Ticker-Android</a>
