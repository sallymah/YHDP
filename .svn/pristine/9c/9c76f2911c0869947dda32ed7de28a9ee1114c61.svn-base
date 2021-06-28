package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpTxnDtl;

import tw.com.hyweb.service.db.info.TbInctlInfo;
import tw.com.hyweb.service.db.info.TbTermBatchInfo;
import tw.com.hyweb.svc.yhdp.online.LMSContext;
import tw.com.hyweb.util.ISODate;

public class ThigBContext extends LMSContext
{
    private TbInctlInfo inctlInfo = null;
    private FF21 ff21 = null;
    private FF11 ff11 = null;
    private String batchDate = null;
    private String batchTime = null;
    private TbTermBatchInfo termBatchInfo = null;
    
    public void setTbTermBatchInfo(TbTermBatchInfo termBatchInfo)
    {
        this.termBatchInfo = termBatchInfo;
    }
    
    public TbTermBatchInfo getTbTermBatchInfo()
    {
        return termBatchInfo;
    }
    
    public void setTbInctlInfo(TbInctlInfo inctlInfo)
    {
        this.inctlInfo = inctlInfo;
    }
    
    public TbInctlInfo getTbInctlInfo()
    {
        return inctlInfo;
    }
    
    public void setFF21(FF21 ff21)
    {
        this.ff21 = ff21;
    }
    
    public FF21 getFF21()
    {
        return ff21;
    }
    
    public void setFF11(FF11 ff11)
    {
        this.ff11 = ff11;
    }
    
    public FF11 getFF11()
    {
        return ff11;
    }
    
    /**
     * @return hostDate
     */
    public String getBatchDate()
    {
        if (this.batchDate==null)
        {
            this.batchDate = ISODate.formatDate(getTimeTxExec(), "yyyyMMdd");
        }

        return this.batchDate;
    }

    /**
     * @return hostTime
     */
    public String getBatchTime()
    {
        if (this.batchTime==null)
        {
            batchTime = ISODate.formatDate(getTimeTxExec(), "HHmmss");
        }
        return batchTime;
    }
    
    /**
     * @return String of mon
     */
    @Override
    public String getParMon()
    {
        return getBatchDate().substring(4,6);
    }

    /**
     * @return String of day
     */
    @Override
    public String getParDay()
    {
        return getBatchDate().substring(6,8);
    }
}
