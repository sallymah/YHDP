/*
 * (版權及授權描述)
 * Copyright 2006 (C) Hyweb Technology Coperation.
 * All Rights Reserved.
 *
 * 2007/12/5
 */
package tw.com.hyweb.svc.yhdp.batch.framework.ftp;

import org.apache.commons.io.FilenameUtils;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import tw.com.hyweb.util.date.DateUtil;
import tw.com.hyweb.util.string.StringUtil;

/**
 * @author Clare
 * 
 */
public class FtpRefilesDelete
{
    private static final String SPRING_PATH = FilenameUtils.separatorsToSystem("config/batch/FtpRefilesDelete/beans-config.xml");

    public static void main(String[] args)
    {
        String batchDate = System.getProperty("date");

        if (StringUtil.isEmpty(batchDate))
        {
            batchDate = DateUtil.getTodayString().substring(0, 8);
        }

        RefileDeleteProcessor process = (RefileDeleteProcessor) new FileSystemXmlApplicationContext(SPRING_PATH).getBean("ftpRefilesDelete");
        process.setBatchDate(batchDate);
        process.setProgramName("FtpRefilesDelete");
        process.run(null);
    }
}
