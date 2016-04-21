"use strict"

var logger = require('../config/logging/logger')
  , fs = require('fs')
  , step = require('step')
  , https = require('https')

/*************************************************************************
Converts a list of parameters to forum data
- `fields` - a property map of key value pairs
- `files` - a list of property maps of content
  - `type` - the type of file data
  - `keyname` - the name of the key corresponding to the file
  - `valuename` - the name of the value corresponding to the file
  - `data` - the data of the file
**************************************************************************/
function getFormDataForPost(fields, files) {
	function encodeFieldPart(boundary,name,value) {
		var return_part = "--" + boundary + "\r\n";
		return_part += "Content-Disposition: form-data; name=\"" + name + "\"\r\n\r\n";
		return_part += value + "\r\n";
		return return_part;
	}
	function encodeFilePart(boundary,type,name,filename) {
		var return_part = "--" + boundary + "\r\n";
		return_part += "Content-Disposition: form-data; name=\"" + name + "\"; filename=\"" + filename + "\"\r\n";
		return_part += "Content-Type: " + type + "\r\n\r\n";
		return return_part;
	}
	var boundary = Math.random();
	var post_data = [];
	var key = '';
	var value = '';
	if (fields) {
		for (key in fields) {
			value = fields[key];
			post_data.push(new Buffer(encodeFieldPart(boundary, key, value), 'ascii'));
		}
	}
	if (files) {
		for (key in files) {
			value = files[key];
			post_data.push(new Buffer(encodeFilePart(boundary, value.type, value.keyname, value.valuename), 'ascii'));
			post_data.push(new Buffer(value.data, 'utf8'));
		}
	}
	post_data.push(new Buffer("\r\n--" + boundary + "--"), 'ascii');
	var length = 0;
 
	for(var i = 0; i < post_data.length; i++) {
		length += post_data[i].length;
	}
	var params = {
		postdata : post_data,
		headers : {
			'Content-Type': 'multipart/form-data; boundary=' + boundary,
			'Content-Length': length
		}
	};
	return params;
}
 
/*************************************************************************
Sends a post form request via http
- `fields` - a property map of key value pairs
- `files` - a list of property maps of content
  - `type` - the type of file data
  - `keyname` - the name of the key corresponding to the file
  - `valuename` - the name of the value corresponding to the file
  - `data` - the data of the file
- `options` is a set of options
  - host
  - port
  - path
  - method
  - encoding
- `headers` headers to be sent with the request
- `callback` - callback to handle the response
**************************************************************************/
function postData(fields, files, options, headers, callback) {
	var headerparams = getFormDataForPost(fields, files);
	var totalheaders = headerparams.headers;
	for (var key in headers) totalheaders[key] = headers[key];
 
	var post_options = {
		host: options.host,
		port: options.port,
		path: options.path,
		method: options.method || 'POST',
		auth: options.auth || '',
		rejectUnauthorized: false,
		headers: totalheaders
	};
	var request = https.request(post_options, function(response) {
		response.body = '';
		response.setEncoding(options.encoding);
		response.on('data', function(chunk){
			logger.debug("Image upload response: " + chunk);
			response.body += chunk;
		});
		response.on('end', function() {
			callback(null, response);
		});
	});
 
	for (var i = 0; i < headerparams.postdata.length; i++) {
		request.write(headerparams.postdata[i]);
	}
	request.end();
}
 
/**************************************************************************
Post an image via http
- `options` is a set of options
  - host
  - port
  - path
  - method
  - encoding
- `filename` filename being uploaded
- `headers` headers to be sent with the request
- `fields` - a property map of key value pairs
**************************************************************************/
function postImage(options, filename, headers, fields) {
	logger.debug("Attempting to post image: " + filename);
	step(
		function readImage() {
			fs.readFile(filename, this);
		},
		function(err, filecontents) {
			if (err) {
				logger.error('Unable to read file: ' + filename);
				return;
			}
			postData(fields, [{type: 'image/gif', keyname: 'file', valuename: 'image.gif', data: filecontents}], options, headers, this);
		},
		function(err, response) {
			logger.info("Response code for file: " + filename + " upload: " + response.statusCode);
		}
	);
}

module.exports.postImage = postImage;