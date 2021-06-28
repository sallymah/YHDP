package tw.com.hyweb.svc.yhdp.batch;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.jpos.iso.ISOException;
import org.jpos.iso.ISOPackager;
import org.jpos.iso.packager.GenericPackager;
import org.jpos.iso.packager.XMLPackager;

import tw.com.hyweb.core.cp.common.hsm.HsmAdapter;
import tw.com.hyweb.core.service.hsm.HsmInterface;
import tw.com.hyweb.core.service.hsm.HsmResult;
import tw.com.hyweb.core.service.hsm.HsmService;
import tw.com.hyweb.iso.field.BerTLV;
import tw.com.hyweb.online.ISOMsg;
import tw.com.hyweb.util.ISOUtil;

public class testclass {
	/*public static void main(String[] argv) throws Exception {
		System.out.println("開始");
		
		String hex = "2020000000C1000B888888";
		ISOMsg iSOMsg = new ISOMsg();
		iSOMsg.unpack(hex.getBytes());
		System.out.println(iSOMsg.getString(1));
		
		
		String lineStr = "FF21027707FF2203000003FF230400001001FF25089862991100000190FF260420991231FF270400000320FF2808BA5CAAC50B9F2F54";
		BerTLV berTLV = BerTLV.createInstance(ISOUtil.hex2byte(lineStr));
		System.out.println(""+berTLV);

		System.out.println("結束");
	}

	public static byte[] convertHexToString(String hex){
     	 
    	byte[] nameByteArray = ISOUtil.hex2byte(hex);
		ByteBuffer byteBuffer = ByteBuffer.allocate(nameByteArray.length + 8);
		byteBuffer.put(nameByteArray);
		
		return nameByteArray;
    }
	
	private static String encryptECB(String inputData, Boolean appendFlag){
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

		HsmResult hsmR = ham.CryptoDES3(HsmInterface.DES_ENCRYPT_ECB, "P3EncKey", appendInputData, "0000000000000000");
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
    }*/
	public static void main(String[] args) {
		String dataString = "02002020000000C1000B88888800000539323031323130303030303739303238303030303133390129FF21027707FF2203000001FF230400001001FF25089851422336844805FF260420991231FF270400000057FF2808773A3132804E52F0FF30080052390010909805FF290720160118113045FF2F06011811304505FF45014CFF3D0103FF500430303030FF57083ECF7BEF27DF7061FF5808339EEF0E9C513E63FF2B0500001000000043FF3828000000000000000026500000010101999912310031000000010000000000000001010199991231010022FF391300000000000000000000000000000000000000E70315D5FC9977AB";
		ISOMsg isoMsg = new ISOMsg();
        isoMsg.setDirection(ISOMsg.INCOMING);
        try {
			isoMsg.setPackager(new GenericPackager("config/default/isoconfig.xml"));
	        isoMsg.unpack(ISOUtil.hex2byte(dataString));
	        System.out.println(isoMsg.getString(1));
	        System.out.println(isoMsg.getString(2));
	        System.out.println(isoMsg.getString(3));
	        System.out.println(isoMsg.getString(4));
	        System.out.println(isoMsg.getString(11));
	        System.out.println(isoMsg.getString(14));
	        System.out.println(isoMsg.getString(38));
	        System.out.println(isoMsg.getString(39));
	        System.out.println(isoMsg.getString(41));
	        System.out.println(isoMsg.getString(42));
	        System.out.println(isoMsg.getString(48));
	        System.out.println(isoMsg.getString(60));
	        System.out.println(isoMsg.getString(61));
	        System.out.println(isoMsg.getString(63));
	        System.out.println(isoMsg.getString(64));
	        
	        BerTLV berTLV48 = BerTLV.createInstance(ISOUtil.hex2byte(isoMsg.getString(48)));
			System.out.println(""+berTLV48);
			
			BerTLV berTLV60 = BerTLV.createInstance(ISOUtil.hex2byte(isoMsg.getString(60)));
			System.out.println(""+berTLV60);

		} catch (ISOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}
