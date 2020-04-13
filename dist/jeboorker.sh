#!/bin/sh
cd `dirname $(readlink -f $0)`

JAVABIN="/usr/bin/java";
CLASSPATH=$(echo lib/*.jar | tr ' ' '\n' | sort -g | tr '\n' ':')

echo using java binary $JAVABIN
echo starting with classpath $CLASSPATH
$JAVABIN -splash:splashscreen.gif -client -Xmx512m -XX:MaxHeapFreeRatio=20 -XX:MinHeapFreeRatio=10 $_JCONSOLE -cp $CLASSPATH org.rr.jeborker.Jeboorker $@