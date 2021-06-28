package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpTxnDtl;

import java.math.BigInteger;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.apache.log4j.Logger;

import tw.com.hyweb.svc.yhdp.batch.framework.Layer1Constants;
import tw.com.hyweb.svc.yhdp.batch.impfiles.ImpTxnDtl.TrafficTxnDetail;
import tw.com.hyweb.svc.yhdp.online.TxException;
import tw.com.hyweb.svc.yhdp.online.util.Rcode;
import tw.com.hyweb.svc.yhdp.online.util.YHDPUtil;
import tw.com.hyweb.util.ISOUtil;
import tw.com.hyweb.util.string.StringUtil;

public class TrafficTxn
{
    private static final Logger logger = Logger.getLogger(TrafficTxn.class);
    public static final String ITEM_HEADER = "3031";
    public static final String ITEM_BUS = "3130";
    public static final String ITEM_BUS_STEP = "3131";
    public static final String ITEM_PARK = "3132";
    private TrafficTxnHeader tfcTxnHeader;
    private TrafficTxnDetail tfcTxnDtl;
    private int headerLen = -1;
    private int detailLen = -1;
    
    public void setTfcTxnDtl(TrafficTxnDetail tfcTxnDtl)
    {
        this.tfcTxnDtl = tfcTxnDtl;
    }
    
    public TrafficTxnDetail getTfcTxnDtl()
    {
        return this.tfcTxnDtl;
    }
    
    public void setTfcTxnHeader(TrafficTxnHeader tfcTxnHeader)
    {
        this.tfcTxnHeader = tfcTxnHeader;
    }
    
    public TrafficTxnHeader getTfcTxnHeader()
    {
        return this.tfcTxnHeader;
    }
    
    public void dataListToString(boolean isHeader, String[] rawDataList, String[] dataList)
    {
        String item = "Detail";
        TrafficDataFormat[] dataFormatList;
        if(isHeader)
        {
            dataFormatList = tfcTxnHeader.getDataFormatList();
            item = "Header";
        }
        else
        {
            dataFormatList = tfcTxnDtl.getDataFormatList();
        }
        StringBuffer sb = new StringBuffer();
        if(null != dataList && dataList.length > 0)
        {
            logger.debug(item +" begin");
            for(int idx = 0;idx < dataList.length; idx++)
            {
                sb.append(dataFormatList[idx].getFieldName() + " RAW[" + rawDataList[idx] + "] DEC[" + dataList[idx] + "], " );
                if(idx % 5 == 0)
                    sb.append("\n");
            }
            logger.debug(sb.toString());
        }
        else
        {
            logger.debug(item +" not data");
        }
    }
    
    public int getDataLen(boolean isHeader)
    {
        int length = 0;
        TrafficDataFormat[] dataFormatList;
        if(isHeader)
        {
            dataFormatList = tfcTxnHeader.getDataFormatList();
            length = headerLen;
        }
        else
        {
            dataFormatList = tfcTxnDtl.getDataFormatList();
            length = detailLen;
        }
      
        if(length == -1)
        {
            length = 0;
            if(null != dataFormatList)
            {
                for(int idx = 0; idx < dataFormatList.length; idx++)
                {
                    length += dataFormatList[idx].length;
                }
            }
        }
        return (length * 2);
    }
    
    public void checkDataLen(BATCHContext ctx)
    {
        String parserData = ctx.getRawData();
        if (parserData.substring(0, 4).equals(TrafficTxn.ITEM_HEADER) && parserData.length() != getDataLen(true)) {
            ctx.setErrDesc("Header fileSize(" + parserData.length() + ") != config fileSize(" + getDataLen(true) + ")");
            logger.error(ctx.getErrDesc());
            ctx.setRcode(Layer1Constants.RCODE_2713_SETTING1_ERR);
        }
        else if (parserData.substring(0, 4).equals(TrafficTxn.ITEM_BUS) && parserData.length() != getDataLen(false)) {
            ctx.setErrDesc("Detail fileSize(" + parserData.length() + ") != config fileSize(" + getDataLen(false) + ")");
            logger.error(ctx.getErrDesc());
            ctx.setRcode(Layer1Constants.RCODE_2713_SETTING1_ERR);
        }/* check detail */
        else if (parserData.substring(0, 4).equals(TrafficTxn.ITEM_BUS_STEP) && parserData.length() != getDataLen(false)) {
            ctx.setErrDesc("Detail fileSize(" + parserData.length() + ") != config fileSize(" + getDataLen(false) + ")");
            logger.error(ctx.getErrDesc());
            ctx.setRcode(Layer1Constants.RCODE_2713_SETTING1_ERR);
        }
    }
    
