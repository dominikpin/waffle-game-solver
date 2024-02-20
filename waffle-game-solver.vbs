Set objShell = CreateObject("WScript.Shell")
objShell.Run "java -jar --enable-preview --module-path lib\javaFX\lib --add-modules javafx.controls,javafx.fxml waffleGame.jar", 0, True