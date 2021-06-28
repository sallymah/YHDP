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
package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpMember;

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
public class ImpMember extends AbstractImpFile
{
    private static Logger log = Logger.getLogger(ImpMember.class);
    
    private static final String SPRING_PATH = "config" + File.separator + "batch" + File.separator +
    "impfiles" + File.separator + "ImpMember" + File.separator + "spring.xml";

    private String batchDate = "";
    
    private MemberData memberData = null;
      
    
    public ImpMember()
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
        
    	memberData = new MemberData(conn, getMemberValues(lineInfo), inctlInfo.getFullFileName());
    	
    	List descInfos = super.checkDataLine(lineInfo);
    	
    	if (descInfos.size() > 0) {
            log.info("checkDataLine:" + descInfos);
            return descInfos;
        }
    	
    	MemberChecker checker = new MemberChecker(memberData, getMemberFieldInfos(lineInfo));
    	descInfos.addAll(checker.checker(conn));
    	
    	log.info("checkDataLine:" + descInfos);
    	
        return descInfos;
    }
    
    /**
     * @param dataline
     * @return
     */
    private Map<String, Object> getMemberValues(DataLineInfo dataline)
    {
        Map<String, Object> memberValues = new HashMap<String, Object>();
        memberValues.put("MEM_GROUP_NAME", (String) dataline.getFieldData("field03"));
        memberValues.put("MEM_NAME", (String) dataline.getFieldData("field05"));
        memberValues.put("MEM_TYPE", (String) dataline.getFieldData("field07"));
        memberValues.put("AGENCY", (String) dataline.getFieldData("field09"));
        memberValues.put("BUS_ID_NO", (String) dataline.getFieldData("field11"));
        memberValues.put("CONTACT", (String) dataline.getFieldData("field13"));
        memberValues.put("TEL", (String) dataline.getFieldData("field15"));
        memberValues.put("FAX", (String) dataline.getFieldData("field17"));
        memberValues.put("EMAIL", (String) dataline.getFieldData("field19"));
        memberValues.put("CITY", (String) dataline.getFieldData("field21"));
        memberValues.put("ZIP_CODE", (String) dataline.getFieldData("field23"));
        memberValues.put("RLN_ENT_ID", (String) dataline.getFieldData("field25"));
        memberValues.put("INDUSTRY_ID", (String) dataline.getFieldData("field27"));
        memberValues.put("ADDRESS", (String) dataline.getFieldData("field29"));
        memberValues.put("EFFECTIVE_DATE", (String) dataline.getFieldData("field31"));
        memberValues.put("STATUS", (String) dataline.getFieldData("field33"));
        memberValues.put("SAM_LOGON_TIME", (Number) dataline.getFieldData("field35"));
        memberValues.put("REGFEES", (String) dataline.getFieldData("field37"));
        memberValues.put("FEE_REMIT_DATE", (String) dataline.getFieldData("field39"));
           
        return memberValues;
    }

    private Map<String, FieldInfo> getMemberFieldInfos(DataLineInfo dataline)
    {
        Map<String, FieldInfo> memberFieldInfos = new HashMap<String, FieldInfo>();
        memberFieldInfos.put("MEM_GROUP_NAME", dataline.getMappingInfo().getField("field03"));
        memberFieldInfos.put("MEM_NAME", dataline.getMappingInfo().getField("field05"));
        memberFieldInfos.put("MEM_TYPE", dataline.getMappingInfo().getField("field07"));
        memberFieldInfos.put("AGENCY", dataline.getMappingInfo().getField("field09"));
        memberFieldInfos.put("BUS_ID_NO", dataline.getMappingInfo().getField("field11"));
        memberFieldInfos.put("CONTACT", dataline.getMappingInfo().getField("field13"));
        memberFieldInfos.put("TEL", dataline.getMappingInfo().getField("field15"));
        memberFieldInfos.put("FAX", dataline.getMappingInfo().getField("field17"));
        memberFieldInfos.put("EMAIL", dataline.getMappingInfo().getField("field19"));
        memberFieldInfos.put("CITY", dataline.getMappingInfo().getField("field21"));
        memberFieldInfos.put("ZIP_CODE", dataline.getMappingInfo().getField("field23"));
        memberFieldInfos.put("RLN_ENT_ID", dataline.getMappingInfo().getField("field25"));
        memberFieldInfos.put("INDUSTRY_ID", dataline.getMappingInfo().getField("field27"));
        memberFieldInfos.put("ADDRESS", dataline.getMappingInfo().getField("field29"));
        memberFieldInfos.put("EFFECTIVE_DATE", dataline.getMappingInfo().getField("field31"));
        memberFieldInfos.put("STATUS", dataline.getMappingInfo().getField("field33"));
        memberFieldInfos.put("SAM_LOGON_TIME", dataline.getMappingInfo().getField("field35"));
        memberFieldInfos.put("REGFEES", dataline.getMappingInfo().getField("field37"));
        memberFieldInfos.put("FEE_REMIT_DATE", dataline.getMappingInfo().getField("field39"));
        
        return memberFieldInfos;
    }
    
    public List handleDataLine(DataLineInfo lineInfo) throws Exception
    {
    	List sqlsInfos = new ArrayList();
    	
    	ExecuteSqlsInfo sqlsInfo2 = new ExecuteSqlsInfo();
        sqlsInfo2.setCommit(false);
        sqlsInfo2.setSavepoint(true);
        sqlsInfo2.setSqls(memberData.handleMember(conn, batchDate));
        sqlsInfos.add(sqlsInfo2);
        
        log.info("handleDataLine:" + sqlsInfos);    
        
        return sqlsInfos;
    }
    
    public static ImpMember getInstance()
    {
        ApplicationContext apContext = new FileSystemXmlApplicationContext(SPRING_PATH);
        ImpMember instance = (ImpMember) apContext.getBean("ImpMember");
        return instance;
    }

    public static void main(String[] args) {
    	ImpMember impMember = null;
        try {
            File f = new File(SPRING_PATH);
            if (f.exists() && f.isFile()) {
            	impMember = getInstance();
            }
            else {
            	impMember = new ImpMember();
            }
            impMember.setFileName("YMEMBER");
            impMember.run(args);
        }
        catch (Exception ignore) {
            log.warn("ImpMember run fail:" + ignore.getMessage(), ignore);
        }
    }

}
