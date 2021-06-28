package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpTxnDtl;

/*
 * (版權及授權描述)
 * Copyright 2006 (C) Hyweb Technology Coperation.
 * All Rights Reserved.
 *
 * $Id$
 * $Date$
 * *********************************************** 
 */

import java.util.HashMap;
import java.util.Hashtable;

import tw.com.hyweb.online.IContextListener;
import tw.com.hyweb.svc.yhdp.batch.framework.impfiles.InctlBean;

/**
 * JobRunnerFactory
 */
public class JobRunnerFactory
{
    /**
     * @param acceptRemoteIp
     * @param lChannel
     * @param lHeader
     * @param lBody
     * @return create default JobRunner
     */

    public JobRunner create(IContextListener ctxListener, InctlBean inctlBean, ImpFileInfo impFileInfo, Hashtable<String, String> inctlResultMap,int recordsPerCommit)
    {
        return new JobRunner(ctxListener, inctlBean, impFileInfo, inctlResultMap,recordsPerCommit);
    }
}
