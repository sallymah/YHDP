package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpBKCD;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import tw.com.hyweb.core.yhdp.common.misc.Constants;
import tw.com.hyweb.svc.yhdp.batch.framework.impfiles.DataLineInfo;
import tw.com.hyweb.svc.yhdp.batch.framework.impfiles.ErrorDescInfo;
import tw.com.hyweb.svc.yhdp.batch.framework.impfiles.FieldInfo;
import tw.com.hyweb.svc.yhdp.batch.framework.impfiles.ImpFilesUtil;

public class BkcdChecker {
	
	private static Logger log = Logger.getLogger(BkcdChecker.class);
	
	
	private BkcdData data = new BkcdData();
	
	
	public BkcdChecker() {}

	
	public void setData(BkcdData data) {
		this.data = data;
	}
	
	public List<ErrorDescInfo> checker(DataLineInfo lineInfo) throws Exception
	{
		List<ErrorDescInfo> descInfos = new ArrayList<ErrorDescInfo>();
		
		String status = data.getDataBean().getStatus();
		if ("0".equals(status) 
				&& isBlankOrNull(data.getDataBean().getBankId())) 
			descInfos.add(addErrorDescInfo(
					Constants.RCODE_2708_MANDATORY_ERR, 
					lineInfo.getMappingInfo().getField("field07"), 
					data.getDataBean().getBankId()));
		if ("1".equals(status) 
				&& isBlankOrNull(data.getDataBean().getFreezeDate())) 
			descInfos.add(addErrorDescInfo(
					Constants.RCODE_2708_MANDATORY_ERR, 
					lineInfo.getMappingInfo().getField("field05"), 
					data.getDataBean().getFreezeDate()));
		
		if ("0".equals(status) 
				&& !isBlankOrNull(data.getDataBean().getBankId())
				&& !isNumber(data.getDataBean().getBankId())) 
			descInfos.add(addErrorDescInfo(
					Constants.RCODE_2709_FORMAT_ERR, 
					lineInfo.getMappingInfo().getField("field07"), 
					data.getDataBean().getBankId()));
		
		if ("2".equals(status) 
				&& !isBlankOrNull(data.getDataBean().getExpiryDate()) 
					&& (!isValidExpiryDate(data.getDataBean().getExpiryDate())
						|| "0000".equals(data.getDataBean().getExpiryDate()))) 
			descInfos.add(addErrorDescInfo(
					Constants.RCODE_2710_INVALID_ERR, 
					lineInfo.getMappingInfo().getField("field04"), 
					data.getDataBean().getExpiryDate()));
		if ("1".equals(status) 
				&& !isBlankOrNull(data.getDataBean().getFreezeDate()) 
					&& (!isValidDate(data.getDataBean().getFreezeDate(), "yyyyMMdd") 
							|| "00000000".equals(data.getDataBean().getFreezeDate()))) 
			descInfos.add(addErrorDescInfo(
					Constants.RCODE_2710_INVALID_ERR, 
					lineInfo.getMappingInfo().getField("field05"), 
					data.getDataBean().getFreezeDate()));
		if ("0".equals(status) 
				&& !isBlankOrNull(data.getDataBean().getBankId()) 
					&& "000".equals(data.getDataBean().getBankId())) 
			descInfos.add(addErrorDescInfo(
					Constants.RCODE_2710_INVALID_ERR, 
					lineInfo.getMappingInfo().getField("field07"), 
					data.getDataBean().getBankId()));
		if (descInfos.size() > 0) return descInfos;
		
		if (data.getCardCnt() == 0) 
			descInfos.add(addErrorDescInfo(
					Constants.RCODE_2716_DATACOUNT_ERR, 
					"cardNo is '" + data.getDataBean().getCardNo() + "', " +
					"expiryDate is '" + data.getDataBean().getExpiryDate() + "'."));
		return descInfos;
	}
	
	/**
	 * Returns true if is a valid date format
	 * @param value
	 * @return true if is a date format, otherwise false
	 */
	private boolean isValidExpiryDate(String value) 
	{
		return isValidDate(value, "yyMM");
	}
	
	/**
	 * Returns true if is a valid date format
	 * @param value
	 * @param pattern 
	 * @return true if is a date format, otherwise false
	 */
	private boolean isValidDate(String value, String pattern)
	{
		DateFormat df = new SimpleDateFormat(pattern);
		df.setLenient(false);
		try {
			df.parse(value);
			return true;
		} catch (Exception e) {
			log.warn("date is '" + value + "', pattrn is '" + pattern + "'");
			return false;
		}
	}
	
	/**
	 * Returns true if is a valid number format
	 * @param value
	 * @return true is number format, otherwise false
	 */
	private boolean isNumber(String value)
	{
		Pattern pattern = Pattern.compile("[\\d]*$");  
	    return pattern.matcher(value).matches(); 
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
