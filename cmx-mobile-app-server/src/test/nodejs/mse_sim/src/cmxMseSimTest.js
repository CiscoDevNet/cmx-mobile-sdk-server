var logger = require('./config/logging/logger');
var cmxCloudConnector = require('./lib/cmxCloudConnector');
var mseClientEvent = require('./lib/mseClientEvent');
var testConfig = require('./config/testConfig');
var clientConfig = require('./lib/clientConfig');
var async = require('async');
var util = require('util');
var optimist = require('optimist')
    .usage('Usage: $0 -h -c -i -s')
    .describe('h', 'Display the usage message')
    .describe('c', 'Client location movement events only')
    .describe('s', 'Setup data only')
    .describe('userId', 'CMX Cloud Server SDK user id')
    .describe('password', 'CMX Cloud Server SDK password')
    .describe('host', 'CMX Cloud Server SDK host addresss')
    .describe('port', 'CMX Cloud Server SDK port')
    .describe('webApp', 'CMX Cloud Server SDK web application instance')
    .describe('numberClients', 'Number of different clients to simulate')
    .describe('macAddressPrefix', 'MAC Address prefrex to use for clients. Default is 00:00:00')
    .describe('numberSimultaneousClients', 'Number of clients which can simultaneously make request to the CMX Cloud Server SDK')
    .describe('sleepIntervalMilliseconds', 'Sleep interval in milliseconds after sending client events');
var argv = optimist.argv;

if (argv.h) {
    optimist.showHelp();
    process.exit(0);
}

function parseOptions(argv) {
    for (var optionName in testConfig.options) {
        if (argv[optionName]) {
            testConfig.options[optionName] = argv[optionName];
        }
    }
}

function sendClientLocations() {
    var startTime = new Date();
    async.eachLimit(clientConfigs, clientConfigs[0].numberSimultaneousClients, mseClientEvent.sendClientLocation, function(err) {
        var processTime = (new Date() - startTime);
        logger.info("!!!!!!!!Completed processing all client location requests: " + processTime + "ms");
        setTimeout(function() {
            sendClientLocations();
        }, clientConfigs[0].sleepIntervalMilliseconds);
    });
    
}

function runMain() {
    parseOptions(argv);

    clientConfigs = clientConfig.createClientConfigs(testConfig.options, clientConfig.options);
 
    logger.debug("Starting test with options: " + util.inspect(clientConfigs[0]));

    if (!argv.s && !argv.c) {
        argv.s = "true";
        argv.c = "true";
    }

    if (argv.i) {
        options.sendInterval = argv.i;
    }

    if (argv.s) {
        cmxCloudConnector.sendVenueData(testConfig.options);
    }

    if (argv.c) {
        sendClientLocations();
    }
}

runMain();