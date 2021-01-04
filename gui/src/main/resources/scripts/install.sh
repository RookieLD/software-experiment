#!/bin/bash

#在用户家目录下建立.local/share/qarchiver文件夹，将程序的二进制文件放入，定时备份的cron任务会用到
#建立/etc/cron.d/qarchiver 这是一个cron任务，用于定时备份

USER_HOME=$(getent passwd "$USER" | cut -d : -f 6)
CONFIG_HOME="$USER_HOME/.config/qarchiver"

mkdir $CONFIG_HOME;
mkdir "$CONFIG_HOME/keys"
mkdir "$CONFIG_HOME/script"
