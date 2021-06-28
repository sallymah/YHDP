package tw.com.hyweb.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.util.HashMap;

import org.apache.log4j.Logger;

import tw.com.hyweb.core.yhdp.batch.framework.impfiles.FieldInfo;
import tw.com.hyweb.core.cp.common.hsm.HsmAdapter;
import tw.com.hyweb.core.service.hsm.HsmInterface;
import tw.com.hyweb.core.service.hsm.HsmResult;
import tw.com.hyweb.core.service.hsm.HsmService;
import tw.com.hyweb.core.yhdp.batch.framework.impfiles.MappingInfo;
import tw.com.hyweb.core.yhdp.batch.framework.impfiles.MappingLoader;
import tw.com.hyweb.core.yhdp.common.misc.DateUtil;
import tw.com.hyweb.service.db.DBService;
import tw.com.hyweb.service.db.info.TbInctlErrInfo;
import tw.com.hyweb.service.db.info.TbInctlInfo;
import tw.com.hyweb.service.db.mgr.TbInctlErrMgr;
import tw.com.hyweb.svc.yhdp.batch.preoperation.CheckPersoFeeback.bean.YhdpPersoFeedBackBean;
import tw.com.hyweb.util.string.StringUtil;

public class IFFUtils 
{
	private static final Logger LOGGER = Logger.getLogger(IFFUtils.class);
	
	private static BufferedOutputStream bw = null;
    private static BufferedInputStream br = null;
    
    // mapping config filename
    protected static String configFilename = "config/batch/MappingInfos.xml";
    // mapping config filename encoding
    protected static String encoding = "UTF-8";
    
    protected static MappingInfo mappingInfo = null;
	
    public static MappingInfo getMappingInfo() {
		return mappingInfo;
	}

	public static void setMappingInfo(MappingInfo mappingInfo) {
		IFFUtils.mappingInfo = mappingInfo;
	}
	
	public static void initial(String fileName) throws Exception 
	{
		MappingLoader ml = new MappingLoader();
        ml.setConfigFilename(configFilename);
        ml.setFile(new File(configFilename));
        ml.setEncoding(encoding);
        ml.startLoading();
        mappingInfo = ml.getMappingInfo(fileName);
        if (mappingInfo == null) {
            throw new Exception("mappingInfo(" + fileName + ") is null!");
        }
	}

	public static HashMap<String, String> cacheRowData(String fileName, String dataLine) throws Exception 
    {
    	int startIdx = 0;
	    int endIdx = 0;
	    
	    initial(fileName);

	    HashMap<String, String> hm = new HashMap<String, String>();
    	for(int i=0; i< mappingInfo.getFields().size(); i++)
    	{
	        startIdx = ((FieldInfo)mappingInfo.getFields().get(i)).getStart();  
	        endIdx = startIdx + ((FieldInfo)mappingInfo.getFields().get(i)).getLength();
	        
	        hm.put(((FieldInfo)mappingInfo.getFields().get(i)).getName(), dataLine.substring(startIdx, endIdx)); 
    	}
    	
    	LOGGER.debug(hm.toString());
    	return hm;
	}
	
	public static YhdpPersoFeedBackBean organizeRowData(String dataLine) throws Exception 
	{
		HashMap<String, String> hm = cacheRowData("IFF", dataLine);
		
		YhdpPersoFeedBackBean dataBean = new YhdpPersoFeedBackBean();
		dataBean.setCardNo(hm.get("field02"));
		dataBean.setMifareId(hm.get("field14"));
		dataBean.setIssNo(hm.get("field18"));
		dataBean.setIssEquipmentNo(hm.get("field19"));
		dataBean.setPersoBatchNo(hm.get("field20"));
		dataBean.setActiveDate(hm.get("field21"));
		dataBean.setExpiryDate(hm.get("field22"));
		dataBean.setCardVersion(hm.get("field23"));
		dataBean.setCardStatus(hm.get("field24"));
		dataBean.setCheckCode1(hm.get("field25"));
		dataBean.setAutoReloadFlag(hm.get("field26"));
		dataBean.setAutoReloadValue(hm.get("field27"));
		dataBean.setBalMaxbal(hm.get("field28"));
		dataBean.setMinBalAmt8(hm.get("field29"));
		dataBean.setAppointReloadFlag(hm.get("field30"));
		dataBean.setAppointReloadValue(hm.get("field31"));
		dataBean.setAutoReloadDate(hm.get("field32"));
		dataBean.setOlAutoReloadMaxTime(hm.get("field33"));
		dataBean.setAutoReloadMaxTime(hm.get("field34"));
		dataBean.setAppointReloadMaxTime(hm.get("field35"));
		dataBean.setCheckCode2(hm.get("field36"));
		dataBean.setHexBarcode1(hm.get("field120"));
		dataBean.setHexPackMaterialNo(hm.get("field124"));
		dataBean.setHexCardMaterialNo(hm.get("field125"));
		dataBean.setHexPackNo(hm.get("field126"));
		dataBean.setHexChestNo(hm.get("field127"));
		dataBean.setHexBoxNo(hm.get("field128"));
		
		return dataBean;
	}
    
