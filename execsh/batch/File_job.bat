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
ECHO "�n�����I Begin--------------------->" >> %LOGFILE%
ECHO "-- �R�����I-FTP���d�n��" >> %LOGFILE%
call ant -f runbatch.xml runFtpIn -Ddate=%sysdate% >> %LOGFILE%

@ECHO ON
ECHO "-- �R�����I-�n������" >> %LOGFILE%
call ant -f runbatch.xml runCangeLocalPath -Ddate=%sysdate% >> %LOGFILE%

@ECHO ON
ECHO "�≺�s Begin--------------------->" >> %LOGFILE%
ECHO "-- �≺�s" >> %LOGFILE%
call C:\Windows\batch_unzip.bat >> %LOGFILE%

cd \
cd %WORK_DIR%

@ECHO ON
ECHO "-- �R�����I-�n���Йn"  >> %LOGFILE%     
call ant -f runbatch.xml runSplitImpTrans -Ddate=%sysdate% >> %LOGFILE%

@ECHO ON
ECHO "-- �R�����I-�R��n��"  >> %LOGFILE%         
call ant -f runbatch.xml runFilesIn -Ddate=%sysdate% >> %LOGFILE%

@ECHO ON
ECHO "-- �R�����I-�R���������יn"  >> %LOGFILE%         
call ant -f runbatch.xml runImpTrans -Ddate=%sysdate% >> %LOGFILE%

@ECHO ON
ECHO "-- �R��ExpAmendCardData�Y�ϙn " >> %LOGFILE%
call ant -f runbatch.xml runExpAmendCardData -Ddate=%sysdate% >> %LOGFILE%

@ECHO ON
ECHO "-- �R��ExpAmendTrans�Y�ϙn " >> %LOGFILE%
call ant -f runbatch.xml runExpAmendTrans -Ddate=%sysdate% >> %LOGFILE%

@ECHO ON
ECHO "-- �R��ExpCardBal�Y�ϙn " >> %LOGFILE%
call ant -f runbatch.xml runExpCardBal -Ddate=%sysdate% >> %LOGFILE%

@ECHO ON
ECHO "-- �R���n��" >> %LOGFILE%
call ant -f runbatch.xml runFilesOut -Ddate=%sysdate% >> %LOGFILE%

@ECHO ON
ECHO "-- FTP�řn" >> %LOGFILE%
call ant -f runbatch.xml runFtpOut -Ddate=%sysdate% >> %LOGFILE%

@ECHO ON
ECHO "�n�����I End --------------------->" >> %LOGFILE%