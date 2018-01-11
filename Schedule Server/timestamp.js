var timestamp = module.exports = {};

timestamp.generate = function() {
  return (new Date(Date.now())).toUTCString();
}
