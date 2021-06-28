package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpTxnDtl;

import org.apache.log4j.Logger;

public class TrafficTxnHeader
{
    private static final Logger logger = Logger.getLogger(TrafficTxnHeader.class);
    private TrafficDataFormat[] dataFormatList;
    private String[] dataList;
    private String[] rawDataList;
    private String item;
    private String sourceDate;
    private String fileSeq;
    private String recrod;
    private String txnValueAmt;
    private String ticketValueAmt;
    private String deviceId;
    private String batchNo;
    private String programVer;
    private String blacklistVer;
    private String parameterVer;
    
    public void parser(String[] dataList)
    {
        this.dataList = dataList;
    }
    
    public void setDataFormatList(TrafficDataFormat[] dataFormatList)
    {
        this.dataFormatList = dataFormatList;
    }
    
    public TrafficDataFormat[] getDataFormatList()
    {
        return this.dataFormatList;
    }
    
    public void setRawDataList(String[] rawDataList)
    {
        this.rawDataList = rawDataList;
    }
    
    public String[] getRawDataList()
    {
        return this.rawDataList;
    }
    
    public void setDataList(String[] dataList)
    {
        this.dataList = dataList;
    }
    
    public String[] getDataList()
    {
        return this.dataList;
    }
    
    public String getDataRecode(int idx)
    {
        return this.dataList[idx];
    }
    
    public String getParameterVer()
    {
        if(null != dataList && dataList.length > 0)
        {
            parameterVer = dataList[10];
        }
        return parameterVer;
    }
    
    public void setParameterVer(String parameterVer)
    {
        this.parameterVer = parameterVer ;
    }
    
    public String getBlacklistVer()
    {
        if(null != dataList && dataList.length > 0)
        {
            blacklistVer = dataList[9];
        }
        return blacklistVer;
    }
    
    public void setBlacklistVer(String blacklistVer)
    {
        this.blacklistVer = blacklistVer ;
    }
    
    public String getProgramVer()
    {
        if(null != dataList && dataList.length > 0)
        {
            programVer = dataList[8];
        }
        return programVer;
    }
    
    public void setProgramVer(String programVer)
    {
        this.programVer = programVer ;
    }
    
    public String getBatchNo()
    {
        if(null != dataList && dataList.length > 0)
        {
            batchNo = dataList[7];
        }
        return batchNo;
    }
    
    public void setBatchNo(String batchNo)
    {
        this.batchNo = batchNo ;
    }
    
    public String getDeviceId()
    {
        if(null != dataList && dataList.length > 0)
        {
            deviceId = dataList[6];
        }
        return deviceId;
    }
    
    public void setDeviceId(String deviceId)
    {
        this.deviceId = deviceId ;
    }
    
    public String getTicketValueAmt()
    {
        if(null != dataList && dataList.length > 0)
        {
            ticketValueAmt = dataList[5];
        }
        return ticketValueAmt;
    }
    
    public void setTicketValueAmt(String ticketValueAmt)
    {
        this.ticketValueAmt = ticketValueAmt ;
    }
    
    public String getTxnValueAmt()
    {
        if(null != dataList && dataList.length > 0)
        {
            txnValueAmt = dataList[4];
        }
        return txnValueAmt;
    }
    
    public void setTxnValueAmt(String txnValueAmt)
    {
        this.txnValueAmt = txnValueAmt ;
    }
    
    public String getRecrod()
    {
        if(null != dataList && dataList.length > 0)
        {
            recrod = dataList[3];
        }
        return recrod;
    }
    
    public void setRecrod(String recrod)
    {
        this.recrod = recrod ;
    }
    
    public String getFileSeq()
    {
        if(null != dataList && dataList.length > 0)
        {
            sourceDate = dataList[2];
        }
        return fileSeq;
    }
    
    public void setFileSeq(String fileSeq)
    {
        this.fileSeq = fileSeq ;
    }
    
    public String getSourceDate()
    {
        if(null != dataList && dataList.length > 0)
        {
            sourceDate = dataList[1];
        }
        return sourceDate;
    }
    
    public void setSourceDate(String sourceDate)
    {
        this.sourceDate = sourceDate ;
    }
    
    public String getItem()
    {
        if(null != dataList && dataList.length > 0)
        {
            item = dataList[0];
        }
        return item;
    }
    
    public void setItem(String item)
    {
        this.item = item ;
    }
}
