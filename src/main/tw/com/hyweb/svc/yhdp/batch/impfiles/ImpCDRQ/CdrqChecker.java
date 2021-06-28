package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpCDRQ;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import tw.com.hyweb.core.yhdp.common.misc.Constants;
import tw.com.hyweb.svc.yhdp.batch.framework.impfiles.DataLineInfo;
import tw.com.hyweb.svc.yhdp.batch.framework.impfiles.ErrorDescInfo;
import tw.com.hyweb.svc.yhdp.batch.framework.impfiles.FieldInfo;
import tw.com.hyweb.svc.yhdp.batch.framework.impfiles.ImpFilesUtil;

public class CdrqChecker {

	private static Logger log = Logger.getLogger(CdrqChecker.class);
	
	private CdrqData data = new CdrqData();
	
	
	public CdrqChecker() {}
	
	
	public void setData(CdrqData data) {
		this.data = data;
	}

	public List<ErrorDescInfo> checker(DataLineInfo lineInfo) throws Exception
	{
		List<ErrorDescInfo> descInfos = new ArrayList<ErrorDescInfo>();
		if (!isValidExpiryDate(data.getDataBean().getExpiryDate())) 
		{
			descInfos.add(addErrorDescInfo(
					Constants.RCODE_2710_INVALID_ERR, 
					lineInfo.getMappingInfo().getField("field04"), 
					data.getDataBean().getExpiryDate()));
			return descInfos;
		}
		
		if (data.getCardCnt() == 0) 
			descInfos.add(addErrorDescInfo(
					Constants.RCODE_2716_DATACOUNT_ERR, 
					"cardNo is '" + data.getDataBean().getCardNo() + "', " 
					+ "expiryDate is '" + data.getDataBean().getExpiryDate() + "'."));
		return descInfos;
	}
	
	/**
	 * Returns true if is a valid date format
	 * @param value
	 * @return true if is a valid date format, otherwise false
	 */
	private boolean isValidExpiryDate(String value)
	{
		DateFormat df = new SimpleDateFormat("yyMM");
		df.setLenient(false);
		try {
			df.parse(value);
			return true;
		} catch (Exception e) {
			log.warn("date is '" + value + "', pattrn is 'yyMM'");
			return false;
		}
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
