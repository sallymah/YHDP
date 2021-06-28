package tw.com.hyweb.svc.yhdp.batch.framework.perso;

import org.apache.log4j.Logger;

import tw.com.hyweb.util.IFFUtils;
import tw.com.hyweb.util.ISOUtil;
import tw.com.hyweb.util.string.StringUtil;

import com.formosoft.ss.stub.CGYHDPSSFacade;

public class CgYhdpSsf {
	
	private static final Logger logger = Logger.getLogger(CgYhdpSsf.class);
	
	private String keyId;
    private String iv;
    private String servletUrl = "http://YhdpSS:36888/YHDP_SSServlet/SS";
    private int slot = 2;
    private String pin = "BAA6B7BBAFCECDCCCBF7F7F7F7F7F7F7F7";
    
    public final int DES3_ECB_E = 1;
    public final int DES3_ECB_D = 2;
   
    public static void main(String[] argv) throws Exception {
    	
    	CgYhdpSsf cgYhdpSsf = new CgYhdpSsf();
    	cgYhdpSsf.setSlot(0);
    	cgYhdpSsf.setKeyId("00103077KEY");
    	
    	byte[] buffer2 = new String ("3132333132333132").getBytes();
    	
    	logger.info(cgYhdpSsf.encryptDES3("3132333132333132", false));
    	logger.info(cgYhdpSsf.encryptDES3("3132333132333132", true));
    	logger.info(cgYhdpSsf.encryptDES3(ISOUtil.hexString(buffer2), false));
    }
    
    public String encryptDES3(String inputData, boolean appendFlag) throws Exception{
    	
    	String appendInputData = "";
    	if(appendFlag == true) {
    		appendInputData = IFFUtils.appendPKCS7(inputData);
    	}
    	else {
    		appendInputData = inputData;
    	}
    	
    	return cryptoDES3(appendInputData, DES3_ECB_E);
    }
    
    public String decryptDES3(String inputData) throws Exception{
    	return cryptoDES3(inputData, DES3_ECB_D);
    }
    
    public String cryptoDES3(String inputData, int iOPMode) throws Exception{
    	
    	String sMac = null;
    	byte[] bIV = null;
		if(!StringUtil.isEmpty(iv)){
			bIV = getByte(true, iv);
		}else{
			bIV = new byte[]{0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00};
		}
		byte[] bInputData = getByte(true, inputData);
    	byte[] bPin = ISOUtil.hex2byte(pin);
    	CGYHDPSSFacade yhdpFacade = new CGYHDPSSFacade(servletUrl);
    	
    	logger.debug("slot: "+ slot
    			+"  bPin: "+ ISOUtil.hexString(bPin)
    			+"  bPin.length: "+ bPin.length
    			+"  iOPMode: "+ iOPMode
    			+"  bIV: "+ ISOUtil.hexString(bIV)
    			+"  keyId: "+ keyId
    			+"  bInputData: "+ ISOUtil.hexString(bInputData));
    	
    	int result = yhdpFacade.CryptoDES3(slot, bPin, bPin.length, iOPMode, bIV, keyId, bInputData);
    	
    	if (result!=0)
        {
    		throw new Exception("encryptDES3 Error: [" + inputData + "]: " + result);
        }
    	else{
    		byte[] bMac = yhdpFacade.GetCryptoResult();
            if(bMac != null)
            {
            	sMac = ISOUtil.hexString(bMac).toUpperCase();
            }
            else{
            	throw new Exception("encryptDES3 Error: [" + inputData + "]");
            }
    	}
		return sMac;
    }
    
    public byte[] getByte(boolean isHexToByte, String date)
    {
        byte[] out = null;
        if(isHexToByte == true)
        {
            out = ISOUtil.hex2byte(date);
        }
        else
        {
            out = date.getBytes();
        }
        return out;
    }
    
	public String getKeyId() {
		return keyId;
	}
	public void setKeyId(String keyId) {
		this.keyId = keyId;
	}
	public String getIv() {
		return iv;
	}
	public void setIv(String iv) {
		this.iv = iv;
	}
	public String getServletUrl() {
		return servletUrl;
	}
	public void setServletUrl(String servletUrl) {
		this.servletUrl = servletUrl;
	}
	public int getSlot() {
		return slot;
	}
	public void setSlot(int slot) {
		this.slot = slot;
	}
	public String getPin() {
		return pin;
	}
	public void setPin(String pin) {
		this.pin = pin;
	}
}
