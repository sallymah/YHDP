package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpBMEM;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import tw.com.hyweb.core.cp.common.misc.ExecuteSqlsInfo;
import tw.com.hyweb.core.yhdp.common.misc.DateUtil;
import tw.com.hyweb.svc.yhdp.batch.framework.impfiles.DataLineInfo;
import tw.com.hyweb.svc.yhdp.batch.framework.impfiles.FieldInfo;
import tw.com.hyweb.svc.yhdp.batch.framework.impfiles.bank.AbstractImpFile;
import tw.com.hyweb.util.DbUtil;
import tw.com.hyweb.util.string.StringUtil;

public class ImpBMEM extends AbstractImpFile {

	private static Logger log = Logger.getLogger(ImpBMEM.class);
	
	/*private static final String SPRING_PATH = "config" + File.separator + "batch" + File.separator +
		    "impfiles" + File.separator + "ImpBMEM" + File.separator + "spring.xml";*/
	
	private String feeCodehead = "IP0";
	
	private String batchDate = "";
	
	private BmemData data;
	
	private HashMap<String, Integer> statusCntList = new HashMap<String, Integer>();
	
	private HashMap<String, String> bankCountryMap = new HashMap<String, String>(); 
	private HashMap<String, String> bankIndustryMap = new HashMap<String, String>(); 
	private String expiryDateFlag = "0";
	private String bankCheckHgFlag = "0";

	public ImpBMEM() {
	}
	
	@Override
	public ExecuteSqlsInfo beforeHandleDataLine() throws Exception 
	{
		batchDate = System.getProperty("date");
		if (StringUtil.isEmpty(batchDate)) {
			batchDate = DateUtil.getTodayString().substring(0, 8);
		}
		
		statusCntList = new HashMap<String, Integer>(); 

		String bankCountrySql = "SELECT COUNTRY_CODE, BANK_COUNTRY_CODE FROM TB_BANK_COUNTRY WHERE MEM_ID = ?";
		Vector<String> params = new Vector<String>();
		params.add(inctlInfo.getMemId());
		
		Vector<HashMap> bankCountryHash = DbUtil.getInfoListHashMap(bankCountrySql, params, conn);
		
		bankCountryMap = new HashMap<String, String>(); 
		for(HashMap bankCountry : bankCountryHash) {
			bankCountryMap.put(bankCountry.get("BANK_COUNTRY_CODE").toString(), bankCountry.get("COUNTRY_CODE").toString());
		}
		params.clear();
		
		
		String bankIndustrySql = "SELECT EMPL_INDUSTRY, BANK_INDUSTRY FROM TB_BANK_INDUSTRY WHERE MEM_ID = ?";
		params.add(inctlInfo.getMemId());

		Vector<HashMap> bankIndustryHash = DbUtil.getInfoListHashMap(bankIndustrySql, params, conn);

		bankIndustryMap = new HashMap<String, String>(); 
		for(HashMap bankIndustry : bankIndustryHash) {
			bankIndustryMap.put(bankIndustry.get("BANK_INDUSTRY").toString(), bankIndustry.get("EMPL_INDUSTRY").toString());
		}
		
		/*expiryDateFlag = 
				DbUtil.getString("SELECT BMEM_EXPIRY_DATE_FLAG FROM TB_MEMBER "
							+ "WHERE MEM_ID = " + StringUtil.toSqlValueWithSQuote(inctlInfo.getMemId()), conn);*/
		
		HashMap memberDate = (HashMap)DbUtil.getInfoListHashMap("SELECT BMEM_EXPIRY_DATE_FLAG, BANK_CHECK_HG_FLAG FROM TB_MEMBER "
				+ "WHERE MEM_ID = " + StringUtil.toSqlValueWithSQuote(inctlInfo.getMemId()), conn).get(0);
		
		expiryDateFlag = memberDate.get("BMEM_EXPIRY_DATE_FLAG") != null ? memberDate.get("BMEM_EXPIRY_DATE_FLAG").toString() : "0";
		bankCheckHgFlag = memberDate.get("BANK_CHECK_HG_FLAG") != null ? memberDate.get("BANK_CHECK_HG_FLAG").toString() : "0";
		
		log.debug("[memId: " + inctlInfo.getMemId() 
		+ ", bmemExpiryDateFlag: " + expiryDateFlag
		+ ", bankCheckHgFlag: " + bankCheckHgFlag + "]");
		
		return null;
	}
	
