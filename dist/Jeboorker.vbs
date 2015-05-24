Option Explicit

Dim strArguments, wshShell

strArguments = "javaw -client -Xmx512m -XX:+UseParNewGC -XX:MaxHeapFreeRatio=20 -XX:MinHeapFreeRatio=10 -XX:+AggressiveOpts -cp .\lib\* org.rr.jeborker.Jeboorker"

Set wshShell = CreateObject( "WScript.Shell" )
wshShell.Run Trim( strArguments ), 0, False
Set wshShell = Nothing
