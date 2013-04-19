
var util = require('util'),
    http = require('http');

http.createServer(function (req, res) {
  // modify sleepTime to increase the fun
  var sleepTime = 60;

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
}).listen(8124);
