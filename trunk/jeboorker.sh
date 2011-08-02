#!/bin/sh
java -Xmx256m $_JCONSOLE -cp lib/*:lib/orientdb/*:lib/epubcheck/* org.rr.jeborker.JEBorker $@
