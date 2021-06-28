package tw.com.hyweb.iff;

import java.io.File;
import java.util.HashMap;

import org.apache.log4j.Logger;

import tw.com.hyweb.core.cp.batch.framework.impfiles.FieldInfo;
import tw.com.hyweb.core.cp.batch.framework.impfiles.MappingInfo;
import tw.com.hyweb.core.cp.batch.framework.impfiles.MappingLoader;
import tw.com.hyweb.iff.impfiles.YhdpPersoFeedBackBean;
import tw.com.hyweb.util.ISOUtil;


public class IFFUtils 
{
	private final Logger LOGGER = Logger.getLogger(IFFUtils.class);
	
	// mapping config filename
    protected String configFilename = "config/batch/MappingInfos.xml";
    // mapping config filename encoding
    protected String encoding = "UTF-8";
    
    protected static MappingInfo mappingInfo = null;
	
	
	public HashMap<String, String> cacheRowData(String dataLine) throws Exception 
    {
    	int startIdx = 0;
	    int endIdx = 0;
	    
	    HashMap<String, String> hm = new HashMap<String, String>();
    	for(int i=0; i< mappingInfo.getFields().size(); i++)
    	{
	        startIdx = ((FieldInfo)mappingInfo.getFields().get(i)).getStart();  
	        endIdx = startIdx + ((FieldInfo)mappingInfo.getFields().get(i)).getLength();
	        
	        hm.put(((FieldInfo)mappingInfo.getFields().get(i)).getName(), dataLine.substring(startIdx, endIdx).trim()); 
    	}
    	
    	LOGGER.debug(hm.toString());
    	return hm;
	}
	
	public YhdpPersoFeedBackBean organizeRowData(String dataLine) throws Exception 
	{
		HashMap<String, String> hm = cacheRowData(dataLine);
		
		YhdpPersoFeedBackBean dataBean = new YhdpPersoFeedBackBean();
		dataBean.setHm(hm);
		
		dataBean.setCardNo(hm.get("field01"));
		dataBean.setMifareId(hm.get("field02"));
		dataBean.setIssNo(hm.get("field03"));
		dataBean.setIssEquipmentNo(hm.get("field04"));
		dataBean.setPersoBatchNo(hm.get("field05"));
		dataBean.setActiveDate(hm.get("field06"));
		dataBean.setExpiryDate(hm.get("field07"));
		dataBean.setCardVersion(hm.get("field08"));
		dataBean.setCardStatus(hm.get("field09"));
		dataBean.setAutoReloadFlag(hm.get("field10"));
		dataBean.setAutoReloadValue(hm.get("field11"));
		dataBean.setBalMaxbal(hm.get("field12"));
		dataBean.setMinBalAmt8(hm.get("field13"));
		dataBean.setAppointReloadFlag(hm.get("field14"));
		dataBean.setAppointReloadValue(hm.get("field15"));
		dataBean.setAutoReloadDate(hm.get("field16"));
		dataBean.setOlAutoReloadMaxTime(hm.get("field17"));
		dataBean.setAutoReloadMaxTime(hm.get("field18"));
		dataBean.setAppointReloadMaxTime(hm.get("field19"));
		dataBean.setHexBarcode1(hm.get("field20"));
		dataBean.setHexPackMaterialNo(hm.get("field21"));
		dataBean.setHexCardMaterialNo(hm.get("field22"));
		dataBean.setHexPackNo(hm.get("field23"));
		dataBean.setHexChestNo(hm.get("field24"));
		dataBean.setHexBoxNo(hm.get("field25"));
		dataBean.setSirId(hm.get("field26"));
		dataBean.setIccId(hm.get("field27"));
		dataBean.setServiceId(hm.get("field28"));
		dataBean.setServiceObjId(hm.get("field29"));
		dataBean.setMnoCardNo(dataBean.getIccId());
		
		return dataBean;
	}
	
	public void initial(String fileName) throws Exception 
	{
		if ( mappingInfo == null || !mappingInfo.getName().equals(fileName)){
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

	public MappingInfo getMappingInfo() {
		return mappingInfo;
	}

	public void setMappingInfo(MappingInfo mappingInfo) {
		IFFUtils.mappingInfo = mappingInfo;
	}
	
}
