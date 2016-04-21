var express = require('express');
var https = require('https');
var logger = require('./config/logging/logger');
var serverConfig = require('./config/default/defaultConfig');
var util = require('util');
var fs = require('fs');
var optimist = require('optimist')
.usage('Usage: $0 -h')
.describe('h', 'Display the usage message')
.describe('host', 'CMX Mobile App Server SDK host addresss')
.describe('port', 'CMX Mobile App Server Mobile port')
.describe('authorizationKey', 'MSE Notification authorization key')
.describe('sdkPort', 'CMX Mobile App Server SDK port')
.describe('sdkUserName', 'CMX Mobile App Server SDK user name')
.describe('sdkPassword', 'CMX Mobile App Server SDK password');
var argv = optimist.argv;
var currentChildCount = 0;
var xmlAppDoc;
var pkg;

if (fs.existsSync('./node_modules/cisco-cmx-app-server-sdk-interface/package.json')) {
    pkg = require('./node_modules/cisco-cmx-app-server-sdk-interface/package.json');
} else {
    pkg = require('../package.json');
}

var options = {
        key: fs.readFileSync('server-key.pem'),
        cert: fs.readFileSync('server-cert.pem')
};
var mobileAppServer = express();

mobileAppServer.use(express.bodyParser());
mobileAppServer.use(express.cookieParser());

if (argv.h) {
    optimist.showHelp();
    process.exit(0);
}

function parseOptions(argv) {
    for (var optionName in serverConfig.options) {
        if (argv[optionName]) {
            serverConfig.options[optionName] = argv[optionName];
        }
    }
}

mobileAppServer.post('/location/update/:deviceId', function(req, res) {
    logger.info("Post location update request from: " + req.ip + " params: " + util.inspect(req.params));
    logger.info("Post location update request from: " + req.ip + " body: " + util.inspect(req.body));
    var mseUdiString = req.body.venueId.substr(0, req.body.venueId.lastIndexOf(':'));
    var movementEvent = { MovementEvent : {
            moveDistanceInFt : "10.00",
            subscriptionName : "CMX_Location_Event",
            timeStamp : "2013-06-10T19:16:26.065-0700",
            entity : "WIRELESS_CLIENTS",
            clientAuthInfo : serverConfig.options.authorizationKey,
            deviceId : req.params.deviceId,
            mseUdi : mseUdiString,
            floorRefId : req.body.floorId,
            locationCoordinate : { x : req.body.mapCoordinate.x, y : req.body.mapCoordinate.y, unit: "FEET" }
        }
    };

    var body = JSON.stringify(movementEvent);
    var options = {
            host : serverConfig.options.host,
            path : '/cmx-cloud-server/api/cmxmobile/v1/notify',
            port : serverConfig.options.port,
            method : 'POST',
            rejectUnauthorized: false,
            headers : {
                "Content-Length" : body.length,
                "Content-Type" : "application/json"
            }
    };
    var serverReq = https.request(options, function(response) {
        var str = '';

        response.on('data', function(chunk) {
            str += chunk;
        });

        response.on('end', function() {
            logger.info("Response from sending client movement data: " + response.statusCode + ": " + str);
        });
    });
    serverReq.write(body);
    serverReq.end();
    logger.info("Movement event sent: " + body);

});

mobileAppServer.get('/', function(req, res) {
    logger.info("Get main page: " + util.inspect(serverConfig));

    var body = fs.readFileSync("./config/html/main.html", "utf8");
    res.setHeader('Content-Type', 'text/html');
    res.send(200, body);
    logger.info("Completed sending main page");

});

