"use strict"

var logger = require('../config/logging/logger')
  , fs = require('fs')
  , https = require('https')
  , util = require('util')

function clone(obj) {
    // Handle the 3 simple types, and null or undefined
    if (null == obj || "object" != typeof obj) return obj;

    // Handle Date
    if (obj instanceof Date) {
        var copy = new Date();
        copy.setTime(obj.getTime());
        return copy;
    }

    // Handle Array
    if (obj instanceof Array) {
        var copy = [];
        for (var i = 0, len = obj.length; i < len; i++) {
            copy[i] = clone(obj[i]);
        }
        return copy;
    }

    // Handle Object
    if (obj instanceof Object) {
        var copy = {};
        for (var attr in obj) {
            if (obj.hasOwnProperty(attr)) copy[attr] = clone(obj[attr]);
        }
        return copy;
    }

    throw new Error("Unable to copy obj! Its type isn't supported.");
}

function createClientConfigs(testOptions) {
    var clientConfigs = [];
    var directoryName = testOptions.configDir + "/config/movements/";
    logger.info("Base configuration directory: " + testOptions.configDir);
    var directoryFiles = fs.readdirSync(directoryName);
    var macAddressFileName = testOptions.macAddressPrefix.replace(/:/g, '');
    logger.info("MAC Address file name: " + macAddressFileName);
    var origDevicesMovements = [];
    for (var i in directoryFiles) {
        if (directoryFiles[i].match(macAddressFileName)) {
            origDevicesMovements = JSON.parse(fs.readFileSync(directoryName + directoryFiles[i]));
        }
    }
    logger.debug("Device movements: " + util.inspect(devicesMovements));
    for (var i = 0; i < testOptions.numberClients; i++) {
        var clientMacAddress = "";
        var devicesMovements = clone(origDevicesMovements);
        var hexStr = (i + 1).toString(16);
        switch (hexStr.length) {
            case 1:
                clientMacAddress = testOptions.macAddressPrefix + ":00:00:0" + hexStr;
                break;
            case 2:
                clientMacAddress = testOptions.macAddressPrefix + ":00:00:" + hexStr;
                break;
            case 3:
                clientMacAddress = testOptions.macAddressPrefix + ":00:0" + hexStr.substring(2,3) + ":" + hexStr.substring(0,2);
                break;
            case 4:
                clientMacAddress = testOptions.macAddressPrefix + ":00:" + hexStr.substring(2,4) + ":" + hexStr.substring(0,2);
                break;
            case 5:
                clientMacAddress = testOptions.macAddressPrefix + ":0" + hexStr.substring(4,5) + ":" + hexStr.substring(2,4) + ":" + hexStr.substring(0,2);
                break;
            case 6:
                clientMacAddress = testOptions.macAddressPrefix + ":" + hexStr.substring(4,6) + ":" + hexStr.substring(2,4) + ":" + hexStr.substring(0,2);
                break;
        }
        for (var j = 0; j < devicesMovements.length; j++) {
            logger.debug("Client movement: " + util.inspect(devicesMovements[j]));
            devicesMovements[j].MovementEvent.deviceId = clientMacAddress;
        }
        var clientConfig = {           
            clientLocationCounter: 0,
            macAddress: clientMacAddress,
            clientLocations: devicesMovements,
        }
        for (var key in testOptions) {
            clientConfig[key] = testOptions[key];
        }
        clientConfigs.push(clientConfig);
    }
    return clientConfigs;
}

module.exports.createClientConfigs = createClientConfigs