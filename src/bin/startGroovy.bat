@REM ----------------------------------------------------------------------------
@REM Licensed to the Apache Software Foundation (ASF) under one
@REM or more contributor license agreements.  See the NOTICE file
@REM distributed with this work for additional information
@REM regarding copyright ownership.  The ASF licenses this file
@REM to you under the Apache License, Version 2.0 (the
@REM "License"); you may not use this file except in compliance
@REM with the License.  You may obtain a copy of the License at
@REM
@REM    http://www.apache.org/licenses/LICENSE-2.0
@REM
@REM Unless required by applicable law or agreed to in writing,
@REM software distributed under the License is distributed on an
@REM "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
@REM KIND, either express or implied.  See the License for the
@REM specific language governing permissions and limitations
@REM under the License.
@REM ----------------------------------------------------------------------------

@if "%DEBUG%" == "" @echo off
@rem ##########################################################################
@rem                                                                         ##
@rem  Groovy JVM Bootstrap for Windows                                       ##
@rem                                                                         ##
@rem ##########################################################################

@rem Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" setlocal enabledelayedexpansion

set DIRNAME=%~1
shift

set CLASS=%~1
shift

if exist "%USERPROFILE%/.groovy/preinit.bat" call "%USERPROFILE%/.groovy/preinit.bat"

@rem Determine the command interpreter to execute the "CD" later
set COMMAND_COM="cmd.exe"
if exist "%SystemRoot%\system32\cmd.exe" set COMMAND_COM="%SystemRoot%\system32\cmd.exe"
if exist "%SystemRoot%\command.com" set COMMAND_COM="%SystemRoot%\command.com"

@rem Use explicit find.exe to prevent cygwin and others find.exe from being used
set FIND_EXE="find.exe"
if exist "%SystemRoot%\system32\find.exe" set FIND_EXE="%SystemRoot%\system32\find.exe"
if exist "%SystemRoot%\command\find.exe" set FIND_EXE="%SystemRoot%\command\find.exe"

:check_JAVA_HOME
@rem Make sure we have a valid JAVA_HOME
if not "%JAVA_HOME%" == "" goto have_JAVA_HOME
set PATHTMP=%PATH%
:loop
for /f "delims=; tokens=1*" %%i in ("!PATHTMP!") do (
    if exist "%%i\..\bin\java.exe" (
        set "JAVA_HOME=%%i\.."
        goto found_JAVA_HOME
    )
    set PATHTMP=%%j
    goto loop
)
goto check_default_JAVA_EXE

:found_JAVA_HOME
@rem Remove trailing \bin\.. from JAVA_HOME
if "%JAVA_HOME:~-7%"=="\bin\.." SET "JAVA_HOME=%JAVA_HOME:~0,-7%"
set JAVA_EXE=%JAVA_HOME%\bin\java.exe

:check_default_JAVA_EXE
if not "%JAVA_HOME%" == "" goto valid_JAVA_HOME
java -version 2>NUL
if not ERRORLEVEL 1 goto default_JAVA_EXE

echo.
echo ERROR: Environment variable JAVA_HOME has not been set.
echo Attempting to find JAVA_HOME from PATH also failed.
goto common_error

:have_JAVA_HOME
@rem Remove trailing slash from JAVA_HOME if found
if "%JAVA_HOME:~-1%"=="\" SET JAVA_HOME=%JAVA_HOME:~0,-1%

@rem Validate JAVA_HOME
%COMMAND_COM% /C DIR "%JAVA_HOME%" 2>&1 | %FIND_EXE% /I /C "%JAVA_HOME%" >nul
if not errorlevel 1 goto valid_JAVA_HOME_DIR

echo.
echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME%

:common_error
echo Please set the JAVA_HOME variable in your environment
echo to match the location of your Java installation.
goto end

:default_JAVA_EXE
set JAVA_EXE=java.exe
goto check_GROOVY_HOME

:valid_JAVA_HOME_DIR
set JAVA_EXE=%JAVA_HOME%\bin\java.exe
if exist "%JAVA_EXE%" goto valid_JAVA_HOME

echo.
echo ERROR: No java.exe found at: %JAVA_EXE%
goto common_error

:valid_JAVA_HOME
if exist "%JAVA_HOME%\lib\tools.jar" set TOOLS_JAR=%JAVA_HOME%\lib\tools.jar

:check_GROOVY_HOME
@rem Define GROOVY_HOME if not set
if "%GROOVY_HOME%" == "" set GROOVY_HOME=%DIRNAME%..

