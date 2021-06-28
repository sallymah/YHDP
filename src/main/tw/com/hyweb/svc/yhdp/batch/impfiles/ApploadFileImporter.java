/**
 * changelog
 * --------------------
 * 20081201
 * duncan,anny,jesse
 * 若 BONUS 效期沒給, 去 TB_BONUS_DTL 主檔取得 BONUS 效期定義
 * --------------------
 * 20081128
 * duncan
 * bug fix, 修正 clare 20081127 修改後造成的 bug
 * --------------------
 * 20081127
 * Clare
 * 當日生效之指定加值後續動作由前置作業處理
 * 匯入指定加值只需insert TB_APPOINT_RELOAD相關資料
 * 也不需異動餘額
 * --------------------
 * 20071212
 * Batch: 持卡人資料異動通知檔/指定回饋名單檔/活動適用名單檔/指定加值匯入檔
 *  - 以下欄位可拆成 “Region_id + 原欄位名稱”
 *    帳戶代碼: Region_id + 帳戶代碼
 *    客戶代號: Region_id +客戶代號
 *  - 需修改項目
 *    檔案 Layout (每筆 record 總長度不變)
 *    Batch 相關程式
 * --------------------
 * 20071002
 * duncan
 * 修改 ComputePointUtil -> ComputeDateUtil (改用 CampaignAdapter API)
 * --------------------
 * 20070905
 * duncan,robert,tracy
 * 之前Robert有說 , BOCCC有一個客製化 :  ==> getPointSdateEdate() 要多填幾個輸入參數, 有call這個API的程式都要配合一起改.
 * 多 balanceType, cardNo, expiryDate, regionId, custId
 * 判斷 balanceType 要用 realBalanceType
 * --------------------
 * 20070627
 * duncan,tracy,anny,tiffany
 * 匯入檔案的更新餘額的 utility, call anny 寫的, 讓更新餘額叫到同一個地方,
 * 依 TB_TXN_DEF.SIGN 來決定要加 CR_BONUS_QTY 或 DB_BONUS_QTY,
 * 並依餘額表是否存在來做 INSERT 或 UPDATE
 * --------------------
 * 20070426
 * 不要用 bonusId 的第一碼來判斷是 chip point or coupon, host point or coupon, paper coupon,
 * 改用 TB_BONUS.BONUS_TYPE
 * --------------------
 * 20070425
 * 套用新的 MappingInfos.xml checkEmpty, checkDate, validValues 設定
 * --------------------
 * 20070313, bug fix, checkDataLine 若在資料檢查後已有錯, 直接傳回, 不再做其他檢
 * --------------------
 * 20070305, 修改程式 for 20070303(會議結論)
 * 20070303(會議結論)
 *     檔案                 如何決定歸戶id                  檢查BONUS_ID
 *    card_no           依其卡種決定其歸戶id            依其歸戶方式檢查bonus_id
 *    acct_id             直接用acct_id                      檢查主機紅利=3,7,8
 *    cust_id             直接用cust_id                     檢查主機紅利=3,7,8
 * paper coupon 一定要給卡號
 * --------------------
 */
package tw.com.hyweb.svc.yhdp.batch.impfiles;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import tw.com.hyweb.core.yhdp.batch.framework.impfiles.AbstractImpFile;
import tw.com.hyweb.core.yhdp.batch.framework.impfiles.DataLineInfo;
import tw.com.hyweb.core.yhdp.batch.framework.impfiles.FieldInfo;
import tw.com.hyweb.core.yhdp.common.misc.DateUtil;
import tw.com.hyweb.core.yhdp.common.misc.ExecuteSqlsInfo;

import tw.com.hyweb.util.string.StringUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <pre>
 * ImpAppload(For THIG, Only 以卡歸戶,主機紅利)
 * </pre>
 * author:Anny
 */
public class ApploadFileImporter extends AbstractImpFile
{
    private static Logger log = Logger.getLogger(ApploadFileImporter.class);
    
    private static final String SPRING_PATH = "config" + File.separator + "batch" + File.separator +
    "impfiles" + File.separator + "ImpAppload" + File.separator + "spring.xml";

    private String batchDate = "";
    
    private ApploadData apploadData = null;
    
    public ApploadFileImporter()
    {
    }

    public ExecuteSqlsInfo beforeHandleDataLine() throws Exception
    {
        batchDate = System.getProperty("date");
        if (StringUtil.isEmpty(batchDate)) {
            batchDate = DateUtil.getTodayString().substring(0, 8);
        }
    	    	    	
        return null;
    }
       
