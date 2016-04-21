"use strict"

var logger = require('../config/logging/logger')
, fs = require('fs')
, https = require('https')
, util = require('util')

function sendClientLocation(clientConfig, clientLocationCallback) {
    logger.debug("Sending client location configuration: " + util.inspect(clientConfig));
    logger.info("Sending client location for: " + clientConfig.macAddress);
    var body = JSON.stringify(clientConfig.clientLocations[clientConfig.clientLocationCounter]);
    ++clientConfig.clientLocationCounter;
    if (clientConfig.clientLocationCounter >= clientConfig.clientLocations.length) {
        clientConfig.clientLocationCounter = 0;
    }
    logger.info("Posting the following data to the server: " + body);
    var options = {
            host : clientConfig.host,
            path : '/' + clientConfig.webApp + '/api/cmxmobile/v1/notify',
            port : clientConfig.port,
            method : 'POST',
            rejectUnauthorized: false,
            headers : {
                "Content-Length" : body.length,
                "Content-Type" : "application/json"
            }
    };
    var req = https.request(options, function(response) {
        var str = '';

        response.on('data', function(chunk) {
            str += chunk;
        });

        response.on('end', function() {
            logger.info("Response from sending client movement data: " + response.statusCode + ": " + str);
            clientLocationCallback();
        });
    });

    req.write(body);
    req.end();
}

module.exports.sendClientLocation = sendClientLocation;