Overview
=========
The following script can be used to see devices current position on a map or change the location of a device.

## Installing the dependencies
###	Node.js
First install node.js. The image can be downloaded from http://nodejs.org/

### Script install
Install the script by typing the command **npm install cisco-cmx-app-server-sdk-interface.tgz**

### Certificate install
Install the certificates needed for the server on windows by changing into the **bin** sub directory
Run the script **cergen.bat** in the **bin** directory
The script will prompt for information for the certificates and generate three files in the install directory: **server-cert.pem, server-csr.pem** and **server-key.pem**
On other platforms you can run the same commands as the script:

+ **openssl genrsa -out server-key.pem 1024**
+ **openssl req -new -key server-key.pem -out server-csr.pem**
+ **openssl x509 -req -in server-csr.pem -signkey server-key.pem -out server-cert.pem**

## Running script
Run the command '**node cmxAppServerSdkInterface.js**'

The following are the parameters which can be used:

+ **host** - CMX Mobile App Server SDK host addresss
+ **port** - CMX Mobile App Server Mobile port
+ **authorizationKey** - MSE Notification authorization key
+ **sdkPort** - CMX Mobile App Server SDK port
+ **sdkUserName** - CMX Mobile App Server SDK user name
+ **sdkPassword** - CMX Mobile App Server SDK password

Example: **node cmxAppServerSdkInterface.js --host 172.19.28.10 --port 8082 --sdkPort 8085 --sdkUserName admin --sdkPassword password**

To view all the options run the command '**node cmxAppServerSdkInterface.js -h**'