/*
 * (版權及授權描述)
 * Copyright 2007 (C) Hyweb Technology Coperation.
 * All Rights Reserved.
 *
 * 2008/4/23
 */
package tw.com.hyweb.core.yhdp.batch.framework.generic;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import tw.com.hyweb.core.yhdp.batch.framework.AbstractBatchBasic;

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
            String springConfigPath = FilenameUtils.normalize(FilenameUtils.separatorsToSystem(args[0]));
            ApplicationContext context = new FileSystemXmlApplicationContext(springConfigPath);

            AbstractBatchBasic processor = ((AbstractBatchBasic) context.getBean(args[1]));
            processor.run(null);
        }
        catch (Throwable e)
        {
            LOGGER.warn("", e);
        }
    }
}
