@echo off
setlocal

rem ==========================================
rem CONFIGURATION
rem ==========================================

rem [IMPORTANT] Set the path to your JavaFX SDK lib folder here
rem If the path or version changes, you only need to update this one line.
set "JAVAFX_LIB_PATH=C:\javafx-sdk-21.0.9\lib"

rem ==========================================
rem JAVA DETECTION
rem ==========================================

rem Check if JAVA_HOME is set
if defined JAVA_HOME (
    set "JAVA_EXEC=%JAVA_HOME%\bin\java.exe"
) else (
    rem Fallback to system path
    set "JAVA_EXEC=java"
)

rem ==========================================
rem EXECUTION
rem ==========================================

echo [INFO] Using Java: "%JAVA_EXEC%"
echo [INFO] Using JavaFX: "%JAVAFX_LIB_PATH%"

if not exist "%JAVAFX_LIB_PATH%" (
    echo [ERROR] JavaFX SDK not found at: "%JAVAFX_LIB_PATH%"
    echo [ACTION] Please edit this script and update JAVAFX_LIB_PATH.
    pause
    exit /b 1
)

rem Define classpath (Application JAR + Dependencies)
set "CP=target\expense-tracker-1.0-SNAPSHOT.jar;target\dependency\*"

rem Run the application
"%JAVA_EXEC%" ^
 --module-path "%JAVAFX_LIB_PATH%" ^
 --add-modules javafx.controls,javafx.fxml,javafx.swing ^
 -cp "%CP%" ^
 com.expensetracker.Main

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] Application exited with error code %ERRORLEVEL%.
    pause
)

endlocal
