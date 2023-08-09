#!/bin/sh
cd `dirname $(readlink -f $0)`

JAVABIN="/usr/bin/java";
CLASSPATH=$(echo lib/*.jar | tr ' ' '\n' | sort -g | tr '\n' ':')

echo using java binary $JAVABIN
echo starting with classpath $CLASSPATH
$JAVABIN -splash:splashscreen.gif -client -Xmx512m -XX:MaxHeapFreeRatio=20 -XX:MinHeapFreeRatio=10 -Djava.library.path=./lib/ --add-exports=java.base/sun.nio.ch=ALL-UNNAMED --add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.lang.reflect=ALL-UNNAMED --add-opens=java.base/java.io=ALL-UNNAMED --add-exports=jdk.unsupported/sun.misc=ALL-UNNAMED --add-opens=java.desktop/javax.swing=ALL-UNNAMED $_JCONSOLE -cp $CLASSPATH org.rr.jeborker.Jeboorker $@