#!/bin/bash
RUN_DIR=`dirname $0`
CURRENT_DIR=`cd $RUN_DIR; pwd`
OUTPUT_LOG_FILE=$CURRENT_DIR/../logs/admin-operations.log

CMX_JAR_HOME=$CURRENT_DIR/../apache-tomcat/webapps/cmx-cloud-server/WEB-INF/lib
CMX_TOMCAT_JAR_HOME=$CURRENT_DIR/../apache-tomcat/lib

#JAVA_DEBUG_OPTIONS="-Xdebug -Xrunjdwp:transport=dt_socket,address=20600,server=y,suspend=y"
JAVA_HOME=$CURRENT_DIR/../java

JAVA_OPTS="\
-Dlogback.configurationFile=$CURRENT_DIR/../apache-tomcat/webapps/cmx-cloud-server/WEB-INF/classes/cli/cliLogback.xml
$JAVA_OPTS "

JAVA_CLASSPATH="\
$CURRENT_DIR/../apache-tomcat/webapps/cmx-cloud-server/WEB-INF/classes/:\
"

for jar in $(ls $CMX_JAR_HOME/*.jar); do
    JAVA_CLASSPATH=${JAVA_CLASSPATH}:${jar}
done

for jar in $(ls $CMX_TOMCAT_JAR_HOME/*.jar); do
    JAVA_CLASSPATH=${JAVA_CLASSPATH}:${jar}
done

$JAVA_HOME/bin/java $JAVA_OPTS $JAVA_DEBUG_OPTIONS -cp $JAVA_CLASSPATH com.cisco.cmxmobile.server.ServerOperations getServerStats $1

