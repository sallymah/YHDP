@ECHO OFF

REM =======�O��OP_MENU�h��׃��==============
SET OPMENU_DRIVE=C:
SET OPMENU_PATH=\THIG_PROD\execsh\op_menu

REM =======�ГQ����Ŀ�=====================
%OPMENU_DRIVE%
cd \
cd %OPMENU_PATH%

REM =======�O��ϵ�y�h��=====================
call profile.bat

cd \
cd %WORK_DIR%

set sysdate=%1
REM ץȡϵ�y���� 
for /f "tokens=1-3 delims=/- " %%a in ('date /t') do set sysdate1=%%a%%b%%c
if "%sysdate%"=="" set sysdate=%sysdate1%

ECHO wscript.echo dateadd("d",-1,date) >%OPMENU_LOG_DIR%\tmp.vbs  
for /f "tokens=1-3 delims=/- " %%a in ('cscript /nologo %OPMENU_LOG_DIR%\tmp.vbs') do set yesterday=%%a%%b%%c

set LOGFILE=%OPMENU_LOG_DIR%\File_job_%sysdate%.log

@ECHO ON
ECHO "����ϵ�y����:%sysdate%"  >> %LOGFILE%


@ECHO ON
ECHO "�≺�s Begin--------------------->" >> %LOGFILE%
ECHO "-- �≺�s" >> %LOGFILE%
call C:\Windows\batch_unzip.bat >> %LOGFILE%


@ECHO ON
ECHO "�n�����I End --------------------->" >> %LOGFILE%