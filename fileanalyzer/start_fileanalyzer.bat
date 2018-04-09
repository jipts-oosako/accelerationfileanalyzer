@echo off
cd fileanalyzer
set JAR_NAME=accelerationfileanalyzer-1.0.0.jar
set LOGCONF=-Dlog4j.configurationFile=./log4j2.xml
start "" /B javaw %LOGCONF% -jar %JAR_NAME%
goto End
:End
