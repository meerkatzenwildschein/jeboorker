Option Explicit

Dim strArguments, wshShell

strArguments = "java -Xmx256m -XX:+AggressiveOpts -Djava.class.path=.\lib\jeboorker.jar;.\lib\junique-1.0.4.jar org.rr.jeborker.Jeboorker"

Set wshShell = CreateObject( "WScript.Shell" )
wshShell.Run Trim( strArguments ), 0, False
Set wshShell = Nothing
