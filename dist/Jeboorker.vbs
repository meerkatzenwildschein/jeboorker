Option Explicit

Dim strArguments, wshShell

strArguments = "javaw -splash:splashscreen.gif -client -Xmx512m -XX:MaxHeapFreeRatio=20 -XX:MinHeapFreeRatio=10 -Djava.library.path=.\lib\ --add-exports=java.base/sun.nio.ch=ALL-UNNAMED --add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.lang.reflect=ALL-UNNAMED --add-opens=java.base/java.io=ALL-UNNAMED --add-exports=jdk.unsupported/sun.misc=ALL-UNNAMED --add-opens=java.desktop/javax.swing=ALL-UNNAMED -cp .\lib\* org.rr.jeborker.Jeboorker"

Set wshShell = CreateObject( "WScript.Shell" )
wshShell.Run Trim( strArguments ), 0, False
Set wshShell = Nothing
