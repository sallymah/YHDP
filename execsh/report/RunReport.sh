#!/bin/bash

cd $REPORT_DIR

report.bat > /dev/null 2> /dev/null

var=$?
echo
if [ $var -eq 0 ]
then
    printf "$PRG_RUN_SUC1";printf "$PRG_RUN_SUC2"
else
    printf "\033[41m $PRG_RUN_FAIL \033[40;0m"
fi
echo
