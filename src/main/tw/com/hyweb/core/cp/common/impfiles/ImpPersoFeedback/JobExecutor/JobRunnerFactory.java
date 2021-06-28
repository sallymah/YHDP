package tw.com.hyweb.core.cp.common.impfiles.ImpPersoFeedback.JobExecutor;

import java.util.HashMap;
import java.util.List;

import tw.com.hyweb.service.db.info.TbInctlInfo;
import tw.com.hyweb.service.db.info.TbPersoInfo;

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

    public JobRunner create(HashMap<String,String> resultMap, int idx, TbInctlInfo inctlInfo, TbPersoInfo persoInfo, List hms)
    {
        return new JobRunner(resultMap, idx, inctlInfo, persoInfo, hms);
    }
}