	public List checkDataLine(DataLineInfo lineInfo) throws Exception 
	{
		data = new BmemData(this.getValues(lineInfo));
		data.setData(conn, batchDate, expiryDateFlag, bankCheckHgFlag);
		
		List descInfos = super.checkDataLine(lineInfo);
		
		if (descInfos.size() > 0) {
			log.warn("checkDataLine: " + descInfos);
			return descInfos;
		}
		
		BmemChecker checker = new BmemChecker(data, getFieldInfos(lineInfo));
		descInfos = checker.checker(conn);
		if (descInfos.size() > 0) {
			log.warn("checkDataLine: " + descInfos);
		}
		return descInfos;
	}
	
	@Override
	public List handleDataLine(DataLineInfo lineInfo) throws Exception 
	{
		List<ExecuteSqlsInfo> sqlsInfoList = new ArrayList<ExecuteSqlsInfo>();
    	ExecuteSqlsInfo sqlsInfo = new ExecuteSqlsInfo();
        sqlsInfo.setCommit(false);
        sqlsInfo.setSavepoint(true);
        sqlsInfo.setSqls(data.makeSqlList(conn, statusCntList,inctlInfo.getFullFileName()));
        sqlsInfoList.add(sqlsInfo);
        log.debug("handleDataLine:" + sqlsInfoList);
        log.debug("statusCntList:" + statusCntList);
		return sqlsInfoList;
	}
	
