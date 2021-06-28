/*
 * $Id: SimulateOnlineJobFactory.java 2526 2010-01-08 09:58:22Z 94068 $
 * 
 * Copyright 2009 Hyweb Technology Coperation.
 * All Rights Reserved.
 */


package tw.com.hyweb.core.cp.batch.preoperation;

import java.sql.Connection;
import org.apache.log4j.Logger;

import tw.com.hyweb.core.cp.batch.framework.generic.CursorBatchJobFactory;
import tw.com.hyweb.service.db.info.TbBatchResultInfo;


/**
 * @author Anny
 * @version $Revision: 2526 $
 */
public abstract class SimulateOnlineJobFactory extends CursorBatchJobFactory
{
    private static final Logger LOGGER = Logger.getLogger(SimulateOnlineJobFactory.class);
    
    //String : TERM_ID + MERCH_ID
    protected InsertTermBatchHandle termBatchHandle = null;


	/**
     * 先insert一筆termBatch
     * 
     * @see tw.com.hyweb.core.cp.batch.framework.generic.CursorBatchJobFactory#init
     *      (java.sql.Connection, java.lang.String)
     */
    @Override
    public void init(Connection connection, String batchDate, TbBatchResultInfo tbBatchResultInfo) throws Exception
    {
        super.init(connection, batchDate, tbBatchResultInfo);

        if (hasNext())
        {
        	termBatchHandle = new InsertTermBatchHandle(connection, false);
        }
    }

}
