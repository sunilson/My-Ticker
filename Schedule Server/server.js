/** Firebase Initialization **/

var admin = require("firebase-admin");
var schedule = require('node-schedule');
var serviceAccount = require(__dirname + '/private/service-credentials.json');
const Queue = require('firebase-queue');

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount),
  databaseURL: "https://pro4-84e1f.firebaseio.com"
});

var db = admin.database();

var livetickerRef = db.ref("/liveticker");

var spec2 = {
  "scheduleSpec": {
    "start_state": null,
    "in_progress_state": "in_progress",
    "finished_state": "success",
    "error_state": "error",
    "timeout": 300000, // 5 minutes
    "retries": 0 // don't retry
  }
}

var scheduleOptions = {
  'specId': 'scheduleSpec'
};

var scheduleQueue = new Queue(db.ref('queue').child("scheduleLivetickerStart"), scheduleOptions, function(data,
  progress, resolve, reject) {
  var utcDate = data.date;
  var date = new Date(+utcDate);
  var job = schedule.scheduleJob(date, function(livetickerID, authorID) {
    console.log('info', 'Starting Liveticker ' + livetickerID);
    db.ref("liveticker/" + livetickerID).update({
      "state": "started",
      "stateTimestamp": Date.now(),
      "idState": authorID + "ab"
    });
    db.ref("contents/" + livetickerID).push({
      "authorID": authorID,
      "type": "state",
      "content": "started",
      "timestamp": Date.now(),
      "livetickerID": livetickerID
    });
  }.bind(null, data.livetickerID, data.authorID));
  resolve();
});

var toObject = function(arr) {
  return arr.reduce(function(acc, cur, i) {
    acc[i] = cur;
    return acc;
  }, {});
}