	/**
	 * get DataLineInfo data
	 * @param lineInfo
	 * @return
	 */
    private Map<String, String> getValues(DataLineInfo lineInfo)
    {
        Map<String, String> values = new HashMap<String, String>();
        
        values.put("CARD_NO", lineInfo.getFieldData("field02").toString());					// ??????
        values.put("STATUS", lineInfo.getFieldData("field03").toString());					// ????????????
        values.put("HG_CARD_NO", lineInfo.getFieldData("field04").toString());				// HappyGO??????
        values.put("BANK_COUNTRY_CODE", lineInfo.getFieldData("field05").toString());		// ??????
        values.put("COUNTRY_CODE", bankCountryMap.get(values.get("BANK_COUNTRY_CODE")));	// ??????????????????
        
        values.put("BANK_INDUSTRY", lineInfo.getFieldData("field06").toString());			// ??????????????????
        values.put("INDUSTRY", bankIndustryMap.get(values.get("BANK_INDUSTRY")));			// ??????????????????????????????
        
        values.put("LOC_NAME", lineInfo.getFieldData("field07").toString());				// ?????????????????????
        values.put("ENG_NAME", lineInfo.getFieldData("field08").toString());				// ?????????????????????
        values.put("PERSON_ID", lineInfo.getFieldData("field09").toString());				// ????????????????????????
        values.put("PERSON_TYPE", lineInfo.getFieldData("field10").toString());				// ????????????????????????
        values.put("GENDER", lineInfo.getFieldData("field11").toString());					// ??????
        values.put("BIRTHDAY", lineInfo.getFieldData("field12").toString());				// ????????????
        values.put("CITY", lineInfo.getFieldData("field13").toString());					// ???????????????
        values.put("ZIP_CODE", lineInfo.getFieldData("field14").toString());				// ??????????????????
        values.put("ADDRESS", lineInfo.getFieldData("field15").toString());					// ????????????
        values.put("PERMANENT_CITY", lineInfo.getFieldData("field16").toString());			// ???????????????
        values.put("PERMANENT_ZIP_CODE", lineInfo.getFieldData("field17").toString());		// ??????????????????
        values.put("PERMANENT_ADDRESS", lineInfo.getFieldData("field18").toString());		// ????????????
        values.put("TEL_HOME", lineInfo.getFieldData("field19").toString());				// ????????????
        values.put("MOBILE", lineInfo.getFieldData("field20").toString());					// ????????????
        values.put("EMAIL", lineInfo.getFieldData("field21").toString());					// ????????????
        values.put("DM_FLAG", lineInfo.getFieldData("field22").toString());					// ??????????????????
        values.put("LEGAL_AGENT_NAME", lineInfo.getFieldData("field23").toString());		// ???????????????????????????
        values.put("LEGAL_AGENT_ENG_NAME", lineInfo.getFieldData("field24").toString());	// ???????????????????????????
        values.put("LEGAL_AGENT_PID", lineInfo.getFieldData("field25").toString());			// ???????????????????????????????????????
        values.put("LEGAL_AGENT_PID_TYPE", lineInfo.getFieldData("field26").toString());	// ???????????????????????????????????????
        values.put("LEGAL_BANK_COUNTRY_CODE", lineInfo.getFieldData("field27").toString());	// ?????????????????????
        values.put("LEGAL_AGENT_COUNTRY_CODE", 
        		bankCountryMap.get(values.get("LEGAL_BANK_COUNTRY_CODE")));					// ?????????????????????????????????
        values.put("LEGAL_AGENT_BIRTHDAY", lineInfo.getFieldData("field28").toString());	// ?????????????????????
        values.put("LEGAL_AGENT_GENDER", lineInfo.getFieldData("field29").toString());		// ?????????????????????
        values.put("LEGAL_AGENT_MOBILE", lineInfo.getFieldData("field30").toString());		// ???????????????????????????
        values.put("LEGAL_AGENT_PHONE", lineInfo.getFieldData("field31").toString());		// ?????????????????????
        values.put("VIP_FLAG", lineInfo.getFieldData("field32").toString());				// VIP ??????
        values.put("ISSUE_DATE", lineInfo.getFieldData("field33").toString());				// ????????????
        values.put("EXPIRY_DATE", lineInfo.getFieldData("field34").toString());				// ?????????????????????
        values.put("MERCH_ID", lineInfo.getFieldData("field35").toString());				// ????????????
        values.put("MARRIAGE", lineInfo.getFieldData("field36").toString());				// ????????????
        values.put("SALE_CODE", lineInfo.getFieldData("field37").toString());				// ????????????
        values.put("R_CODE", lineInfo.getFieldData("field38").toString());					// ??????????????????
        
        return values;
    }
    
