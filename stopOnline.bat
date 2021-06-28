@ECHO OFF

sc stop HywebSvc
%LMS_DIR%\bin\sleep 5

:end
