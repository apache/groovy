@if "%DEBUG%" == "" @echo off
@rem ##########################################################################
@rem                                                                         ##
@rem  Groovy JVM Bootstrap for Windowz                                       ##
@rem                                                                         ##
@rem ##########################################################################

@rem 
@rem $Revision$ $Date$
@rem 

@rem Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" setlocal

@rem The version of classworlds to boot with
set CLASSWORLDS_VERSION=1.0

:begin
@rem Determine what directory it is in.
set DIRNAME=%~dp0
if "%DIRNAME%" == "" set DIRNAME=.\

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

echo.
echo ERROR: Environment variable JAVA_HOME has not been set.
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.
echo.
goto end

:have_JAVA_HOME
@rem Validate JAVA_HOME
%COMMAND_COM% /C DIR "%JAVA_HOME%" 2>&1 | %FIND_EXE% /I /C "%JAVA_HOME%" >nul
if not errorlevel 1 goto check_GROOVY_HOME

echo.
echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME%
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.
echo.
goto end

:check_GROOVY_HOME
@rem Define GROOVY_HOME if not set
if "%GROOVY_HOME%" == "" set GROOVY_HOME=%DIRNAME%..

:init
@rem Get command-line arguments, handling Windowz variants
if not "%OS%" == "Windows_NT" goto win9xME_args
if "%eval[2+2]" == "4" goto 4NT_args

@rem Regular WinNT shell
set CMD_LINE_ARGS=%*
goto execute

:win9xME_args
@rem Slurp the command line arguments.  This loop allows for an unlimited number
set CMD_LINE_ARGS=

:win9xME_args_slurp
if "x%1" == "x" goto execute
set CMD_LINE_ARGS=%CMD_LINE_ARGS% %1
shift
goto win9xME_args_slurp

:4NT_args
@rem Get arguments from the 4NT Shell from JP Software
set CMD_LINE_ARGS=%$

:execute
@rem Setup the command line
set CLASSWORLDS_CLASSPATH=%GROOVY_HOME%\lib\classworlds-%CLASSWORLDS_VERSION%.jar;%CLASSPATH%
set CLASSWORLDS_MAIN_CLASS=org.codehaus.classworlds.Launcher
set CLASSWORLDS_CONF=%GROOVY_HOME%\conf\groovyc-classworlds.conf

set JAVA_EXE=%JAVA_HOME%\bin\java.exe
set TOOLS_JAR=%JAVA_HOME%\lib\tools.jar

if "%JAVA_OPTS%" == "" set JAVA_OPTS="-Xmx128m"
set JAVA_OPTS=%JAVA_OPTS% -Dprogram.name="%PROGNAME%"
set JAVA_OPTS=%JAVA_OPTS% -Dgroovy.home="%GROOVY_HOME%"
set JAVA_OPTS=%JAVA_OPTS% -Dtools.jar="%TOOLS_JAR%"
set JAVA_OPTS=%JAVA_OPTS% -Dclassworlds.conf="%CLASSWORLDS_CONF%"

@rem Execute Groovy
"%JAVA_EXE%" %JAVA_OPTS% -classpath "%CLASSWORLDS_CLASSPATH%" %CLASSWORLDS_MAIN_CLASS% %CMD_LINE_ARGS%

:end
@rem End local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" endlocal

@rem Optional pause the batch file
if "%GROOVY_BATCH_PAUSE%" == "on" pause