    private Map<String, FieldInfo> getFieldInfos(DataLineInfo lineInfo){
    	
    	Map<String, FieldInfo> values = new HashMap<String, FieldInfo>();
    	
    	values.put("CARD_NO", lineInfo.getMappingInfo().getField("field02"));				// ??????
    	values.put("STATUS", lineInfo.getMappingInfo().getField("field03"));				// ????????????
    	values.put("HG_CARD_NO", lineInfo.getMappingInfo().getField("field04"));			// HappyGO??????
    	values.put("BANK_COUNTRY_CODE", lineInfo.getMappingInfo().getField("field05"));		// ??????
    	values.put("BANK_INDUSTRY", lineInfo.getMappingInfo().getField("field06"));			// ??????????????????
    	values.put("LOC_NAME", lineInfo.getMappingInfo().getField("field07"));				// ?????????????????????
    	values.put("ENG_NAME", lineInfo.getMappingInfo().getField("field08"));				// ?????????????????????
    	values.put("PERSON_ID", lineInfo.getMappingInfo().getField("field09"));				// ????????????????????????
    	values.put("PERSON_TYPE", lineInfo.getMappingInfo().getField("field10"));			// ????????????????????????
    	values.put("GENDER", lineInfo.getMappingInfo().getField("field11"));				// ??????
    	values.put("BIRTHDAY", lineInfo.getMappingInfo().getField("field12"));				// ????????????
    	values.put("CITY", lineInfo.getMappingInfo().getField("field13"));					// ???????????????
    	values.put("ZIP_CODE", lineInfo.getMappingInfo().getField("field14"));				// ??????????????????
    	values.put("ADDRESS", lineInfo.getMappingInfo().getField("field15"));				// ????????????
    	values.put("PERMANENT_CITY", lineInfo.getMappingInfo().getField("field16"));		// ???????????????
    	values.put("PERMANENT_ZIP_CODE", lineInfo.getMappingInfo().getField("field17"));	// ??????????????????
    	values.put("PERMANENT_ADDRESS", lineInfo.getMappingInfo().getField("field18"));		// ????????????
    	values.put("TEL_HOME", lineInfo.getMappingInfo().getField("field19"));				// ????????????
    	values.put("MOBILE", lineInfo.getMappingInfo().getField("field20"));				// ????????????
    	values.put("EMAIL", lineInfo.getMappingInfo().getField("field21"));					// ????????????
    	values.put("DM_FLAG", lineInfo.getMappingInfo().getField("field22"));				// ??????????????????
    	values.put("LEGAL_AGENT_NAME", lineInfo.getMappingInfo().getField("field23"));		// ???????????????????????????
    	values.put("LEGAL_AGENT_ENG_NAME", lineInfo.getMappingInfo().getField("field24"));	// ???????????????????????????
    	values.put("LEGAL_AGENT_PID", lineInfo.getMappingInfo().getField("field25"));		// ???????????????????????????????????????
    	values.put("LEGAL_AGENT_PID_TYPE", lineInfo.getMappingInfo().getField("field26"));	// ???????????????????????????????????????
    	values.put("LEGAL_BANK_COUNTRY_CODE", 
    			lineInfo.getMappingInfo().getField("field27"));								// ?????????????????????
    	values.put("LEGAL_AGENT_BIRTHDAY", lineInfo.getMappingInfo().getField("field28"));	// ?????????????????????
    	values.put("LEGAL_AGENT_GENDER", lineInfo.getMappingInfo().getField("field29"));	// ?????????????????????
    	values.put("LEGAL_AGENT_MOBILE", lineInfo.getMappingInfo().getField("field30"));	// ???????????????????????????
    	values.put("LEGAL_AGENT_PHONE", lineInfo.getMappingInfo().getField("field31"));		// ?????????????????????
    	values.put("VIP_FLAG", lineInfo.getMappingInfo().getField("field32"));				// VIP ??????
    	values.put("ISSUE_DATE", lineInfo.getMappingInfo().getField("field33"));			// ????????????
    	values.put("EXPIRY_DATE", lineInfo.getMappingInfo().getField("field34"));			// ?????????????????????
    	values.put("MERCH_ID", lineInfo.getMappingInfo().getField("field35"));				// ????????????
    	values.put("MARRIAGE", lineInfo.getMappingInfo().getField("field36"));				// ????????????
    	values.put("SALE_CODE", lineInfo.getMappingInfo().getField("field37"));				// ????????????
    	values.put("R_CODE", lineInfo.getMappingInfo().getField("field38"));				// ??????????????????
        
        return values;
    }
    