	public static void insertInctlErrInfo(int idx, String lineInfo, TbInctlInfo inctlInfo, String rcode, String desc) throws Exception 
	{
		Connection conn = null;
		try {
			conn = DBService.getDBService().getConnection("batch");
	    	if (inctlInfo == null) {
	            throw new IllegalArgumentException("inctlInfo is null!");
	        }    	 
	        TbInctlErrInfo inctlErrInfo = new TbInctlErrInfo();
	        // set by inctlInfo
	        inctlErrInfo.setMemId(inctlInfo.getMemId());
	        inctlErrInfo.setFileName(inctlInfo.getFileName());
	        inctlErrInfo.setFileDate(inctlInfo.getFileDate());
	        inctlErrInfo.setSeqno(inctlInfo.getSeqno());
	        inctlErrInfo.setFileType(inctlInfo.getFileType());
	        inctlErrInfo.setFullFileName(inctlInfo.getFullFileName());
	        // set by lineInfo
	        if (lineInfo == null) {
	            inctlErrInfo.setLineNo(new Integer(0));
	            inctlErrInfo.setMessage("");
	            inctlErrInfo.setMessageLen(new Integer(0));
	        }
	        else {
	            inctlErrInfo.setLineNo(idx++);
	            String message = StringUtil.getMaxString(lineInfo, "UTF-8", 1200);
	            inctlErrInfo.setMessage(message);
	            inctlErrInfo.setMessageLen(new Integer(lineInfo.length()));

	        }
	        String lineSep = System.getProperty("line.separator", "\n");
	        // set by descInfo
	        StringBuffer sb = new StringBuffer();
	        sb.append("[errorCode]=" + rcode + " [errorMsg]=資料不合法" + " [content]=" + desc);
	        // avoid exceed table field length
	        String errorDesc = StringUtil.getMaxString(sb.toString(), "UTF-8", 1200);
	        inctlErrInfo.setErrorDesc(errorDesc);
	        // set others
	        String dateTime = DateUtil.getTodayString();
	        String sysDate = dateTime.substring(0, 8);
	        String sysTime = dateTime.substring(8, 14);
	        String parMon = dateTime.substring(4, 6);
	        String parDay = dateTime.substring(6, 8);
	        inctlErrInfo.setSysDate(sysDate);
	        inctlErrInfo.setSysTime(sysTime);
	        inctlErrInfo.setParMon(parMon);
	        inctlErrInfo.setParDay(parDay);
	        TbInctlErrMgr mgr = new TbInctlErrMgr(conn);
	        mgr.insert(inctlErrInfo);
	        conn.commit();
		} finally {
			conn.close();
		}   
    }
	
	public static String getInctlErrInfo(int idx, String lineInfo, TbInctlInfo inctlInfo, String rcode, String desc) throws Exception 
	{

    	if (inctlInfo == null) {
            throw new IllegalArgumentException("inctlInfo is null!");
        }    	 
        TbInctlErrInfo inctlErrInfo = new TbInctlErrInfo();
        // set by inctlInfo
        inctlErrInfo.setMemId(inctlInfo.getMemId());
        inctlErrInfo.setFileName(inctlInfo.getFileName());
        inctlErrInfo.setFileDate(inctlInfo.getFileDate());
        inctlErrInfo.setSeqno(inctlInfo.getSeqno());
        inctlErrInfo.setFileType(inctlInfo.getFileType());
        inctlErrInfo.setFullFileName(inctlInfo.getFullFileName());
        // set by lineInfo
        if (lineInfo == null) {
            inctlErrInfo.setLineNo(new Integer(0));
            inctlErrInfo.setMessage("");
            inctlErrInfo.setMessageLen(new Integer(0));
        }
        else {
            inctlErrInfo.setLineNo(idx++);
            String message = StringUtil.getMaxString(lineInfo, "UTF-8", 1200);
            inctlErrInfo.setMessage(message);
            inctlErrInfo.setMessageLen(new Integer(lineInfo.length()));

        }
        String lineSep = System.getProperty("line.separator", "\n");
        // set by descInfo
        StringBuffer sb = new StringBuffer();
        sb.append("[errorCode]=" + rcode + " [errorMsg]=資料不合法" + " [content]=" + desc);
        // avoid exceed table field length
        String errorDesc = StringUtil.getMaxString(sb.toString(), "UTF-8", 1200);
        inctlErrInfo.setErrorDesc(errorDesc);
        // set others
        String dateTime = DateUtil.getTodayString();
        String sysDate = dateTime.substring(0, 8);
        String sysTime = dateTime.substring(8, 14);
        String parMon = dateTime.substring(4, 6);
        String parDay = dateTime.substring(6, 8);
        inctlErrInfo.setSysDate(sysDate);
        inctlErrInfo.setSysTime(sysTime);
        inctlErrInfo.setParMon(parMon);
        inctlErrInfo.setParDay(parDay);
        
        
        return inctlErrInfo.toInsertSQL();
    }
	
