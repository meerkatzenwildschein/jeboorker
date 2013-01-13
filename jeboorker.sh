#!/bin/sh
cd `dirname $(readlink -f $0)`
java -client -Xmx512m -XX:+UseParNewGC -XX:MaxHeapFreeRatio=20 -XX:MinHeapFreeRatio=10 -XX:+AggressiveOpts -XX:+OptimizeStringConcat $_JCONSOLE -cp lib/jeboorker.jar:lib/jrcommons.jar:lib/jrswingcommons.jar:lib/junique-1.0.4.jar org.rr.jeborker.Jeboorker $@