#!/usr/bin/env bash

if [ ! -z "$JAVA_HOME" ] ; then
    JAVA=$JAVA_HOME/bin/java
else
    JAVA=`which java`
fi

if [ ! -x "$JAVA" ] ; then
  echo Cannot find java. Set JAVA_HOME or add java to path.
  exit 1
fi

if [[ ! `ls alien4cloud-ui-standalone.war 2> /dev/null` ]] ; then
  if [[ ! `ls alien4cloud-standalone/alien4cloud-ui-standalone.war 2> /dev/null` ]] ; then
    echo Command must be run from the directory where the WAR is installed or its parent.
    exit 4
  fi
  cd alien4cloud-standalone
fi

if [ -z "$JAVA_OPTIONS" ] ; then
    JAVA_OPTIONS="-server -showversion -XX:+AggressiveOpts -Xmx2g -Xms2g -XX:+HeapDumpOnOutOfMemoryError -XX:+UseParNewGC -XX:+UseConcMarkSweepGC -XX:CMSInitiatingOccupancyFraction=75 -XX:+UseCMSInitiatingOccupancyOnly -XX:+ScavengeBeforeFullGC -XX:+CMSScavengeBeforeRemark -XX:+DisableExplicitGC"
fi

# $JAVA_EXT_OPTIONS are extended JVM options than can be filled before A4C start
$JAVA $JAVA_OPTIONS $JAVA_EXT_OPTIONS \
    -cp config/:alien4cloud-ui-standalone.war \
    org.springframework.boot.loader.WarLauncher
    "$@"
