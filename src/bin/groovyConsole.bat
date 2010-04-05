@if "%DEBUG%" == "" @echo off

@rem 
@rem $Revision$ $Date$
@rem 

@rem Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" setlocal

:begin
@rem Determine what directory it is in.
set DIRNAME=%~dp0
if "%DIRNAME%" == "" set DIRNAME=.\

"%DIRNAME%\startGroovy.bat" "%DIRNAME%" groovy.ui.Console %*

@rem End local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" endlocal
%COMSPEC% /C exit /B %ERRORLEVEL%