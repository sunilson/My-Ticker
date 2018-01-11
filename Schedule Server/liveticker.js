/** Utilities Initialization **/

var utilities = require(__dirname + '/utilities.js');

var logger = require(__dirname + '/logger.js');

function Liveticker(data) {
  this.data = data;
}

Liveticker.prototype.getData = function() {
  return this.data;
}

Liveticker.prototype.sanitize = function() {
  //Sanitize Strings
  this.data.title = utilities.sanitizeStringStrict(this.data.title);
  this.data.description = utilities.sanitizeString(this.data.description);
  this.data.authorID = utilities.sanitizeStringStrict(this.data.authorID);
  this.data.privacy = utilities.sanitizeStringStrict(this.data.privacy);
  this.data.startDate = utilities.sanitizeStringStrict(this.data.startDate);
  this.data.status = utilities.sanitizeString(this.data.status);
}

Liveticker.prototype.validate = function() {
  //Check if required data is correct
  if (!utilities.checkValidString(this.data.title, false, 5, 30)) {
    logger.log('error', "Rejected new Liveticker. Reason: Title Invalid");
    return "Title invalid!";
  } else if (!utilities.checkValidString(this.data.description, false, 5, 30)) {
    logger.log('error', "Rejected new Liveticker. Reason: Description Invalid");
    return "Description invalid!";
  } else if (this.data.privacy !== "public" && this.data.privacy !== "private") {
    logger.log('error',
      "Rejected new Liveticker. Reason: Privacy not set");
    return "Privacy must be set!";
  } else if (!utilities.validateUserID(this.data.authorID)) {
    logger.log('error', "Rejected new Liveticker. Reason: ID Invalid");
    return "ID invalid!";
  } else {
    return true;
  }
}

Liveticker.prototype.applyStartDate = function() {
  if (!utilities.checkValidDate(this.data.startDate)) {
    logger.log('warn', 'Start Date was not valid!');
    this.data.startDate = Date.now();
    this.data.state = "started";
  } else if (this.data.startDate < Date.now()) {
    logger.log('warn', 'Start Date was too small!');
    this.data.startDate = Date.now();
    this.data.state = "started";
  } else {
    if (this.data.startDate > Date.now() + 432000000) {
      logger.log('warn', 'Start Date was too high!');
      this.data.startDate = Date.now() + 432000000;
    }
    this.data.state = "not started";
  }
}

module.exports = Liveticker;
