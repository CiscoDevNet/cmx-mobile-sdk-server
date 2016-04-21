"use strict"

var logger = require('../config/logging/logger')
  , https = require('https')
  , util = require('util')
  , querystring = require('querystring');

function getClientLocation(clientConfig, clientLocationCallback) {
	logger.debug("Get client location configuration: " + util.inspect(clientConfig));
	logger.info("Get client location for: " + clientConfig.macAddress);
	
	var options = {
		host : clientConfig.host,
		path : '/' + clientConfig.webApp + '/api/cmxmobile/v1/clients/location/' + clientConfig.deviceId,
		port : clientConfig.port,
		method : 'GET',
		rejectUnauthorized: false,
        //agent: false,
		headers : {
			"Cookie" : clientConfig.devicePassword
		}
	};
	
    var startTime = new Date();
    var req = https.request(options, function(response) {
        var str = '';
        response.on('data', function(chunk) {
            str += chunk;
        });

        response.on('end', function() {
			clientConfig.lastResponseTimeMilliSeconds = (new Date() - startTime);
            logger.info("Response time for client location [" + clientConfig.macAddress + "]: " + clientConfig.lastResponseTimeMilliSeconds + "ms");
            logger.debug("Received response for client location [" + clientConfig.macAddress + "]:" + str);
            clientLocationCallback();
        });
    });
    req.end();
    logger.info("Completed sending request for client location: " + clientConfig.macAddress);
}

module.exports.getClientLocation = getClientLocation;