mobileAppServer.get('/positionUpdateDevices', function(req, res) {
    logger.info("Get page for listing devices to position");

    var options = {
            host : serverConfig.options.host,
            path : '/cmx-cloud-server-sdk/api/cmxmobileserver/v1/clients/all',
            port : serverConfig.options.sdkPort,
            method : 'GET',
            auth: serverConfig.options.sdkUserName + ':' + serverConfig.options.sdkPassword,
            rejectUnauthorized: false,
            encoding : 'utf8'
    };

    var serverReq = https.request(options, function(response) {
        var str = '';
        response.on('data', function(chunk) {
            str += chunk;
        });

        response.on('end', function() {
            logger.info("List of all clients returned: " + str);
            var jsonData = JSON.parse(str);
            var body = fs.readFileSync("./config/html/positionUpdateDevices.html", "utf8");
            var tableRows = "";
            for (var i in jsonData) {
                locationUpdateTime = new Date (jsonData[i].lastLocationUpdateTime);
                tableRows += '<tr>';
                tableRows += '<td><a href="/positionUpdateMaps/'+jsonData[i].macAddress+'">'+jsonData[i].macAddress+'</a></td>';
                tableRows += '<td><a href="/positionUpdateMaps/'+jsonData[i].macAddress+'">'+locationUpdateTime.toLocaleString()+'</a></td>';
                tableRows += '</tr>';
            }
            body = body.replace(/%CMX_INSERT_TABLE_ROWS%/g, tableRows);
            res.setHeader('Content-Type', 'text/html');
            res.send(200, body);
        });
    });
    serverReq.end();
    logger.info("Completed sending page for listing devices to position");

});

mobileAppServer.get('/locations/', function(req, res) {
    logger.info("Get page for listing maps for doing location");
    var options = {
            host : serverConfig.options.host,
            path : '/cmx-cloud-server/api/cmxmobile/v1/venues/info',
            port : serverConfig.options.port,
            method : 'GET',
            rejectUnauthorized: false
    };
    var deviceIdParam = req.params.deviceId;

    var serverReq = https.request(options, function(response) {
        var str = '';
        response.on('data', function(chunk) {
            str += chunk;
        });

        response.on('end', function() {
            logger.info("Returned venue information: " + str);
            var jsonData = JSON.parse(str);
            var body = fs.readFileSync("./config/html/locationAllFloorsList.html", "utf8");
            var tableRows = "";
            var deviceId = deviceIdParam;
            for (var i in jsonData) {
                for (var j in jsonData[i].floors) {
                    tableRows += '<tr><td><a href="/locationsFloor/' + jsonData[i].floors[j].venueid+'/'+ jsonData[i].floors[j].floorId+'">'+jsonData[i].floors[j].mapHierarchyString+'</a></td><td><a href="/locationsFloor/' + jsonData[i].floors[j].venueid+'/'+ jsonData[i].floors[j].floorId+'"><img src="https://'+serverConfig.options.host+':'+serverConfig.options.port+'/cmx-cloud-server/api/cmxmobile/v1/maps/image/' + jsonData[i].floors[j].venueid+'/'+ jsonData[i].floors[j].floorId+'" height="100" width="100"></a></td></tr>';
                }
            }
            var mapImageLink = "https://"+serverConfig.options.host+":"+serverConfig.options.port+"/cmx-cloud-server/api/cmxmobile/v1/maps/image/" + jsonData[0].floors[0].venueid+"/"+ jsonData[0].floors[0].floorId;
            body = body.replace(/%CMX_MAP_IMAGE_LINK%/g, mapImageLink);
            body = body.replace(/%CMX_INSERT_TABLE_ROWS%/g, tableRows);
            res.setHeader('Content-Type', 'text/html');
            res.send(200, body);
        });
    });
    serverReq.end();
    logger.info("Completed sending page for listing maps for doing location");
});

