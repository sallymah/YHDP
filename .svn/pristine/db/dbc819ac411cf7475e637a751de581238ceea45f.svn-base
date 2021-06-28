package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpCpcTxnDtl;

import java.util.HashMap;
import java.util.Hashtable;

import tw.com.hyweb.online.IContextListener;
import tw.com.hyweb.svc.yhdp.batch.framework.impfiles.InctlBean;
import tw.com.hyweb.svc.yhdp.batch.impfiles.ImpTxnDtl.ImpFileInfo;

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
