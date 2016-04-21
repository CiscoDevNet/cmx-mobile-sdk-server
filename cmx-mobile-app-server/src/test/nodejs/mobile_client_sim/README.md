Overview
=========
The following test script can be used to simulate CMX Mobile Clients making requests for the current position.
The clients will first register with the cloud server.
After successfully registering with the server the client will continually make requests for client location

## Installing the dependencies
###	Node.js
Node.js can be downloaded from http://nodejs.org.

## Running tests
Run the command '**node cmxMobileClientSimTest.js**'

The following are the parameters which will be needed:

+ **userId** - User Id required by cloud server to authenticate user
+ **password** - User password required by cloud server to authenticate user
+ **host** - Host address of the cloud server
+ **numberClients** - Number of clients to simulate

Example: *node cmxMobileClientSimTest.js --numberClients 1000 --userId admin --password password --host 172.19.28.10*

There are other options which can be used. To view all the options run the command '**node cmxMobileClientSimTest.js -h**'