    public TrafficTxnHeader parserHeader(String parserData) throws TxException
    {
        int beginIndex = 0;
        int endIndex = 0;
        TrafficDataFormat[] dataFormatList = tfcTxnHeader.getDataFormatList();;
        TrafficTxnHeader header = new TrafficTxnHeader();
        header.setDataFormatList(dataFormatList);
        String[] dataList = new String[dataFormatList.length];
        String[] rawDataList = new String[dataFormatList.length];
        String rawData;
        String cnvData = null;
        String checkEmpty = "";
        for(int idx = 0; idx < dataList.length; idx++)
        {
            endIndex += (dataFormatList[idx].getLength() * 2);
            rawData = parserData.substring(beginIndex, endIndex);
            checkEmpty = ISOUtil.padLeft("", dataFormatList[idx].getLength() *2, '0');
            rawDataList[idx] = rawData;
            if(dataFormatList[idx].getFormat() == 0)//AN
            {
                if(checkEmpty.equals(rawData))
                {
                    cnvData = "";
                }
                else
                {
                    cnvData =  new String(ISOUtil.hex2byte(rawData));
                }
            }
            else//binary
            {
                if(dataFormatList[idx].getLsb() == 1)//hex轉int
                {
                    //check lsb
                    cnvData =  hex2IntString(rawData);
                }
                else if(dataFormatList[idx].getLsb() == 2)//unixtime轉yyyymmddhh24miss
                {
                    //check lsb
                    cnvData = unixTimeToYYYMMDD(rawData);
                }
                else
                {
                    cnvData = rawData;
                }
            }
            checkIsEmpty(dataFormatList[idx], cnvData);
            dataList[idx] = cnvData;
            beginIndex += (dataFormatList[idx].getLength() * 2);
        }
        header.setDataList(dataList);
        header.setRawDataList(rawDataList);
        return header;
    }
    
    public TrafficTxnDetail parserDetail(String parserData) throws TxException
    {
        int beginIndex = 0;
        int endIndex = 0;
        TrafficDataFormat[] dataFormatList = tfcTxnDtl.getDataFormatList();
        TrafficTxnDetail detail = new TrafficTxnDetail();
        String[] dataList = new String[dataFormatList.length];
        String[] rawDataList = new String[dataFormatList.length];
        String rawData;
        String cnvData = null;
        String checkEmpty = "";
        detail.setDataFormatList(dataFormatList);
        for(int idx = 0; idx < dataList.length; idx++)
        {
            endIndex += (dataFormatList[idx].getLength() * 2);
            rawData = parserData.substring(beginIndex, endIndex);
            checkEmpty = ISOUtil.padLeft("", dataFormatList[idx].getLength() *2, '0');
            rawDataList[idx] = rawData;
            if(dataFormatList[idx].getFormat() == 0)//AN
            {
                if(checkEmpty.equals(rawData))
                {
                    cnvData = "";
                }
                else
                {
                    cnvData =  new String(ISOUtil.hex2byte(rawData));
                }
            }
            else//binary
            {
                if(dataFormatList[idx].getLsb() == 1)//hex轉int
                {
                    //check lsb
                    cnvData =  hex2IntString(rawData);
                }
                else if(dataFormatList[idx].getLsb() == 2)//unixtime轉yyyymmddhh24miss
                {
                    //check lsb
                    cnvData = unixTimeToYYYMMDD(rawData);
                }
                else
                {
                    cnvData = rawData;
                }
            }
            checkIsEmpty(dataFormatList[idx], cnvData);
            dataList[idx] = cnvData;
            beginIndex += (dataFormatList[idx].getLength() * 2);
        }
        detail.setDataList(dataList);
        detail.setRawDataList(rawDataList);
        return detail;
    }
    
    public void checkIsEmpty(TrafficDataFormat dataFormat, String rawData) throws TxException
    {
        if(dataFormat.getIsNecessary())
        {
            if(StringUtil.isEmpty(rawData.trim()))
            {
                logger.warn(dataFormat.getFieldName() + "[" + rawData + "] is empty");
                throw new TxException(dataFormat.getFieldName() + "[" + rawData + "] is empty");
            }
        }
    }
    
    public String hex2IntString(String lsdHex)
    {
        int amt = 0;
        if(!StringUtil.isEmpty(lsdHex))
        {
            amt = Integer.parseInt(lsdHex, 16);
        }
        return String.valueOf(amt);
    }
    
    public String unixTimeToYYYMMDD(String lsdHex)
    {
        if(!StringUtil.isEmpty(lsdHex))
        {
            if(!lsdHex.equals("00000000"))
            {
                DateFormat dFormat = new SimpleDateFormat("yyyyMMddHHmmss");
                long unixTime = new BigInteger(lsdHex, 16).longValue()*1000;
                Date currDate = new Date(unixTime);
                return dFormat.format(currDate);
            }
        }
        return "";
    }
}
