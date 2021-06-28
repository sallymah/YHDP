package tw.com.hyweb.svc.yhdp.batch.preoperation.CheckPersoFeeback.JobExecutor;

import java.util.HashMap;
import java.util.List;

import tw.com.hyweb.service.db.info.TbMemberInfo;

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

    public JobRunner create(HashMap<String,List> resultMap, int idx, TbMemberInfo memberInfo, Object decryptionUtil, List hms)
    {
        return new JobRunner(resultMap, idx, memberInfo, decryptionUtil, hms);
    }
}
