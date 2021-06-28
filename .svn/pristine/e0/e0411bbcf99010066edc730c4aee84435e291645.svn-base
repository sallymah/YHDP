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

REM @ECHO ON
REM ECHO "-- 匯入作業-外顯序?和UID對應檔"   >> %LOGFILE%       
REM call ant -f runbatch.xml runImpShowCardUidMapping -Ddate=%sysdate% >> %LOGFILE%

REM @ECHO ON
REM ECHO "-- 匯入事先?名新卡檔" >> %LOGFILE%
REM call ant -f runbatch.xml runImpCardAssociator -Ddate=%sysdate% >> %LOGFILE%

REM @ECHO ON
REM ECHO "-- 匯入事先?名補發卡檔" >> %LOGFILE%
REM call ant -f runbatch.xml runImpCardAssociatorReplace -Ddate=%sysdate% >> %LOGFILE%

REM @ECHO ON
REM ECHO "-- 匯入作業-活動適用名單檔" >> %LOGFILE%
REM call ant -f runbatch.xml runImpProgList -Ddate=%sysdate% >> %LOGFILE%

REM @ECHO ON
REM ECHO "-- 匯入作業-促銷?息檔" >> %LOGFILE%
REM call ant -f runbatch.xml runImpPromotionMsg -Ddate=%sysdate% >> %LOGFILE%

REM @ECHO ON
REM ECHO "-- 匯入作業-ImpHolder" >> %LOGFILE%
REM call ant -f runbatch.xml runImpHolder -Ddate=%sysdate% >> %LOGFILE%

REM @ECHO ON
REM ECHO "-- 匯入作業-ImpCrdsta" >> %LOGFILE%
REM call ant -f runbatch.xml runImpCrdSta -Ddate=%sysdate% >> %LOGFILE%

@ECHO ON
ECHO "-- 匯入作業-指定加值檔" >> %LOGFILE%
call ant -f runbatch.xml runImpAppload -Ddate=%sysdate% >> %LOGFILE%

@ECHO ON
ECHO "-- 匯入作業-调整交易檔" >> %LOGFILE%
call ant -f runbatch.xml runImpAdjustTxn -Ddate=%sysdate% >> %LOGFILE%

@ECHO ON
ECHO "匯入作業 End --------------------->" >> %LOGFILE%


@ECHO ON
ECHO "批次作業 Begin--------------------->" >> %LOGFILE%
ECHO "-- 前置作業-自動結帳 "  >> %LOGFILE%                              
call ant -f runbatch.xml runAutoSettleAccount -Ddate=%sysdate% >> %LOGFILE%

@ECHO ON
ECHO "-- 前置作業-指定加值 "  >> %LOGFILE%                              
call ant -f runbatch.xml runSimulateAppointReloadDownload -Ddate=%sysdate% >> %LOGFILE%

@ECHO ON
ECHO "-- 前置作業-定期回饋 " >> %LOGFILE%
call ant -f runbatch.xml runRegularAward -Ddate=%sysdate% >> %LOGFILE%

REM @ECHO ON
REM ECHO "-- 前置作業-?子券延展效期 " >> %LOGFILE%
REM call ant -f runbatch.xml runExtendCouponHostBase -Ddate=%sysdate% >> %LOGFILE%

REM @ECHO ON
REM ECHO "-- 前置作業-點數延展效期 " >> %LOGFILE%
REM call ant -f runbatch.xml runExtendBonus -Ddate=%sysdate% >> %LOGFILE%

@ECHO ON
ECHO "-- 前置作業-模擬退卡交易" >> %LOGFILE%
call ant -f runbatch.xml runSimulateCardReturn -Ddate=%sysdate% >> %LOGFILE%

@ECHO ON
ECHO "-- 前置作業-點數清除 " >> %LOGFILE%
call ant -f runbatch.xml runCleanCardBalance -Ddate=%sysdate% >> %LOGFILE%

REM @ECHO ON
REM ECHO "-- 前置作業-Generate KEK Key Version             " >> %LOGFILE%
REM call ant -f runbatch.xml runGenerateKEKKeyVersion -Ddate=%sysdate% >> %LOGFILE%

REM @ECHO ON
REM ECHO "-- 前置作業-不平帳?理Proc           " >> %LOGFILE%
REM call ant -f runbatch.xml runProcUnbalTermBatch -Ddate=%sysdate% >> %LOGFILE%

