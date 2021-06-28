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
package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpMemGP;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import tw.com.hyweb.core.yhdp.batch.framework.impfiles.AbstractImpFile;
import tw.com.hyweb.core.yhdp.batch.framework.impfiles.DataLineInfo;
import tw.com.hyweb.core.yhdp.batch.framework.impfiles.FieldInfo;
import tw.com.hyweb.core.yhdp.common.misc.DateUtil;
import tw.com.hyweb.core.yhdp.common.misc.ExecuteSqlsInfo;
import tw.com.hyweb.util.string.StringUtil;

/**
 * <pre>
 * ImpMemGP(For YHDP)
 * </pre>
 * author:Kevin
 */
public class ImpMemGP extends AbstractImpFile
{
    private static Logger log = Logger.getLogger(ImpMemGP.class);
    
    private static final String SPRING_PATH = "config" + File.separator + "batch" + File.separator +
    "impfiles" + File.separator + "ImpMemGP" + File.separator + "spring.xml";

    private String batchDate = "";
    
    private MemGPData memGPData = null;
      
    
    public ImpMemGP()
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
    
    public ExecuteSqlsInfo afterHandleDataLine() throws Exception
    {    	
    	 return null;
    }
       
    public List checkDataLine(DataLineInfo lineInfo) throws Exception {
        
    	memGPData = new MemGPData(conn, getMemGPValues(lineInfo), inctlInfo.getFullFileName());
    	
    	List descInfos = super.checkDataLine(lineInfo);
    	
    	if (descInfos.size() > 0) {
            log.info("checkDataLine:" + descInfos);
            return descInfos;
        }
    	
    	MemGPChecker checker = new MemGPChecker(memGPData, getMemGPFieldInfos(lineInfo));
    	descInfos.addAll(checker.checker(conn));
    	
    	log.info("checkDataLine:" + descInfos);
    	
        return descInfos;
    }
    
    /**
     * @param dataline
     * @return
     */
    private Map<String, String> getMemGPValues(DataLineInfo dataline)
    {
        Map<String, String> memGPValues = new HashMap<String, String>();
        memGPValues.put("MEM_GROUP_NAME", (String) dataline.getFieldData("field03"));
        memGPValues.put("BUS_ID_NO", (String) dataline.getFieldData("field05"));
        memGPValues.put("CONTACT", (String) dataline.getFieldData("field07"));
        memGPValues.put("TEL", (String) dataline.getFieldData("field09"));
        memGPValues.put("FAX", (String) dataline.getFieldData("field11"));
        memGPValues.put("EMAIL", (String) dataline.getFieldData("field13"));
        memGPValues.put("CITY", (String) dataline.getFieldData("field15"));
        memGPValues.put("ZIP_CODE", (String) dataline.getFieldData("field17"));
        memGPValues.put("RLN_ENT_ID", (String) dataline.getFieldData("field19"));
        memGPValues.put("INDUSTRY_ID", (String) dataline.getFieldData("field21"));
        memGPValues.put("ADDRESS", (String) dataline.getFieldData("field23"));
        memGPValues.put("SIGN_DATE", (String) dataline.getFieldData("field25"));
        memGPValues.put("CANCEL_DATE", (String) dataline.getFieldData("field27"));
        memGPValues.put("EFFECTIVE_DATE", (String) dataline.getFieldData("field29"));
        memGPValues.put("RLD_MAX_AMT", (String) dataline.getFieldData("field31"));
           
        return memGPValues;
    }

    private Map<String, FieldInfo> getMemGPFieldInfos(DataLineInfo dataline)
    {
        Map<String, FieldInfo> memGPFieldInfos = new HashMap<String, FieldInfo>();
        memGPFieldInfos.put("MEM_GROUP_NAME", dataline.getMappingInfo().getField("field03"));
        memGPFieldInfos.put("BUS_ID_NO", dataline.getMappingInfo().getField("field05"));
        memGPFieldInfos.put("CONTACT", dataline.getMappingInfo().getField("field07"));
        memGPFieldInfos.put("TEL", dataline.getMappingInfo().getField("field09"));
        memGPFieldInfos.put("FAX", dataline.getMappingInfo().getField("field11"));
        memGPFieldInfos.put("EMAIL", dataline.getMappingInfo().getField("field13"));
        memGPFieldInfos.put("CITY", dataline.getMappingInfo().getField("field15"));
        memGPFieldInfos.put("ZIP_CODE", dataline.getMappingInfo().getField("field17"));
        memGPFieldInfos.put("RLN_ENT_ID", dataline.getMappingInfo().getField("field19"));
        memGPFieldInfos.put("INDUSTRY_ID", dataline.getMappingInfo().getField("field21"));
        memGPFieldInfos.put("ADDRESS", dataline.getMappingInfo().getField("field23"));
        memGPFieldInfos.put("SIGN_DATE", dataline.getMappingInfo().getField("field25"));
        memGPFieldInfos.put("CANCEL_DATE", dataline.getMappingInfo().getField("field27"));
        memGPFieldInfos.put("EFFECTIVE_DATE", dataline.getMappingInfo().getField("field29"));
        memGPFieldInfos.put("RLD_MAX_AMT", dataline.getMappingInfo().getField("field31"));
                
        return memGPFieldInfos;
    }
    
    public List handleDataLine(DataLineInfo lineInfo) throws Exception
    {
    	List sqlsInfos = new ArrayList();
    	
    	ExecuteSqlsInfo sqlsInfo2 = new ExecuteSqlsInfo();
        sqlsInfo2.setCommit(false);
        sqlsInfo2.setSavepoint(true);
        sqlsInfo2.setSqls(memGPData.handleMemGP(conn, batchDate));
        sqlsInfos.add(sqlsInfo2);
        
        log.info("handleDataLine:" + sqlsInfos);    
        
        return sqlsInfos;
    }
    
    public static ImpMemGP getInstance()
    {
        ApplicationContext apContext = new FileSystemXmlApplicationContext(SPRING_PATH);
        ImpMemGP instance = (ImpMemGP) apContext.getBean("ImpMemGP");
        return instance;
    }

    public static void main(String[] args) {
    	ImpMemGP impMemGP = null;
        try {
            File f = new File(SPRING_PATH);
            if (f.exists() && f.isFile()) {
            	impMemGP = getInstance();
            }
            else {
            	impMemGP = new ImpMemGP();
            }
            impMemGP.setFileName("YMEMGR");
            impMemGP.run(args);
        }
        catch (Exception ignore) {
            log.warn("ImpMemGP run fail:" + ignore.getMessage(), ignore);
        }
    }

}
