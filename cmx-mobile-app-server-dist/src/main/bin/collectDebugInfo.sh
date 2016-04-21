#!/bin/bash

RUN_DIR=`dirname $0`
INSTALL_DIR=`cd $RUN_DIR/..; pwd`
LOGS_DIRECTORY=$INSTALL_DIR/logs
OUTPUT_LOG_FILE=$LOGS_DIRECTORY/debugInfo.txt
CMX_BIN_DIR=$INSTALL_DIR/bin
CMX_DATA_DIRECTORY=$INSTALL_DIR/data
PERIODIC_LOG_FILE=$LOGS_DIRECTORY/cmx-periodic-stats.log
PERIODIC_ROTATE_LOG_FILE=$LOGS_DIRECTORY/cmx-periodic-stats.log.1
PERIODIC_LOG_FILE_MAX_SIZE=100
PERIODIC_LOG_FILE_MAX_DAYS=2
REDIS_ROOT_DIRECTORY=$INSTALL_DIR/redis
REDIS_BIN_DIRECTORY=$REDIS_ROOT_DIRECTORY/bin
REDIS_LOGS_DIRECTORY=$REDIS_ROOT_DIRECTORY/logs
REDIS_CLI=$REDIS_BIN_DIRECTORY/redis-cli
APACHE_LOGS_DIRECTORY=$INSTALL_DIR/apache-tomcat/logs
APACHE_SDK_LOGS_DIRECTORY=$INSTALL_DIR/apache-tomcat-sdk/logs
IS_RUNNING_AS_PERIODIC="false"

if [ "$1" != "" ]; then
    if [ "$1" != "periodic" ]; then
        echo "Usage: collectDebugInfo.sh [ periodic ]"
        exit 1
    fi
    OUTPUT_LOG_FILE=$PERIODIC_LOG_FILE
    IS_RUNNING_AS_PERIODIC="true"
    if [ -f $OUTPUT_LOG_FILE ]; then
        OUTPUT_LOG_FILE_SIZE=`du -m $OUTPUT_LOG_FILE | awk '{print $1}'`
        if [ $OUTPUT_LOG_FILE_SIZE -ge "$PERIODIC_LOG_FILE_MAX_SIZE" ]; then
            echo "{`date`} Log file has reached maximum size and will be rotated" >> $OUTPUT_LOG_FILE 2>&1
            mv $OUTPUT_LOG_FILE $PERIODIC_ROTATE_LOG_FILE
        fi
    fi
    find $APACHE_LOGS_DIRECTORY/localhost_access_log* -mtime +$PERIODIC_LOG_FILE_MAX_DAYS | xargs rm -f >> $OUTPUT_LOG_FILE 2>&1
    find $APACHE_LOGS_DIRECTORY/catalina*.log -mtime +$PERIODIC_LOG_FILE_MAX_DAYS | xargs rm -f >> $OUTPUT_LOG_FILE 2>&1
    find $APACHE_LOGS_DIRECTORY/host-manager*.log -mtime +$PERIODIC_LOG_FILE_MAX_DAYS | xargs rm -f >> $OUTPUT_LOG_FILE 2>&1
    find $APACHE_LOGS_DIRECTORY/localhost*.log -mtime +$PERIODIC_LOG_FILE_MAX_DAYS | xargs rm -f >> $OUTPUT_LOG_FILE 2>&1
    find $APACHE_LOGS_DIRECTORY/manager*.log -mtime +$PERIODIC_LOG_FILE_MAX_DAYS | xargs rm -f >> $OUTPUT_LOG_FILE 2>&1
    find $APACHE_SDK_LOGS_DIRECTORY/localhost_access_log* -mtime +2 | xargs rm -f >> $OUTPUT_LOG_FILE 2>&1
    find $APACHE_SDK_LOGS_DIRECTORY/catalina*.log -mtime +$PERIODIC_LOG_FILE_MAX_DAYS | xargs rm -f >> $OUTPUT_LOG_FILE 2>&1
    find $APACHE_SDK_LOGS_DIRECTORY/host-manager*.log -mtime +$PERIODIC_LOG_FILE_MAX_DAYS | xargs rm -f >> $OUTPUT_LOG_FILE 2>&1
    find $APACHE_SDK_LOGS_DIRECTORY/localhost*.log -mtime +$PERIODIC_LOG_FILE_MAX_DAYS | xargs rm -f >> $OUTPUT_LOG_FILE 2>&1
    find $APACHE_SDK_LOGS_DIRECTORY/manager*.log -mtime +$PERIODIC_LOG_FILE_MAX_DAYS | xargs rm -f >> $OUTPUT_LOG_FILE 2>&1
