@echo off

REM
REM Copyright  2001-2002,2004-2005 The Apache Software Foundation
REM
REM  Licensed under the Apache License, Version 2.0 (the "License");
REM  you may not use this file except in compliance with the License.
REM  You may obtain a copy of the License at
REM
REM      http://www.apache.org/licenses/LICENSE-2.0
REM
REM  Unless required by applicable law or agreed to in writing, software
REM  distributed under the License is distributed on an "AS IS" BASIS,
REM  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
REM  See the License for the specific language governing permissions and
REM  limitations under the License.
REM
REM

if "%OS%"=="Windows_NT" @setlocal
if "%OS%"=="WINNT" @setlocal

if ""%1""=="""" goto runCommand

rem Change drive and directory to %1
if "%OS%"=="Windows_NT" cd /d ""%1""
if not "%OS%"=="Windows_NT" cd ""%1""
shift

rem Slurp the command line arguments. This loop allows for an unlimited number
rem of agruments (up to the command line limit, anyway).
set ANT_RUN_CMD=%1
if ""%1""=="""" goto runCommand
shift
:loop
if ""%1""=="""" goto runCommand
set ANT_RUN_CMD=%ANT_RUN_CMD% %1
shift
goto loop

:runCommand
rem echo %ANT_RUN_CMD%
%ANT_RUN_CMD%

if "%OS%"=="Windows_NT" @endlocal
if "%OS%"=="WINNT" @endlocal