mobileAppServer.get('/associateDevice/', function(req, res) {
    logger.info("Get page for associating a new device");
    var options = {
            host : serverConfig.options.host,
            path : '/cmx-cloud-server/api/cmxmobile/v1/venues/info',
            port : serverConfig.options.port,
            method : 'GET',
            rejectUnauthorized: false
    };
    var deviceIdParam = req.params.deviceId;

    var serverReq = https.request(options, function(response) {
        var str = '';
        response.on('data', function(chunk) {
            str += chunk;
        });

        response.on('end', function() {
            logger.info("Returned venue information for associating device: " + str);
            var jsonData = JSON.parse(str);
            var body = fs.readFileSync("./config/html/associateDevice.html", "utf8");
            var venueId = jsonData[0].floors[0].venueid;
            var mseUdi = venueId.substr(0, venueId.lastIndexOf(":"));
            var floorId = jsonData[0].floors[0].floorId;
            logger.info("MSE UDI: " + mseUdi);
            logger.info("Floor ID: " + floorId);
            body = body.replace(/%MSE_UDI%/g, mseUdi);
            body = body.replace(/%FLOOR_ID%/g, floorId);
            res.setHeader('Content-Type', 'text/html');
            res.send(200, body);
        });
    });
    serverReq.end();
    logger.info("Completed sending page for associating new device");
});

mobileAppServer.post('/associateNewDevice', function(req, res) {
    logger.info("Post associate new device request from: " + req.ip + " body: " + util.inspect(req.body));
    var addDeviceInfo = req.body;
    var associationEvent = { AssociationEvent : {
            subscriptionName : "CMX_Location_Event",
            timeStamp : "2013-06-10T19:16:26.065-0700",
            entity : "WIRELESS_CLIENTS",
            clientAuthInfo : serverConfig.options.authorizationKey,
            deviceId : addDeviceInfo.deviceMac,
            mseUdi : addDeviceInfo.mseUDI,
            floorRefId : addDeviceInfo.floorId,
            apMacAddress : addDeviceInfo.apMac,
            ipAddress : [ addDeviceInfo.deviceIpAddress ]
        }
    };

    var body = JSON.stringify(associationEvent);
    logger.info("Associate event being sent: " + body);
    var options = {
            host : serverConfig.options.host,
            path : '/cmx-cloud-server/api/cmxmobile/v1/notify',
            port : serverConfig.options.port,
            method : 'POST',
            rejectUnauthorized: false,
            headers : {
                "Content-Length" : body.length,
                "Content-Type" : "application/json"
            }
    };
    var serverReq = https.request(options, function(response) {
        var str = '';

        response.on('data', function(chunk) {
            str += chunk;
        });

        response.on('end', function() {
            logger.info("Response from sending associate event: " + response.statusCode + ": " + str);
            var body = fs.readFileSync("./config/html/associateDeviceResults.html", "utf8");
            var tableRow = "";
            if (response.statusCode === 200) {
                tableRow += '<tr>';
                tableRow += '<td>Successfully associated new device</td>';
                tableRow += '</tr>';
            } else {
                tableRow += '<tr>';
                tableRow += '<td>Failed to associate new device</td>';
                tableRow += '</tr>';
            }
            body = body.replace(/%CMX_INSERT_TABLE_ROWS%/g, tableRow);
            res.setHeader('Content-Type', 'text/html');
            res.send(200, body);
        });
    });
    serverReq.write(body);
    serverReq.end();
    logger.info("Completed sending associate device event");
});
   
