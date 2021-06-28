package tw.com.hyweb.svc.yhdp.batch.preoperation.CheckPersoFeeback.JobExecutor;

import java.util.ArrayList;

import tw.com.hyweb.service.db.info.TbInctlInfo;
import tw.com.hyweb.svc.yhdp.batch.preoperation.CheckPersoFeeback.YhdpPersoFeebackDataCheck;
import tw.com.hyweb.svc.yhdp.batch.preoperation.CheckPersoFeeback.YhdpPersoFeebackDataGenerator;

/**
 * JobRunnerFactory
 */
public class CheckLineJobRunnerFactory
{
    /**
     * @param acceptRemoteIp
     * @param lChannel
     * @param lHeader
     * @param lBody
     * @return create default JobRunner
     */

    public CheckLineJobRunner create(YhdpPersoFeebackDataCheck check, YhdpPersoFeebackDataGenerator dataGen, 
    		ArrayList<String> sqlList, int idx, String dataLine, TbInctlInfo inctlInfo)
    {
        return new CheckLineJobRunner(check, dataGen, sqlList, idx, dataLine, inctlInfo);
    }
}
