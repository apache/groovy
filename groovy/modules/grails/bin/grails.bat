@echo off

if "%JAVA_HOME%"=="" goto javaHomeNotSet
if "%ANT_HOME%"=="" goto antHomeNotSet
if "%GRAILS_HOME"=="" goto grailsHomeNotSet
goto :getArguments

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
if ""%1""="""" goto startGrails
shift
:loopArguments
if ""%1""="""" goto startGrails
set GRAILS_ARGUMENTS=%GRAILS_ARGUMENTS% %1
shift
goto loopArguments

:startGrails

call %ANT_HOME%\bin\ant.bat -f %GRAILS_HOME%\src\grails\build.xml %GRAILS_ARGUMENTS%

:errorExit

