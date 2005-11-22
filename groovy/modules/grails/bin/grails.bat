@echo off

if "%JAVA_HOME%"=="" goto javaHomeNotSet
if "%ANT_HOME%"=="" goto antHomeNotSet
if "%GRAILS_HOME"=="" goto grailsHomeNotSet
goto getArguments

:javaHomeNotSet
echo Error: JAVA_HOME is not defined
echo Please define JAVA_HOME and start Grails again
goto errorExit

:antHomeNotSet
echo Error: ANT_HOME is not defined
echo Please define ANT_HOME and start Grails again
goto errorExit

:grailsHomeNotSet
echo Error: GRAILS_HOME is not defined
echo Please defined GRAILS_HOME and start Grails again
goto errorExit

:getArguments
set GRAILS_ARGUMENTS=%1
if ""%1""=="""" goto getClasspath
shift
:loopArguments
if ""%1""=="""" goto getClasspath
set GRAILS_ARGUMENTS=%GRAILS_ARGUMENTS% %1
shift
goto loopArguments

:getClasspath
set GRAILS_ANT_CLASSPATH="%GRAILS_HOME%\lib\bsf.jar;%GRAILS_HOME%\lib\groovy-all-1.0-jsr-05-SNAPSHOT.jar;%GRAILS_HOME%\lib\org.mortbay.jetty.jar;%GRAILS_HOME%\lib\commons-logging.jar;%GRAILS_HOME%\lib\commons-el.jar;%GRAILS_HOME%\lib\javax.servlet.jar;%GRAILS_HOME%\lib\log4j-1.2.8.jar;%GRAILS_HOME%\lib\jasper-compiler.jar;%GRAILS_HOME%\lib\jasper-runtime.jar"
goto startGrails

:startGrails
call %ANT_HOME%\bin\ant.bat -lib %GRAILS_ANT_CLASSPATH% -f %GRAILS_HOME%\src\grails\build.xml -Dbasedir=%CD% %GRAILS_ARGUMENTS%

:errorExit