else
    PROMPT_QUESTION=true
    while [ "$PROMPT_QUESTION" = "true" ]; do
        echo -n "Is the current debug collection related to a specific client [y/n] "
        read getClientMacAddress
        PROMPT_QUESTION=false
        if [ "$getClientMacAddress" != "Y" ] && [ "$getClientMacAddress" != "y" ] && [ "$getClientMacAddress" != "N" ] && [ "$getClientMacAddress" != "n" ]; then
            echo "!!! Please enter a valid y/n option !!!"
            PROMPT_QUESTION=true
        fi      
    done
    if [ "$getClientMacAddress" == "Y" ] || [ "$getClientMacAddress" == "y" ]; then
        PROMPT_QUESTION=true
        while [ "$PROMPT_QUESTION" = "true" ]; do
            echo -n "Please enter the MAC address of the client: "
            read clientMacAddress
            PROMPT_QUESTION=false
            TEST_CLIENT_MAC_ADDRESS=`echo $clientMacAddress | egrep "^([0-9a-fA-F]{2}:){5}[0-9a-fA-F]{2}$"`
            if [ "$TEST_CLIENT_MAC_ADDRESS" == "" ]; then
                echo "!!! Please enter a valid MAC address [01:23:45:67:89:ab] !!!"
                PROMPT_QUESTION=true
            fi
        done
    fi
    echo -n "Please enter a description of the issue the debug collection is being executed for: "
    read issueDescription
fi

START_TIME=`date`

if [ "$IS_RUNNING_AS_PERIODIC" = "false" ]; then
    echo "" > $OUTPUT_LOG_FILE
fi

echo "===================================================" >> $OUTPUT_LOG_FILE
echo "Starting to collect debug information: $START_TIME" >> $OUTPUT_LOG_FILE
echo "===================================================" >> $OUTPUT_LOG_FILE

if [ "$IS_RUNNING_AS_PERIODIC" = "false" ]; then
    echo "==================================================="
    echo "Starting to collect debug information: $START_TIME" 
    echo "==================================================="

    echo "===================================================" >> $OUTPUT_LOG_FILE
    echo "Issue Description" >> $OUTPUT_LOG_FILE
    echo "===================================================" >> $OUTPUT_LOG_FILE
    echo $issueDescription >> $OUTPUT_LOG_FILE

    if [ "$getClientMacAddress" == "Y" ] || [ "$getClientMacAddress" == "y" ]; then
        echo "===================================================" >> $OUTPUT_LOG_FILE
        echo "Client MAC Address: $clientMacAddress" >> $OUTPUT_LOG_FILE
        echo "===================================================" >> $OUTPUT_LOG_FILE
    fi
fi