@rem Remove trailing slash from GROOVY_HOME if found
if "%GROOVY_HOME:~-1%"=="\" SET GROOVY_HOME=%GROOVY_HOME:~0,-1%

@rem classpath handling
set _SKIP=2
set CP=
if "x%~1" == "x-cp" set CP=%~2
if "x%~1" == "x-classpath" set CP=%~2
if "x%~1" == "x--classpath" set CP=%~2
if "x" == "x%CP%" goto preview_check
set _SKIP=4
shift
shift

:preview_check
@rem classpath handling
set PREV=
set REPLACE_PREVIEW=
if "x%~1" == "x-pr" goto preview_enable
if "x%~1" == "x--enable-preview" goto preview_enable
goto init

:preview_enable
set JAVA_OPTS=%JAVA_OPTS% --enable-preview -Dgroovy.preview.features=true
@rem for now, also remember arg to pass through
set REPLACE_PREVIEW=--enable-preview
if "%_SKIP%" == "4" set _SKIP=5
if "%_SKIP%" == "2" set _SKIP=3
shift

:init
@rem get name of script to launch with full path
set GROOVY_SCRIPT_NAME=%~f1
@rem Get command-line arguments, handling Windows variants
if not "%OS%" == "Windows_NT" goto win9xME_args
if "%eval[2+2]" == "4" goto 4NT_args

:win9xME_args
@rem Slurp the command line arguments.
set CMD_LINE_ARGS=

:win9xME_args_slurp
if "x%~1" == "x" goto execute

rem horrible roll your own arg processing inspired by jruby equivalent

rem escape minus (-d), quotes (-q), star (-s).
set _ARGS=%*
if not defined _ARGS goto execute
set _ARGS=%_ARGS:-=-d%
set _ARGS=%_ARGS:"=-q%
set _ARGS=%_ARGS:?=-n%
rem Windows will try to match * with files so we escape it here
rem but it is also a meta char for env var string substitution
rem so it can't be first char here, hack just for common cases.
rem If in doubt use a space or bracket before * if using -e.
set _ARGS=%_ARGS: *= -s%
set _ARGS=%_ARGS:)*=)-s%
set _ARGS=%_ARGS:0*=0-s%
set _ARGS=%_ARGS:1*=1-s%
set _ARGS=%_ARGS:2*=2-s%
set _ARGS=%_ARGS:3*=3-s%
set _ARGS=%_ARGS:4*=4-s%
set _ARGS=%_ARGS:5*=5-s%
set _ARGS=%_ARGS:6*=6-s%
set _ARGS=%_ARGS:7*=7-s%
set _ARGS=%_ARGS:8*=8-s%
set _ARGS=%_ARGS:9*=9-s%

rem prequote all args for 'for' statement
set _ARGS="%_ARGS%"

set _ARG=
:win9xME_args_loop
rem split args by spaces into first and rest
for /f "tokens=1,*" %%i in (%_ARGS%) do call :get_arg "%%i" "%%j"
goto process_arg

:get_arg
rem remove quotes around first arg and don't expand wildcards
for /f %%i in (%1) do set _ARG=%_ARG% %%~i
rem set the remaining args
set _ARGS=%2
rem remove the leading space we'll add the first time
if "x%_ARG:~0,1%" == "x " set _ARG=%_ARG:~1%
rem return
goto :EOF

:process_arg
if "%_ARG%" == "" goto execute

rem collect all parts of a quoted argument containing spaces
if not "%_ARG:~0,2%" == "-q" goto :argIsComplete
if "%_ARG:~-2%" == "-q" goto :argIsComplete
rem _ARG starts with a quote but does not end with one:
rem  add the next part to _ARG until the matching quote is found
goto :win9xME_args_loop

:argIsComplete
if "x5" == "x%_SKIP%" goto skip_5
if "x4" == "x%_SKIP%" goto skip_4
if "x3" == "x%_SKIP%" goto skip_3
if "x2" == "x%_SKIP%" goto skip_2
if "x1" == "x%_SKIP%" goto skip_1

rem now unescape -s, -q, -n, -d
rem -d must be the last to be unescaped
set _ARG=%_ARG:-s=*%
set _ARG=%_ARG:-q="%
set _ARG=%_ARG:-n=?%
set _ARG=%_ARG:-d=-%

set CMD_LINE_ARGS=%CMD_LINE_ARGS% %_ARG%
set _ARG=
goto win9xME_args_loop

