@ECHO OFF

REM =======設定OP_MENU環境變數==============
SET OPMENU_DRIVE=C:
SET OPMENU_PATH=\THIG_PROD\execsh\op_menu

REM =======切換工作目錄=====================
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

set LOGFILE=%OPMENU_LOG_DIR%\File_job_%sysdate%.log

@ECHO ON
ECHO "執行系統日期:%sysdate%"  >> %LOGFILE%

@ECHO ON
ECHO "檔案作業 Begin--------------------->" >> %LOGFILE%
ECHO "-- 匯入作業-FTP下載檔案" >> %LOGFILE%
call ant -f runbatch.xml runFtpIn -Ddate=%sysdate% >> %LOGFILE%

@ECHO ON
ECHO "-- 匯入作業-檔案整理" >> %LOGFILE%
call ant -f runbatch.xml runCangeLocalPath -Ddate=%sysdate% >> %LOGFILE%

@ECHO ON
ECHO "解壓縮 Begin--------------------->" >> %LOGFILE%
ECHO "-- 解壓縮" >> %LOGFILE%
call C:\Windows\batch_unzip.bat >> %LOGFILE%

cd \
cd %WORK_DIR%

@ECHO ON
ECHO "-- 匯入作業-檔案切檔"  >> %LOGFILE%     
call ant -f runbatch.xml runSplitImpTrans -Ddate=%sysdate% >> %LOGFILE%

@ECHO ON
ECHO "-- 匯入作業-匯入檔案"  >> %LOGFILE%         
call ant -f runbatch.xml runFilesIn -Ddate=%sysdate% >> %LOGFILE%

@ECHO ON
ECHO "-- 匯入作業-匯入批量交易檔"  >> %LOGFILE%         
call ant -f runbatch.xml runImpTrans -Ddate=%sysdate% >> %LOGFILE%

@ECHO ON
ECHO "-- 匯出ExpAmendCardData資料檔 " >> %LOGFILE%
call ant -f runbatch.xml runExpAmendCardData -Ddate=%sysdate% >> %LOGFILE%

@ECHO ON
ECHO "-- 匯出ExpAmendTrans資料檔 " >> %LOGFILE%
call ant -f runbatch.xml runExpAmendTrans -Ddate=%sysdate% >> %LOGFILE%

@ECHO ON
ECHO "-- 匯出ExpCardBal資料檔 " >> %LOGFILE%
call ant -f runbatch.xml runExpCardBal -Ddate=%sysdate% >> %LOGFILE%

@ECHO ON
ECHO "-- 匯出檔案" >> %LOGFILE%
call ant -f runbatch.xml runFilesOut -Ddate=%sysdate% >> %LOGFILE%

@ECHO ON
ECHO "-- FTP放檔" >> %LOGFILE%
call ant -f runbatch.xml runFtpOut -Ddate=%sysdate% >> %LOGFILE%

@ECHO ON
ECHO "檔案作業 End --------------------->" >> %LOGFILE%