#/bin/sh
CMX_BASE_DIR=/opt/cmx-mobile-app-server
INSTALL_LOG_FILE=$CMX_BASE_DIR/install/install.log
#
# First parameter equal to 0 indicates the RPM is being removed and not upgraded
#
if [ "$1" = 0 ]; then
    echo "Starting to remove Cisco CMX Mobile App Server: `date`"
    echo "Starting to remove Cisco CMX Mobile App Server: `date`" >> $INSTALL_LOG_FILE 2>&1
    /etc/init.d/cmx-mobile-app-server stop
fi