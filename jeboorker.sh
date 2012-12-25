#!/bin/sh
cd `dirname $(readlink -f $0)`
java -Xmx256m -XX:+AggressiveOpts -XX:+OptimizeStringConcat $_JCONSOLE -cp lib/jeboorker.jar:lib/junique-1.0.4.jar org.rr.jeborker.Jeboorker $@
