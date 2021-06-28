package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpTxnDtl;

import org.apache.log4j.Logger;

import tw.com.hyweb.svc.yhdp.batch.framework.Layer1Constants;
import tw.com.hyweb.util.ISOUtil;

public class TrafficTxnDetail
{
    private static final Logger logger = Logger.getLogger(TrafficTxnDetail.class);
    
    public TrafficDataFormat[] dataFormatList;
    public String[] dataList;
    public String[] rawDataList;
    private String item;
    private String txnType;
    private String mifareId;
    private String transNo;
    private String pid;
    private String transDate;
    private String transType;
    private String beforAmt;
    private String txnAmt;
    private String afterAmt;
    private String transSysNo;
    private String locId;
    private String devId;
    private String samOsn;
    private String samTransNo;
    private String samMac;
    private String termTxnSeq;
    private String termTxnDate;
    private String termId;
    private String storeId;
    private String transNoCancel;
    private String transDateCancel;
    private String txprocessDate;
    private String shiftNbr;
    private String firstEntryStation;
    private String firstDeductValue;
    private String inOutCode;
    private String trtcCtsn;
    private String rejCode;
    private String consumptionPoint;
    private String uaerType;
    private String transferFavorAmt;
    private String totalTransFare;
    private String otherFavorAmt;
    private String batchNo;
    private String inShuttleCode;
    private String inStationCode;
    private String boardingStopCode;
    private String outShuttleCode;
    private String outStationCode;
    private String alightingStopCode;
    private String transferFlag;
    private String cashForInsufficiunt;
    private String busLincenseId;
    private String busDriverId;
    private String busRouteDoman;
    private String cuteDate;
    private String cuteDateClass;
    private String totalTransAmt;
    private String freeCode;
    private String freeBusRebate;
    private String priceMargin;
    private String cardType;
    private String premiumProvider;
    private String userTypeFavorAmt;
    private String peakFavorAmt;
    private String bussinessDate;
    private String penaltyAmt;
    private String carrigeType;
    private String favorFlag;
    private String preChkinFlag;
    private String preChkinDeductFlag;
    private String preChkinCntFlag;
    private String specIdtOrigCnt;
    private String specIdtUsageCnt;
    private String specIdtResetDate;
    private String specIdtActiveDate;
    private String specIdtExpDate;
    private String specIdtDepartureStation;
    private String specIdtArrivalStation;
    private String specIdtRouteId;
    private String bonusUsage;
    private String bonusValidDate;
    private String bonusSerno;
    private String bonusTransAmt;
    private String bonusRemain;
    private String specialFlag;
   
