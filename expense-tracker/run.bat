@echo off
rem === Configure paths ===
set "JAVAFX_LIB=C:\javafx-sdk-21.0.9\lib"

rem Our app jar + all dependency jars
set "CP=target\expense-tracker-1.0-SNAPSHOT.jar;target\dependency\*"

rem === Run the JavaFX application ===
"C:\Program Files\Eclipse Adoptium\jdk-17.0.17.10-hotspot\bin\java.exe" ^
 --module-path "%JAVAFX_LIB%" ^
 --add-modules javafx.controls,javafx.fxml,javafx.swing ^
 -cp "%CP%" ^
 com.expensetracker.Main

pause
