"use strict"

var logger = require('../config/logging/logger')
  , fs = require('fs')
  , https = require('https')
  , util = require('util')

function createClientConfigs(testOptions) {
    var clientConfigs = [];
    var directoryName = testOptions.configDir + "/config/movements/";
    logger.info("Base configuration directory: " + testOptions.configDir);
    var directoryFiles = fs.readdirSync(directoryName);
    var devicesMovements = [];
    for (var i in directoryFiles) {
        if (directoryFiles[i].match(".json$")) {
            devicesMovements = JSON.parse(fs.readFileSync(directoryName + directoryFiles[i]));
            var clientConfig = {           
                clientLocationCounter: 0,
                macAddress: devicesMovements[0].deviceId,
                clientLocations: devicesMovements,
            }
            for (var key in testOptions) {
                clientConfig[key] = testOptions[key];
            }
            clientConfigs.push(clientConfig);
        }
    }
    return clientConfigs;
}

module.exports.createClientConfigs = createClientConfigs