package tw.com.hyweb.svc.yhdp.batch.framework.perso;

import org.apache.log4j.Logger;

import tw.com.hyweb.core.cp.common.hsm.HsmAdapter;
import tw.com.hyweb.core.service.hsm.HsmInterface;
import tw.com.hyweb.core.service.hsm.HsmResult;
import tw.com.hyweb.core.service.hsm.HsmService;
import tw.com.hyweb.util.IFFUtils;

public class HsmAdapterECB {
	
	private static final Logger logger = Logger.getLogger(HsmAdapterECB.class);
	
    private String pinKeyId;
    private String iv;

    //解密
    public String descryptECB(String inputData) throws Exception
	{
    	logger.info("Hsm ip: " + System.getenv("ET_HSM_NETCLIENT_SERVERLIST"));
	   	String encString = "";
	   	HsmAdapter ham = new HsmAdapter(HsmService.getHsmService());
	   	
		HsmResult hsmR = ham.CryptoDES3(HsmInterface.DES_DECRYPT_ECB, pinKeyId, inputData, iv);
		if (null != hsmR && hsmR.getValue() == 0)
		{
			encString = hsmR.getString(0);
			//logger.debug("encString = " + encString);
		}
		else
		{
			logger.info("HSM process error: " + hsmR.getValue());
			throw new Exception("HSM process error: " + hsmR.getValue());
		}
		return encString;
	}
    
    //加密
    public String encryptECB(String inputData, Boolean appendFlag){
    	String appendInputData = "";
    	logger.info("Hsm ip: " + System.getenv("ET_HSM_NETCLIENT_SERVERLIST"));
    	String encString = "";
    	HsmAdapter ham = new HsmAdapter(HsmService.getHsmService());
    	
    	if(appendFlag == true) {
    		appendInputData = IFFUtils.appendPKCS7(inputData);
    	}
    	else {
    		appendInputData = inputData;
    	}

		HsmResult hsmR = ham.CryptoDES3(HsmInterface.DES_ENCRYPT_ECB, getPinKeyId(), appendInputData, getIv());
		if (null != hsmR && hsmR.getValue() == 0)
		{
			encString = hsmR.getString(0);
			logger.debug("encString = " + encString);
		}
		else
		{
			logger.error("HSM process error: " + hsmR.getValue());
		}
    	return encString;
    }
    
    public String getIv() {
		return iv;
	}
	public void setIv(String iv) {
		this.iv = iv;
	}
    public String getPinKeyId() {
		return pinKeyId;
	}
	public void setPinKeyId(String pinKeyId) {
		this.pinKeyId = pinKeyId;
	}
}