:skip_5
set _ARG=
set _SKIP=4
goto win9xME_args_loop

:skip_4
set _ARG=
set _SKIP=3
goto win9xME_args_loop

:skip_3
set _ARG=
set _SKIP=2
goto win9xME_args_loop

:skip_2
set _ARG=
set _SKIP=1
goto win9xME_args_loop

:skip_1
set _ARG=
set _SKIP=0
goto win9xME_args_loop

:4NT_args
@rem Get arguments from the 4NT Shell from JP Software
set CMD_LINE_ARGS=%$

:execute
@rem Setup the command line
set STARTER_CLASSPATH=%GROOVY_HOME%\lib\@GROOVYJAR@

if exist "%USERPROFILE%/.groovy/init.bat" call "%USERPROFILE%/.groovy/init.bat"

@rem Setting a classpath using the -cp or -classpath option means not to use
@rem the global classpath. Groovy behaves then the same as the java
@rem interpreter
if "x" == "x%CP%" goto empty_cp
:non_empty_cp
set CP=%CP%;.
goto after_cp
:empty_cp
set CP=.
if "x" == "x%CLASSPATH%" goto after_cp
set CP=%CLASSPATH%;%CP%
:after_cp

set STARTER_MAIN_CLASS=org.codehaus.groovy.tools.GroovyStarter
set STARTER_CONF=%GROOVY_HOME%\conf\groovy-starter.conf

set GROOVY_OPTS=-Dprogram.name="%PROGNAME%"
set GROOVY_OPTS=%GROOVY_OPTS% -Dgroovy.home="%GROOVY_HOME%"
if not "%TOOLS_JAR%" == "" set GROOVY_OPTS=%GROOVY_OPTS% -Dtools.jar="%TOOLS_JAR%"
set GROOVY_OPTS=%GROOVY_OPTS% -Dgroovy.starter.conf="%STARTER_CONF%"
set GROOVY_OPTS=%GROOVY_OPTS% -Dscript.name="%GROOVY_SCRIPT_NAME%"

