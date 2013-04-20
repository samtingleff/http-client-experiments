
var util = require('util'),
    http = require('http');

var createServer = function(port) {
  http.createServer(function(req, res) {
    // sleep for a random time between 40 and 150 ms
    var sleepTime = (Math.random() * (150 - 40)) + 40;

    var response = {
      status : 10,
      hello : "world"
    };
    var responseBody = JSON.stringify(response);
    setTimeout(function() {
      res.writeHead(200, {'Content-Type':'application/json', 'Content-Length':responseBody.length});
      res.write(responseBody);
      res.end();
    }, sleepTime);
  }).listen(port);
};

createServer(8124);
createServer(8125);
createServer(8126);
createServer(8127);
createServer(8128);
createServer(8129);
createServer(8130);
createServer(8131);
createServer(8132);
createServer(8133);
