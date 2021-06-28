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
package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpCanceled;

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
 * ImpCanceled(For YHDP)
 * </pre>
 * author:Kevin
 */
public class ImpCanceled extends AbstractImpFile
{
    private static Logger log = Logger.getLogger(ImpCanceled.class);
    
    private static final String SPRING_PATH = "config" + File.separator + "batch" + File.separator +
    "impfiles" + File.separator + "ImpCanceled" + File.separator + "spring.xml";

    private String batchDate = "";
    
    private CanceledData canceledData = null;
      
    
    public ImpCanceled()
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
    	ExecuteSqlsInfo sqlsInfo2 = new ExecuteSqlsInfo();
        sqlsInfo2.setCommit(false);
        sqlsInfo2.setSavepoint(true);
        sqlsInfo2.setSqls(canceledData.addCardUpt(conn, batchDate));
        
        log.info("afterHandleDataLine:" + sqlsInfo2);    
        
        return sqlsInfo2;
    }
       
    public List checkDataLine(DataLineInfo lineInfo) throws Exception {
        
    	canceledData = new CanceledData(conn, getCanceledValues(lineInfo));
    	
    	List descInfos = super.checkDataLine(lineInfo);
    	
    	if (descInfos.size() > 0) {
            log.info("checkDataLine:" + descInfos);
            return descInfos;
        }
    	
    	CanceledChecker checker = new CanceledChecker(canceledData, getCanceledFieldInfos(lineInfo));
    	descInfos.addAll(checker.checker(conn));
    	
    	log.info("checkDataLine:" + descInfos);
    	
        return descInfos;
    }
    
    /**
     * @param dataline
     * @return
     */
    private Map<String, String> getCanceledValues(DataLineInfo dataline)
    {
        Map<String, String> canceledValues = new HashMap<String, String>();
        canceledValues.put("CARD_NO", (String) dataline.getFieldData("field02"));
        
        return canceledValues;
    }

    private Map<String, FieldInfo> getCanceledFieldInfos(DataLineInfo dataline)
    {
        Map<String, FieldInfo> canceledFieldInfos = new HashMap<String, FieldInfo>();
        canceledFieldInfos.put("CARD_NO", dataline.getMappingInfo().getField("field02"));
        
        return canceledFieldInfos;
    }
    
    public List handleDataLine(DataLineInfo lineInfo) throws Exception
    {
    	List sqlsInfos = new ArrayList();
    	
    	ExecuteSqlsInfo sqlsInfo2 = new ExecuteSqlsInfo();
        sqlsInfo2.setCommit(false);
        sqlsInfo2.setSavepoint(true);
        sqlsInfo2.setSqls(canceledData.handleCanceled(conn, batchDate));
        sqlsInfos.add(sqlsInfo2);
        
        log.info("handleDataLine:" + sqlsInfos);    
        
        return sqlsInfos;
    }
    
    public static ImpCanceled getInstance()
    {
        ApplicationContext apContext = new FileSystemXmlApplicationContext(SPRING_PATH);
        ImpCanceled instance = (ImpCanceled) apContext.getBean("impCanceled");
        return instance;
    }

    public static void main(String[] args) {
    	ImpCanceled impCanceled = null;
        try {
            File f = new File(SPRING_PATH);
            if (f.exists() && f.isFile()) {
            	impCanceled = getInstance();
            }
            else {
            	impCanceled = new ImpCanceled();
            }
            impCanceled.setFileName("DESTROY");
            impCanceled.run(args);
        }
        catch (Exception ignore) {
            log.warn("impCanceled run fail:" + ignore.getMessage(), ignore);
        }
    }

}
