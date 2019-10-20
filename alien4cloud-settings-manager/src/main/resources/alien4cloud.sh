#!/usr/bin/env bash

if [ ! -z "$JAVA_HOME" ] ; then
    JAVA=$JAVA_HOME/bin/java
else
    JAVA=`which java`
fi

JAVA_XMX_MEMO='4g'
if [ -z $1 ]
    then
        echo "No Java Xmx memory value supplied from command line; using ${JAVA_XMX_MEMO}"
else
    $JAVA -Xmx$1 -version
    retval=$?
    if [ $retval -eq 0 ]; then
        echo "Setting Java Xmx to $1"
        JAVA_XMX_MEMO=$1
    else
        echo "Error in the value of Xmx; Please provide a valid value"
        exit 1
    fi
fi

if [ ! -x "$JAVA" ] ; then
  echo Cannot find java. Set JAVA_HOME or add java to path.
  exit 1
fi

if [[ ! `ls alien4cloud-standalone.war 2> /dev/null` ]] ; then
  if [[ ! `ls alien4cloud-standalone/alien4cloud-standalone.war 2> /dev/null` ]] ; then
    echo Command must be run from the directory where the WAR is installed or its parent.
    exit 4
  fi
  cd alien4cloud-standalone
fi

if [ -z "$JAVA_OPTIONS" ] ; then
    JAVA_OPTIONS="-showversion -XX:+AggressiveOpts -Xmx${JAVA_XMX_MEMO} -Xms4g -XX:+HeapDumpOnOutOfMemoryError -XX:CMSInitiatingOccupancyFraction=75 -XX:+UseCMSInitiatingOccupancyOnly -XX:+ScavengeBeforeFullGC -XX:+CMSScavengeBeforeRemark -XX:+DisableExplicitGC"
fi

# $JAVA_EXT_OPTIONS are extended JVM options than can be filled before A4C start
$JAVA $JAVA_OPTIONS $JAVA_EXT_OPTIONS \
    -cp config/:alien4cloud-standalone.war -Ddebug  -Dspring.profiles.active=oidc-auth \
    org.springframework.boot.loader.WarLauncher 
    "$@"
