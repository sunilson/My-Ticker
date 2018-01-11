var validator = require("validator");

var exports = module.exports = {};

exports.sanitize = function(data) {
  var newData = [];

  for (var key in data) {
    newData[key] = sanitizeString(data[key]);
  }
  return newData;
}

var sanitizeString = exports.sanitizeString = function(data) {
  logger.log('info', "santized: " + data);
  data = data.toString().trim();
  data = validator.escape(data);
  return data;
}

var sanitizeStringStrict = exports.sanitizeStringStrict = function(data) {
  logger.log('info', "santized: " + data);
  data = data.toString().trim();
  data = validator.escape(data);
  data = validator.whitelist(data, 'a-zA-Z0-9-.!?&\u00c4\u00e4\u00d6\u00f6\u00dc\u00fc\u00df');
  return data;
}

exports.validateUserID = function(id) {
  return true;
}

exports.checkValidDate = function(date) {
  logger.log('info', "Validated Date " + date);
  return true;
}

exports.sanitizeDate = function(date) {
  return date;
}

exports.checkValidString = function(string, canBeNull, minLength, maxLength) {
  logger.log('info', "Validated String " + string);

  if (string == null) {
    if (canBeNull) {
      return true;
    } else {
      return false;
    }
  } else {
    if (!validator.isLength(string, {
        min: minLength,
        max: maxLength
      })) {
      return false;
    }
  }

  return true;
}

exports.validateEmail = function(email) {
  logger.log('info', "Validated Email " + email);
  return validator.isEmail(email);
}
