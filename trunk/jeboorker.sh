#!/bin/sh
cd `dirname $(readlink -f $0)`

javaBin="/usr/bin/java";
for entry in `update-alternatives --list java`; do 

	case "$entry" in
		*"java-7"* )
		    javaBin="$entry"
			break;
		    ;;
		*)
	esac
	case "$entry" in
		*"jdk1.7"* )
		    javaBin="$entry"
			break;
		    ;;
		*)
	esac

done

$javaBin -client -Xmx512m -XX:+UseParNewGC -XX:MaxHeapFreeRatio=20 -XX:MinHeapFreeRatio=10 -XX:+AggressiveOpts $_JCONSOLE -cp lib/jeboorker.jar:lib/jrcommons.jar:lib/jrswingcommons.jar:lib/junique-1.0.4.jar org.rr.jeborker.Jeboorker $@