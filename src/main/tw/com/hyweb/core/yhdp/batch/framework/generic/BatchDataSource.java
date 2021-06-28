/*
 * $Id$
 * 
 * Copyright 2009 Hyweb Technology Coperation.
 * All Rights Reserved.
 */


package tw.com.hyweb.core.yhdp.batch.framework.generic;

import javax.sql.DataSource;

import org.apache.commons.io.FilenameUtils;
import org.springframework.context.support.FileSystemXmlApplicationContext;


/**
 * @author Clare
 * @version $Revision$
 */
public class BatchDataSource
{
    private static final String DATA_SOURCE_SPRING_PATH = FilenameUtils.separatorsToSystem("config/batch/datasource.xml");

    private static final BatchDataSource instance = new BatchDataSource();

    private final DataSource dataSource;

    private BatchDataSource()
    {
        dataSource = (DataSource) new FileSystemXmlApplicationContext(DATA_SOURCE_SPRING_PATH).getBean("dataSource");
    }

    public static DataSource getDataSource()
    {
        return instance.dataSource;
    }
}
