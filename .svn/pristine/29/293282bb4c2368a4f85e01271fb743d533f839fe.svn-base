#!/bin/bash

cd $WORK_DIR
printf "\n\033[36;40m\033[5m $PRG_RUN_MSG \033[40;0m\033[39m\n\n"

ant -f runbatch.xml runExpBlackCard -Ddate=$1 > /dev/null 2> /dev/null

var=$?
echo
if [ $var -eq 0 ]
then
    printf "$PRG_RUN_SUC1";printf "$PRG_RUN_SUC2"
else
    printf "\033[41m $PRG_RUN_FAIL \033[40;0m"
fi
echo
