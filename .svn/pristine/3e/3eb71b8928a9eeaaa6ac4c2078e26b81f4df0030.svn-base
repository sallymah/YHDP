/*
 * (版權及授權描述)
 * Copyright 2007 (C) Hyweb Technology Coperation.
 * All Rights Reserved.
 *
 * 2008/5/26
 */
package tw.com.hyweb.svc.cp.batch;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import tw.com.hyweb.core.cp.batch.framework.generic.BatchJob;
import tw.com.hyweb.core.cp.batch.framework.generic.BatchJobFactory;

/**
 * @author Clare
 * 
 */
public abstract class BatchJobFactoryTestCase extends BatchTestCase
{
    /**
     * @throws Exception
     */
    public void testFactory() throws Exception
    {
        BatchJobFactory factory = null;

        try
        {
            factory = getFactory();
            factory.init(connection.getConnection(), getBatchDate());

            Set<String> names = new HashSet<String>();

            while (factory.hasNext())
            {
                BatchJob next = factory.next(connection.getConnection(), getBatchDate());
                names.add(next.toString());
            }

            names.retainAll(Arrays.asList(getJobNames()));

            assertEquals(new HashSet<String>(Arrays.asList(getJobNames())), names);
        }
        finally
        {
            factory.destroy();
        }
    }

    protected abstract BatchJobFactory getFactory();

    protected abstract String getBatchDate();

    protected abstract String[] getJobNames();
}
