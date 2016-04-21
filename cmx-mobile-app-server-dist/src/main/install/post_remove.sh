#/bin/sh
CMX_BASE_DIR=/opt/cmx-mobile-app-server
#
# First parameter equal to 0 indicates the RPM is being removed and not upgraded
#
if [ "$1" = 0 ]; then
    rm -rf $CMX_BASE_DIR
    rm -rf /etc/init.d/cmx-mobile-app-server
    rm -rf /etc/rc0.d/K27cmx-mobile-app-server
    rm -rf /etc/rc1.d/K27cmx-mobile-app-server
    rm -rf /etc/rc2.d/S77cmx-mobile-app-server
    rm -rf /etc/rc3.d/S77cmx-mobile-app-server
    rm -rf /etc/rc4.d/S77cmx-mobile-app-server
    rm -rf /etc/rc5.d/S77cmx-mobile-app-server
    rm -rf /etc/rc6.d/K27cmx-mobile-app-server
    crontab -l > /tmp/crontab.out
    sed -e '/collectDebugInfo.sh/d' /tmp/crontab.out > /tmp/crontab.in
    crontab /tmp/crontab.in
    rm -f /tmp/crontab.out
    rm -f /tmp/crontab.in
    echo "Completed removing of Cisco CMX Mobile App Server: `date`"
fi