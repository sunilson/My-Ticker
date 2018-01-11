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
  data = data.toString().trim();
  data = validator.escape(data);
  return data;
}

var sanitizeStringStrict = exports.sanitizeStringStrict = function(data) {
  data = data.toString().trim();
  data = validator.escape(data);
  data = validator.whitelist(data, 'a-zA-Z0-9-_ !?&\u00c4\u00e4\u00d6\u00f6\u00dc\u00fc\u00df');
  return data;
}

exports.validateImageURL = function(data) {
  if (data == null) {
    return false;
  } else {
    return validator.isURL(data.toString(), {
      protocols: ["https"],
      require_tld: true,
      require_protocol: true,
      require_host: true,
      require_valid_protocol: true,
      allow_underscores: false,
      host_whitelist: false,
      host_blacklist: false,
      allow_trailing_dot: false,
      allow_protocl_relative_urls: false
    });
  }
}

exports.checkValidDate = function(date) {
  return validator.isNumeric(date.toString());;
}

exports.sanitizeNumber = function(data) {
  data = data.toString().trim();
  data = validator.escape(data);
  data = validator.whitelist(data, '0-9');
  return parseInt(data);
}

exports.checkValidNumber = function(number) {
  return validator.isNumeric(number.toString());
}

exports.toObject = function(arr) {
  return arr.reduce(function(acc, cur, i) {
    acc[i] = cur;
    return acc;
  }, {});
}

exports.checkValidString = function(string, canBeNull, minLength, maxLength) {
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
  return validator.isEmail(email);
}
