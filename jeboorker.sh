#!/bin/sh
cd `dirname $(readlink -f $0)`
java -Xmx256m $_JCONSOLE -jar lib/jeboorker.jar $@