for /f "tokens=3" %%g in ('call "%JAVA_EXE%" -version 2^>^&1 ^| findstr /i "version"') do (
  SET JAVA_VERSION=%%g
)
for /f "useback tokens=*" %%a in ('%JAVA_VERSION%') do set JAVA_VERSION=%%~a
set JAVA_VERSION=%JAVA_VERSION:~0,5%
set ADD_MODULES_OPT=--add-modules
if "%JAVA_VERSION%" gtr "1.8.0" set JAVA_OPTS=%JAVA_OPTS% -Dgroovy.jaxb=jaxb
if "%JAVA_VERSION%" gtr "1.8.0" if "%GROOVY_TURN_OFF_JAVA_WARNINGS%" == "true"                 set JAVA_OPTS=%JAVA_OPTS% --add-opens=java.base/java.io=ALL-UNNAMED --add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.lang.annotation=ALL-UNNAMED --add-opens=java.base/java.lang.invoke=ALL-UNNAMED --add-opens=java.base/java.lang.module=ALL-UNNAMED --add-opens=java.base/java.lang.ref=ALL-UNNAMED --add-opens=java.base/java.lang.reflect=ALL-UNNAMED --add-opens=java.base/java.math=ALL-UNNAMED --add-opens=java.base/java.net=ALL-UNNAMED --add-opens=java.base/java.net.spi=ALL-UNNAMED --add-opens=java.base/java.nio=ALL-UNNAMED --add-opens=java.base/java.nio.channels=ALL-UNNAMED --add-opens=java.base/java.nio.channels.spi=ALL-UNNAMED --add-opens=java.base/java.nio.charset=ALL-UNNAMED --add-opens=java.base/java.nio.charset.spi=ALL-UNNAMED --add-opens=java.base/java.nio.file=ALL-UNNAMED --add-opens=java.base/java.nio.file.attribute=ALL-UNNAMED --add-opens=java.base/java.nio.file.spi=ALL-UNNAMED --add-opens=java.base/java.security=ALL-UNNAMED --add-opens=java.base/java.security.acl=ALL-UNNAMED --add-opens=java.base/java.security.cert=ALL-UNNAMED --add-opens=java.base/java.security.interfaces=ALL-UNNAMED --add-opens=java.base/java.security.spec=ALL-UNNAMED --add-opens=java.base/java.text=ALL-UNNAMED --add-opens=java.base/java.text.spi=ALL-UNNAMED --add-opens=java.base/java.time=ALL-UNNAMED --add-opens=java.base/java.time.chrono=ALL-UNNAMED --add-opens=java.base/java.time.format=ALL-UNNAMED --add-opens=java.base/java.time.temporal=ALL-UNNAMED --add-opens=java.base/java.time.zone=ALL-UNNAMED --add-opens=java.base/java.util=ALL-UNNAMED --add-opens=java.base/java.util.concurrent=ALL-UNNAMED --add-opens=java.base/java.util.concurrent.atomic=ALL-UNNAMED --add-opens=java.base/java.util.concurrent.locks=ALL-UNNAMED --add-opens=java.base/java.util.function=ALL-UNNAMED --add-opens=java.base/java.util.jar=ALL-UNNAMED --add-opens=java.base/java.util.regex=ALL-UNNAMED --add-opens=java.base/java.util.spi=ALL-UNNAMED --add-opens=java.base/java.util.stream=ALL-UNNAMED --add-opens=java.base/java.util.zip=ALL-UNNAMED --add-opens=java.datatransfer/java.awt.datatransfer=ALL-UNNAMED --add-opens=java.desktop/java.applet=ALL-UNNAMED --add-opens=java.desktop/java.awt=ALL-UNNAMED --add-opens=java.desktop/java.awt.color=ALL-UNNAMED --add-opens=java.desktop/java.awt.desktop=ALL-UNNAMED --add-opens=java.desktop/java.awt.dnd=ALL-UNNAMED --add-opens=java.desktop/java.awt.dnd.peer=ALL-UNNAMED --add-opens=java.desktop/java.awt.event=ALL-UNNAMED --add-opens=java.desktop/java.awt.font=ALL-UNNAMED --add-opens=java.desktop/java.awt.geom=ALL-UNNAMED --add-opens=java.desktop/java.awt.im=ALL-UNNAMED --add-opens=java.desktop/java.awt.im.spi=ALL-UNNAMED --add-opens=java.desktop/java.awt.image=ALL-UNNAMED --add-opens=java.desktop/java.awt.image.renderable=ALL-UNNAMED --add-opens=java.desktop/java.awt.peer=ALL-UNNAMED --add-opens=java.desktop/java.awt.print=ALL-UNNAMED --add-opens=java.desktop/java.beans=ALL-UNNAMED --add-opens=java.desktop/java.beans.beancontext=ALL-UNNAMED --add-opens=java.instrument/java.lang.instrument=ALL-UNNAMED --add-opens=java.logging/java.util.logging=ALL-UNNAMED --add-opens=java.management/java.lang.management=ALL-UNNAMED --add-opens=java.prefs/java.util.prefs=ALL-UNNAMED --add-opens=java.rmi/java.rmi=ALL-UNNAMED --add-opens=java.rmi/java.rmi.activation=ALL-UNNAMED --add-opens=java.rmi/java.rmi.dgc=ALL-UNNAMED --add-opens=java.rmi/java.rmi.registry=ALL-UNNAMED --add-opens=java.rmi/java.rmi.server=ALL-UNNAMED --add-opens=java.sql/java.sql=ALL-UNNAMED  --add-opens=java.desktop/javax.swing=ALL-UNNAMED --add-opens=java.desktop/javax.swing.border=ALL-UNNAMED --add-opens=java.desktop/javax.swing.text=ALL-UNNAMED --add-opens=java.desktop/javax.swing.text.html=ALL-UNNAMED --add-opens=java.desktop/sun.awt.windows=ALL-UNNAMED --add-opens=java.desktop/sun.awt=ALL-UNNAMED --add-opens=java.desktop/sun.java2d=ALL-UNNAMED --add-opens=java.desktop/sun.font=ALL-UNNAMED

if exist "%USERPROFILE%/.groovy/postinit.bat" call "%USERPROFILE%/.groovy/postinit.bat"

@rem Execute Groovy
"%JAVA_EXE%" %GROOVY_OPTS% %JAVA_OPTS% -classpath "%STARTER_CLASSPATH%" %STARTER_MAIN_CLASS% --main %CLASS% --conf "%STARTER_CONF%" --classpath "%CP%" %REPLACE_PREVIEW% %CMD_LINE_ARGS%

:end
@rem End local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" endlocal

@rem Optional pause the batch file
if "%GROOVY_BATCH_PAUSE%" == "on" pause
%COMSPEC% /C exit /B %ERRORLEVEL%
