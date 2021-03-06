package tw.com.hyweb.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import tw.com.hyweb.core.cp.batch.util.BatchUtil;
import tw.com.hyweb.core.cp.common.hsm.HsmAdapter;
import tw.com.hyweb.core.service.hsm.HsmInterface;
import tw.com.hyweb.core.service.hsm.HsmResult;
import tw.com.hyweb.core.service.hsm.HsmService;
import tw.com.hyweb.svc.yhdp.batch.framework.perso.CgYhdpSsf;
import tw.com.hyweb.util.ISOUtil;
import tw.com.hyweb.util.ReleaseResource;

public class CipherUtils 
{
	private static final Logger LOGGER = Logger.getLogger(CipherUtils.class);
	
	private final static int DATA_MAX_LENGTH = 65512;
	
	private static BufferedOutputStream bw = null;
    private static BufferedInputStream br = null;
    
	public static void encrypt2File(String cipherFile, String origFile, 
			String keyId, String iv, String servletUrl, Integer slot, String pin) throws Exception 
	{
		if (DATA_MAX_LENGTH % 8 != 0) {
			throw new Exception("max length can not divided by 8");
		}
		
		br = new BufferedInputStream(new FileInputStream(new File(origFile)));
		bw = new BufferedOutputStream(new FileOutputStream(new File(cipherFile)));
		
		int times = br.available() / DATA_MAX_LENGTH;
		int bytes = br.available() % DATA_MAX_LENGTH;
		
		LOGGER.info("times:[" + (bytes==0?times:(times+1)) + "], bytes:[" + bytes + "]");
		
		byte[] buffer1 = new byte[DATA_MAX_LENGTH];
		byte[] buffer2 = new byte[bytes];
		
		String enString1 = null;
		String enString2 = null;
		
		for(int i=0; i<times; i++)
		{
			br.read(buffer1);
			if ("P3EncKey".equals(keyId)) {
				enString1 = encryptECB(ISOUtil.hexString(buffer1), keyId, iv, false);	
			} else {
				enString1 = encryptECB(ISOUtil.hexString(buffer1), keyId, iv, servletUrl, slot, pin, false);
			}
			LOGGER.debug("encrypt each one: " + enString1);
			bw.write(ISOUtil.hex2byte(enString1));
		}
		
		if (bytes != 0) {
			br.read(buffer2);
			if ("P3EncKey".equals(keyId)) {
				enString2 = encryptECB(ISOUtil.hexString(buffer2), keyId, iv, true);
			} else {
				enString2 = encryptECB(ISOUtil.hexString(buffer2), keyId, iv, servletUrl, slot, pin, true);
			}
			LOGGER.info("encrypt last one: " + enString2);
			bw.write(ISOUtil.hex2byte(enString2));
		}
		
		bw.flush();
		ReleaseResource.releaseIO(br);	
		ReleaseResource.releaseIO(bw);	
	}
	
