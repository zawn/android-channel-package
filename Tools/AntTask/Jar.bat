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

echo %tools_dir%
rem Check we have a valid Java.exe in the path.
set java_exe=
call %tools_dir%..\lib\find_java.bat
if not defined java_exe goto :EOF

if "%ANT_PATH%"=="" set ANT_PATH=%tools_dir%..\apache-ant-1.8.4

set PATH=%PATH%
set PATH=%PATH%;%ANT_PATH%\bin

:Build
call ant jar
:endBuild
pause
rem EOF
