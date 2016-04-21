var fs = require('fs');
var fsextra = require('fs-extra');
var destDir = "../../";

fsextra.copy("./", destDir, function(err){
	if (err) {
		console.error(err);
	}
	else {
		fsextra.removeSync(destDir + "scripts");
		fs.unlinkSync(destDir + "package.json");
	}
});