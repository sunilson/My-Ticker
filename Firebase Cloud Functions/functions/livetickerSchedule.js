var schedule = require('node-schedule');

//Used to store Jobs, so they can be canceled
var livetickerStartSchedules = new Map();

var exports = module.exports = {};

exports.startLivetickerAtDate = function(utcDate, path, livetickerID, authorID, db) {
  var date = new Date(+utcDate);
  console.log('info', "Scheduling start of Liveticker " + livetickerID + " at " + date);
  var job = schedule.scheduleJob(date, function() {
    console.log('info', 'Starting Liveticker ' + path);
    db.ref(path).update({
      "state": "started",
      "stateTimestamp": Date.now(),
      "idState": authorID + "ab"
    });
    db.ref("contents/" + livetickerID).push({
      authorID: authorID,
      type: "state",
      content: "started",
      timestamp: Date.now(),
      livetickerID: livetickerID
    });
  });

  livetickerStartSchedules.set(livetickerID, job);
}
