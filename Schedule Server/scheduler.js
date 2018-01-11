var schedule = require('node-schedule');

var logger = require(__dirname + '/logger.js');

//Used to store Jobs, so they can be canceled
var livetickerStartSchedules = new Map();

var exports = module.exports = {};

exports.startLivetickerAtDate = function(utcDate, path, livetickerID, db) {
  var date = new Date(+utcDate);
  logger.log('info', "Scheduling start of Liveticker " + livetickerID + " at " + date);
  var job = schedule.scheduleJob(date, function() {
    logger.log('info', 'Starting Liveticker ' + path);
    db.ref(path).child("status").set("started");
  });

  livetickerStartSchedules.set(livetickerID, job);
}
