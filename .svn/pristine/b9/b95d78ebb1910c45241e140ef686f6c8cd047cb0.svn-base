#!/bin/bash

# last_date=`date -d last-day +%Y%m%d`
last_date=`date -d "$1-1 days"  +%Y%m%d`

job_dir=/cygdrive/c/THIG_PROD/Host_Batch/batch_file/temp_file/in/IMPTRANS/
folder_list=/tmp/folder_list.tmp

cd ${job_dir}

ls -ld * | awk '{print $9}' > ${folder_list}
count_folder=`cat ${folder_list} | wc -l`
if [ ${count_folder} -ne 0 ]
then
   count_num=0
   while (test ${count_num} -lt ${count_folder})
   do
      count_num=`expr ${count_num} + 1`
      folder_name=`head -${count_num} ${folder_list} | tail -1`
	  if [ -d ${folder_name} ]
	  then
	     cd ${folder_name}
	     # unzip -o *${last_date}*.zip
	     /bin/find . -name "*${last_date}*.zip" -exec unzip -quo {} \;
	     cd ${job_dir}
      fi
   done
fi