    public void parser(String[] dataList)
    {
        this.dataList = dataList;
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
    
    public void setDataRecode(int idx, String data)
    {
        this.dataList[idx] = data;
    }
    
    public String getRawDataRecode(int idx)
    {
        return this.rawDataList[idx];
    }
    
    public void setDataFormatList(TrafficDataFormat[] dataFormatList)
    {
        this.dataFormatList = dataFormatList;
    }
    
    public TrafficDataFormat[] getDataFormatList()
    {
        return this.dataFormatList;
    }
    
    public String getTransferFlag()
    {
        if(null != dataList && dataList.length > 0)
        {
            transferFlag = dataList[41];
        }
        return transferFlag;
    }
    
    public void setTransferFlag(String transferFlag)
    {
        this.transferFlag = transferFlag;
    }
    
    public String getAlightingStopCode()
    {
        if(null != dataList && dataList.length > 0)
        {
            alightingStopCode = dataList[40];
        }
        return alightingStopCode;
    }
    
    public void setAlightingStopCode(String alightingStopCode)
    {
        this.alightingStopCode = alightingStopCode;
    }
    
    public String getOutStationCode()
    {
        if(null != dataList && dataList.length > 0)
        {
            outStationCode = dataList[39];
        }
        return outStationCode;
    }
    
    public void setOutStationCode(String outStationCode)
    {
        this.outStationCode = outStationCode;
    }
    
    public String getOutShuttleCode()
    {
        if(null != dataList && dataList.length > 0)
        {
            outShuttleCode = dataList[38];
        }
        return outShuttleCode;
    }
    
    public void setOutShuttleCode(String outShuttleCode)
    {
        this.outShuttleCode = outShuttleCode;
    }
    
    public String getBoardingStopCode()
    {
        if(null != dataList && dataList.length > 0)
        {
            boardingStopCode = dataList[37];
        }
        return boardingStopCode;
    }
    
    public void setBoardingStopCode(String boardingStopCode)
    {
        this.boardingStopCode = boardingStopCode;
    }
    
    public String getInStationCode()
    {
        if(null != dataList && dataList.length > 0)
        {
            inStationCode = dataList[36];
        }
        return inStationCode;
    }
    
    public void setInStationCode(String inStationCode)
    {
        this.inStationCode = inStationCode;
    }
    
    public String getInShuttleCode()
    {
        if(null != dataList && dataList.length > 0)
        {
            inShuttleCode = dataList[35];
        }
        return inShuttleCode;
    }
    
    public void setInShuttleCode(String inShuttleCode)
    {
        this.inShuttleCode = inShuttleCode;
    }
    
    public String getBatchNo()
    {
        if(null != dataList && dataList.length > 0)
        {
            batchNo = dataList[34];
        }
        return batchNo;
    }
    
    public void setBatchNo(String batchNo)
    {
        this.batchNo = batchNo;
    }
    
    public String getOtherFavorAmt()
    {
        if(null != dataList && dataList.length > 0)
        {
            otherFavorAmt = dataList[33];
        }
        return otherFavorAmt;
    }
    
    public void setOtherFavorAmt(String otherFavorAmt)
    {
        this.otherFavorAmt = otherFavorAmt;
    }
    
    public String getTotalTransFare()
    {
        if(null != dataList && dataList.length > 0)
        {
            totalTransFare = dataList[32];
        }
        return totalTransFare;
    }
    
    public void setTotalTransFare(String totalTransFare)
    {
        this.totalTransFare = totalTransFare;
    }
    
    public String getTransferFavorAmt()
    {
        if(null != dataList && dataList.length > 0)
        {
            transferFavorAmt = dataList[31];
        }
        return transferFavorAmt;
    }
    
    public void setTransferFavorAmt(String transferFavorAmt)
    {
        this.transferFavorAmt = transferFavorAmt;
    }
    
    public String getUaerType()
    {
        if(null != dataList && dataList.length > 0)
        {
            uaerType = dataList[30];
        }
        return uaerType;
    }
    
    public void setUaerType(String uaerType)
    {
        this.uaerType = uaerType;
    }
    
    public String getConsumptionPoint()
    {
        if(null != dataList && dataList.length > 0)
        {
            consumptionPoint = dataList[29];
        }
        return consumptionPoint;
    }
    
    public void setConsumptionPoint(String consumptionPoint)
    {
        this.consumptionPoint = consumptionPoint;
    }
    
    public String getRejCode()
    {
        if(null != dataList && dataList.length > 0)
        {
            rejCode = dataList[28];
        }
        return rejCode;
    }
    
    public void setRejCode(String rejCode)
    {
        this.rejCode = rejCode;
    }
    
    public String getTrtcCtsn()
    {
        if(null != dataList && dataList.length > 0)
        {
            trtcCtsn = dataList[27];
        }
        return trtcCtsn;
    }
    
    public void setTrtcCtsn(String trtcCtsn)
    {
        this.trtcCtsn = trtcCtsn;
    }
    
    public String getInOutCode()
    {
        if(null != dataList && dataList.length > 0)
        {
            inOutCode = dataList[26];
        }
        return inOutCode;
    }
    
    public void setInOutCode(String inOutCode)
    {
        this.inOutCode = inOutCode;
    }
            
    
    public String getFirstDeductValue()
    {
        if(null != dataList && dataList.length > 0)
        {
            firstDeductValue = dataList[25];
        }
        return firstDeductValue;
    }
    
    public void setFirstDeductValue(String firstDeductValue)
    {
        this.firstDeductValue = firstDeductValue;
    }
    
    public String getFirstEntryStation()
    {
        if(null != dataList && dataList.length > 0)
        {
            firstEntryStation = dataList[24];
        }
        return firstEntryStation;
    }
    
    public void setFirstEntryStation(String firstEntryStation)
    {
        this.firstEntryStation = firstEntryStation;
    }
    
    public String getShiftNbr()
    {
        if(null != dataList && dataList.length > 0)
        {
            shiftNbr = dataList[23];
        }
        return shiftNbr;
    }
    
    public void setShiftNbr(String shiftNbr)
    {
        this.shiftNbr = shiftNbr;
    }
    
    public String getTxprocessDate()
    {
        if(null != dataList && dataList.length > 0)
        {
            txprocessDate = dataList[22];
        }
        return txprocessDate;
    }
    
    public void setTxprocessDate(String txprocessDate)
    {
        this.txprocessDate = txprocessDate;
    }
    
    public String getTransDateCancel()
    {
        if(null != dataList && dataList.length > 0)
        {
            transDateCancel = dataList[21];
        }
        return transDateCancel;
    }
    
    public void setTransDateCancel(String transDateCancel)
    {
        this.transDateCancel = transDateCancel;
    }
    
    public String getTransNoCancel()
    {
        if(null != dataList && dataList.length > 0)
        {
            transNoCancel = dataList[20];
        }
        return transNoCancel;
    }
    
    public void setTransNoCancel(String transNoCancel)
    {
        this.transNoCancel = transNoCancel;
    }
    
    public String getStoreId()
    {
        if(null != dataList && dataList.length > 0)
        {
            storeId = dataList[19];
        }
        return storeId;
    }
    
    public void setStoreId(String storeId)
    {
        this.storeId = storeId;
    }
    
    public String getTermId()
    {
        if(null != dataList && dataList.length > 0)
        {
            termId = dataList[18];
        }
        return termId;
    }
    
    public void setTermId(String termId)
    {
        this.termId = termId;
    }
    
    public String getTermTxnDate()
    {
        if(null != dataList && dataList.length > 0)
        {
            termTxnDate = dataList[17];
        }
        return termTxnDate;
    }
    
    public void setTermTxnDate(String termTxnDate)
    {
        this.termTxnDate = termTxnDate;
    }
    
    public String getTermTxnSeq()
    {
        if(null != dataList && dataList.length > 0)
        {
            termTxnSeq = dataList[16];
        }
        return termTxnSeq;
    }
    
    public void getTermTxnSeq(String termTxnSeq)
    {
        this.termTxnSeq = termTxnSeq;
    }
    
    public String getSamMac()
    {
        if(null != dataList && dataList.length > 0)
        {
            samMac = dataList[15];
        }
        return samMac;
    }
    
    public void setSamMac(String samMac)
    {
        this.samMac = samMac;
    }
    
    public String getSamTransNo()
    {
        if(null != dataList && dataList.length > 0)
        {
            samTransNo = dataList[14];
        }
        return samTransNo;
    }
    
    public void setSamTransNo(String samTransNo)
    {
        this.samTransNo = samTransNo;
    }
    
    public String getSamOsn()
    {
        if(null != dataList && dataList.length > 0)
        {
            samOsn = dataList[13];
        }
        return samOsn;
    }
    
    public void setSamOsn(String samOsn)
    {
        this.samOsn = samOsn;
    }
    
    public String getDevId()
    {
        if(null != dataList && dataList.length > 0)
        {
            devId = dataList[12];
        }
        return devId;
    }
    
    public void setDevId(String devId)
    {
        this.devId = devId;
    }
    
    public String getLocId()
    {
        if(null != dataList && dataList.length > 0)
        {
            locId = dataList[11];
        }
        return locId;
    }
    
    public void setLocId(String locId)
    {
        this.locId = locId;
    }
    
    public String getTransSysNo()
    {
        if(null != dataList && dataList.length > 0)
        {
            transSysNo = dataList[10];
        }
        return transSysNo;
    }
    
    public void setTransSysNo(String transSysNo)
    {
        this.transSysNo = transSysNo;
    }
    
    public String getAfterAmt()
    {
        if(null != dataList && dataList.length > 0)
        {
            afterAmt = dataList[9];
        }
        return afterAmt;
    }
    
    public void setAfterAmt(String afterAmt)
    {
        this.afterAmt = afterAmt;
    }
    
    public String getTxnAmt()
    {
        if(null != dataList && dataList.length > 0)
        {
            txnAmt = dataList[8];
        }
        return txnAmt;
    }
    
    public void setTxnAmt(String txnAmt)
    {
        this.txnAmt = txnAmt;
    }
    
    public String getBeforAmt()
    {
        if(null != dataList && dataList.length > 0)
        {
            beforAmt = dataList[7];
        }
        return beforAmt;
    }
    
    public void setBeforAmt(String beforAmt)
    {
        this.beforAmt = beforAmt;
    }
    
    public String getTransType()
    {
        if(null != dataList && dataList.length > 0)
        {
            transType = dataList[6];
        }
        return transType;
    }
    
    public void setTransType(String transType)
    {
        this.transType = transType;
    }
    
    public String getTransDate()
    {
        if(null != dataList && dataList.length > 0)
        {
            transDate = dataList[5];
        }
        return transDate;
    }
    
    public void setTransDate(String transDate)
    {
        this.transDate = transDate;
    }
    
    public String getTransNo()
    {
        if(null != dataList && dataList.length > 0)
        {
            transNo = dataList[4];
        }
        return transNo;
    }
    
    public void setTransNo(String transNo)
    {
        this.transNo = transNo;
    }
    
    public String getPId()
    {
        if(null != dataList && dataList.length > 0)
        {
            pid = dataList[3];
        }
        return pid;
    }
    
    public void setPId(String pid)
    {
        this.pid = pid;
    }
    
    public String getMifareId()
    {
        if(null != dataList && dataList.length > 0)
        {
            mifareId = dataList[2];
        }
        return mifareId;
    }
    
    public void setMifareId(String mifareId)
    {
        this.mifareId = mifareId;
    }
    
    public String getTxnType()
    {
        if(null != dataList && dataList.length > 0)
        {
            txnType = dataList[1];
        }
        return txnType;
    }
    
    public void setTxnType(String txnType)
    {
        this.txnType = txnType ;
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