REM @ECHO ON
REM ECHO "-- 前置作業-不平帳?裡Patch             " >> %LOGFILE%
REM call ant -f runbatch.xml runPatchUnbalTermBatch -Ddate=%sysdate% >> %LOGFILE%

@ECHO ON
ECHO "-- 前置作業-卡片用途             " >> %LOGFILE%
call ant -f runbatch.xml runImpCardUse -Ddate=%sysdate% >> %LOGFILE%

@ECHO ON
ECHO "-- 批次作業-端末交易資料過檔 " >> %LOGFILE%
call ant -f runbatch.xml runCutOnlTxnStandard -Ddate=%sysdate%  >> %LOGFILE%

REM @ECHO ON
REM ECHO "-- 批次作業-CheckTxn     " >> %LOGFILE%
REM call ant -f runbatch.xml runCutCheckTxn -Ddate=%sysdate%  >> %LOGFILE%

@ECHO ON
ECHO "-- 批次作業-人工?單過檔 " >> %LOGFILE%
call ant -f runbatch.xml runCutCapturedTxn -Ddate=%sysdate% >> %LOGFILE%

@ECHO ON
ECHO "-- 批次作業-調帳資料過檔 " >> %LOGFILE%
call ant -f runbatch.xml runCutAdjustTxn -Ddate=%sysdate% >> %LOGFILE%

REM @ECHO ON
REM ECHO "-- 批次作業-交易合法性?證 " >> %LOGFILE%
REM call ant -f runbatch.xml runCheckOfflineTxn -Ddate=%sysdate% >> %LOGFILE%

@ECHO ON
ECHO "-- 批次作業-彙整餘額 " >> %LOGFILE%
call ant -f runbatch.xml runProcBalance -Ddate=%sysdate% >> %LOGFILE%

@ECHO ON
ECHO "-- 前置作業-餘額轉置 " >> %LOGFILE%
call ant -f runbatch.xml runSimulateBalTransferDownload -Ddate=%sysdate% >> %LOGFILE%

@ECHO ON
ECHO "-- 批次作業-清算?理 " >> %LOGFILE%
call ant -f runbatch.xml runProcSettle -Ddate=%sysdate%  >> %LOGFILE%

@ECHO ON
ECHO "-- ?算手續費" >> %LOGFILE%
call ant -f runbatch.xml runProcFee -Ddate=%sysdate% >> %LOGFILE%

@ECHO ON
ECHO "-- 報表作業-紅利彙? " >> %LOGFILE%
call ant -f runbatch.xml runSumBonus -Ddate=%sysdate%  >> %LOGFILE%

@ECHO ON
ECHO "-- 報表作業-卡種彙? " >> %LOGFILE%
call ant -f runbatch.xml runSumCardProduct -Ddate=%sysdate%  >> %LOGFILE%

@ECHO ON
ECHO "-- 報表作業-特店彙? " >> %LOGFILE%
call ant -f runbatch.xml runSumMerch -Ddate=%sysdate%  >> %LOGFILE%

@ECHO ON
ECHO "-- 報表作業-忠誠方案彙? " >> %LOGFILE%
call ant -f runbatch.xml runSumPbnld -Ddate=%sysdate%  >> %LOGFILE%

@ECHO ON
ECHO "-- 報表作業-卡片狀態彙? " >> %LOGFILE%
call ant -f runbatch.xml runSumCardStatus -Ddate=%sysdate%  >> %LOGFILE%

@ECHO ON
ECHO "-- 報表作業-每日交易量彙?" >> %LOGFILE%
call ant -f runbatch.xml runSumTxnCnt -Ddate=%sysdate%  >> %LOGFILE%

REM @ECHO ON
REM ECHO "-- 報表作業-產品代碼彙? " >> %LOGFILE%
REM call ant -f runbatch.xml runSumProduct -Ddate=%sysdate%  >> %LOGFILE%

@ECHO ON
ECHO "-- 報表作業-庫存彙? " >> %LOGFILE%
call ant -f runbatch.xml runSumStock -Ddate=%sysdate%  >> %LOGFILE%

REM @ECHO ON
REM ECHO "-- 批次作業ProcPerso " >> %LOGFILE%
REM call ant -f runbatch.xml runProcPerso -Ddate=%sysdate%  >> %LOGFILE%

@ECHO ON
ECHO "-- 批次作業-參數下載 " >> %LOGFILE%
call ant -f runbatch.xml runProcParm -Ddate=%sysdate%  >> %LOGFILE%

