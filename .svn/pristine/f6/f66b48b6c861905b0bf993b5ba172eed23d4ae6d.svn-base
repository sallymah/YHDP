package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpTxnDtl;

import tw.com.hyweb.service.db.info.TbInctlInfo;
import tw.com.hyweb.service.db.info.TbTermBatchInfo;
import tw.com.hyweb.service.db.info.TbTrafficTxnErrInfo;
import tw.com.hyweb.service.db.info.TbTrafficTxnInfo;
import tw.com.hyweb.svc.yhdp.batch.impfiles.ImpCpcTxnDtl.CPCUtil;
import tw.com.hyweb.svc.yhdp.online.LMSContext;
import tw.com.hyweb.util.ISODate;

public class BATCHContext extends LMSContext
{
    private TbInctlInfo inctlInfo = null;
    private String batchDate = null;
    private String batchTime = null;
    private String fullFileName = null;
    public String autoLoadAmt;//消費時自動加值金額
    public String autoLoadAtc;//自動加值卡片交易序號
    public String samId;//SAM ID
    public String dongleRespCode;
    public boolean isDupTxn = false;
    private TrafficTxnHeader tfcHeader;
    private TrafficTxnDetail tfcDetail;
    private TbTrafficTxnInfo trafficTxnInfo;
    private TbTrafficTxnErrInfo trafficTxnErrInfo;
    private String fileTxnStatus;
    private String realLocId;
    private double chipAfterBonusQty = 0.0F; 
    
    /**
     * @return the chipAfterBonusQty
     */
    public double getChipAfterBonusQty()
    {
        return chipAfterBonusQty;
    }

    /**
     * @param chipAfterBonusQty the chipAfterBonusQty to set
     */
    public void setChipAfterBonusQty(double chipAfterBonusQty)
    {
        this.chipAfterBonusQty = chipAfterBonusQty;
    }

    /**
     * @return the realLocId
     */
    public String getRealLocId()
    {
        return realLocId;
    }

    /**
     * @param realLocId the realLocId to set
     */
    public void setRealLocId(String realLocId)
    {
        this.realLocId = realLocId;
    }

    public String getFileTxnStatus()
    {
        return fileTxnStatus;
    }

    public void setFileTxnStatus(String fileTxnStatus)
    {
        this.fileTxnStatus = fileTxnStatus;
    }

    public void setTbTrafficTxnInfo(TbTrafficTxnInfo trafficTxnInfo)
    {
        this.trafficTxnInfo = trafficTxnInfo;
    }
    
    public TbTrafficTxnInfo getTbTrafficTxnInfo()
    {
        return trafficTxnInfo;
    }
    
    public void setTbTrafficTxnErrInfo(TbTrafficTxnErrInfo trafficTxnErrInfo)
    {
        this.trafficTxnErrInfo = trafficTxnErrInfo;
    }
    
    public TbTrafficTxnErrInfo getTbTrafficTxnErrInfo()
    {
        return trafficTxnErrInfo;
    }
    
    public void setTrafficTxnDetail(TrafficTxnDetail tfcDetail)
    {
        this.tfcDetail = tfcDetail;
    }
    
    public TrafficTxnDetail getTrafficTxnDetail()
    {
        return tfcDetail;
    }
    
    public void setTrafficTxnHeader(TrafficTxnHeader tfcHeader)
    {
        this.tfcHeader = tfcHeader;
    }
    
    public TrafficTxnHeader getTrafficTxnHeader()
    {
        return tfcHeader;
    }
    
    public boolean getIsDupTxn() {
        return isDupTxn;
    }

    public void setIsDupTxn(boolean isDupTxn) {
        this.isDupTxn = isDupTxn;
    }
    public String getDongleRespCode() {
        return dongleRespCode;
    }

    public void setDongleRespCode(String dongleRespCode) {
        this.dongleRespCode = dongleRespCode;
    }
    public String getSamId() {
        return samId;
    }
    public void setSamId(String samId) {
        this.samId = samId;
    }
    public String getAutoLoadAmt() {
        return autoLoadAmt;
    }

    public void setAutoLoadAmt(String autoLoadAmt) {
        this.autoLoadAmt = autoLoadAmt;
    }
        
    public String getAutoLoadAtc() {
        return autoLoadAtc;
    }

    public void setAutoLoadAtc(String autoLoadAtc) {
        this.autoLoadAtc = autoLoadAtc;
    }
    
    public void setTbInctlInfo(TbInctlInfo inctlInfo)
    {
        this.inctlInfo = inctlInfo;
    }
    
    public TbInctlInfo getTbInctlInfo()
    {
        return this.inctlInfo;
    }
    
    public String getFullFileName()
    {
        return fullFileName;
    }
    
    public void setFullFileName(String fullFileName)
    {
        this.fullFileName = fullFileName;
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
    
    public boolean isCpc()
    {
        String fileName = inctlInfo.getFullFileName();
        String acqMemId = fileName.substring(CPCUtil.FILE_NAME_KEY.length(), CPCUtil.FILE_NAME_KEY.length() + 8);
        return acqMemId.equals(CPCUtil.CPC_ACQ_MEM_ID);
    }
}
