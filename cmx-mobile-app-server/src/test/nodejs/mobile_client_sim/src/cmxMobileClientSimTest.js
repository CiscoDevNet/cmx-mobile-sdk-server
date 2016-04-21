var logger = require('./config/logging/logger');
var testConfig = require('./lib/testConfig');
var clientConfig = require('./lib/clientConfig');
var clientRegistration = require('./lib/clientRegistration');
var clientLocation = require('./lib/clientLocation');
var https = require('https');
var async = require('async');
var util = require('util');

var optimist = require('optimist')
	.usage('Usage: $0 -h -i')
	.describe('h', 'Display the usage message')
	.describe('host', 'CMX Cloud Server SDK host addresss')
	.describe('port', 'CMX Cloud Server SDK port')
	.describe('webApp', 'CMX Cloud Server SDK web application instance')
	.describe('numberClients', 'Number of different clients to simulate')
	.describe('numberSimultaneousClients', 'Number of clients which can simultaneously make request to the CMX Cloud Server SDK')
var argv = optimist.argv;
var clientConfigs = {};

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

function getClientLocations() {
	var startTime = new Date();
    async.eachLimit(clientConfigs, clientConfigs[0].numberSimultaneousClients, clientLocation.getClientLocation, function(err) {
		var processTime = (new Date() - startTime);
        getClientLocations();
		logger.info("!!!!!!!!Completed processing all client location requests: " + processTime + "ms");
    });
};

function runMain() {
	parseOptions(argv);
	
	clientConfigs = clientConfig.createClientConfigs(testConfig.options, clientConfig.options);

	logger.info("Starting test with options: " + util.inspect(clientConfigs[0]));
	//https.globalAgent.maxSockets = clientConfigs[0].numberSimultaneousClients + 10;

	logger.info("Starting to register all clients");
	async.eachLimit(clientConfigs, clientConfigs[0].numberSimultaneousClients, clientRegistration.getRegistrationParams, function(err) {
		logger.info("Completed registration of all clients");
		logger.info("Starting client location requests");
		getClientLocations();
	});
}

process.on('SIGINT', function() {
	logger.info("\nGracefully shutting down from SIGINT (Ctrl+C)");
	var responseTimeSum = 0;
	clientConfigs.forEach(function(config) {
		responseTimeSum += config.lastResponseTimeMilliSeconds;
	});
	var averageResponseTime = responseTimeSum / clientConfigs[0].numberClients;
	logger.info("Average response time for " + clientConfigs[0].numberClients + " clients: " + averageResponseTime + "ms");
	process.exit();
});

runMain();