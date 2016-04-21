"use strict";

var options = {
    macAddressPrefix: '00:00:00'
};

function createClientConfigs(testOptions, clientOptions) {
    var clientConfigs = [];
    for (var i = 0; i < testOptions.numberClients; i++) {
        var clientConfig = {           
            id: i + 1,
            macAddress: function() {
                var hexStr = (i + 1).toString(16);
                switch (hexStr.length) {
                    case 1:
                        return (clientOptions.macAddressPrefix + ":00:00:0" + hexStr);
                    case 2:
                        return (clientOptions.macAddressPrefix + ":00:00:" + hexStr);
                    case 3:
                        return (clientOptions.macAddressPrefix + ":00:0" + hexStr.substring(2,3) + ":" + hexStr.substring(0,2));
                    case 4:
                        return (clientOptions.macAddressPrefix + ":00:" + hexStr.substring(2,4) + ":" + hexStr.substring(0,2));
                    case 5:
                        return (clientOptions.macAddressPrefix + ":0" + hexStr.substring(4,5) + ":" + hexStr.substring(2,4) + ":" + hexStr.substring(0,2));
                    case 6:
                        return (clientOptions.macAddressPrefix + ":" + hexStr.substring(4,6) + ":" + hexStr.substring(2,4) + ":" + hexStr.substring(0,2));
                }
            }()
        }
        for (var key in testOptions) {
            clientConfig[key] = testOptions[key];
        }
        clientConfigs.push(clientConfig);
    }
    return clientConfigs;
}

module.exports.options = options
module.exports.createClientConfigs = createClientConfigs