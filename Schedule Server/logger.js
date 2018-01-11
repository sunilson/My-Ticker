var timestamp = require(__dirname + "/timestamp.js");

var logger = module.exports = {};
logger.log = function(level, message) {
  var color;
  if (typeof message !== 'string') {
    message = JSON.stringify(message);
  };
  switch (level) {
    case 'error':
      color = "\x1b[31m";
      break;
    case 'info':
      color = "\x1b[37m";
      break;
    case 'warn':
      color = "\x1b[33m";
      break;
    default:
      color = "\x1b[37m";
  }

  console.log(color, timestamp.generate() + " - " + level + ' : ' + message);
}
