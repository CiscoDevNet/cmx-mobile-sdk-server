#/bin/sh
CMX_BASE_BACKUP_DIR=/opt/cmx-mobile-app-server-backup
CMX_BASE_DIR=/opt/cmx-mobile-app-server
INSTALL_LOG_FILE=$CMX_BASE_DIR/install/install.log
#
# First parameter greater than 1 indicates the RPM is being upgraded
#
if [ "$1" -ge 2 ]; then
    echo "Starting to upgrade Cisco CMX Mobile App Server: `date`"
    echo "Starting to upgrade Cisco CMX Mobile App Server: `date`" >> $INSTALL_LOG_FILE 2>&1
    echo "Backing up Cisco CMX Mobile App Server data for upgrade" >> $INSTALL_LOG_FILE 2>&1
    /etc/init.d/cmx-mobile-app-server stop >> $INSTALL_LOG_FILE 2>&1
    mkdir -p $CMX_BASE_BACKUP_DIR
    cp -rp $CMX_BASE_DIR/data $CMX_BASE_BACKUP_DIR/data
    if [ -d $CMX_BASE_DIR/images ]; then
        cp -rp $CMX_BASE_DIR/images $CMX_BASE_BACKUP_DIR/images
    fi
    if [ -d $CMX_BASE_DIR/certs ]; then
        cp -rp $CMX_BASE_DIR/certs $CMX_BASE_BACKUP_DIR/certs
    fi
    if [ -f $CMX_BASE_DIR/apache-tomcat/webapps/cmx-cloud-server/WEB-INF/classes/config/user.properties ]; then
        mkdir -p $CMX_BASE_BACKUP_DIR/apache-tomcat
        cp -p $CMX_BASE_DIR/apache-tomcat/webapps/cmx-cloud-server/WEB-INF/classes/config/user.properties $CMX_BASE_BACKUP_DIR/apache-tomcat
    fi
    if [ -f $CMX_BASE_DIR/apache-tomcat-sdk/webapps/cmx-cloud-server-sdk/WEB-INF/classes/config/user.properties ]; then
        mkdir -p $CMX_BASE_BACKUP_DIR/apache-tomcat-sdk
        cp -p $CMX_BASE_DIR/apache-tomcat-sdk/webapps/cmx-cloud-server-sdk/WEB-INF/classes/config/user.properties $CMX_BASE_BACKUP_DIR/apache-tomcat-sdk
    fi
    rm -rf $CMX_BASE_DIR/apache-tomcat/webapps/cmx-cloud-server
    rm -rf $CMX_BASE_DIR/apache-tomcat-sdk/webapps/cmx-cloud-server-sdk
else
    echo "Starting to install Cisco CMX Mobile App Server: `date`"
fi