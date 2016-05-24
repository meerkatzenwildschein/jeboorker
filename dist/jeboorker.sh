#!/bin/sh
cd `dirname $(readlink -f $0)`

JAVABIN="/usr/bin/java";
CLASSPATH=$(echo lib/*.jar | tr ' ' ':')

echo using java binary $JAVABIN
echo starting with classpath $CLASSPATH
$JAVABIN -splash:splashscreen.gif -client -Xmx512m -XX:+UseParNewGC -XX:MaxHeapFreeRatio=20 -XX:MinHeapFreeRatio=10 -XX:+AggressiveOpts $_JCONSOLE -cp $CLASSPATH org.rr.jeborker.Jeboorker $@