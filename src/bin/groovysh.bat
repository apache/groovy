@if "%DEBUG%" == "" @echo off

@rem 
@rem $Id$
@rem

@rem Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" setlocal

:begin
@rem Determine what directory it is in.
set DIRNAME=%~dp0
if "%DIRNAME%" == "" set DIRNAME=.\

rem 
rem HACK: Temporary support to run the current or new shells
rem 

set CLASSNAME=org.codehaus.groovy.tools.shell.Main
if "%NEWSHELL%" == "" set CLASSNAME=groovy.ui.InteractiveShell

"%DIRNAME%\startGroovy.bat" "%DIRNAME%" %CLASSNAME% %*

@rem End local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" endlocal