	public static void encrypt2File(String file, String tmpFile, String keyId, String iv, int maxLength) throws Exception 
	{
		String enString1 = null;
		String enString2 = null;
		
		try {
			br = new BufferedInputStream(new FileInputStream(new File(tmpFile)));
			bw = new BufferedOutputStream(new FileOutputStream(new File(file)));
			
			int times = br.available() / maxLength;
			//logger.info("times: " + times);
			int bytes = br.available() % maxLength;
			//logger.info("bytes: " + bytes);
			
			byte[] buffer1 = new byte[maxLength];
			byte[] buffer2 = new byte[bytes];

			if(times >0) {
				for(int i=0; i<times; i++)
				{
					br.read(buffer1);
					if(bytes == 0) {
						enString1 = encryptECB(ISOUtil.hexString(buffer1), keyId, iv, true);
					}
					else {
						enString1 = encryptECB(ISOUtil.hexString(buffer1), keyId, iv, false);
					}
					
					//logger.info("encrypt String1: " + enString1);
					bw.write(ISOUtil.hex2byte(enString1));
				}
			}
			
			if(bytes != 0) {
				br.read(buffer2);
				enString2 = encryptECB(ISOUtil.hexString(buffer2), keyId, iv, true);
				//logger.info("encrypt String2: " + enString2);
				bw.write(ISOUtil.hex2byte(enString2));
			}
			
			/*String deString = decryptCBC(enString2);
			logger.info("decrypt: " + deString);*/
			
			bw.flush();
		} 
		finally {
			ReleaseResource.releaseIO(br);	
			ReleaseResource.releaseIO(bw);
		}
	}  
	
	private static String encryptECB(String inputData, String keyId, String iv, Boolean appendFlag)
	{
    	String appendInputData = "";
    	//logger.info("Hsm ip: " + System.getenv("ET_HSM_NETCLIENT_SERVERLIST"));
    	String encString = "";
    	HsmAdapter ham = new HsmAdapter(HsmService.getHsmService());
    	
    	if(appendFlag == true) {
    		appendInputData = appendPKCS7(inputData);
    	}
    	else {
    		appendInputData = inputData;
    	}

		HsmResult hsmR = ham.CryptoDES3(HsmInterface.DES_ENCRYPT_ECB, keyId, appendInputData, iv);
		if (null != hsmR && hsmR.getValue() == 0)
		{
			encString = hsmR.getString(0);
			//logger.debug("encString = " + encString);
		}
		else
		{
			//logger.error("HSM process error: " + hsmR.getValue());
		}
    	return encString;
    }
	
