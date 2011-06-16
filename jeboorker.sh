#!/bin/sh
java -Xmx256m $_JCONSOLE -cp lib/*:lib/orientdb/* org.rr.jeborker.JEBorker $@
