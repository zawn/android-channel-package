@echo off
rem don't modify the caller's environment
setlocal

rem Set up prog to be the path of this script, including following symlinks,
rem and set up progdir to be the fully-qualified pathname of its directory.
set prog=%~f0
set tools_dir=%~dp0

rem Grab current directory before we change it
set work_dir="%cd%"

rem Change current directory and drive to where the script is, to avoid
rem issues with directories containing whitespaces.
cd /d %~dp0

if "%ANT_HOME%"=="" set ANT_HOME=%tools_dir%\Tools\apache-ant-1.8.4

set PATH=%PATH%
set PATH=%PATH%;%ANT_HOME%\bin

rem Check we have a valid Java.exe in the path.
set java_exe=
call %tools_dir%\Tools\lib\find_java.bat
if not defined java_exe goto :EOF

:Build
call ant
:endBuild
pause
rem EOF
