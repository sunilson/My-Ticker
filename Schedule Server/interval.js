var logger = require(__dirname + '/logger.js');
logger.debugLevel = 'info';

var exports = module.exports = {};

exports.sanitizeSearchQueue = function(ref, delay) {

}

exports.sanitizeLivetickerFeedQueue = function(db, delay) {
  logger.log("warn", "Starting to sanitize Liveticker Feed Request Queue");
  sanitizeLivetickerFeedQueueNow(db);
  setInterval(() => {
    sanitizeLivetickerFeedQueueNow(db);
  }, delay);
}

exports.sanitizeLivetickerQueue = function(db, delay) {
  logger.log("warn", "Starting to sanitize Liveticker Queue");
  sanitizeLivetickerQueueNow(db);
  setInterval(() => {
    sanitizeLivetickerQueueNow(db);
  }, delay);
}

function sanitizeLivetickerFeedQueueNow(db) {
  logger.log("info", "Sanitizing Liveticker Feed Queue");
  db.ref("queue/requestLivetickerQueue/tasks").once('value').then(function(snapshot) {
    snapshot.forEach((childSnapshot) => {
      if (childSnapshot.val()._state === "error" || childSnapshot.val()._state === "success") {
        if (childSnapshot.val()._state_changed + 600000 < Date.now()) {
          db.ref("queue/requestLivetickerQueue/tasks").child(childSnapshot.key).remove();
          logger.log("info", "Deleted item with state " + childSnapshot.val()._state);
        }
      }
    });
  });
}

function sanitizeLivetickerQueueNow(db) {
  logger.log("info", "Sanitizing Liveticker Queue");
  db.ref("queue/livetickerQueue/tasks").once('value').then(function(snapshot) {
    snapshot.forEach((childSnapshot) => {
      if (childSnapshot.val()._state === "error" || childSnapshot.val()._state === "success" ||
        childSnapshot.val()._state === "livetickerAdded") {
        if (childSnapshot.val()._state_changed + 600000 < Date.now()) {
          db.ref("queue/livetickerQueue/tasks").child(childSnapshot.key).remove();
          logger.log("info", "Deleted item with state " + childSnapshot.val()._state);
        }
      }
    });
  });
}

exports.stopAll = function() {

}
