/*
 * (版權及授權描述)
 * Copyright 2007 (C) Hyweb Technology Coperation.
 * All Rights Reserved.
 *
 * 2008/4/23
 */
package tw.com.hyweb.cp.ws;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import tw.com.hyweb.core.cp.batch.framework.AbstractBatchBasic;
import tw.com.hyweb.cp.ws.appointreload.AppointReloadServiceImp;

/**
 * @author Clare
 * 
 */
public class BatchExecutor
{
    private final static Logger LOGGER = Logger.getLogger(BatchExecutor.class);

    /**
     * @param args
     */
    public static void main(String[] args)
    {
        try
        {
        	DOMConfigurator.configureAndWatch("config//ws-cxf//log4j.xml", 60000);
            String springConfigPath = FilenameUtils.normalize(FilenameUtils.separatorsToSystem("config/ws-cxf/applicationContext.xml"));
            ApplicationContext context = new FileSystemXmlApplicationContext(springConfigPath);

            AppointReloadServiceImp processor = ((AppointReloadServiceImp) context.getBean("AppointReloadService"));
            LOGGER.info("begin");
            LOGGER.info("APPLOAD.11043173.2013093011.csv");
            processor.simulateAppointReloadByFileName("APPLOAD.11043173.2013100111.csv");
            LOGGER.info("end");
        }
        catch (Throwable e)
        {
            LOGGER.warn("", e);
        }
    }
}
