#!/bin/sh
cd `dirname $(readlink -f $0)`

JAVABIN="/usr/bin/java";
CLASSPATH=$(echo lib/*.jar | tr ' ' ':')

if [ -f /usr/bin/update-alternatives ]
then
for entry in `update-alternatives --list java`; do 

	case "$entry" in
		*"java-7"* )
		    JAVABIN="$entry"
			break;
		    ;;
		*)
	esac
	case "$entry" in
		*"jdk1.7"* )
		    JAVABIN="$entry"
			break;
		    ;;
		*)
	esac

done
fi

echo using java binary $JAVABIN
echo starting with classpath $CLASSPATH
$JAVABIN -client -Xmx512m -XX:+UseParNewGC -XX:MaxHeapFreeRatio=20 -XX:MinHeapFreeRatio=10 -XX:+AggressiveOpts $_JCONSOLE -cp $CLASSPATH org.rr.jeborker.Jeboorker $@