#!/usr/bin/env bash
export ANDROID_HOME=`find /nix/store/ -name *android-21`
echo "first: " $ANDROID_HOME
#export ANDROID_HOME=`find /nix/store/ -name *android-sdk*`
export ANDROID_HOME=`echo $ANDROID_HOME | awk '{print $1;}'`
echo `echo this is $ANDROID_HOME`
export ANDROID_HOME=`echo $ANDROID_HOME | sed "s#/libexec.*##g"`
echo `echo this is $ANDROID_HOME`
export ANDROID_HOME="$ANDROID_HOME/libexec"
echo `echo this is $ANDROID_HOME`
#export ANDROID_HOME=`find /nix/store -name "*android-platform-5*" -not -name "*.drv"`
#export ANDROID_HOME="$ANDROID_HOME/android-5.0.1"
echo $ANDROID_HOME
#echo `ls $ANDROID_HOME`
gradle -g `pwd`/gradle-files $@
