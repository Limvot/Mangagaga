#!/usr/bin/env bash
export ANDROID_HOME=`find /nix/store/ -name *android-21*`
#export ANDROID_HOME=`find /nix/store/ -name *android-sdk*`
export ANDROID_HOME=`echo $ANDROID_HOME | awk '{print $1;}'`
export ANDROID_HOME=`echo $ANDROID_HOME | sed "s#/libexec.*##g"`
export ANDROID_HOME="$ANDROID_HOME/libexec"
#export ANDROID_HOME=`find /nix/store -name "*android-platform-5*" -not -name "*.drv"`
#export ANDROID_HOME="$ANDROID_HOME/android-5.0.1"
echo $ANDROID_HOME
#echo `ls $ANDROID_HOME`
gradle -g `pwd`/gradle-files $@
