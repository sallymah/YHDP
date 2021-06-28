package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpBMEM;

import java.sql.Connection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import tw.com.hyweb.core.yhdp.common.misc.Constants;
import tw.com.hyweb.svc.yhdp.batch.framework.impfiles.ErrorDescInfo;
import tw.com.hyweb.svc.yhdp.batch.framework.impfiles.FieldInfo;
import tw.com.hyweb.svc.yhdp.batch.framework.impfiles.ImpFilesUtil;
import tw.com.hyweb.util.string.StringUtil;

public class BmemChecker {

	private static Logger log = Logger.getLogger(BmemChecker.class);
	public static final String DF_yyMM = "yyMM";
	
	private BmemData data;
	private Map<String, FieldInfo> map;
	
	
	public BmemChecker(BmemData data, Map<String, FieldInfo> map) {
		this.data = data;
		this.map = map;
	}
	
	public List<ErrorDescInfo> checker(Connection conn) throws Exception
	{
		List<ErrorDescInfo> descInfos = new ArrayList<ErrorDescInfo>();
		
		if (!isValidDate(data.getTempMap().get("EXPIRY_DATE"), DF_yyMM)) {
			descInfos.add(ImpFilesUtil.getErrorDescInfo(Constants.RCODE_2709_FORMAT_ERR, map.get("EXPIRY_DATE"), data.getTempMap().get("EXPIRY_DATE")));
		}
		//member.BANK_CHECK_HG_FLAG 0 : 不檢核BMEM的Happy Go
		//卡片狀態3:個資更新以外 HG_CARD_NO必填
		if (	data.getBankCheckHgFlag().equals("1")
				&& !"3".equals(data.getTempMap().get("STATUS")) 
				&& StringUtil.isEmpty(data.getTempMap().get("HG_CARD_NO"))) {
			descInfos.add(ImpFilesUtil.getErrorDescInfo(Constants.RCODE_2710_INVALID_ERR, map.get("HG_CARD_NO"), data.getTempMap().get("STATUS") + "["+ data.getTempMap().get("HG_CARD_NO") +"]"));
		}
		if (!StringUtil.isEmpty(data.getTempMap().get("ZIP_CODE"))
				&& !StringUtil.isNumeric(data.getTempMap().get("ZIP_CODE"))) { 
			descInfos.add(ImpFilesUtil.getErrorDescInfo(Constants.RCODE_2709_FORMAT_ERR, map.get("ZIP_CODE"), data.getTempMap().get("ZIP_CODE")));
		}
		//檢查國籍對應 TB_BANK_COUNTRY
		if(!StringUtil.isEmpty(data.getTempMap().get("BANK_COUNTRY_CODE"))
				&& StringUtil.isEmpty(data.getTempMap().get("COUNTRY_CODE"))) {
			descInfos.add(ImpFilesUtil.getErrorDescInfo(Constants.RCODE_2709_FORMAT_ERR, map.get("BANK_COUNTRY_CODE"), data.getTempMap().get("BANK_COUNTRY_CODE")));
		}
		if(!StringUtil.isEmpty(data.getTempMap().get("LEGAL_BANK_COUNTRY_CODE"))
				&& StringUtil.isEmpty(data.getTempMap().get("LEGAL_AGENT_COUNTRY_CODE"))) {
			descInfos.add(ImpFilesUtil.getErrorDescInfo(Constants.RCODE_2709_FORMAT_ERR, map.get("LEGAL_BANK_COUNTRY_CODE"), data.getTempMap().get("LEGAL_BANK_COUNTRY_CODE")));
		}
		//檢查行業別
		if(!StringUtil.isEmpty(data.getTempMap().get("BANK_INDUSTRY"))
				&& StringUtil.isEmpty(data.getTempMap().get("INDUSTRY"))) {
			descInfos.add(ImpFilesUtil.getErrorDescInfo(Constants.RCODE_2709_FORMAT_ERR, map.get("BANK_INDUSTRY"), data.getTempMap().get("BANK_INDUSTRY")));
		}
		
		//#13, #14, #15為一組欄位   通訊縣市別、通訊郵遞區號、通訊地址 如有一個有值，則全要有值。 
		if(!StringUtil.isEmpty(data.getTempMap().get("CITY"))
			||	!StringUtil.isEmpty(data.getTempMap().get("ZIP_CODE"))
			||	!StringUtil.isEmpty(data.getTempMap().get("ADDRESS"))) {
			
			if(StringUtil.isEmpty(data.getTempMap().get("CITY"))
			|| StringUtil.isEmpty(data.getTempMap().get("ZIP_CODE"))
			|| StringUtil.isEmpty(data.getTempMap().get("ADDRESS"))) {
				
				descInfos.add(ImpFilesUtil.getErrorDescInfo(Constants.RCODE_2709_FORMAT_ERR, null, "CITY, ZIP_CODE, ADDRESS Dependency Error."));
			}
		}
		//#16, #17, #18為一組欄位   戶籍縣市別、戶籍郵遞區號、戶籍地址 如有一個有值，則全要有值。 
		if(!StringUtil.isEmpty(data.getTempMap().get("PERMANENT_CITY"))
			||	!StringUtil.isEmpty(data.getTempMap().get("PERMANENT_ZIP_CODE"))
			||	!StringUtil.isEmpty(data.getTempMap().get("PERMANENT_ADDRESS"))) {
			
			if(StringUtil.isEmpty(data.getTempMap().get("PERMANENT_CITY"))
			|| StringUtil.isEmpty(data.getTempMap().get("PERMANENT_ZIP_CODE"))
			|| StringUtil.isEmpty(data.getTempMap().get("PERMANENT_ADDRESS"))) {
				
				descInfos.add(ImpFilesUtil.getErrorDescInfo(Constants.RCODE_2709_FORMAT_ERR, null, "PERMANENT_CITY, PERMANENT_ZIP_CODE, PERMANENT_ADDRESS Dependency Error."));
			}
		}
		//(#13, #14, #15) (#16, #17, #18) 兩組欄位須擇一組欄位有值
		if(StringUtil.isEmpty(data.getTempMap().get("ADDRESS"))
			&& StringUtil.isEmpty(data.getTempMap().get("PERMANENT_ADDRESS"))) {
			descInfos.add(ImpFilesUtil.getErrorDescInfo(Constants.RCODE_2709_FORMAT_ERR, null, "No Any Address Date."));
		}
		//市內電話及、行動電話 擇一輸入
		if(StringUtil.isEmpty(data.getTempMap().get("TEL_HOME"))
				&& StringUtil.isEmpty(data.getTempMap().get("MOBILE"))) {
			descInfos.add(ImpFilesUtil.getErrorDescInfo(Constants.RCODE_2709_FORMAT_ERR, null, "Tel home And Mobile Is Null."));
		}
		/*法定代理人中文姓名、法定代理人英文姓名 其中一欄位有值
		     法定代理人身份證明文件號碼、法定代理人身分證明文件種類、法定代理人國籍  都必須有值
		     法定代理人行動電話、法定代理人電話 擇一輸入 */
		if(!StringUtil.isEmpty(data.getTempMap().get("LEGAL_AGENT_NAME"))
			|| !StringUtil.isEmpty(data.getTempMap().get("LEGAL_AGENT_ENG_NAME"))) {

			//法定代理人身份證明文件號碼、法定代理人身分證明文件種類、法定代理人國籍  都必須有值
			if(StringUtil.isEmpty(data.getTempMap().get("LEGAL_AGENT_PID"))) {
				descInfos.add(ImpFilesUtil.getErrorDescInfo(Constants.RCODE_2709_FORMAT_ERR, 
						map.get("LEGAL_AGENT_PID"), data.getTempMap().get("LEGAL_AGENT_PID")));
			}
			if(StringUtil.isEmpty(data.getTempMap().get("LEGAL_AGENT_PID_TYPE"))) {
				descInfos.add(ImpFilesUtil.getErrorDescInfo(Constants.RCODE_2709_FORMAT_ERR, 
						map.get("LEGAL_AGENT_PID_TYPE"), data.getTempMap().get("LEGAL_AGENT_PID_TYPE")));
			}
			if(StringUtil.isEmpty(data.getTempMap().get("LEGAL_BANK_COUNTRY_CODE"))) {
				descInfos.add(ImpFilesUtil.getErrorDescInfo(Constants.RCODE_2709_FORMAT_ERR, 
						map.get("LEGAL_BANK_COUNTRY_CODE"), data.getTempMap().get("LEGAL_BANK_COUNTRY_CODE")));
			}
			//法定代理人行動電話、法定代理人電話 擇一輸入
			if(StringUtil.isEmpty(data.getTempMap().get("LEGAL_AGENT_MOBILE"))
				&& StringUtil.isEmpty(data.getTempMap().get("LEGAL_AGENT_PHONE"))) {
				
				descInfos.add(ImpFilesUtil.getErrorDescInfo(Constants.RCODE_2709_FORMAT_ERR, 
						null, data.getTempMap().get("ZIP_CODE")));
			}
		}
		
		if (data.getCardCnt() == 0) { 
			descInfos.add(ImpFilesUtil.getErrorDescInfo(Constants.RCODE_2716_DATACOUNT_ERR, map.get("CARD_NO"), "cardNo [" + data.getTempMap().get("CARD_NO") + "] not exist."));
		}
		return descInfos;
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
}
