@ECHO OFF

REM =======�O��ϵ�y�h��׃��=====================
SET JAVA_HOME=C:\Java\jdk1.5.0_22
SET CLASSPATH=.;%JAVA_HOME%\LIB\TOOLS.JAR
SET ANT_HOME=%ANT_HOME%
SET PERL_HOME=C:\Perl
SET PERL5LIB=%PERL_HOME%\lib
SET PATH=.;.\bin;%JAVA_HOME%\bin;%ANT_HOME%\bin;%PERL_HOME%\bin;%PATH%

REM =======�O������ϵ�yĿ�=============
SET LMS_DIR=C:\THIG_PROD
SET OPMENU_LOG_DIR=%LMS_DIR%\\log
SET WORK_DIR=%LMS_DIR%\\Host_Batch
SET CRM_DIR=%LMS_DIR%\\CRM
SET RPT_WORK_DIR=%LMS_DIR%\\Report\\Deploy

REM =======�xȡ Project CLASSPATH===========
call get_classpath.bat