    public ExecuteSqlsInfo afterHandleDataLine() throws Exception {
        
    	List<String> sqlList = new ArrayList<String>();
    	
    	for(String key : statusCntList.keySet()){
    		
    		//key = dataBean.getSaleCode() + dataBean.getStatus();
    		String status = key.substring(key.length()-1);
    		String saleCode = key.substring(0, key.length()-1);
    		String feeCode = feeCodehead+ status;
    		
    		StringBuffer feeCodeSql = new StringBuffer();
    		feeCodeSql.append("SELECT FEE_CONFIG_ID FROM TB_FEE_CARD_ISSUE_CFG");
    		feeCodeSql.append(" WHERE ACQ_MEM_ID = ").append(StringUtil.toSqlValueWithSQuote(inctlInfo.getMemId()));
    		feeCodeSql.append(" AND FEE_CODE = ").append(StringUtil.toSqlValueWithSQuote(feeCode));
    		feeCodeSql.append(" AND ").append(StringUtil.toSqlValueWithSQuote(batchDate)).append(" BETWEEN VALID_SDATE AND VALID_EDATE");
    		feeCodeSql.append(" AND EXISTS(SELECT 1 FROM TB_FEE_CARD_ISSUE_CFG_DTL");
    		feeCodeSql.append(" WHERE SALE_CODE = ").append(StringUtil.toSqlValueWithSQuote(saleCode));
    		feeCodeSql.append(" AND TB_FEE_CARD_ISSUE_CFG_DTL.FEE_CONFIG_ID = TB_FEE_CARD_ISSUE_CFG.FEE_CONFIG_ID");
    		feeCodeSql.append(" AND TB_FEE_CARD_ISSUE_CFG_DTL.FEE_CODE = TB_FEE_CARD_ISSUE_CFG.FEE_CODE");
    		feeCodeSql.append(" AND TB_FEE_CARD_ISSUE_CFG_DTL.ISS_MEM_ID = TB_FEE_CARD_ISSUE_CFG.ISS_MEM_ID");
    		feeCodeSql.append(" AND TB_FEE_CARD_ISSUE_CFG_DTL.ACQ_MEM_ID = TB_FEE_CARD_ISSUE_CFG.ACQ_MEM_ID");
    		feeCodeSql.append(" AND TB_FEE_CARD_ISSUE_CFG_DTL.VALID_SDATE = TB_FEE_CARD_ISSUE_CFG.VALID_SDATE)");
    		
    		String feeConfigId = DbUtil.getString(feeCodeSql.toString(), conn);
    		
    		if (!StringUtil.isEmpty(feeConfigId)){
	    		StringBuffer sql = new StringBuffer();
	    		sql.append("UPDATE TB_FEE_CARD_ISSUE_SUM SET");
	    		sql.append(" THIS_ISS_CNT = THIS_ISS_CNT + ").append(statusCntList.get(key));
	    		sql.append(" WHERE FEE_CONFIG_ID = ").append(StringUtil.toSqlValueWithSQuote(feeConfigId));
	    		sql.append(" AND FEE_CODE = ").append(StringUtil.toSqlValueWithSQuote(feeCode));
	    		sql.append(" AND MEM_ID = ").append(StringUtil.toSqlValueWithSQuote(inctlInfo.getMemId()));
	    		sql.append(" AND SALE_CODE = ").append(StringUtil.toSqlValueWithSQuote(saleCode));
	    		sql.append(" AND PROC_DATE = ").append(StringUtil.toSqlValueWithSQuote(batchDate));
	    		log.debug(sql.toString());
	    		sqlList.add(sql.toString());
    		}
    		else{
    			log.warn("AcqMemId: " + impFileInfo.getAcqMemId() + ", saleCode: " + saleCode + ", feeCode: " + feeCode 
    					+ " FeeConfigId is not exist." );
    		}
    	}
    	
    	ExecuteSqlsInfo sqlsInfo = new ExecuteSqlsInfo();
        sqlsInfo.setCommit(false);
        sqlsInfo.setSavepoint(true);
        sqlsInfo.setSqls(sqlList);
        
    	return sqlsInfo;
    }
	
	public static ImpBMEM getInstance(String springPath) 
	{
		ApplicationContext apContext = new FileSystemXmlApplicationContext(springPath);
		ImpBMEM instance = (ImpBMEM) apContext.getBean("ImpBMEM");
		return instance;
	}
	
	public static void main(String[] args) {
		ImpBMEM impBMEM = null;
        try {
        	String springPath = args[0];
        	
            File f = new File(springPath);
            if (f.exists() && f.isFile()) {
            	impBMEM = getInstance(springPath);
            	impBMEM.run(args);
            }
            else{
            	throw new Exception(springPath + " is not exists.");
            }
        }
        catch (Exception ignore) {
            log.warn("ImpBMEM run fail:" + ignore.getMessage(), ignore);
        }
	}
}