echo "===================================================" >> $OUTPUT_LOG_FILE
echo "Getting file system usage" >> $OUTPUT_LOG_FILE
echo "===================================================" >> $OUTPUT_LOG_FILE
df -h >> $OUTPUT_LOG_FILE 2>&1
echo "===================================================" >> $OUTPUT_LOG_FILE
echo "Getting list of current processes" >> $OUTPUT_LOG_FILE
echo "===================================================" >> $OUTPUT_LOG_FILE
ps axjf >> $OUTPUT_LOG_FILE 2>&1
echo "===================================================" >> $OUTPUT_LOG_FILE
echo "Getting the top processes" >> $OUTPUT_LOG_FILE
echo "===================================================" >> $OUTPUT_LOG_FILE
top -bn 1 >> $OUTPUT_LOG_FILE 2>&1
echo "===================================================" >> $OUTPUT_LOG_FILE
echo "Getting the processor related stats" >> $OUTPUT_LOG_FILE
echo "===================================================" >> $OUTPUT_LOG_FILE
mpstat -P ALL 1 1 >> $OUTPUT_LOG_FILE 2>&1
echo "===================================================" >> $OUTPUT_LOG_FILE
echo "Getting the I/O related stats" >> $OUTPUT_LOG_FILE
echo "===================================================" >> $OUTPUT_LOG_FILE
iostat -p ALL >> $OUTPUT_LOG_FILE 2>&1
echo "===================================================" >> $OUTPUT_LOG_FILE
echo "Getting the Redis information" >> $OUTPUT_LOG_FILE
echo "===================================================" >> $OUTPUT_LOG_FILE
$REDIS_CLI info >> $OUTPUT_LOG_FILE 2>&1
echo "===================================================" >> $OUTPUT_LOG_FILE
echo "Getting the Redis client list" >> $OUTPUT_LOG_FILE
echo "===================================================" >> $OUTPUT_LOG_FILE
$REDIS_CLI client list >> $OUTPUT_LOG_FILE 2>&1
echo "===================================================" >> $OUTPUT_LOG_FILE
echo "Getting the Mobile App Server stats" >> $OUTPUT_LOG_FILE
echo "===================================================" >> $OUTPUT_LOG_FILE
$CMX_BIN_DIR/getServerStats.sh >> $OUTPUT_LOG_FILE 2>&1
echo "===================================================" >> $OUTPUT_LOG_FILE
echo "Getting the netstat interface stats" >> $OUTPUT_LOG_FILE
echo "===================================================" >> $OUTPUT_LOG_FILE
netstat -i >> $OUTPUT_LOG_FILE 2>&1
echo "===================================================" >> $OUTPUT_LOG_FILE
echo "Getting the netstat connection stats" >> $OUTPUT_LOG_FILE
echo "===================================================" >> $OUTPUT_LOG_FILE
netstat -nap >> $OUTPUT_LOG_FILE 2>&1
if [ "$IS_RUNNING_AS_PERIODIC" = "false" ]; then
    echo "===================================================" >> $OUTPUT_LOG_FILE
    echo "Getting the linux server information" >> $OUTPUT_LOG_FILE
    echo "===================================================" >> $OUTPUT_LOG_FILE
    dmesg | head -1 >> $OUTPUT_LOG_FILE 2>&1
    cat /proc/version >> $OUTPUT_LOG_FILE 2>&1
    uname -a >> $OUTPUT_LOG_FILE 2>&1
    echo "===================================================" >> $OUTPUT_LOG_FILE
    echo "Getting the Mobile App Server configuration" >> $OUTPUT_LOG_FILE
    echo "===================================================" >> $OUTPUT_LOG_FILE
    $CMX_BIN_DIR/getServerConfig.sh >> $OUTPUT_LOG_FILE 2>&1
    echo "===================================================" >> $OUTPUT_LOG_FILE
    echo "Getting RPM verification information" >> $OUTPUT_LOG_FILE
    echo "===================================================" >> $OUTPUT_LOG_FILE
    rpm -V cmx-mobile-app-server >> $OUTPUT_LOG_FILE 2>&1
    echo "===================================================" >> $OUTPUT_LOG_FILE
    echo "Getting the iptables listing" >> $OUTPUT_LOG_FILE
    echo "===================================================" >> $OUTPUT_LOG_FILE
    iptables -nL >> $OUTPUT_LOG_FILE 2>&1
    echo "===================================================" >> $OUTPUT_LOG_FILE
    echo "Getting a java thread dump" >> $OUTPUT_LOG_FILE 2>&1
    echo "===================================================" >> $OUTPUT_LOG_FILE
    ps -ef | grep java | grep "$INSTALL_DIR" | awk '{print $2}' | xargs kill -3 >> $OUTPUT_LOG_FILE
    echo "===================================================" >> $OUTPUT_LOG_FILE
    echo "Displaying /var/log/messages" >> $OUTPUT_LOG_FILE 2>&1
    echo "===================================================" >> $OUTPUT_LOG_FILE
    cat /var/log/messages >> $OUTPUT_LOG_FILE 2>&1
fi

END_TIME=`date`
echo "===================================================" >> $OUTPUT_LOG_FILE
echo "Completed collecting debug information" >> $OUTPUT_LOG_FILE
echo "Start Time: $START_TIME" >> $OUTPUT_LOG_FILE
echo "Completed Time: $END_TIME" >> $OUTPUT_LOG_FILE
echo "===================================================" >> $OUTPUT_LOG_FILE

if [ "$IS_RUNNING_AS_PERIODIC" = "false" ]; then
    rm -f $LOGS_DIRECTORY/cmx-debug-logs.tar
    rm -f $LOGS_DIRECTORY/cmx-debug-logs.tar.gz
    tar -cvf $LOGS_DIRECTORY/cmx-debug-logs.tar $LOGS_DIRECTORY/ $APACHE_LOGS_DIRECTORY/ $APACHE_SDK_LOGS_DIRECTORY/ $CMX_DATA_DIRECTORY/ > /dev/null 2>&1
    gzip $LOGS_DIRECTORY/cmx-debug-logs.tar > /dev/null 2>&1

    echo "==================================================="
    echo "Completed collecting debug information"
    echo "Logging information collected and bundled into file $LOGS_DIRECTORY/cmx-debug-logs.tar.gz"
    echo "==================================================="
fi