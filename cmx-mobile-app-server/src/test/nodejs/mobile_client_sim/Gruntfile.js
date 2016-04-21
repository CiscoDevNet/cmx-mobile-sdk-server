module.exports = function(grunt) {

	var exec = require('child_process').exec;
	var path = require('path');
	var fs = require('fs');

	//
	// Project configuration.
	//
	grunt.initConfig({
		pkg: grunt.file.readJSON('package.json'),
		meta: {
			banner: '/*! <%=pkg.name%> - v<%=pkg.version%> (build <%=pkg.build%>) - ' + '<%=grunt.template.today("dddd, mmmm dS, yyyy, h:MM:ss TT")%> */'
		},
		src: {
			jsfiles: ["src/*.js"]
		},
		jshint: {
			options: {
				curly: false,
				eqeqeq: true,
				immed: true,
				latedef: true,
				newcap: true,
				noarg: true,
				sub: true,
				undef: false,
				boss: true,
				eqnull: true,
				browser: true,
				evil: true,
				globals: {
					jQuery: true,
					Holder: true,
					_: true,
					console: true,
					validate: true
				}
			},
			all: ['Gruntfile.js', '<%=src.jsfiles%>']
		},
		clean: {
			src: ["dist"]
		},
		copy: {
			main: {
				files: [
					{expand: true, cwd: 'src/', src: ['**'], dest: 'dist/'},
					{src: ['package.json'], dest: 'dist/'},
				]
			}
		},
		'string-replace': {
			dist: {
				files: {
					'dist/': 'package.json'
				},
				options: {
					replacements: [{
						pattern: /"scripts": {/ig,
						replacement: '"scripts": {\n    "postinstall": "node scripts/postinstall.js",'
					}]
				}
			}
		},
		compress: {
			main: {
				options: {
					archive: 'target/cisco-cmx-mobile-client-sim-test.tgz',
					mode: 'tgz'
				},
				files: [
					{expand: true, cwd: 'dist/', src: ['**'], dest: 'package/'}
				]
			}
		},
	});

	//
	// increment build number
	//
	grunt.registerTask('increment-build', "Update build number, called internally at the end of each build", function() {
		var pkg = grunt.file.readJSON('package.json');
		pkg.build = parseInt(pkg.build, 10) + 1;
		grunt.file.write('package.json', JSON.stringify(pkg, null, 2));
	});

	grunt.loadNpmTasks('grunt-contrib-jshint');
	grunt.loadNpmTasks('grunt-contrib-clean');
	grunt.loadNpmTasks('grunt-contrib-copy');
	grunt.loadNpmTasks('grunt-contrib-compress');
	grunt.loadNpmTasks('grunt-string-replace');

	//
	// default task
	//
	grunt.registerTask('default', ['jshint', 'clean', 'copy', 'string-replace', 'compress']);
};
