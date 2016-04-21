"use strict"

var logger = require('../config/logging/logger')
  , fs = require('fs')
  , step = require('step')
  , https = require('https')
  , util = require('util')
  , httpsPostImage = require('./httpsPostImage')

var MSE_UDI_ID = "AIR-MSE-3355-K9:V01:KQYGBRD";

/**************************************************************************
Setup callback invoked after the json setup data is uploaded. The images
are then loaded to the server
- `response` - response from server
**************************************************************************/
var setupCallback = function(response) {
	var str = '';
	response.on('data', function(chunk) {
		str += chunk;
	});

	response.on('end', function() {
		logger.info("Received response for sending CMX Cloud Server venue data: " + str);
		var baseMapFileName = globalConfigDir + "/config/maps/";
		var mapFileName = '';
		var auth2 = "Basic " + new Buffer(globalUserId + ":" + globalPassword).toString("base64");
		var options = {
			host : globalHost,
			path : globalWebApp + '/api/v1/setup/uploadFloorImage',
			port : globalPort,
			method : 'POST',
			auth: globalUserId + ':' + globalPassword,
			rejectUnauthorized: false,
			encoding : 'utf8'
		};
		logger.info("Attempting to send images to the CMX Cloud Server");
		for (var j in allVenueInfo) {
			for (var i in allVenueInfo[j].floorList) {
				mapFileName = baseMapFileName + allVenueInfo[j].floorList[i].venueUdId + "/" + allVenueInfo[j].floorList[i].filename;
				if (!fs.existsSync(mapFileName)) {
					logger.info("Venue file map does not exist and will be skipped: " + mapFileName);
				}
				var fields = {
					mseVenueUdId : allVenueInfo[j].floorList[i].venueUdId,
					mseFloorId : allVenueInfo[j].floorList[i].mseFloorId,
					filename : allVenueInfo[j].floorList[i].filename
				};
				httpsPostImage.postImage(options, mapFileName, {'Cookie': 'cookiename=cookievalue'}, fields);
			}
		}
		var basePoiFileName = globalConfigDir + "/config/pois/";
		var poiFileName = '';
		options = {
			host : globalHost,
			path : globalWebApp + '/api/v1/setup/uploadPoiImage',
			port : globalPort,
			method : 'POST',
			auth : globalUserId + ':' + globalPassword,
			rejectUnauthorized: false,
			encoding : 'utf8'
		};
		for (var j in allVenueInfo) {
			for (var i in allVenueInfo[j].floorList) {
				for (var n in allVenueInfo[j].floorList[i].poiList) {
					if (allVenueInfo[j].floorList[i].poiList[n].imageId !== 'none') {
						if (allVenueInfo[j].floorList[i].poiList[n].imageId === undefined) {
							logger.debug("POI image id is not defined and will be skipped: " + poiFileName);
							continue;
						}
						poiFileName = basePoiFileName + allVenueInfo[j].floorList[i].poiList[n].venueUdId + "/" + allVenueInfo[j].floorList[i].poiList[n].imageId + "." + allVenueInfo[j].floorList[i].poiList[n].imageType;
						if (!fs.existsSync(poiFileName)) {
							logger.info("POI image does not exist and will be skipped: " + poiFileName);
							continue;
						}
						var fields = {
								mseVenueUdId : allVenueInfo[j].floorList[i].poiList[n].venueUdId,
								mseFloorId : allVenueInfo[j].floorList[i].poiList[n].mseFloorId,
								poiId : allVenueInfo[j].floorList[i].poiList[n].id
						};
						httpsPostImage.postImage(options, poiFileName, {'Cookie': 'cookiename=cookievalue'}, fields);
					}
				}
			}
		}
	});
};