    public List checkDataLine(DataLineInfo lineInfo) throws Exception {
        
    	apploadData = new ApploadData(conn, getApploadValues(lineInfo), inctlInfo.getFullFileName());
    	
    	List descInfos = super.checkDataLine(lineInfo);
    	
    	if (descInfos.size() > 0) {
            log.info("checkDataLine:" + descInfos);
            return descInfos;
        }
    	
    	ApploadChecker checker = new ApploadChecker(apploadData, getApploadFieldInfos(lineInfo));
    	descInfos.addAll(checker.checker(conn));
    	
    	log.info("checkDataLine:" + descInfos);
    	
        return descInfos;
    }
    
    /**
     * @param dataline
     * @return
     */
    private Map<String, String> getApploadValues(DataLineInfo dataline)
    {
        Map<String, String> apploadValues = new HashMap<String, String>();
        apploadValues.put("CARD_NO", (String) dataline.getFieldData("field02"));
        apploadValues.put("EXPIRY_DATE", DateUtil.normalizeDate((String) dataline.getFieldData("field03")));
        // set back
        dataline.getFieldData().put("field03", apploadValues.get("EXPIRY_DATE"));
        apploadValues.put("ACQ_MEM_ID", (String) dataline.getFieldData("field04"));
        apploadValues.put("MERCH_ID", (String) dataline.getFieldData("field05"));
        apploadValues.put("AWARD_DATE", (String) dataline.getFieldData("field06"));
        apploadValues.put("BONUS_ID", (String) dataline.getFieldData("field07"));
        apploadValues.put("BONUS_SDATE",  (String) dataline.getFieldData("field08"));
        apploadValues.put("BONUS_EDATE", (String) dataline.getFieldData("field09"));
        apploadValues.put("BONUS_QTY", Double.toString(((Number) dataline.getFieldData("field10")).doubleValue()));
        apploadValues.put("VALID_SDATE", (String) dataline.getFieldData("field11"));
        apploadValues.put("VALID_EDATE", (String) dataline.getFieldData("field12"));
        apploadValues.put("APPRLD_DESC", (String) dataline.getFieldData("field13"));
        
        
        return apploadValues;
    }

    private Map<String, FieldInfo> getApploadFieldInfos(DataLineInfo dataline)
    {
        Map<String, FieldInfo> apploadFieldInfos = new HashMap<String, FieldInfo>();
        apploadFieldInfos.put("CARD_NO", dataline.getMappingInfo().getField("field02"));
        apploadFieldInfos.put("EXPIRY_DATE", dataline.getMappingInfo().getField("field03"));
        apploadFieldInfos.put("ACQ_MEM_ID", dataline.getMappingInfo().getField("field04"));
        apploadFieldInfos.put("MERCH_ID", dataline.getMappingInfo().getField("field05"));
        apploadFieldInfos.put("AWARD_DATE", dataline.getMappingInfo().getField("field06"));
        apploadFieldInfos.put("BONUS_ID", dataline.getMappingInfo().getField("field07"));
        apploadFieldInfos.put("BONUS_SDATE", dataline.getMappingInfo().getField("field08"));
        apploadFieldInfos.put("BONUS_EDATE", dataline.getMappingInfo().getField("field09"));
        apploadFieldInfos.put("BONUS_QTY", dataline.getMappingInfo().getField("field10"));
        apploadFieldInfos.put("VALID_SDATE", dataline.getMappingInfo().getField("field11"));
        apploadFieldInfos.put("VALID_EDATE", dataline.getMappingInfo().getField("field12"));
        apploadFieldInfos.put("APPRLD_DESC", dataline.getMappingInfo().getField("field13"));
        
        return apploadFieldInfos;
    }
    
    public List handleDataLine(DataLineInfo lineInfo) throws Exception
    {
    	List sqlsInfos = new ArrayList();
    	
    	ExecuteSqlsInfo sqlsInfo = new ExecuteSqlsInfo();
        sqlsInfo.setCommit(false);
        sqlsInfo.setSavepoint(true);
        
        sqlsInfo.setSqls(apploadData.handleAppointReload(conn, batchDate));
        
        sqlsInfos.add(sqlsInfo);
        log.info("handleDataLine:" + sqlsInfos);    
        
        return sqlsInfos;
    }
    
    public static ApploadFileImporter getInstance()
    {
        ApplicationContext apContext = new FileSystemXmlApplicationContext(SPRING_PATH);
        ApploadFileImporter instance = (ApploadFileImporter) apContext.getBean("impAppload");
        return instance;
    }

    public static void main(String[] args) {
    	ApploadFileImporter impAppload = null;
        try {
            File f = new File(SPRING_PATH);
            if (f.exists() && f.isFile()) {
            	impAppload = getInstance();
            }
            else {
            	impAppload = new ApploadFileImporter();
            }
            impAppload.setFileName("APPLOAD");
            impAppload.run(args);
        }
        catch (Exception ignore) {
            log.warn("ApploadFileImporter run fail:" + ignore.getMessage(), ignore);
        }
    }

	@Override
	public ExecuteSqlsInfo afterHandleDataLine() throws Exception {
		return null;
	}

}
