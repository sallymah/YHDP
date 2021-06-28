package tw.com.hyweb.svc.yhdp.batch.expfiles.ExpTeff;


import org.apache.log4j.Logger;

import tw.com.hyweb.core.cp.common.hsm.HsmAdapter;
import tw.com.hyweb.core.service.hsm.HsmResult;
import tw.com.hyweb.core.service.hsm.HsmService;
import tw.com.hyweb.svc.yhdp.batch.util.BatchUtils;
import tw.com.hyweb.svc.yhdp.online.TxException;
import tw.com.hyweb.svc.yhdp.online.util.YHDPUtil;
import tw.com.hyweb.util.ISOUtil;

public class RespMacDataFiller {
	private static final Logger logger = Logger.getLogger(RespMacDataFiller.class);
	private String macKeyID = "SingleTAC";
	private String PRESET_MAC = "0000000000000000";
	
	public String validate(String tsamosn, String rawData) throws TxException{
		
		logger.debug("Mac Filler");
		
        
		HsmAdapter hsmAdapter = new HsmAdapter(HsmService.getHsmService());
		//HsmAdapter hsmAdapter = null;
        
        if(null != rawData && rawData.length() > 0)
        {

        	StringBuffer strPad = new StringBuffer();
            int padNum = (8-(rawData.length()/2)%8);
            if (padNum>0 && padNum<8)
            {//補1~7個byte 剛好整除時不需要補
                for (int i=0;i<padNum;i++)
                {
                    strPad.append("FF");
                }
            }
            rawData = rawData+strPad.toString();
            rawData = rawData+strPad.toString();
            logger.debug("rawData=["+rawData+"]");
            byte[] bRaw = ISOUtil.hex2byte(rawData);
            byte[] inputData = new byte[]{bRaw[0],bRaw[1],bRaw[2],bRaw[3],bRaw[4],bRaw[5],bRaw[6],bRaw[7]};
            int num = bRaw.length/8;
            for (int i=1;i<num;i++)
            {
                byte[] dest = new byte[8];
                System.arraycopy(bRaw, i*8, dest, 0, 8);
                //logger.debug(i+":"+ISOUtil.hexString(inputData)+" xor "+ISOUtil.hexString(dest));

                for (int j=0;j<8;j++)
                {
                    inputData[j] = (byte)(inputData[j] ^ bRaw[i*8+j]);
                }
            }
            
            String divData = tsamosn;
            
            if(BatchUtils.isBlankOrNull(divData))
            {
            	divData = "0000000000000000";
            }
            
            String iv = ISOUtil.hexString("YHDPCARD".getBytes());
            String inputDataS = ISOUtil.hexString(inputData);
            
            logger.debug("iv=["+ iv + "] divData=["+divData+"] inputDataS=["+inputDataS+"]");
            long start = java.util.Calendar.getInstance().getTimeInMillis();
            HsmResult hsmR = hsmAdapter.GenerateTAC_1(macKeyID, divData, inputDataS, iv);
            YHDPUtil.checkMillisTime("RespMacDataFiller GenerateTAC_1", 1000, start);
            if (hsmR.getValue()!=0)
            {
                logger.error("iv=["+ iv + "] divData=["+divData+"] inputDataS=["+inputDataS+"]");
                logger.error("hsm return error. hsm ret code="+(hsmR!=null?Integer.toHexString(hsmR.getValue()):"null"));
				throw new TxException("hsm return error. hsm ret code="+hsmR.getValue());
            }
            else
            {
            	return hsmR.getString(0);
            }
        }
        return PRESET_MAC;
	}

	public String getMacKeyID() {
		return macKeyID;
	}
	public void setMacKeyID(String macKeyID) {
		this.macKeyID = macKeyID;
	}
	
}
