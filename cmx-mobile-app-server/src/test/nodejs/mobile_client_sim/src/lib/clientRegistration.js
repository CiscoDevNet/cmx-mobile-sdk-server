"use strict"

var logger = require('../config/logging/logger')
  , https = require('https')
  , util = require('util')
  , querystring = require('querystring');

function getRegistrationParams(registrationConfig, registrationCallback) {
	logger.debug("Starting to get registration parameters: " + util.inspect(registrationConfig));
	
	var body = querystring.stringify({
        'clientType' : 'android',
        'clientMACAddress' : registrationConfig.macAddress,
        'pushNotificationRegistrationId' : 'none'
    });
    
	var options = {
		host : registrationConfig.host,
		path : '/' + registrationConfig.webApp + '/api/cmxmobile/v1/clients/register',
		port : registrationConfig.port,
		method : 'POST',
		rejectUnauthorized: false,
        //agent: false,
		headers : {
			"Content-Length" : body.length,
			"Content-Type" : "application/x-www-form-urlencoded"
		}
	};
	
	logger.debug("Requesting device registration parameters using the following data: " + body);
	var req = https.request(options, function(response) {
        var str = '';
        logger.debug("headers: " + util.inspect(response.headers));
        var locationLink = response.headers['location'];
        var clientDeviceId = locationLink.substring(locationLink.lastIndexOf("/")+1);
        var cookies = response.headers['set-cookie'][0].split(';');
        var clientDevicePassword = "";
        for (var i=0; i < cookies.length; i++) {
            if(cookies[i].indexOf("cmxMobileApplicationCookie") > -1) {
                clientDevicePassword = cookies[i].split('=')[1];
                break;
            }
        }
        response.on('data', function(chunk) {
            str += chunk;
        });

        response.on('end', function() {
            logger.info("Received response for device registration parameters for device: " + registrationConfig.macAddress + ":" + clientDeviceId + ":" + clientDevicePassword);
            registrationConfig.deviceId = clientDeviceId;
            registrationConfig.devicePassword = clientDevicePassword;
            registrationCallback();
        });
    });
	req.write(body);
	req.end();
	logger.info("Completed sending request for device registration parameters: " + registrationConfig.macAddress);
}

module.exports.getRegistrationParams = getRegistrationParams;