mobileAppServer.get('/positionUpdateMaps/:deviceId', function(req, res) {
    logger.info("Get page for listing maps for positioning device: " + util.inspect(req.params));
    var options = {
            host : serverConfig.options.host,
            path : '/cmx-cloud-server/api/cmxmobile/v1/venues/info',
            port : serverConfig.options.port,
            method : 'GET',
            rejectUnauthorized: false
    };
    var deviceIdParam = req.params.deviceId;

    var serverReq = https.request(options, function(response) {
        var str = '';
        response.on('data', function(chunk) {
            str += chunk;
        });

        response.on('end', function() {
            logger.debug("Returned venue information for position maps: " + str);
            var jsonData = JSON.parse(str);
            var body = fs.readFileSync("./config/html/positionUpdateAllFloorsList.html", "utf8");
            var tableRows = "";
            var deviceId = deviceIdParam;
            for (var i in jsonData) {
                for (var j in jsonData[i].floors) {
                    tableRows += '<tr><td><a href="/positionUpdateFloor/'+ deviceId + '/' + jsonData[i].floors[j].venueid+'/'+ jsonData[i].floors[j].floorId+'">'+jsonData[i].floors[j].mapHierarchyString+'</a></td><td><a href="/positionUpdateFloor/'+ deviceId + '/' + jsonData[i].floors[j].venueid+'/'+ jsonData[i].floors[j].floorId+'"><img src="https://'+serverConfig.options.host+':'+serverConfig.options.port+'/cmx-cloud-server/api/cmxmobile/v1/maps/image/' + jsonData[i].floors[j].venueid+'/'+ jsonData[i].floors[j].floorId+'" height="100" width="100"></a></td></tr>';
                }
            }
            var mapImageLink = "https://"+serverConfig.options.host+":"+serverConfig.options.port+"/cmx-cloud-server/api/cmxmobile/v1/maps/image/" + jsonData[0].floors[0].venueid+"/"+ jsonData[0].floors[0].floorId;
            body = body.replace(/%CMX_MAP_IMAGE_LINK%/g, mapImageLink);
            body = body.replace(/%CMX_INSERT_TABLE_ROWS%/g, tableRows);
            body = body.replace(/%CMX_DEVICE_ID%/g, deviceIdParam);
            res.setHeader('Content-Type', 'text/html');
            res.send(200, body);
        });
    });
    serverReq.end();
    logger.info("Completed sending page for listing maps for positioning device");
});

mobileAppServer.get('/locationsFloor/:venueId/:floorId', function(req, res) {
    logger.info("Get page for displaying locations on floor: " + util.inspect(req.params));
    var options = {
            host : serverConfig.options.host,
            path : '/cmx-cloud-server/api/cmxmobile/v1/maps/info/' + req.params.venueId + '/' + req.params.floorId,
            port : serverConfig.options.port,
            method : 'GET',
            rejectUnauthorized: false
    };

    var deviceIdParam = req.params.deviceId;
    var serverReq = https.request(options, function(response) {
        var str = '';
        response.on('data', function(chunk) {
            str += chunk;
        });

        response.on('end', function() {
            logger.info("Returned maps information for displaying position on a floor: " + str);
            var jsonData = JSON.parse(str);
            var body = fs.readFileSync("./config/html/locationFloor.html", "utf8");
            var deviceId = deviceIdParam;
            var venueId = jsonData.venueid;
            var floorId = jsonData.floorId;
            var dimWidth = jsonData.dimension.width;
            var dimLength = jsonData.dimension.length;
            var dimOffsetRatio = jsonData.dimension.offsetX;
            var floorName = jsonData.name;
            body = body.replace(/%CMX_SERVER_IP%/g, serverConfig.options.host);
            body = body.replace(/%CMX_SERVER_PORT%/g, serverConfig.options.port);
            body = body.replace(/%CMX_DEVICE_ID%/g, deviceId);
            body = body.replace(/%CMX_VENUE_ID%/g, venueId);
            body = body.replace(/%CMX_FLOOR_ID%/g, floorId);
            body = body.replace(/%CMX_MAP_WIDTH%/g, dimWidth);
            body = body.replace(/%CMX_MAP_LENGTH%/g, dimLength);
            body = body.replace(/%CMX_OFFSET_RATIO%/g, dimOffsetRatio);
            res.setHeader('Content-Type', 'text/html');
            res.send(200, body);
        });
    });
    serverReq.end();
    logger.info("Completed sending page for displaying locations on floor");
});