@ECHO ON
ECHO "-- 匯出ExpSumTrans資料檔 " >> %LOGFILE%
call ant -f runbatch.xml runExpSumTrans -Ddate=%sysdate% >> %LOGFILE%

REM @ECHO ON
REM ECHO "-- 批次作業-CRM統?作業" >> %LOGFILE%
REM call ant -f runbatch.xml runCrmMakeGroup -Ddate=%sysdate%  >> %LOGFILE%

@ECHO ON
ECHO "-- 匯出Transation資料檔 " >> %LOGFILE%
call ant -f runbatch.xml runExpTransation -Ddate=%sysdate% >> %LOGFILE%

@ECHO ON
ECHO "-- 匯出ExpSumTxnDetail資料檔 " >> %LOGFILE%
call ant -f runbatch.xml runExpSumTransDetail -Ddate=%sysdate% >> %LOGFILE%

@ECHO ON
ECHO "-- 匯出ExpAmendMemberData資料檔 " >> %LOGFILE%
call ant -f runbatch.xml runExpAmendMemberData -Ddate=%sysdate% >> %LOGFILE%

REM @ECHO ON
REM ECHO "-- 匯出ExpAmendCardData資料檔 " >> %LOGFILE%
REM call ant -f runbatch.xml runExpAmendCardData -Ddate=%sysdate% >> %LOGFILE%

REM @ECHO ON
REM ECHO "-- 匯出ExpAmendTrans資料檔 " >> %LOGFILE%
REM call ant -f runbatch.xml runExpAmendTrans -Ddate=%sysdate% >> %LOGFILE%

@ECHO ON
ECHO "-- 匯出ExpMerchData資料檔 " >> %LOGFILE%
call ant -f runbatch.xml runExpMerchData -Ddate=%sysdate% >> %LOGFILE%

@ECHO ON
ECHO "-- 匯出ExpMemberData資料檔 " >> %LOGFILE%
call ant -f runbatch.xml runExpMemberData -Ddate=%sysdate% >> %LOGFILE%

@ECHO ON
ECHO "-- 匯出ExpMerchGroup資料檔 " >> %LOGFILE%
call ant -f runbatch.xml runExpMerchGroup -Ddate=%sysdate% >> %LOGFILE%

@ECHO ON
ECHO "-- 匯出ExpAwardProg資料檔 " >> %LOGFILE%
call ant -f runbatch.xml runExpAwardProg -Ddate=%sysdate% >> %LOGFILE%

@ECHO ON
ECHO "-- 匯出ExpBonus資料檔 " >> %LOGFILE%
call ant -f runbatch.xml runExpBonus -Ddate=%sysdate% >> %LOGFILE%

@ECHO ON
ECHO "-- 匯出檔案" >> %LOGFILE%
call ant -f runbatch.xml runFilesOut -Ddate=%sysdate% >> %LOGFILE%

@ECHO ON
ECHO "-- FTP放檔" >> %LOGFILE%
call ant -f runbatch.xml runFtpOut -Ddate=%sysdate% >> %LOGFILE%

REM @ECHO ON
REM ECHO "-- 過期餘額為??理" >> %LOGFILE%
REM call ant -f runbatch.xml runBalUpdateBadBonus -Ddate=%sysdate% >> %LOGFILE%

REM @ECHO ON
REM ECHO "-- 履約額度警示通知" >> %LOGFILE%
REM call ant -f runbatch.xml runCheckQuotaAlert -Ddate=%sysdate% >> %LOGFILE%

REM @ECHO ON
REM ECHO "-- ?算未加值額度" >> %LOGFILE%
REM call ant -f runbatch.xml runSumUnReload -Ddate=%sysdate% >> %LOGFILE%

REM @ECHO ON
REM ECHO "-- 發卡履約日誌" >> %LOGFILE%
REM call ant -f runbatch.xml runSumIssPb -Ddate=%sysdate% >> %LOGFILE%

REM @ECHO ON
REM ECHO "-- 加值超過一年不信?餘額" >> %LOGFILE%
REM call ant -f runbatch.xml runCardInvalidEscrow -Ddate=%sysdate% >> %LOGFILE%

REM @ECHO ON
REM ECHO "-- 贖回交易統?資?" >> %LOGFILE%
REM call ant -f runbatch.xml runSumCardEscrow -Ddate=%sysdate% >> %LOGFILE%

@ECHO ON
ECHO "批次作業 End --------------------->" >> %LOGFILE%