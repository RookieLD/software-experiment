#!/bin/bash
PASSWD="$(zenity --password --title=需要管理员权限)"
#echo $PASSWD|sudo -S su

if [ -f cronTemp ]; then
    rm qarchiver
fi

cp cronTaskTemp qarchiver

echo "$1    root    $2" >> qarchiver
echo $PASSWD|sudo -S mv qarchiver /etc/cron.d/
echo $PASSWD|sudo -S chown root:root /etc/cron.d/qarchiver

exit 0