	public static String appendPKCS7(String inputData)
    {
    	//logger.info("size: " + inputData.length());
    	//logger.info("org input data: " + inputData);
    	int rem = (inputData.length() % 16);
    	//logger.info("rem: " + rem);
    	String appendString = null;
    	
    	switch (rem) {
/*		case 1:
			appendString = org.apache.commons.lang.StringUtils.rightPad(inputData, inputData.length() + (16 - rem), "15");
			break;
			*/
		case 2:
			appendString = org.apache.commons.lang.StringUtils.rightPad(inputData, inputData.length() + (16 - rem), "07");
			break;	
	
/*		case 3:
			appendString = org.apache.commons.lang.StringUtils.rightPad(inputData, inputData.length() + (16 - rem), "13");
			break;	
			*/
		case 4:
			appendString = org.apache.commons.lang.StringUtils.rightPad(inputData, inputData.length() + (16 - rem), "06");
			break;
			
/*		case 5:
			appendString = org.apache.commons.lang.StringUtils.rightPad(inputData, inputData.length() + (16 - rem), "11");
			break;
			*/
		case 6:
			appendString = org.apache.commons.lang.StringUtils.rightPad(inputData, inputData.length() + (16 - rem), "05");
			break;
			
/*		case 7:
			appendString = org.apache.commons.lang.StringUtils.rightPad(inputData, inputData.length() + (16 - rem), "09");
			break;
			*/
		case 8:
			appendString = org.apache.commons.lang.StringUtils.rightPad(inputData, inputData.length() + (16 - rem), "04");
			break;
			
/*		case 9:
			appendString = org.apache.commons.lang.StringUtils.rightPad(inputData, inputData.length() + (16 - rem), "07");
			break;
			*/
		case 10:
			appendString = org.apache.commons.lang.StringUtils.rightPad(inputData, inputData.length() + (16 - rem), "03");
			break;
			
/*		case 11:
			appendString = org.apache.commons.lang.StringUtils.rightPad(inputData, inputData.length() + (16 - rem), "05");
			break;
			*/
		case 12:
			appendString = org.apache.commons.lang.StringUtils.rightPad(inputData, inputData.length() + (16 - rem), "02");
			break;
			
/*		case 13:
			appendString = org.apache.commons.lang.StringUtils.rightPad(inputData, inputData.length() + (16 - rem), "03");
			break;
			*/
		case 14:
			appendString = org.apache.commons.lang.StringUtils.rightPad(inputData, inputData.length() + (16 - rem), "01");
			break;
			
/*		case 15:
			appendString = org.apache.commons.lang.StringUtils.rightPad(inputData, inputData.length() + (16 - rem), "01");
			break;
			*/
		default:
			appendString = org.apache.commons.lang.StringUtils.rightPad(inputData, inputData.length() + (16 - rem), "08");
			break;
		}	
    	//logger.info("append input data: " + appendString);
    	return appendString;
    }
	
	public static void decryptToFile(String file, String tmpFile, String keyId, String iv, int maxLength) throws Exception 
    {
		String enString1 = null;
		String enString2 = null;
		
		try {
			br = new BufferedInputStream(new FileInputStream(new File(file)));
	        bw = new BufferedOutputStream(new FileOutputStream(new File(tmpFile)));
		
			int times = br.available() / maxLength;
			//LOGGER.info("times: " + times);
			int bytes = br.available() % maxLength;
			//LOGGER.info("bytes: " + bytes);
			
			byte[] buffer1 = new byte[maxLength];
			byte[] buffer2 = new byte[bytes];
			
			if(times >0) {
				for(int i=0; i<times; i++)
				{
					br.read(buffer1);
					if(bytes == 0) {
						enString1 = descryptECB(ISOUtil.hexString(buffer1), keyId, iv);
					}
					else {
						enString1 = descryptECB(ISOUtil.hexString(buffer1), keyId, iv);
					}
					
					LOGGER.info("encrypt String1: " + enString1);
					bw.write(ISOUtil.hex2byte(enString1));
				}
			}
			
			if(bytes != 0) {
				br.read(buffer2);
				enString2 = descryptECB(ISOUtil.hexString(buffer2), keyId, iv);
				LOGGER.info("encrypt String2: " + enString2);
				bw.write(ISOUtil.hex2byte(enString2));
			}
			bw.flush();
		} 
        finally {
        	ReleaseResource.releaseIO(br);	
    		ReleaseResource.releaseIO(bw);
        }
	}
	
	public static String descryptECB(String inputData, String keyId, String iv) throws Exception
	{
		LOGGER.info("Hsm ip: " + System.getenv("ET_HSM_NETCLIENT_SERVERLIST"));
	   	String encString = "";
	   	HsmAdapter ham = new HsmAdapter(HsmService.getHsmService());
	   	
	   	LOGGER.info("KeyId: " + keyId);
	   	LOGGER.info("Iv: " + iv);
		HsmResult hsmR = ham.CryptoDES3(HsmInterface.DES_DECRYPT_ECB, keyId, inputData, iv);
		if (null != hsmR && hsmR.getValue() == 0)
		{
			encString = hsmR.getString(0);
			LOGGER.debug("encString = " + encString);
		}
		else
		{
			LOGGER.info("HSM process error: " + hsmR.getValue());
			throw new Exception("HSM process error: " + hsmR.getValue());
		}
		return encString;
	}
	
	public static byte XorData(String data)
    {
    	if(data.length() % 2 != 0) {
    		data += "F";
    	}
    	
    	byte[] bytes = ISOUtil.hex2byte(data);
		byte tmpByte = 0;
		
		for(int b=0; b<bytes.length; b++)
		{
			if(b==0) {
				tmpByte = bytes[b];
				continue;
			}
			tmpByte ^= bytes[b];
		}
		return tmpByte;
    }
	
	public static String hex2ascii(String hex) 
	{
		StringBuilder output = new StringBuilder();
		for (int i = 0; i < hex.length(); i+=2) 
		{
			String str = hex.substring(i, i+2);
			output.append((char)Integer.parseInt(str, 16));
		}
		return output.toString();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
}