/**************************************************************************
Send the setup data to the server
- `options` -options for sending data
  - `host` - Host to send data to
  - `port` - Port to use when posting data
  - `webApp` - Web application name to use
  - `configDir` - Configuraiton directory
**************************************************************************/
function sendVenueData(options) {
	options = options || {};
	globalHost = options.host || 'localhost';
	globalPort = options.port || '8082';
	globalWebApp = options.webApp || 'cmx-cloud-server';
	if (globalWebApp.length > 0) {
		globalWebApp = "/" + globalWebApp;
	}
	globalConfigDir = options.configDir || '.';
	globalUserId = options.userId;
	globalPassword = options.password;

	logger.info("Sending CMX Cloud Server venue data: " + util.inspect(options));
	var venueDirectoryName = globalConfigDir + "/config/venues/";
	logger.debug("Base configuration directory: " + globalConfigDir);
	var venueDirectoryFiles = fs.readdirSync(venueDirectoryName);
	var venueInfoCount = 0;
	for (var i in venueDirectoryFiles) {
		if (venueDirectoryFiles[i].match(".json$")) {
			var singleVenueInfo = JSON.parse(fs.readFileSync(venueDirectoryName + venueDirectoryFiles[i]));
			logger.info("Loading venue data for: " + singleVenueInfo.name);
			var mapDirectoryName = globalConfigDir + "/config/maps/" + singleVenueInfo.venueId + "/";
			var mapDirectoryFiles = fs.readdirSync(mapDirectoryName);
			var floorsInfo = [];
			var floorsInfoCount = 0;
			for (var k in mapDirectoryFiles) {
				if (mapDirectoryFiles[k].match(".json$")) {
					var mapFloorInfo = JSON.parse(fs.readFileSync(mapDirectoryName + mapDirectoryFiles[k]));
					logger.info("Loading floor information for: " + mapFloorInfo.name);
					var poiFileName = globalConfigDir + "/config/pois/" + singleVenueInfo.venueId + "/" + mapFloorInfo.floorId + ".json";
					var poisInfo = [];
					if (fs.existsSync(poiFileName)) {
						var poiFloorInfo = JSON.parse(fs.readFileSync(poiFileName));
						for (var n = 0; n < poiFloorInfo.length; ++n) {
							logger.info("Loading point of interest information for: " + poiFloorInfo[n].name);
							var poiInfo = {
								venueUdId : singleVenueInfo.venueId,
								mseFloorId : mapFloorInfo.floorId,
								mseUdId : MSE_UDI_ID,
								mseVenueId : venueInfoCount,
								id : poiFloorInfo[n].id,
								name : poiFloorInfo[n].name,
								imageType : poiFloorInfo[n].imageType,
								imageId : poiFloorInfo[n].imageId,
								x : poiFloorInfo[n].points[0].x,
								y : poiFloorInfo[n].points[0].y
							}
							poisInfo[n] = poiInfo;
						}
					}
					var floorInfo = {
						venueUdId : singleVenueInfo.venueId,
						mseFloorId : mapFloorInfo.floorId,
						mseUdId : MSE_UDI_ID,
						mseVenueId: venueInfoCount,
						name : mapFloorInfo.name,
						description : mapFloorInfo.mapHierarchyString,
						filename : mapFloorInfo.floorId+".gif",
						poiList : poisInfo,
						length : mapFloorInfo.dimension.length,
						width : mapFloorInfo.dimension.width
					}
					floorsInfo[floorsInfoCount] = floorInfo;
					++floorsInfoCount;
				}
			}
			var venueInfo = {
				lat : "0",
				lon : "0",
				name : singleVenueInfo.name,
				description : "Test Description",
				address : singleVenueInfo.streetAddress,
				mseUdId : MSE_UDI_ID,
				mseVenueId: venueInfoCount,
				venueUdId : singleVenueInfo.venueId,
				locationUpdateInterval : singleVenueInfo.locationUpdateInterval,
				wifiConnectionMode : singleVenueInfo.wifiConnectionMode,
				floorList : floorsInfo
			}
			allVenueInfo[venueInfoCount] = venueInfo;
			++venueInfoCount;
		}
	}
	
	var body = JSON.stringify(allVenueInfo);
	var options = {
		host : globalHost,
		path : globalWebApp + '/api/v1/setup',
		port : globalPort,
		method : 'POST',
		rejectUnauthorized: false,
		auth: globalUserId + ':' + globalPassword,
		headers : {
			"Content-Length" : body.length,
			"Content-Type" : "application/json"
		}
	};
	
	logger.debug("Posting the following data to the server: " + body);
	var req = https.request(options, setupCallback);
	req.write(body);
	req.end();
	logger.info("Completed sending CMX Cloud Server venue data");
}

var allVenueInfo = [];
var globalHost;
var globalPort;
var globalWebApp;
var globalConfigDir;
var globalUserId;
var globalPassword;

module.exports.sendVenueData = sendVenueData;