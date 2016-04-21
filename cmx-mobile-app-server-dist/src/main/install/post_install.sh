#/bin/sh
CMX_BASE_BACKUP_DIR=/opt/cmx-mobile-app-server-backup
CMX_BASE_DIR=/opt/cmx-mobile-app-server
CMX_USER=nobody
INSTALL_LOG_FILE=$CMX_BASE_DIR/install/install.log

#
# Add periodic job to collect debug information
#
crontab -l > /tmp/crontab.out
CRONTAB_CHECK_RESULT=`grep "collectDebugInfo.sh" /tmp/crontab.out`
if  [ "$CRONTAB_CHECK_RESULT" = "" ]; then
    sed -e '/collectDebugInfo.sh/d' /tmp/crontab.out > /tmp/crontab.in
    echo "0 * * * * $CMX_BASE_DIR/bin/collectDebugInfo.sh periodic" >> /tmp/crontab.in
    crontab /tmp/crontab.in
    rm -f /tmp/crontab.in
fi
rm -f /tmp/crontab.out

#
# First parameter equal to 1 indicates the RPM is being installed new and not upgraded
#
if [ "$1" = 1 ]; then
    mkdir -p $CMX_BASE_DIR/certs
    chown nobody.nobody $CMX_BASE_DIR/certs
    mkdir -p $CMX_BASE_DIR/images
    chown nobody.nobody $CMX_BASE_DIR/images
    mkdir -p $CMX_BASE_DIR/install
    chown nobody.nobody $CMX_BASE_DIR/install
    mkdir -p $CMX_BASE_DIR/logs
    chown nobody.nobody $CMX_BASE_DIR/logs
    mkdir -p $CMX_BASE_DIR/data/1
    chown nobody.nobody $CMX_BASE_DIR/data/1
    mkdir -p $CMX_BASE_DIR/redis/var
    chown nobody.nobody $CMX_BASE_DIR/redis/var
    mkdir -p $CMX_BASE_DIR/apache-tomcat/logs
    chown -R nobody.nobody $CMX_BASE_DIR/apache-tomcat
    mkdir -p $CMX_BASE_DIR/apache-tomcat-sdk/logs
    chown -R nobody.nobody $CMX_BASE_DIR/apache-tomcat-sdk
    echo "Starting Cisco CMX Mobile App Server post install: `date`" >> $INSTALL_LOG_FILE 2>&1
    pushd /etc/init.d >> $INSTALL_LOG_FILE 2>&1
    ln -s $CMX_BASE_DIR/init.d/cmx-mobile-app-server cmx-mobile-app-server >> $INSTALL_LOG_FILE 2>&1
    popd >> $INSTALL_LOG_FILE 2>&1
    pushd /etc/rc0.d >> $INSTALL_LOG_FILE 2>&1
    ln -s ../init.d/cmx-mobile-app-server K27cmx-mobile-app-server >> $INSTALL_LOG_FILE 2>&1
    popd >> $INSTALL_LOG_FILE 2>&1
    pushd /etc/rc1.d >> $INSTALL_LOG_FILE 2>&1
    ln -s ../init.d/cmx-mobile-app-server K27cmx-mobile-app-server >> $INSTALL_LOG_FILE 2>&1
    popd >> $INSTALL_LOG_FILE 2>&1
    pushd /etc/rc2.d >> $INSTALL_LOG_FILE 2>&1
    ln -s ../init.d/cmx-mobile-app-server S77cmx-mobile-app-server >> $INSTALL_LOG_FILE 2>&1
    popd >> $INSTALL_LOG_FILE 2>&1
    pushd /etc/rc3.d >> $INSTALL_LOG_FILE 2>&1
    ln -s ../init.d/cmx-mobile-app-server S77cmx-mobile-app-server >> $INSTALL_LOG_FILE 2>&1
    popd >> $INSTALL_LOG_FILE 2>&1
    pushd /etc/rc4.d >> $INSTALL_LOG_FILE 2>&1
    ln -s ../init.d/cmx-mobile-app-server S77cmx-mobile-app-server >> $INSTALL_LOG_FILE 2>&1
    popd >> $INSTALL_LOG_FILE 2>&1
    pushd /etc/rc5.d >> $INSTALL_LOG_FILE 2>&1
    ln -s ../init.d/cmx-mobile-app-server S77cmx-mobile-app-server >> $INSTALL_LOG_FILE 2>&1
    popd >> $INSTALL_LOG_FILE 2>&1
    pushd /etc/rc6.d >> $INSTALL_LOG_FILE 2>&1
    ln -s ../init.d/cmx-mobile-app-server K27cmx-mobile-app-server >> $INSTALL_LOG_FILE 2>&1
    popd >> $INSTALL_LOG_FILE 2>&1
    $CMX_BASE_DIR/java/bin/keytool -genkey -alias tomcat -keyalg RSA -keysize 2048 -sigalg SHA1withRSA -dname "CN=CMX, OU=WNBU, O=Cisco, L=San Jose, ST=CA, C=US" -validity 730 -keypass CiScOCmXPass -keystore $CMX_BASE_DIR/certs/keystore -storepass CiScOCmXPass -storetype JKS >> $INSTALL_LOG_FILE 2>&1
    pushd $CMX_BASE_DIR/redis/compile >> $INSTALL_LOG_FILE 2>&1
    make MALLOC=libc PREFIX=$CMX_BASE_DIR/redis install >> $INSTALL_LOG_FILE 2>&1
    popd >> $INSTALL_LOG_FILE 2>&1
    /etc/init.d/cmx-mobile-app-server start >> $INSTALL_LOG_FILE 2>&1
    echo "Completed Cisco CMX Mobile App Server install: `date`" >> $INSTALL_LOG_FILE 2>&1
    chown nobody.nobody $CMX_BASE_DIR/logs/*
    echo "Completed Cisco CMX Mobile App Server install: `date`"
    echo ""
    echo "************************************************************"
    echo "*** Run $CMX_BASE_DIR/setup/setup.sh script ***"
    echo "*** to configure the server. The credentials for         ***"
    echo "*** the server need to be configured.                    ***"
    echo "***                                                      ***"
    echo "*** Documentation for the server is located at           ***"
    echo "*** 'doc/README'                                         ***"
    echo "***                                                      ***"
    echo "*** Release notes for changes is located at              ***"
    echo "*** 'doc/ReleaseNotes'                                   ***"
    echo "************************************************************"
else
    echo "Starting to restore Cisco CMX Mobile App Server: `date`"
    echo "Starting to restore Cisco CMX Mobile App Server: `date`" >> $INSTALL_LOG_FILE 2>&1
    pushd $CMX_BASE_DIR/apache-tomcat/webapps/ >> $INSTALL_LOG_FILE 2>&1
    unzip -d  cmx-cloud-server cmx-cloud-server.war >> $INSTALL_LOG_FILE 2>&1
    chown -R nobody.nobody cmx-cloud-server >> $INSTALL_LOG_FILE 2>&1
    popd >> $INSTALL_LOG_FILE 2>&1
    pushd $CMX_BASE_DIR/apache-tomcat-sdk/webapps/ >> $INSTALL_LOG_FILE 2>&1
    unzip -d  cmx-cloud-server-sdk cmx-cloud-server-sdk.war >> $INSTALL_LOG_FILE 2>&1
    chown -R nobody.nobody cmx-cloud-server-sdk >> $INSTALL_LOG_FILE 2>&1
    popd >> $INSTALL_LOG_FILE 2>&1
    if [ -d $CMX_BASE_BACKUP_DIR/data ]; then
        rm -rf $CMX_BASE_DIR/data
        cp -rpf $CMX_BASE_BACKUP_DIR/data $CMX_BASE_DIR/data
        chown -R nobody.nobody $CMX_BASE_DIR/data
    fi
    if [ -d $CMX_BASE_BACKUP_DIR/images ]; then
        rm -rf $CMX_BASE_DIR/images
        cp -rpf $CMX_BASE_BACKUP_DIR/images $CMX_BASE_DIR/images
        chown -R nobody.nobody $CMX_BASE_DIR/images
    fi
    if [ -d $CMX_BASE_BACKUP_DIR/certs ]; then
        rm -rf $CMX_BASE_DIR/certs
        cp -r $CMX_BASE_BACKUP_DIR/certs $CMX_BASE_DIR/certs
        chown -R nobody.nobody $CMX_BASE_DIR/certs
    fi
    if [ -f $CMX_BASE_BACKUP_DIR/apache-tomcat/user.properties ]; then
        cp -pf $CMX_BASE_BACKUP_DIR/apache-tomcat/user.properties $CMX_BASE_DIR/apache-tomcat/webapps/cmx-cloud-server/WEB-INF/classes/config/user.properties
        chown nobody.nobody $CMX_BASE_DIR/apache-tomcat/webapps/cmx-cloud-server/WEB-INF/classes/config/user.properties
    fi
    if [ -f $CMX_BASE_BACKUP_DIR/apache-tomcat-sdk/user.properties ]; then
        cp -pf $CMX_BASE_BACKUP_DIR/apache-tomcat-sdk/user.properties $CMX_BASE_DIR/apache-tomcat-sdk/webapps/cmx-cloud-server-sdk/WEB-INF/classes/config/user.properties
        chown nobody.nobody $CMX_BASE_DIR/apache-tomcat-sdk/webapps/cmx-cloud-server-sdk/WEB-INF/classes/config/user.properties
    fi
    echo "Starting to upgrade Cisco CMX Mobile App Server data: `date`"
    echo "Starting to upgrade Cisco CMX Mobile App Server data: `date`" >> $INSTALL_LOG_FILE 2>&1
    /etc/init.d/cmx-mobile-app-server startredis >> $INSTALL_LOG_FILE 2>&1
    pushd $CMX_BASE_DIR/upgrade/ >> $INSTALL_LOG_FILE 2>&1
    $CMX_BASE_DIR/redis/bin/redis-cli eval "$(cat upgrade_data)" 0 >> $INSTALL_LOG_FILE 2>&1
    popd >> $INSTALL_LOG_FILE 2>&1
    /etc/init.d/cmx-mobile-app-server stop >> $INSTALL_LOG_FILE 2>&1
    /etc/init.d/cmx-mobile-app-server start >> $INSTALL_LOG_FILE 2>&1
    rm -rf $CMX_BASE_BACKUP_DIR
    echo "Completed Cisco CMX Mobile App Server upgrade: `date`" >> $INSTALL_LOG_FILE 2>&1
    chown nobody.nobody $CMX_BASE_DIR/logs/*
    echo "Completed Cisco CMX Mobile App Server upgrade: `date`"
    echo ""
    echo "Setup script does not have to be run."
    echo "Server was upgraded from previous version."
    echo ""
    echo "Documentation for the server is located at"
    echo "'doc/README'"
    echo ""
    echo "Release notes for changes is located at"
    echo "'doc/ReleaseNotes'"
fi