	private static String encryptECB(String inputData, String keyId, String iv, Boolean appendFlag) throws Exception
	{
    	String appendInputData = "";
    	LOGGER.info("Hsm ip: " + System.getenv("ET_HSM_NETCLIENT_SERVERLIST") + ", KeyId: " + keyId + ", Iv: " + iv);
    	
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
		}
		else
		{
			LOGGER.error("HSM process error: " + hsmR.getValue());
			throw new Exception("HSM process error: " + hsmR.getValue());
		}
    	return encString;
    }
	
	private static String encryptECB(String inputData, 
			String keyId, String iv, String servletUrl, Integer slot, String pin, 
			Boolean appendFlag) throws Exception {
		CgYhdpSsf cgYhdpSsf = new CgYhdpSsf();
		cgYhdpSsf.setKeyId(keyId);
		cgYhdpSsf.setIv(iv);
		cgYhdpSsf.setServletUrl(servletUrl);
		cgYhdpSsf.setSlot(slot);
		cgYhdpSsf.setPin(pin);
		return cgYhdpSsf.encryptDES3(inputData, appendFlag);
	}
	
	private static String appendPKCS7(String inputData)
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
	
	public static void decryptToFile(String cipherFile, String origFile, 
			String keyId, String iv, String servletUrl, Integer slot, String pin) throws Exception 
    {
		String deString1 = null;
		String deString2 = null;
		
		if(DATA_MAX_LENGTH % 8 != 0) {
			throw new Exception("max length can not divided by 8");
		}
		
        br = new BufferedInputStream(new FileInputStream(new File(origFile)));
        bw = new BufferedOutputStream(new FileOutputStream(new File(cipherFile)));
	
		int times = br.available() / DATA_MAX_LENGTH;
		int bytes = br.available() % DATA_MAX_LENGTH;
		LOGGER.info("times:[" + (bytes==0?times:(times+1)) + "], bytes:[" + bytes + "]");
		
		byte[] buffer1 = new byte[DATA_MAX_LENGTH];
		byte[] buffer2 = new byte[bytes];
		
		for(int i=0; i<times; i++)
		{
			br.read(buffer1);
			if ("P3EncKey".equals(keyId)) {
				deString1 = descryptECB(ISOUtil.hexString(buffer1), keyId, iv);
			} else {
				deString1 = descryptECB(ISOUtil.hexString(buffer1), keyId, iv, servletUrl, slot, pin);
			}
			LOGGER.debug("decrypt each one: " + deString1);
			bw.write(ISOUtil.hex2byte(deString1));
		}
		
		if (bytes != 0) {
			br.read(buffer2);
			
			if ("P3EncKey".equals(keyId)) {
				deString2 = descryptECB(ISOUtil.hexString(buffer2), keyId, iv);
			} else {
				deString2 = descryptECB(ISOUtil.hexString(buffer2), keyId, iv, servletUrl, slot, pin);
			}
			deString2 = deString2.substring(0, deString2.lastIndexOf("0D0A")+4);
			LOGGER.debug("decrypt last one: " + deString2);
			bw.write(ISOUtil.hex2byte(deString2));
		}
		bw.flush();
		br.close();
		bw.close();
	}
	
	public static String descryptECB(String inputData, String keyId, String iv) throws Exception
	{
		LOGGER.info("Hsm ip: " + System.getenv("ET_HSM_NETCLIENT_SERVERLIST") + ", KeyId: " + keyId + ", Iv: " + iv);
	   	String encString = "";
	   	HsmAdapter ham = new HsmAdapter(HsmService.getHsmService());

	   	HsmResult hsmR = ham.CryptoDES3(HsmInterface.DES_DECRYPT_ECB, keyId, inputData, iv);
		if (null != hsmR && hsmR.getValue() == 0)
		{
			encString = hsmR.getString(0);
		}
		else
		{
			LOGGER.error("HSM process error: " + hsmR.getValue());
			throw new Exception("HSM process error: " + hsmR.getValue());
		}
		return encString;
	}
	
	public static String descryptECB(String inputData, 
			String keyId, String iv, String servletUrl, Integer slot, String pin) throws Exception {
		CgYhdpSsf cgYhdpSsf = new CgYhdpSsf();
		cgYhdpSsf.setKeyId(keyId);
		cgYhdpSsf.setIv(iv);
		cgYhdpSsf.setServletUrl(servletUrl);
		cgYhdpSsf.setSlot(slot);
		cgYhdpSsf.setPin(pin);
		return cgYhdpSsf.decryptDES3(inputData);
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
	public static void main(String[] args) 
	{
		try {
			DOMConfigurator.configureAndWatch("config/batch/log4j.xml", 60000);
			
			String parentPath = BatchUtil.getWorkDirectory();
			System.out.println("parentPath:" + parentPath);
			String origFile = parentPath + "\\out\\FTCB\\" + "FTCB.805.2015112001";
			String encryptFile = parentPath + "\\out\\FTCB\\" + "FTCB.805.2015112001.encrypt";
			String decryptFile = parentPath + "\\out\\FTCB\\" + "FTCB.805.2015112001.decrypt";
			
			String keyId = "P3EncKey";
			String iv = "0000000000000000";
			
//			encrypt2File(encryptFile, origFile, keyId, iv);
			
//			decryptToFile(decryptFile, encryptFile, keyId, iv);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
