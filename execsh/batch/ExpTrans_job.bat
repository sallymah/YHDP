@ECHO OFF

REM =======設定OP_MENU環境?數==============
SET OPMENU_DRIVE=C:
SET OPMENU_PATH=\THIG_PROD\execsh\op_menu

REM =======切換工作目?=====================
%OPMENU_DRIVE%
cd \
cd %OPMENU_PATH%

REM =======設定系統環境=====================
call profile.bat

cd \
cd %WORK_DIR%


set sysdate=%1
REM 抓取系統日期 
for /f "tokens=1-3 delims=/- " %%a in ('date /t') do set sysdate1=%%a%%b%%c
if "%sysdate%"=="" set sysdate=%sysdate1%

ECHO wscript.echo dateadd("d",-1,date) >%OPMENU_LOG_DIR%\tmp.vbs  
for /f "tokens=1-3 delims=/- " %%a in ('cscript /nologo %OPMENU_LOG_DIR%\tmp.vbs') do set yesterday=%%a%%b%%c

set LOGFILE=%OPMENU_LOG_DIR%\Daily_job_%sysdate%.log

@ECHO ON
ECHO "執行系統日期:%sysdate%"  >> %LOGFILE%

@ECHO ON
ECHO "-- 匯出ExpAmendTrans資料檔 " >> %LOGFILE%
call ant -f runbatch.xml runExpAmendTrans -Ddate=20140531 -DallDate=true >> %LOGFILE%

@ECHO ON
ECHO "批次作業 End --------------------->" >> %LOGFILE%