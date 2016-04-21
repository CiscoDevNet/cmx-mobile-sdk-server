"use strict"

var options = {
    numberClients: 1000,
    numberSimultaneousClients: 10,
    sleepIntervalMilliseconds: 20000,
    macAddressPrefix: '00:00:00',
    configDir: '.',
    host: 'localhost',
    port: '8082',
    webApp: 'cmx-cloud-server',
    userId: 'admin',
    password: 'Password123'
};

module.exports.options = options