mobileAppServer.get('/positionUpdateFloor/:deviceId/:venueId/:floorId', function(req, res) {
    logger.info("Get page for updating locations on floor: " + util.inspect(req.params));
    var options = {
            host : serverConfig.options.host,
            path : '/cmx-cloud-server/api/cmxmobile/v1/maps/info/' + req.params.venueId + '/' + req.params.floorId,
            port : serverConfig.options.port,
            method : 'GET',
            rejectUnauthorized: false
    };

    var deviceIdParam = req.params.deviceId;
    var serverReq = https.request(options, function(response) {
        var str = '';
        response.on('data', function(chunk) {
            str += chunk;
        });

        response.on('end', function() {
            logger.info("Returned maps information for updating position on a floor: " + str);
            var jsonData = JSON.parse(str);
            var body = fs.readFileSync("./config/html/positionUpdateFloor.html", "utf8");
            var deviceId = deviceIdParam;
            var venueId = jsonData.venueid;
            var floorId = jsonData.floorId;
            var dimWidth = jsonData.dimension.width;
            var dimLength = jsonData.dimension.length;
            var dimOffsetRatio = jsonData.dimension.offsetX;
            var floorName = jsonData.name;
            body = body.replace(/%CMX_SERVER_IP%/g, serverConfig.options.host);
            body = body.replace(/%CMX_SERVER_PORT%/g, serverConfig.options.port);
            body = body.replace(/%CMX_DEVICE_ID%/g, deviceId);
            body = body.replace(/%CMX_VENUE_ID%/g, venueId);
            body = body.replace(/%CMX_FLOOR_ID%/g, floorId);
            body = body.replace('%CMX_MAP_WIDTH%', dimWidth);
            body = body.replace('%CMX_MAP_LENGTH%', dimLength);
            body = body.replace(/%CMX_OFFSET_RATIO%/g, dimOffsetRatio);
            res.setHeader('Content-Type', 'text/html');
            res.send(200, body);
        });
    });
    serverReq.end();
    logger.info("Completed sending page for updating locations on floor");
});

mobileAppServer.get('/location/devices/:venueId/:floorId', function(req, res) {
    logger.info("Get devices locations on floor: " + util.inspect(req.params));
    var options = {
            host : serverConfig.options.host,
            path : '/cmx-cloud-server-sdk/api/cmxmobileserver/v1/clients/all',
            port : serverConfig.options.sdkPort,
            method : 'GET',
            auth: serverConfig.options.sdkUserName + ':' + serverConfig.options.sdkPassword,
            rejectUnauthorized: false,
            encoding : 'utf8'
    };

    var venueIdParam = req.params.venueId;
    var floorIdParam = req.params.floorId;
    var serverReq = https.request(options, function(response) {
        var str = '';
        response.on('data', function(chunk) {
            str += chunk;
        });

        response.on('end', function() {
            logger.info("Returned list of all clients for device location: " + str);
            var jsonData = JSON.parse(str);
            var returnData = [];
            var returnDataCounter = 0;
            for (var i in jsonData) {
                if (jsonData[i].venueId === venueIdParam && jsonData[i].floorId === floorIdParam) {
                    returnData[returnDataCounter] = jsonData[i];
                    ++returnDataCounter;
                }
            }
            res.setHeader('Content-Type', 'application/json');
            res.send(200, returnData);
        });
    });
    serverReq.end();
    logger.info("Completed sending devices locations on floor");
});

mobileAppServer.get('/image/:image', function(req, res) {
    logger.debug("Request for image: " + req.params.image);
    res.sendfile("./config/html/" + req.params.image);
});

mobileAppServer.get('/image/images/:image', function(req, res) {
    logger.debug("Request for images: " + req.params.image);
    res.sendfile("./config/html/images/" + req.params.image);
});

function runMain() {
    parseOptions(argv);
    https.createServer(options, mobileAppServer).listen(8080);
    logger.info("CMX Mobile App Server Interface Version: " + pkg.version + " listening on HTTPS port 8080");	
}

runMain();