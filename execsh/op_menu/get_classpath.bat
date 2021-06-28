@ECHO OFF

REM First entry in classpath is the Squirrel application
cd \
cd %WORK_DIR%\bin
SET PRJ_BIN_CP=

FOR /R %%D in (*.jar) DO (
  call :processbin %%D  
)
goto :next2

:processbin
IF !%1==! goto :end
if "%PRJ_BIN_CP%"=="" set PRJ_BIN_CP=%1& goto :end
set PRJ_BIN_CP=%PRJ_BIN_CP%;%1
goto :end

:next2
cd \
cd %WORK_DIR%\lib
SET PRJ_LIB_CP=
FOR /R %%D in (*.jar) DO (
  call :processlib %%D  
)
:processlib
IF !%1==! goto :end
if "%PRJ_LIB_CP%"=="" set PRJ_LIB_CP=%1& goto :end
set PRJ_LIB_CP=%PRJ_LIB_CP%;%1
goto :end

:end

set PRJ_CP=.;%PRJ_BIN_CP%;%PRJ_LIB_CP%