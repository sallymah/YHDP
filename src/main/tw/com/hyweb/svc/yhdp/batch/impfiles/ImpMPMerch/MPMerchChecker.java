package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpMPMerch;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import tw.com.hyweb.core.yhdp.common.misc.Constants;
import tw.com.hyweb.svc.yhdp.batch.framework.impfiles.ErrorDescInfo;
import tw.com.hyweb.svc.yhdp.batch.framework.impfiles.FieldInfo;
import tw.com.hyweb.svc.yhdp.batch.framework.impfiles.ImpFilesUtil;

public class MPMerchChecker {
	
	private static Logger log = Logger.getLogger(MPMerchChecker.class);
	
	private MPMerchData data = new MPMerchData();;
	
	
	public MPMerchChecker() {}
	
	
	public void setData(MPMerchData data) 
	{
		this.data = data;
	}
	
	/**
	 * 檢查收單單位
	 * @return error message
	 * @throws Exception
	 */
	public List<ErrorDescInfo> checker() throws Exception
	{
		List<ErrorDescInfo> descInfos = new ArrayList<ErrorDescInfo>();
		if (isValidMemId()) {
			descInfos.add(addErrorDescInfo(
					Constants.RCODE_2710_INVALID_ERR, 
					"memId is '" + data.getMemId() + "'"));
		}
		
		if ("00043057".equals(data.getMemId())) {
			if (isBlankOrNull(data.getBean().getEffectiveDate())) {
				descInfos.add(addErrorDescInfo(
						Constants.RCODE_2708_MANDATORY_ERR, 
						"EFFECTIVE_DATE is emtpy!"));
			}
			if (isBlankOrNull(data.getBean().getTerminationDate())) {
				descInfos.add(addErrorDescInfo(
						Constants.RCODE_2708_MANDATORY_ERR, 
						"TERMINATION_DATE is emtpy!"));
			}
		} else {
			if (!("1".equals(data.getBean().getStatus()) 
					|| "0".equals(data.getBean().getStatus()))) {
				descInfos.add(addErrorDescInfo(
						Constants.RCODE_2710_INVALID_ERR, 
						"status is '" + data.getBean().getStatus() + "'"));
			}
		}
		
		return descInfos;
	}
	
	/**
	 * 檢查TRAILER_RECORD的總資料筆數是否正確
	 * @param n 總資料筆數
	 * @return error message
	 * @throws Exception
	 */
	public List<ErrorDescInfo> checkTotDataNum(int n) throws Exception
	{
		List<ErrorDescInfo> descInfos = new ArrayList<ErrorDescInfo>();
		if (n != data.getTotRec()) 
			descInfos.add(addErrorDescInfo(
					Constants.RCODE_2706_DATANUM_ERR, 
	    			"total data number '" + n + "' != '" + data.getTotRec() + "'."));
		return descInfos;
	}
	
	/**
	 * Returns true if null or length is 0
	 * @param value memId
	 * @return true if null or length is 0, otherwise false
	 */
	private boolean isValidMemId()
	{
		if (data.getMemCount() == 0)
			return true;
		else
			return false;
	}
	
	/**
	 * Returns true if null or empty
	 * @param value String
	 * @return true if null or empty, otherwise false
	 */
	public boolean isBlankOrNull(String value) 
	{
		return (value == null || "".equals(value.trim()));
	}
	
	private ErrorDescInfo addErrorDescInfo(String rcode, String content)
	{
		return addErrorDescInfo(rcode, null, content);
	}
	
	private ErrorDescInfo addErrorDescInfo(String rcode, FieldInfo fieldInfo, String content)
	{
		return ImpFilesUtil.getErrorDescInfo(rcode, fieldInfo, content);
	}
}
