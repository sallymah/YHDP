package tw.com.hyweb.svc.yhdp.batch.expfiles.ExpSettleOut;

import static tw.com.hyweb.core.cp.batch.util.PreparedStatementUtils.executeUpdate;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import tw.com.hyweb.core.cp.batch.framework.Layer1Constants;
import tw.com.hyweb.core.cp.batch.util.BatchUtil;
import tw.com.hyweb.core.cp.common.misc.DateRange;
import tw.com.hyweb.util.date.DateUtil;
import tw.com.hyweb.service.db.DBService;
import tw.com.hyweb.service.db.info.TbBatchResultInfo;
import tw.com.hyweb.svc.yhdp.batch.util.BatchUtils;
import tw.com.hyweb.util.ReleaseResource;
import tw.com.hyweb.util.string.StringUtil;


public class ArrangeSettleOut {
	private static final Logger log = Logger.getLogger(ArrangeSettleOut.class);
	
	private Connection conn = null;
	
	private final TbBatchResultInfo batchResultInfo;
	
	private HashMap memberDatalist = new HashMap();
	private String ecashBonusIds = "";
	
	protected DateRange procPeriod = null;
	
	private int expSeqNo = 1;
	
	private String SRA = "SRA";
	private String FRA = "FRA";
	private String OTA = "OTA";

	public ArrangeSettleOut(TbBatchResultInfo batchResultInfo) {
		// TODO Auto-generated constructor stub
		this.batchResultInfo = batchResultInfo;
	}

	public void action(String batchDate) throws Exception {

    	try{
    		this.conn = DBService.getDBService().getConnection(Layer1Constants.DSNAME_BATCH);
    		
    		memberDatalist = getMember(conn);
    		ecashBonusIds = getSVBonus(conn);
    		
    		String codeId = "";
    		
    		//日產帳務代碼  統一邏輯SRA
    		//Case code_id in (‘S100’,’S109’,’S209’,’S120’,’S200’,’S104’,’S105’,’S106’,’S250’,’S251’) 
    		//S100加值款,S109加值調整,S120退卡退費,S200消費扣款,S209消費款調整,S104自動加值,S105自動加值剔退,S106自動加值剔退再提示
    		//2015/10/26>S250 HappyCash聯名卡餘額轉退,S251 HappyCash聯名卡掛失贖回餘額
    		Vector sraCodeDefListDaily = getCodeDaily(conn, SRA);
    		
    		for ( int i= 0; i < sraCodeDefListDaily.size(); i++ ){
    			HashMap codeDefDaily = (HashMap) sraCodeDefListDaily.get(i);
    			codeId = (String) codeDefDaily.get("CODE_ID");
    			
    			log.debug("procSettleResult CODE_ID= "+codeDefDaily.get("CODE_ID")+"  CODE_DESC= "+codeDefDaily.get("CODE_DESC"));
				procSettleResult(codeDefDaily,batchDate);
				if ( codeId.equalsIgnoreCase("S100") ){
					procAppReload(codeDefDaily,batchDate);
					remarkAppReload(batchDate);
				}
    		}
    		
    		//日產帳務代碼  客製化邏輯OTA
    		Vector otaCodeDefListDaily = getCodeDaily(conn, OTA);
    		
    		for ( int i= 0; i < otaCodeDefListDaily.size(); i++ ){
    			HashMap codeDefDaily = (HashMap) otaCodeDefListDaily.get(i);
    			codeId = (String) codeDefDaily.get("CODE_ID");
    			
    			if (codeId.equalsIgnoreCase("S199")){
    				log.debug("procOnlTxn CODE_ID= "+codeDefDaily.get("CODE_ID")+"  CODE_DESC= "+codeDefDaily.get("CODE_DESC"));
    				procOnlTxn(codeDefDaily,batchDate);
    			}
    			else if (codeId.equalsIgnoreCase("S202") || codeId.equalsIgnoreCase("S203")){
    				log.debug("procTrans CODE_ID= "+codeDefDaily.get("CODE_ID")+"  CODE_DESC= "+codeDefDaily.get("CODE_DESC"));
    				
    				List pcodeList = new ArrayList();
    				if ( codeId.equalsIgnoreCase("S202") ){
    					pcodeList.add("9757");
    					pcodeList.add("9827");
    					pcodeList.add("7567");
    					pcodeList.add("7568");
    				}
    				else if (codeId.equalsIgnoreCase("S203")){
    					pcodeList.add("7547");
    					pcodeList.add("7557");
    					// 贖回調整2016/03/14*/ ’7967’,’7977’,’7987’,’7997’
    					pcodeList.add("7967");
    					pcodeList.add("7977");
    					pcodeList.add("7987");
    					pcodeList.add("7997");
    				}
    				
    				procTrans(codeDefDaily,batchDate,pcodeList);
    			}
    			//代墊款 S205 消費代墊款, S107 加值代款款(沖銷款)
    			else if (codeId.equalsIgnoreCase("S205") || codeId.equalsIgnoreCase("S107")){
    				log.debug("procTrans CODE_ID= "+codeDefDaily.get("CODE_ID")+"  CODE_DESC= "+codeDefDaily.get("CODE_DESC"));
    				
    				List pcodeList = new ArrayList();

    				StringBuffer sqlWhere = new StringBuffer();
    				
    				//代墊款(交通P_CODE)
    				if ( codeId.equalsIgnoreCase("S205") ){
    					pcodeList.add("7402");	// 電子票值扣款進站/ 進場
    					pcodeList.add("7403");	// 電子票卡扣點進站/ 進場
    					pcodeList.add("7405");	// 罰款
    					pcodeList.add("7410");	// 電子票值扣款出站/ 出場
    					pcodeList.add("7412");	// 電子票值扣款出站/ 出場 補登
    					pcodeList.add("7418");	// 離線現金加值取消
    					pcodeList.add("7438");	// 信用卡離線自動加值取消
    					pcodeList.add("7428");	// 友善模式加值(送錢)取消                  
    					pcodeList.add("7448");	// 友善模式加值(送錢)取消補登
    					pcodeList.add("7458");	// 同站加值補回取消(同站進出補回取消
    					pcodeList.add("7415");	// 電子票值扣款進站/ 進場(優惠)
    					pcodeList.add("7417");	// 電子票卡扣點進站/ 進場(優惠)
    					pcodeList.add("7419");	// 罰款(優惠)
    					pcodeList.add("7420");	// 電子票值扣款出站/ 出場(優惠)
    					pcodeList.add("7421");	// 電子票值扣款出站/ 出場 補登(優惠)
    					pcodeList.add("7617");	// 交通一般扣款
    					pcodeList.add("7627");	// 交通優惠扣款
    					pcodeList.add("7422");	// 一般商店消費
    					pcodeList.add("7424");	// 電子票值扣款進站/ 進場 補登
    					pcodeList.add("7468");	// 現金加值取消補登
    					pcodeList.add("7478");	// 信用卡自動加值取消補登
    					pcodeList.add("7430");	// 異常交易處理- 電子票值減少
    					
    					sqlWhere.append("AND P_CODE IN ("); 
    					for( int j = 0; j < pcodeList.size(); j++ ){
    						sqlWhere.append("'" + pcodeList.get(j) + "'");
    							if ( j < pcodeList.size()-1 )
    								sqlWhere.append(", ");
    					}
    					sqlWhere.append(") ");
    					
    				}
    				//代墊款沖銷(所有正向交易)
    				else if (codeId.equalsIgnoreCase("S107")){
    					sqlWhere.append("AND TXN_CODE IN (SELECT TXN_CODE FROM TB_TXN_DEF WHERE BAL_FLAG = '1' AND SIGN = 'P') ");
    				}
    				
    				procAdvOthers(codeDefDaily,batchDate,sqlWhere.toString());
    			}
    			//日產手續費
    			else if (codeId.equalsIgnoreCase("S108")){
    				procFeeResultForDaily(codeDefDaily,batchDate);
    			}
    			//退卡退費實付金額(應付金額-罰款)
    			else if (codeId.equalsIgnoreCase("S206")){
    				procBackCardRefund(codeDefDaily,batchDate);
    			}
    			//退卡退費手續費(罰款)
    			else if (codeId.equalsIgnoreCase("S207")){
    				procBackCardFine(codeDefDaily,batchDate);
    			}
    			//自動加值剔退再提示_不處理
    			else if (codeId.equalsIgnoreCase("S113")){
    				procNotReminder(codeDefDaily,batchDate);
    			}
    			//指定加值效期修改駁回退款
    			else if (codeId.equalsIgnoreCase("S114")){
    				procRejectAppload(codeDefDaily,batchDate);
    			}
    		}
    		
    		String feeRemitDay = "";
    		String memId = "";
    		
    		Vector memberInfoList = getMemInfoList(conn);
    		Vector fraCodeDefListMonthly= getCodeMonthly(conn, FRA);
    		Vector otaCodeDefListMonthly= getCodeMonthly(conn, OTA);
    		
    		for ( int i= 0; i < memberInfoList.size(); i++ ){
    			HashMap memberInf = (HashMap) memberInfoList.get(i);
    			feeRemitDay = (String) memberInf.get("FEE_REMIT_DATE");
    			log.debug("MEM_ID: "+memberInf.get("MEM_ID")+
    					"  AGENCY: "+memberInf.get("AGENCY")+
    					"  INDUSTRY_ID: "+memberInf.get("INDUSTRY_ID")+
    					"  FEE_REMIT_DATE: "+memberInf.get("FEE_REMIT_DATE")+
    					"  REGFEES: "+memberInf.get("REGFEES")+
    					"  NEWCARD_PERFEE: "+memberInf.get("NEWCARD_PERFEE")+
    					"  REPCARD_PERFEE: "+memberInf.get("REPCARD_PERFEE"));

    			if ( feeRemitDay != null ){
    				if (feeRemitDay.equals(batchDate.substring(6,8))|| 
    					(feeRemitDay.equals("99") && BatchUtil.getLastDayOfMonth(batchDate).equals(batchDate.substring(6,8)))){
    				
	    				String cycle = "M"+feeRemitDay;
	    	    		setProcPeriod(handleMXXCycle(batchDate, cycle));
	    	    		log.debug("cycle: "+cycle+ "  procPeriod.getStartDate(): "+procPeriod.getStartDate()+ "  procPeriod.getEndDate(): "+procPeriod.getEndDate());
	    				
	    	    		memId = (String) memberInf.get("MEM_ID");
	    				
	    	    		//月產帳務代碼  統一邏輯FRA 
	    	    		//S101加值手續費成本,S201消費手續費收入,S103自動加值手續費,S402簽帳回饋金
	    	    		// 20161212 S400: 發卡權利金(新卡回饋金)、S401: 發卡權利金(換補發卡回饋金) 改為統一邏輯FRA
	    	    		for ( int j= 0; j < fraCodeDefListMonthly.size(); j++ ){
	    	    			HashMap codeDefMonthly = (HashMap) fraCodeDefListMonthly.get(j);
	    	    			codeId = (String) codeDefMonthly.get("CODE_ID");
	    	    			
	    	    			log.debug("procSettleResult CODE_ID= "+codeDefMonthly.get("CODE_ID")+"  CODE_DESC= "+codeDefMonthly.get("CODE_DESC"));
    	    				procFeeResult(codeDefMonthly,batchDate,memId);
	    				}
	    	    			
	    	    		//月產帳務代碼  客製化邏輯OTA
	    	    		for ( int j= 0; j < otaCodeDefListMonthly.size(); j++ ){
	    	    			
	    	    			HashMap codeDefMonthly = (HashMap) otaCodeDefListMonthly.get(j);
	    	    			codeId = (String) codeDefMonthly.get("CODE_ID");
	    	    			
	    	    			//S102 記名手續費
	    	    			if ( codeId.equalsIgnoreCase("S102") && Double.parseDouble(memberInf.get("REGFEES").toString()) > 0){
	    	    				log.debug("procSettleResult CODE_ID= "+codeDefMonthly.get("CODE_ID")+"  CODE_DESC= "+codeDefMonthly.get("CODE_DESC"));
	    	    				String regfees = memberInf.get("REGFEES").toString();
	    	    				procRegCard(codeDefMonthly,batchDate,memId,regfees);
	    	    			}
//	    	    			else if ( codeId.equalsIgnoreCase("S400") && Double.parseDouble(memberInf.get("NEWCARD_PERFEE").toString()) > 0){
//	    	    				log.debug("procSettleResult CODE_ID= "+codeDefMonthly.get("CODE_ID")+"  CODE_DESC= "+codeDefMonthly.get("CODE_DESC"));
//	    	    				String newCardFee = memberInf.get("NEWCARD_PERFEE").toString();
//	    	    				procCardFee(codeDefMonthly,batchDate,memId,newCardFee," AND PRIMARY_CARD IN ('0','1') ");
//	    	    			}
//	    	    			else if ( codeId.equalsIgnoreCase("S401") && Double.parseDouble(memberInf.get("REPCARD_PERFEE").toString()) > 0){
//	    	    				log.debug("procSettleResult CODE_ID= "+codeDefMonthly.get("CODE_ID")+"  CODE_DESC= "+codeDefMonthly.get("CODE_DESC"));
//	    	    				String repCardFee = memberInf.get("REPCARD_PERFEE").toString();
//	    	    				procCardFee(codeDefMonthly,batchDate,memId,repCardFee," AND PRIMARY_CARD = '2' ");
//	    	    			}
	    	    			//S111 代墊款餘額
	    	    			else if ( codeId.equalsIgnoreCase("S111")){
	    	    				log.debug("procSettleResult CODE_ID= "+codeDefMonthly.get("CODE_ID")+"  CODE_DESC= "+codeDefMonthly.get("CODE_DESC"));
	    	    				procExpPrepaidBal(codeDefMonthly,batchDate,memId);
	    	    			}
	    	    			//退卡手續費(罰款)_未稅
	    	    			else if (codeId.equalsIgnoreCase("S404")){
	    	    				procTrafficPenalty(codeDefMonthly,batchDate,memId);
	    	    			}
	    	    			//退卡佣金_未稅
	    	    			else if (codeId.equalsIgnoreCase("S405")){
	    	    				procCommission(codeDefMonthly,batchDate,memId);
	    	    			}
	    	    		}
    				}
    			}
    		}
    	}
    	catch (Exception ignore) {
            log.warn("Settleout action warn:" + ignore.getMessage(), ignore);
            throw ignore;
        }
    	finally {
    		 ReleaseResource.releaseDB(conn);
    	}
    }

	private String getSVBonus(Connection conn) throws SQLException {
		List ecashBonusIds = new ArrayList();
		Statement stmt = null;
        ResultSet rs = null;
        String ecashBonusId = "";

        String seqnoSql = "SELECT ECASH_BONUS_ID FROM TB_BONUS_ISS_DEF WHERE BONUS_BASE='C'";
        try {
        	stmt = conn.createStatement();
        	log.debug("getSVBonusSql: "+seqnoSql);
        	rs = stmt.executeQuery(seqnoSql);
        	while (rs.next()) {
        		ecashBonusId = rs.getString(1);
        		ecashBonusIds.add(ecashBonusId);
        	}
        }
        catch (SQLException e) {
            log.warn("Settleout action warn:" + e.getMessage());
        }
        finally {
              ReleaseResource.releaseDB(null, stmt, rs);
        }
        
        StringBuilder condition = new StringBuilder();

        condition.append('(');

        for (int i = 0; i < ecashBonusIds.size(); ++i)
        {
            condition.append("'").append(ecashBonusIds.get(i)).append("'");

            if (i != ecashBonusIds.size() - 1)
            {
                condition.append(",");
            }
        }

        condition.append(')').append(" ");
        log.info("condition.toString(): "+condition.toString());
        return condition.toString();

	}

	private HashMap<String, MemberData> getMember(Connection conn) throws SQLException {
		HashMap<String, MemberData> memberDatalist = new HashMap<String, MemberData>();
		Statement stmt = null;
        ResultSet rs = null;
        String memId = "";
        String seqnoSql = "SELECT MEM_ID, AGENCY, INDUSTRY_ID, TAX_CONTAIN_FLAG FROM TB_MEMBER";
        try {
        	stmt = conn.createStatement();
        	log.debug("getMemberSql: "+seqnoSql);
        	rs = stmt.executeQuery(seqnoSql);
        	while (rs.next()) {
        		MemberData memberData = new MemberData();
        		memId = rs.getString(1);
        		memberData.setMapInfo(rs);
        		/*if ( rs.getString(1) != null )
        			memberData.setMemId(memId);
        		if ( rs.getString(2) != null )
        			memberData.setAgency((String) rs.getString(2));
        		if ( rs.getString(3) != null )
        			memberData.setIndustryId((String) rs.getString(3));	*/
        		memberDatalist.put(memId, memberData);
        	}
        }
        catch (SQLException e) {
            log.warn("Settleout action warn:" + e.getMessage());
        }
        finally {
              ReleaseResource.releaseDB(null, stmt, rs);
        }
		
		return memberDatalist;
	}

	private Vector getCodeDaily(Connection conn, String codeClass) throws SQLException {
		
		StringBuffer sql = new StringBuffer();
        sql.append("SELECT CODE_ID, CODE_DESC FROM TB_CODE_DEF ");
        sql.append("WHERE CODE_CLASS= '").append(codeClass).append("' AND UPT_USERID= 'DAILY'");
        log.info("sql.toString(): "+sql.toString());
        Vector targetDefVtr = BatchUtil.getInfoListHashMap(sql.toString());
        
        if (targetDefVtr.size()==0) 
        	log.warn("getCodeDaily(): no CODE_ID CODE_DESC in TB_CODE_DEF");

        log.info("targetDefVtr: "+targetDefVtr);
        return targetDefVtr;
	}
	
	private Vector getMemInfoList(Connection conn) {
		StringBuffer sql = new StringBuffer();
        sql.append("SELECT MEM_ID, AGENCY, INDUSTRY_ID, LPAD(FEE_REMIT_DATE, 2, '0') AS FEE_REMIT_DATE, ");
        sql.append("REGFEES, NEWCARD_PERFEE, REPCARD_PERFEE ");
        sql.append("FROM TB_MEMBER WHERE (");
        sql.append("SUBSTR(MEM_TYPE,1,1)='1' OR ");
        sql.append("SUBSTR(MEM_TYPE,2,1)='1' OR ");
        sql.append("SUBSTR(MEM_TYPE,3,1)='1' OR ");
        sql.append("SUBSTR(MEM_TYPE,4,1)='1' ) ");
        sql.append("AND TEST_FLAG = '0'");
        Vector targetDefVtr = BatchUtil.getInfoListHashMap(sql.toString());
        
        if (targetDefVtr.size()==0) 
        	log.warn("getMemInfoList(): no MEM_ID in TB_MEMBER");
        return targetDefVtr;
	}
	
	private Vector getCodeMonthly(Connection conn, String codeClass) throws SQLException {
		
		StringBuffer sql = new StringBuffer();
        sql.append("SELECT CODE_ID, CODE_DESC FROM TB_CODE_DEF ");
        sql.append("WHERE CODE_CLASS= '").append(codeClass).append("' AND UPT_USERID= 'MONTHLY'");
        Vector targetDefVtr = BatchUtil.getInfoListHashMap(sql.toString());
        
        if (targetDefVtr.size()==0) 
        	log.warn("getCodeMonthly(): no CODE_ID CODE_DESC in TB_CODE_DEF");
        return targetDefVtr;
	}
	
	private void procSettleResult( HashMap codeDef, String batchDate) throws Exception{
		List<SettleoutData> settleoutlist = new ArrayList();
		
		Statement stmt = null;
        ResultSet rs = null;
        SettleoutData settleout = null;
        MemberData memberData = null;
        StringBuffer seqnoSql = new StringBuffer();
        seqnoSql.append("SELECT TR.ACCOUNT_CODE, TR.TERM_SETTLE_DATE,TR.PROC_DATE, ");
		seqnoSql.append("(CASE WHEN TR.CREDIT_UNIT= 'U' THEN TR.CREDIT_ID WHEN TR.DEBIT_UNIT = 'U' THEN TR.DEBIT_ID ELSE TR.ACQ_MEM_ID END) AS ACQ_MEM_ID ,TB_CARD.CARD_PRODUCT,");
        seqnoSql.append("SUM(TR.SETTLE_AMT*(CASE WHEN SIGN = 'P' THEN 1 ELSE -1 END)) AS SETTLE_AMT, TR.EXP_PAY_DATE ");
        seqnoSql.append("FROM (SELECT * FROM TB_SETTLE_RESULT WHERE PROC_DATE = ").append(StringUtil.toSqlValueWithSQuote(batchDate)).append(" ) TR, TB_CARD,TB_TXN_DEF ");
        seqnoSql.append("WHERE TR.CARD_NO=TB_CARD.CARD_NO ");
        seqnoSql.append("AND TR.EXPIRY_DATE=TB_CARD.EXPIRY_DATE ");
        seqnoSql.append("AND ACCOUNT_CODE = ").append(StringUtil.toSqlValueWithSQuote((String) codeDef.get("CODE_ID"))).append(" ");
        seqnoSql.append("AND TR.TXN_CODE=TB_TXN_DEF.TXN_CODE ");
        seqnoSql.append("GROUP BY TR.ACCOUNT_CODE, TR.TERM_SETTLE_DATE,TR.PROC_DATE, (CASE WHEN TR.CREDIT_UNIT= 'U' THEN TR.CREDIT_ID WHEN TR.DEBIT_UNIT = 'U' THEN TR.DEBIT_ID ELSE TR.ACQ_MEM_ID END), TB_CARD.CARD_PRODUCT, TR.EXP_PAY_DATE ");
        seqnoSql.append("ORDER BY TR.ACCOUNT_CODE, TR.TERM_SETTLE_DATE,TR.PROC_DATE, (CASE WHEN TR.CREDIT_UNIT= 'U' THEN TR.CREDIT_ID WHEN TR.DEBIT_UNIT = 'U' THEN TR.DEBIT_ID ELSE TR.ACQ_MEM_ID END), TB_CARD.CARD_PRODUCT, TR.EXP_PAY_DATE ");
        
        		/*"SELECT TR.ACCOUNT_CODE, TR.TERM_SETTLE_DATE,TR.PROC_DATE, TR.ACQ_MEM_ID,TB_CARD.CARD_PRODUCT," +
        		"SUM(TR.SETTLE_AMT*(CASE WHEN ACQ_MEM_ID=DEBIT_ID THEN 1 ELSE -1 END)) AS SETTLE_AMT " +
        		"FROM (SELECT * FROM TB_SETTLE_RESULT WHERE PROC_DATE = " + StringUtil.toSqlValueWithSQuote(batchDate) + " ) TR, TB_CARD " +
        		"WHERE TR.CARD_NO=TB_CARD.CARD_NO AND TR.EXPIRY_DATE=TB_CARD.EXPIRY_DATE " +
        		"AND ACCOUNT_CODE = "+ StringUtil.toSqlValueWithSQuote((String) codeDef.get("CODE_ID"))+" " +
        		"GROUP BY TR.ACCOUNT_CODE, TR.TERM_SETTLE_DATE,TR.PROC_DATE, TR.ACQ_MEM_ID,TB_CARD.CARD_PRODUCT " +
        		"ORDER BY TR.ACCOUNT_CODE, TR.TERM_SETTLE_DATE,TR.PROC_DATE, TR.ACQ_MEM_ID,TB_CARD.CARD_PRODUCT";*/
        log.debug("procSettleResultSql: "+seqnoSql.toString());
        
        try {
        	stmt = conn.createStatement();

        	rs = stmt.executeQuery(seqnoSql.toString());
        	while (rs.next()) {
        		settleout = new SettleoutData();
        		settleout.setMapInfo(rs);

        		memberData = (MemberData) memberDatalist.get(settleout.getAcqMemId());
        		String Agency = StringUtil.isEmpty(memberData.getAgency()) ? "Z" : memberData.getAgency();
        		
        		settleout.setAgency(Agency);
        		settleout.setIndustryId(memberData.getIndustryId());
        		settleout.setCardCatId(rs.getString(5).substring(0,2)+"000");
        		settleout.setProductTypeId(rs.getString(5).substring(2,5) );
        		settleout.setExpPayDate(rs.getString(7));
        		settleoutlist.add(settleout);
        	}
        }
        catch (SQLException e) {
            log.warn("Settleout action warn:" + e.getMessage());
            throw e;
        }
        finally {
              ReleaseResource.releaseDB(null, stmt, rs);
        }
        
        if ( settleoutlist.size() != 0 ){
	        for (int i = 0; i < settleoutlist.size(); i++){
	        	executeUpdate(conn, getInsertHeaderSQL( ), getparameterValues(settleoutlist.get(i),batchDate));
	        	
	        	if ( i%100 ==0 )
	        		conn.commit();
	        }
	        conn.commit();
        }
	}

	private void procAppReload( HashMap codeDef, String batchDate) throws Exception{
		List<SettleoutData> settleoutlist = new ArrayList();
		
		Statement stmt = null;
        ResultSet rs = null;
        SettleoutData settleout = null;
        MemberData memberData = null;
        StringBuffer seqnoSql = new StringBuffer();
        
        seqnoSql.append(" SELECT EX_MEM_ID, CARD_PRODUCT, SUM(BONUS_QTY) FROM");
        seqnoSql.append(" (SELECT EX_MEM_ID, CARD_PRODUCT, BONUS_QTY");
        seqnoSql.append(" FROM TB_APPOINT_RELOAD, TB_APPOINT_RELOAD_DTL, TB_CARD");
        seqnoSql.append(" WHERE TB_APPOINT_RELOAD_DTL.CARD_NO = TB_APPOINT_RELOAD.CARD_NO");
        seqnoSql.append(" AND TB_APPOINT_RELOAD_DTL.EXPIRY_DATE = TB_APPOINT_RELOAD.EXPIRY_DATE");
        seqnoSql.append(" AND TB_APPOINT_RELOAD_DTL.BALANCE_ID = TB_APPOINT_RELOAD.BALANCE_ID");
        seqnoSql.append(" AND TB_APPOINT_RELOAD_DTL.AR_SERNO = TB_APPOINT_RELOAD.AR_SERNO");
        seqnoSql.append(" AND TB_CARD.CARD_NO = TB_APPOINT_RELOAD.CARD_NO");
        seqnoSql.append(" AND TB_CARD.EXPIRY_DATE = TB_APPOINT_RELOAD.EXPIRY_DATE");
        seqnoSql.append(" AND ((((TB_APPOINT_RELOAD.APRV_DATE =").append(StringUtil.toSqlValueWithSQuote(batchDate));
        seqnoSql.append(" AND AR_SRC ='B')");
        seqnoSql.append(" OR (TB_APPOINT_RELOAD.APRV_DATE =").append(StringUtil.toSqlValueWithSQuote(BatchUtil.getSomeDay(batchDate,-1)));
        seqnoSql.append(" AND AR_SRC ='U'))");
        seqnoSql.append(" AND OUTPUT_DATE = '00000000')");
        seqnoSql.append(" OR OUTPUT_DATE = ").append(StringUtil.toSqlValueWithSQuote(batchDate)).append(")");
        seqnoSql.append(" AND EX_MEM_ID <> '00000000'");
        seqnoSql.append(" AND BONUS_ID IN ").append(ecashBonusIds);
        seqnoSql.append(" AND NOT EXISTS");
        seqnoSql.append(" (SELECT 1");
        seqnoSql.append(" FROM TB_MEMBER");
        seqnoSql.append(" WHERE TEST_FLAG ='1'");
        seqnoSql.append(" AND TB_APPOINT_RELOAD.EX_MEM_ID = TB_MEMBER.MEM_ID))");
        /*seqnoSql.append(" UNION ALL");
        seqnoSql.append(" SELECT EX_MEM_ID, CARD_PRODUCT, BONUS_QTY*-1");
        seqnoSql.append(" FROM TB_APPOINT_RELOAD, TB_APPOINT_RELOAD_DTL, TB_CARD");
        seqnoSql.append(" WHERE TB_APPOINT_RELOAD_DTL.CARD_NO = TB_APPOINT_RELOAD.CARD_NO");
        seqnoSql.append(" AND TB_APPOINT_RELOAD_DTL.EXPIRY_DATE = TB_APPOINT_RELOAD.EXPIRY_DATE");
        seqnoSql.append(" AND TB_APPOINT_RELOAD_DTL.BALANCE_ID = TB_APPOINT_RELOAD.BALANCE_ID");
        seqnoSql.append(" AND TB_APPOINT_RELOAD_DTL.AR_SERNO = TB_APPOINT_RELOAD.AR_SERNO");
        seqnoSql.append(" AND TB_CARD.CARD_NO = TB_APPOINT_RELOAD.CARD_NO");
        seqnoSql.append(" AND TB_CARD.EXPIRY_DATE = TB_APPOINT_RELOAD.EXPIRY_DATE");
        seqnoSql.append(" AND REJECT_DATE =").append(StringUtil.toSqlValueWithSQuote(BatchUtil.getSomeDay(batchDate,-1)));
        seqnoSql.append(" AND EX_MEM_ID <> '00000000'");
        seqnoSql.append(" AND BONUS_ID IN ").append(ecashBonusIds);
        seqnoSql.append(" AND NOT EXISTS");
        seqnoSql.append(" (SELECT 1");
        seqnoSql.append(" FROM TB_MEMBER");
        seqnoSql.append(" WHERE TEST_FLAG ='1'");
        seqnoSql.append(" AND TB_APPOINT_RELOAD.EX_MEM_ID = TB_MEMBER.MEM_ID))");*/
        seqnoSql.append(" GROUP BY EX_MEM_ID, CARD_PRODUCT");
        seqnoSql.append(" ORDER BY EX_MEM_ID, CARD_PRODUCT");
        
        /*seqnoSql.append("SELECT TT.PROC_DATE, TT.EX_MEM_ID,TB_CARD.CARD_PRODUCT, SUM(TTD.BONUS_QTY) AS QTY ");
        seqnoSql.append("FROM (SELECT * FROM TB_APPOINT_RELOAD WHERE ");
						"PROC_DATE = '"+batchDate+"' "+
        seqnoSql.append("((APRV_DATE='").append(batchDate).append("' AND AR_SRC='B') OR ");
        seqnoSql.append("(APRV_DATE='").append(BatchUtil.getSomeDay(batchDate,-1)).append("' AND AR_SRC='U')) ");
        seqnoSql.append("AND EX_MEM_ID <> '00000000' ");
        seqnoSql.append("AND NOT EXISTS (SELECT * FROM TB_MEMBER WHERE TEST_FLAG='1' AND TB_APPOINT_RELOAD.EX_MEM_ID = TB_MEMBER.MEM_ID) ) TT, ");
        seqnoSql.append("(SELECT * FROM TB_APPOINT_RELOAD_DTL ");
        seqnoSql.append("WHERE APRV_DATE BETWEEN '").append(BatchUtil.getSomeDay(batchDate,-1)).append("' AND '").append(batchDate).append("' ");
        seqnoSql.append(") TTD, TB_CARD ");
        seqnoSql.append("WHERE TT.CARD_NO=TB_CARD.CARD_NO ");
        seqnoSql.append("AND TT.EXPIRY_DATE=TB_CARD.EXPIRY_DATE ");
        seqnoSql.append("AND TTD.BONUS_ID IN "+ ecashBonusIds );
        seqnoSql.append("AND TT.CARD_NO=TTD.CARD_NO ");
		seqnoSql.append("AND TT.EXPIRY_DATE=TTD.EXPIRY_DATE ");
        seqnoSql.append("AND TT.AR_SERNO=TTD.AR_SERNO ");
        seqnoSql.append("GROUP BY TT.PROC_DATE, TT.EX_MEM_ID,TB_CARD.CARD_PRODUCT ");
        seqnoSql.append("ORDER BY TT.PROC_DATE, TT.EX_MEM_ID,TB_CARD.CARD_PRODUCT ");*/
        
        log.debug("procAppReloadSql: "+seqnoSql.toString());
        
        try {
        	stmt = conn.createStatement();

        	rs = stmt.executeQuery(seqnoSql.toString());
        	while (rs.next()) {
        		settleout = new SettleoutData();       		

        		settleout.setAcqMemId(rs.getString(1));
        		
        		memberData = (MemberData) memberDatalist.get(settleout.getAcqMemId());
        		String Agency = StringUtil.isEmpty(memberData.getAgency()) ? "Z" : memberData.getAgency();
        		
        		settleout.setAccountCode((String) codeDef.get("CODE_ID"));
        		settleout.setTermSettleDate(BatchUtil.getSomeDay(batchDate,-1));
        		settleout.setProcDate(batchDate);
        		settleout.setAgency(Agency);
        		settleout.setCardCatId(rs.getString(2).substring(0,2)+"000");
        		settleout.setProductTypeId(rs.getString(2).substring(2,5));
        		settleout.setIndustryId(memberData.getIndustryId());
        		settleout.setAmt(rs.getString(3));
        		
        		settleoutlist.add(settleout);
        	}
        }
        catch (SQLException e) {
            log.warn("Settleout action warn:" + e.getMessage());
            throw e;
        }
        finally {
              ReleaseResource.releaseDB(null, stmt, rs);
        }
        
        if ( settleoutlist.size() != 0 ){
	        for (int i = 0; i < settleoutlist.size(); i++){
	        	executeUpdate(conn, getInsertHeaderSQL( ), getparameterValues(settleoutlist.get(i),batchDate));
	        	
	        	if ( i%100 ==0 )
	        		conn.commit();
	        }
	        
	        conn.commit();
        }
	}
	
	private void procRejectAppload( HashMap codeDef, String batchDate) throws Exception{
		List<SettleoutData> settleoutlist = new ArrayList();
		
		Statement stmt = null;
        ResultSet rs = null;
        SettleoutData settleout = null;
        MemberData memberData = null;
        StringBuffer seqnoSql = new StringBuffer();
        
        seqnoSql.append(" SELECT EX_MEM_ID, CARD_PRODUCT, SUM(BONUS_QTY) FROM");
        seqnoSql.append(" (SELECT EX_MEM_ID, CARD_PRODUCT, (BONUS_QTY*-1) AS BONUS_QTY");
        seqnoSql.append(" FROM TB_APPOINT_RELOAD, TB_APPOINT_RELOAD_DTL, TB_CARD");
        seqnoSql.append(" WHERE TB_APPOINT_RELOAD_DTL.CARD_NO = TB_APPOINT_RELOAD.CARD_NO");
        seqnoSql.append(" AND TB_APPOINT_RELOAD_DTL.EXPIRY_DATE = TB_APPOINT_RELOAD.EXPIRY_DATE");
        seqnoSql.append(" AND TB_APPOINT_RELOAD_DTL.BALANCE_ID = TB_APPOINT_RELOAD.BALANCE_ID");
        seqnoSql.append(" AND TB_APPOINT_RELOAD_DTL.AR_SERNO = TB_APPOINT_RELOAD.AR_SERNO");
        seqnoSql.append(" AND TB_CARD.CARD_NO = TB_APPOINT_RELOAD.CARD_NO");
        seqnoSql.append(" AND TB_CARD.EXPIRY_DATE = TB_APPOINT_RELOAD.EXPIRY_DATE");
        seqnoSql.append(" AND REJECT_DATE =").append(StringUtil.toSqlValueWithSQuote(BatchUtil.getSomeDay(batchDate,-1)));
        seqnoSql.append(" AND EX_MEM_ID <> '00000000'");
        seqnoSql.append(" AND BONUS_ID IN ").append(ecashBonusIds);
        seqnoSql.append(" AND NOT EXISTS");
        seqnoSql.append(" (SELECT 1");
        seqnoSql.append(" FROM TB_MEMBER");
        seqnoSql.append(" WHERE TEST_FLAG ='1'");
        seqnoSql.append(" AND TB_APPOINT_RELOAD.EX_MEM_ID = TB_MEMBER.MEM_ID))");
        seqnoSql.append(" GROUP BY EX_MEM_ID, CARD_PRODUCT");
        seqnoSql.append(" ORDER BY EX_MEM_ID, CARD_PRODUCT");
        
        log.debug("procRejectApploadSql: "+seqnoSql.toString());
        
        try {
        	stmt = conn.createStatement();

        	rs = stmt.executeQuery(seqnoSql.toString());
        	while (rs.next()) {
        		settleout = new SettleoutData();       		

        		settleout.setAcqMemId(rs.getString(1));
        		
        		memberData = (MemberData) memberDatalist.get(settleout.getAcqMemId());
        		String Agency = StringUtil.isEmpty(memberData.getAgency()) ? "Z" : memberData.getAgency();
        		
        		settleout.setAccountCode((String) codeDef.get("CODE_ID"));
        		settleout.setTermSettleDate(BatchUtil.getSomeDay(batchDate,-1));
        		settleout.setProcDate(batchDate);
        		settleout.setAgency(Agency);
        		settleout.setCardCatId(rs.getString(2).substring(0,2)+"000");
        		settleout.setProductTypeId(rs.getString(2).substring(2,5));
        		settleout.setIndustryId(memberData.getIndustryId());
        		settleout.setAmt(rs.getString(3));
        		
        		settleoutlist.add(settleout);
        	}
        }
        catch (SQLException e) {
            log.warn("Settleout action warn:" + e.getMessage());
            throw e;
        }
        finally {
              ReleaseResource.releaseDB(null, stmt, rs);
        }
        
        if ( settleoutlist.size() != 0 ){
	        for (int i = 0; i < settleoutlist.size(); i++){
	        	executeUpdate(conn, getInsertHeaderSQL( ), getparameterValues(settleoutlist.get(i),batchDate));
	        	
	        	if ( i%100 ==0 )
	        		conn.commit();
	        }
	        
	        conn.commit();
        }
	}
	
	private void remarkAppReload( String batchDate) throws Exception{
		
		StringBuffer seqnoSql = new StringBuffer();
		seqnoSql.append(" UPDATE TB_APPOINT_RELOAD ");
		seqnoSql.append(" SET OUTPUT_DATE= '"+batchDate+"' ");
						/*"WHERE PROC_DATE = '"+batchDate+"' "+*/
		seqnoSql.append(" WHERE ((((TB_APPOINT_RELOAD.APRV_DATE =").append(StringUtil.toSqlValueWithSQuote(batchDate));
        seqnoSql.append(" AND AR_SRC ='B')");
        seqnoSql.append(" OR (TB_APPOINT_RELOAD.APRV_DATE =").append(StringUtil.toSqlValueWithSQuote(BatchUtil.getSomeDay(batchDate,-1)));
        seqnoSql.append(" AND AR_SRC ='U'))");
        seqnoSql.append(" AND OUTPUT_DATE = '00000000')");
        seqnoSql.append(" OR OUTPUT_DATE = ").append(StringUtil.toSqlValueWithSQuote(batchDate)).append(")");
		seqnoSql.append(" AND EX_MEM_ID <> '00000000' ");
		seqnoSql.append(" AND NOT EXISTS (SELECT * FROM TB_MEMBER WHERE TEST_FLAG='1' AND TB_APPOINT_RELOAD.EX_MEM_ID = TB_MEMBER.MEM_ID)");
						
						/*"WHERE (APRV_DATE='"+batchDate+"' AND AR_SRC='B') OR " +
						"(APRV_DATE='"+BatchUtil.getSomeDay(batchDate,-1)+"' AND AR_SRC='U')";*/
		
		DBService.getDBService().sqlAction(seqnoSql.toString(), conn, false);
		conn.commit();
	}
	
	private void procOnlTxn( HashMap codeDef, String batchDate) throws Exception{
		List<SettleoutData> settleoutlist = new ArrayList();
		
		Statement stmt = null;
        ResultSet rs = null;
        SettleoutData settleout = null;
        MemberData memberData = null;
        StringBuffer seqnoSql = new StringBuffer();
        seqnoSql.append("SELECT TXN_DATE, ACQ_MEM_ID,TB_CARD.CARD_PRODUCT,SUM(TTD.BONUS_QTY*(CASE WHEN SIGN = 'P' THEN 1 ELSE -1 END)) AS QSUM ");
        seqnoSql.append("FROM (SELECT * FROM TB_ONL_TXN WHERE TXN_DATE = '").append(BatchUtil.getSomeDay(batchDate,-1)).append("' AND P_CODE = '7707' AND STATUS='1' " );
        seqnoSql.append("AND NOT EXISTS (SELECT * FROM TB_MEMBER WHERE TEST_FLAG='1' AND TB_ONL_TXN.ACQ_MEM_ID = TB_MEMBER.MEM_ID) ) TT," );
        seqnoSql.append("(SELECT * FROM TB_ONL_TXN_DTL WHERE P_CODE = '7707') TTD," );
        seqnoSql.append("TB_CARD ,TB_TXN_DEF ");
        seqnoSql.append("WHERE TT.CARD_NO=TB_CARD.CARD_NO AND TT.EXPIRY_DATE=TB_CARD.EXPIRY_DATE AND TTD.BONUS_ID IN ").append( ecashBonusIds );
        seqnoSql.append("AND TT.CARD_NO=TTD.CARD_NO AND TT.EXPIRY_DATE=TTD.EXPIRY_DATE AND TT.LMS_INVOICE_NO=TTD.LMS_INVOICE_NO ");
        seqnoSql.append("AND TTD.TXN_CODE=TB_TXN_DEF.TXN_CODE ");
        seqnoSql.append("GROUP BY TXN_DATE, ACQ_MEM_ID, TB_CARD.CARD_PRODUCT ");
        seqnoSql.append("ORDER BY TXN_DATE, ACQ_MEM_ID, TB_CARD.CARD_PRODUCT ");
        log.debug("procOnlTxnSql: "+seqnoSql.toString());
        
        try {
        	stmt = conn.createStatement();

        	rs = stmt.executeQuery(seqnoSql.toString());
        	while (rs.next()) {
        		settleout = new SettleoutData();
        		settleout.setAccountCode((String) codeDef.get("CODE_ID"));
        		settleout.setTermSettleDate(rs.getString(1));
        		settleout.setAcqMemId(rs.getString(2));
        		settleout.setCardCatId(rs.getString(3).substring(0,2)+"000");
        		settleout.setProductTypeId(rs.getString(3).substring(2,5) );
        		settleout.setAmt(rs.getString(4));
        		settleout.setProcDate(batchDate);
        		
        		memberData = (MemberData) memberDatalist.get(settleout.getAcqMemId());
        		String Agency = StringUtil.isEmpty(memberData.getAgency()) ? "Z" : memberData.getAgency();
        		
        		settleout.setAgency(Agency);
        		settleout.setIndustryId(memberData.getIndustryId());
        		
        		settleoutlist.add(settleout);
        	}
        }
        catch (SQLException e) {
            log.warn("Settleout action warn:" + e.getMessage());
            throw e;
        }
        finally {
              ReleaseResource.releaseDB(null, stmt, rs);
        }
        
        for (int i = 0; i < settleoutlist.size(); i++){
        	executeUpdate(conn, getInsertHeaderSQL( ), getparameterValues(settleoutlist.get(i),batchDate));
        	
        	if ( i%100 ==0 )
        		conn.commit();
        }
        conn.commit();
	}
	
	private void procTrans( HashMap codeDef, String batchDate, List pcodeList) throws Exception{
		List<SettleoutData> settleoutlist = new ArrayList();
		
		String pcodeListSql = "";
		for( int i = 0; i < pcodeList.size(); i++ ){
				pcodeListSql = pcodeListSql + "'" + pcodeList.get(i) + "'";
				if ( i < pcodeList.size()-1 )
					pcodeListSql = pcodeListSql + ", ";
		}
		
		Statement stmt = null;
        ResultSet rs = null;
        SettleoutData settleout = null;
        MemberData memberData = null;
        StringBuffer seqnoSql = new StringBuffer();
        seqnoSql.append("SELECT TERM_SETTLE_DATE, ACQ_MEM_ID,TB_CARD.CARD_PRODUCT,TT.CARD_NO, SUM(TTD.BONUS_QTY*(CASE WHEN SIGN = 'P' THEN 1 ELSE -1 END)) AS QTY ");
        seqnoSql.append("FROM (SELECT * FROM TB_TRANS WHERE CUT_DATE = '").append( batchDate ).append("' AND P_CODE IN (").append( pcodeListSql ).append(") ");
        seqnoSql.append("AND NOT EXISTS (SELECT * FROM TB_MEMBER WHERE TEST_FLAG='1' AND TB_TRANS.ACQ_MEM_ID = TB_MEMBER.MEM_ID) ) TT, ");
        seqnoSql.append("(SELECT * FROM TB_TRANS_DTL WHERE CUT_DATE = '").append( batchDate ).append("' AND P_CODE IN (").append( pcodeListSql ).append(")) TTD, ");
        seqnoSql.append("TB_CARD ,TB_TXN_DEF ");
        seqnoSql.append("WHERE TT.CARD_NO=TB_CARD.CARD_NO AND TT.EXPIRY_DATE=TB_CARD.EXPIRY_DATE AND TTD.BONUS_ID IN "+ ecashBonusIds);
        seqnoSql.append("AND TT.CARD_NO=TTD.CARD_NO AND TT.EXPIRY_DATE=TTD.EXPIRY_DATE ");
        seqnoSql.append("AND TT.LMS_INVOICE_NO=TTD.LMS_INVOICE_NO AND TTD.TXN_CODE=TB_TXN_DEF.TXN_CODE ");
        seqnoSql.append("GROUP BY TERM_SETTLE_DATE, ACQ_MEM_ID,TB_CARD.CARD_PRODUCT,TT.CARD_NO ");
        seqnoSql.append("ORDER BY TERM_SETTLE_DATE, ACQ_MEM_ID,TB_CARD.CARD_PRODUCT,TT.CARD_NO");
        
        log.debug("procTransSql: "+seqnoSql.toString());
        
        try {
        	stmt = conn.createStatement();
        	rs = stmt.executeQuery(seqnoSql.toString());
        	while (rs.next()) {
        		settleout = new SettleoutData();
        		settleout.setAccountCode((String) codeDef.get("CODE_ID"));
        		settleout.setTermSettleDate(rs.getString(1));
        		settleout.setAcqMemId(rs.getString(2));
        		settleout.setCardCatId(rs.getString(3).substring(0,2)+"000");
        		settleout.setProductTypeId(rs.getString(3).substring(2,5) );
        		settleout.setCardNo(rs.getString(4));
        		settleout.setAmt(rs.getString(5));
        		settleout.setProcDate(batchDate);
        		
        		memberData = (MemberData) memberDatalist.get(settleout.getAcqMemId());
        		String Agency = StringUtil.isEmpty(memberData.getAgency()) ? "Z" : memberData.getAgency();
        		
        		settleout.setAgency(Agency);
        		settleout.setIndustryId(memberData.getIndustryId());
        		
        		settleoutlist.add(settleout);
        	}
        }
        catch (SQLException e) {
            log.warn("Settleout action warn:" + e.getMessage());
            throw e;
        }
        finally {
              ReleaseResource.releaseDB(null, stmt, rs);
        }
        
        for (int i = 0; i < settleoutlist.size(); i++){
        	executeUpdate(conn, getInsertHeaderSQL( ), getparameterValues(settleoutlist.get(i),batchDate));
        	
        	if ( i%100 ==0 )
        		conn.commit();
        }
        conn.commit();
	}
	
	private void procAdvOthers( HashMap codeDef, String batchDate, String sqlWhere) throws Exception{
		List<SettleoutData> settleoutlist = new ArrayList();
		
		Statement stmt = null;
        ResultSet rs = null;
        SettleoutData settleout = null;
        MemberData memberData = null;
        StringBuffer seqnoSql = new StringBuffer();
        seqnoSql.append("SELECT PROC_DATE, ACQ_MEM_ID, SUM(TT.PREPAID_CHARGE*(CASE WHEN SIGN = 'P' THEN 1 ELSE -1 END)) AS QTY ");
        seqnoSql.append("FROM (SELECT * FROM TB_PREPAID_CHARGE TPC ");
        seqnoSql.append("WHERE PROC_DATE='").append( batchDate ).append("' ");
        seqnoSql.append(sqlWhere); 
        seqnoSql.append("AND NOT EXISTS (SELECT * FROM TB_MEMBER TM WHERE TEST_FLAG='1' AND TM.MEM_ID=TPC.ACQ_MEM_ID)) TT, TB_TXN_DEF ");
        seqnoSql.append("WHERE TT.TXN_CODE=TB_TXN_DEF.TXN_CODE ");
        seqnoSql.append("GROUP BY PROC_DATE, ACQ_MEM_ID ");
        seqnoSql.append("ORDER BY PROC_DATE, ACQ_MEM_ID ");
        
        log.debug("procAdvOthersSql: "+seqnoSql.toString());
        
        try {
        	stmt = conn.createStatement();
        	rs = stmt.executeQuery(seqnoSql.toString());
        	while (rs.next()) {
        		settleout = new SettleoutData();
        		settleout.setAccountCode((String) codeDef.get("CODE_ID"));
        		settleout.setTermSettleDate(rs.getString(1));
        		settleout.setAcqMemId(rs.getString(2));
        		settleout.setCardCatId("99999");
        		settleout.setProductTypeId("999");
        		settleout.setAmt(rs.getString(3));
        		settleout.setProcDate(batchDate);
        		
        		memberData = (MemberData) memberDatalist.get(settleout.getAcqMemId());
        		String Agency = StringUtil.isEmpty(memberData.getAgency()) ? "Z" : memberData.getAgency();
        		
        		settleout.setAgency(Agency);
        		settleout.setIndustryId(memberData.getIndustryId());
        		
        		settleoutlist.add(settleout);
        	}
        }
        catch (SQLException e) {
            log.warn("Settleout action warn:" + e.getMessage());
            throw e;
        }
        finally {
              ReleaseResource.releaseDB(null, stmt, rs);
        }
        
        for (int i = 0; i < settleoutlist.size(); i++){
        	executeUpdate(conn, getInsertHeaderSQL( ), getparameterValues(settleoutlist.get(i),batchDate));
        	
        	if ( i%100 ==0 )
        		conn.commit();
        }
        conn.commit();
	}
	
	private void procFeeResultForDaily(HashMap codeDef, String batchDate) throws Exception{
		List<SettleoutData> settleoutlist = new ArrayList();
		
		Statement stmt = null;
        ResultSet rs = null;
        SettleoutData settleout = null;
        MemberData memberData = null;
        StringBuffer seqnoSql = new StringBuffer();
        seqnoSql.append("SELECT ACCOUNT_CODE, (CASE WHEN CREDIT_UNIT = 'I' THEN DEBIT_ID ELSE CREDIT_ID END) AS MEM_ID, ");
        seqnoSql.append("SUM(FEE_AMT*(CASE WHEN CREDIT_UNIT = 'I' THEN 1 ELSE -1 END)) AS FSUM, SUM(NUM_OF_TXN) AS NUM ");
        seqnoSql.append("FROM TB_FEE_RESULT ");
        seqnoSql.append("WHERE PROC_DATE =  ").append(StringUtil.toSqlValueWithSQuote(batchDate));
        seqnoSql.append(" AND ACCOUNT_CODE = '" ).append(codeDef.get("CODE_ID") ).append("' ");
        seqnoSql.append("GROUP BY ACCOUNT_CODE, (CASE WHEN CREDIT_UNIT = 'I' THEN DEBIT_ID ELSE CREDIT_ID END) ");
        seqnoSql.append("ORDER BY ACCOUNT_CODE, (CASE WHEN CREDIT_UNIT = 'I' THEN DEBIT_ID ELSE CREDIT_ID END)");
        
        log.debug("procFeeResultSql: "+seqnoSql.toString());
        
        try {
        	stmt = conn.createStatement();

        	rs = stmt.executeQuery(seqnoSql.toString());
        	while (rs.next()) {
        		settleout = new SettleoutData();
        		
        		settleout.setAccountCode(rs.getString(1));
        		settleout.setAcqMemId(rs.getString(2));
        		settleout.setProcDate(batchDate);
        		settleout.setAmt(rs.getString(3));
        		settleout.setNum(rs.getString(4));
        		settleout.setCardCatId("99999");
        		settleout.setProductTypeId("999");
        		settleout.setExpPayDate("00000000");
        		settleout.setTermSettleDate("00000000");
        		
        		memberData = (MemberData) memberDatalist.get(settleout.getAcqMemId());
        		
        		String amtNotax = settleout.getAmt();
        		if ( memberData.getTaxContainFlag() != null ){
	        		if (memberData.getTaxContainFlag().equals("Y")){
	        			//四捨五入到小數點第二位
	        			amtNotax = String.valueOf(new BigDecimal(BatchUtils.div(
	        					Double.parseDouble(settleout.getAmt()), 1.05)).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
	        		}
        		}
        		settleout.setAmtNotax(amtNotax);
        		
        		String txnFee = String.valueOf(new BigDecimal(BatchUtils.mul(
	        					Double.parseDouble(settleout.getAmtNotax()), 0.05)).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
        		settleout.setTaxFee(txnFee);
        		
        		settleout.setAmtWithtax(String.valueOf(BatchUtils.add(
        				Double.parseDouble(settleout.getAmtNotax()), Double.parseDouble(settleout.getTaxFee()))));
        		
        		
        		String Agency = StringUtil.isEmpty(memberData.getAgency()) ? "Z" : memberData.getAgency();
        		settleout.setAgency(Agency);
        		settleout.setIndustryId(memberData.getIndustryId());
        		
        		settleoutlist.add(settleout);
        	}
        }
        catch (SQLException e) {
            log.warn("Settleout action warn:" + e.getMessage());
            throw e;
        }
        finally {
              ReleaseResource.releaseDB(null, stmt, rs);
        }
        
        for (int i = 0; i < settleoutlist.size(); i++){
        	executeUpdate(conn, getInsertHeaderSQL( ), getparameterValues(settleoutlist.get(i),batchDate));
        	
        	if ( i%100 ==0 )
        		conn.commit();
        }
        conn.commit();		
	}
	
	private void procBackCardRefund( HashMap codeDef, String batchDate) throws Exception{
		List<SettleoutData> settleoutlist = new ArrayList();
		
		Statement stmt = null;
        ResultSet rs = null;
        SettleoutData settleout = null;
        MemberData memberData = null;
        StringBuffer seqnoSql = new StringBuffer();
        
        seqnoSql.append(" SELECT TR.ACCOUNT_CODE, TR.TERM_SETTLE_DATE, TR.PROC_DATE, ACQ_MEM_ID ,TB_CARD.CARD_PRODUCT,");
        seqnoSql.append(" SUM((TR.SETTLE_AMT - PENALTY_AMT)*(CASE WHEN SIGN = 'P' THEN 1 ELSE -1 END)) AS SETTLE_AMT, TR.EXP_PAY_DATE");
        seqnoSql.append(" FROM (");
        seqnoSql.append(" SELECT TB_SETTLE_RESULT.CARD_NO, EXPIRY_DATE, EXP_PAY_DATE,");
        seqnoSql.append(" (CASE WHEN CREDIT_UNIT= 'U' THEN CREDIT_ID WHEN DEBIT_UNIT = 'U' THEN DEBIT_ID ELSE ACQ_MEM_ID END) AS ACQ_MEM_ID,");
        seqnoSql.append(" TXN_CODE, ACCOUNT_CODE, TERM_SETTLE_DATE, PROC_DATE, SETTLE_AMT, NVL(PENALTY_AMT, 0) AS PENALTY_AMT");
        seqnoSql.append(" FROM TB_SETTLE_RESULT, TB_TRAFFIC_TXN");
        seqnoSql.append(" WHERE PROC_DATE = ").append(StringUtil.toSqlValueWithSQuote(batchDate));
        seqnoSql.append(" AND TB_SETTLE_RESULT.CARD_NO = TB_TRAFFIC_TXN.CARD_NO(+)");
        seqnoSql.append(" AND TB_SETTLE_RESULT.LMS_INVOICE_NO = TB_TRAFFIC_TXN.LMS_INVOICE_NO(+)");
        seqnoSql.append(" ) TR, TB_CARD, TB_TXN_DEF");
        seqnoSql.append(" WHERE TR.CARD_NO=TB_CARD.CARD_NO"); 
        seqnoSql.append(" AND TR.EXPIRY_DATE=TB_CARD.EXPIRY_DATE");
        seqnoSql.append(" AND ACCOUNT_CODE = 'S206'");
        seqnoSql.append(" AND TR.TXN_CODE=TB_TXN_DEF.TXN_CODE");
        seqnoSql.append(" GROUP BY TR.ACCOUNT_CODE, TR.TERM_SETTLE_DATE,TR.PROC_DATE, ACQ_MEM_ID, TB_CARD.CARD_PRODUCT, TR.EXP_PAY_DATE"); 
        seqnoSql.append(" ORDER BY TR.ACCOUNT_CODE, TR.TERM_SETTLE_DATE,TR.PROC_DATE, ACQ_MEM_ID, TB_CARD.CARD_PRODUCT, TR.EXP_PAY_DATE");
        
        log.debug("procSettleResultSql: "+seqnoSql.toString());
        
        try {
        	stmt = conn.createStatement();

        	rs = stmt.executeQuery(seqnoSql.toString());
        	while (rs.next()) {
        		settleout = new SettleoutData();
        		settleout.setMapInfo(rs);

        		memberData = (MemberData) memberDatalist.get(settleout.getAcqMemId());
        		String Agency = StringUtil.isEmpty(memberData.getAgency()) ? "Z" : memberData.getAgency();
        		
        		settleout.setAgency(Agency);
        		settleout.setIndustryId(memberData.getIndustryId());
        		settleout.setCardCatId(rs.getString(5).substring(0,2)+"000");
        		settleout.setProductTypeId(rs.getString(5).substring(2,5) );
        		settleout.setExpPayDate(rs.getString(7));
        		settleoutlist.add(settleout);
        	}
        }
        catch (SQLException e) {
            log.warn("Settleout action warn:" + e.getMessage());
            throw e;
        }
        finally {
              ReleaseResource.releaseDB(null, stmt, rs);
        }
        
        if ( settleoutlist.size() != 0 ){
	        for (int i = 0; i < settleoutlist.size(); i++){
	        	executeUpdate(conn, getInsertHeaderSQL( ), getparameterValues(settleoutlist.get(i),batchDate));
	        	
	        	if ( i%100 ==0 )
	        		conn.commit();
	        }
	        conn.commit();
        }
	}
	
	private void procBackCardFine( HashMap codeDef, String batchDate) throws Exception{
		List<SettleoutData> settleoutlist = new ArrayList();
		
		Statement stmt = null;
        ResultSet rs = null;
        SettleoutData settleout = null;
        MemberData memberData = null;
        StringBuffer seqnoSql = new StringBuffer();
        
        seqnoSql.append(" SELECT TR.ACCOUNT_CODE, TR.TERM_SETTLE_DATE, TR.PROC_DATE, ACQ_MEM_ID ,TB_CARD.CARD_PRODUCT,");
        seqnoSql.append(" SUM(PENALTY_AMT*(CASE WHEN SIGN = 'P' THEN 1 ELSE -1 END)) AS PENALTY_AMT, TR.EXP_PAY_DATE");
        seqnoSql.append(" FROM (");
        seqnoSql.append(" SELECT TB_SETTLE_RESULT.CARD_NO, EXPIRY_DATE, EXP_PAY_DATE,");
        seqnoSql.append(" (CASE WHEN CREDIT_UNIT= 'U' THEN CREDIT_ID WHEN DEBIT_UNIT = 'U' THEN DEBIT_ID ELSE ACQ_MEM_ID END) AS ACQ_MEM_ID,");
        seqnoSql.append(" TXN_CODE, ACCOUNT_CODE, TERM_SETTLE_DATE, PROC_DATE, PENALTY_AMT");
        seqnoSql.append(" FROM TB_SETTLE_RESULT, TB_TRAFFIC_TXN");
        seqnoSql.append(" WHERE PROC_DATE = ").append(StringUtil.toSqlValueWithSQuote(batchDate));
        seqnoSql.append(" AND TB_SETTLE_RESULT.CARD_NO = TB_TRAFFIC_TXN.CARD_NO");
        seqnoSql.append(" AND TB_SETTLE_RESULT.LMS_INVOICE_NO = TB_TRAFFIC_TXN.LMS_INVOICE_NO");
        seqnoSql.append(" ) TR, TB_CARD, TB_TXN_DEF");
        seqnoSql.append(" WHERE TR.CARD_NO=TB_CARD.CARD_NO"); 
        seqnoSql.append(" AND TR.EXPIRY_DATE=TB_CARD.EXPIRY_DATE");
        seqnoSql.append(" AND ACCOUNT_CODE = 'S206'");
        seqnoSql.append(" AND TR.TXN_CODE=TB_TXN_DEF.TXN_CODE");
        seqnoSql.append(" GROUP BY TR.ACCOUNT_CODE, TR.TERM_SETTLE_DATE,TR.PROC_DATE, ACQ_MEM_ID, TB_CARD.CARD_PRODUCT, TR.EXP_PAY_DATE"); 
        seqnoSql.append(" ORDER BY TR.ACCOUNT_CODE, TR.TERM_SETTLE_DATE,TR.PROC_DATE, ACQ_MEM_ID, TB_CARD.CARD_PRODUCT, TR.EXP_PAY_DATE");
        
        log.debug("procSettleResultSql: "+seqnoSql.toString());
        
        try {
        	stmt = conn.createStatement();

        	rs = stmt.executeQuery(seqnoSql.toString());
        	while (rs.next()) {
        		settleout = new SettleoutData();
        		settleout.setMapInfo(rs);

        		memberData = (MemberData) memberDatalist.get(settleout.getAcqMemId());
        		String Agency = StringUtil.isEmpty(memberData.getAgency()) ? "Z" : memberData.getAgency();
        		
        		settleout.setAccountCode("S207");		
        		settleout.setTermSettleDate(rs.getString(2));
        		settleout.setProcDate(rs.getString(3));
        		settleout.setAcqMemId(rs.getString(4));
        		settleout.setAmt(rs.getString(6).toString());
        		settleout.setAgency(Agency);
        		settleout.setIndustryId(memberData.getIndustryId());
        		settleout.setCardCatId(rs.getString(5).substring(0,2)+"000");
        		settleout.setProductTypeId(rs.getString(5).substring(2,5) );
        		settleout.setExpPayDate(rs.getString(7));
        		settleoutlist.add(settleout);
        	}
        }
        catch (SQLException e) {
            log.warn("Settleout action warn:" + e.getMessage());
            throw e;
        }
        finally {
              ReleaseResource.releaseDB(null, stmt, rs);
        }
        
        if ( settleoutlist.size() != 0 ){
	        for (int i = 0; i < settleoutlist.size(); i++){
	        	executeUpdate(conn, getInsertHeaderSQL( ), getparameterValues(settleoutlist.get(i),batchDate));
	        	
	        	if ( i%100 ==0 )
	        		conn.commit();
	        }
	        conn.commit();
        }
	}
	
	private void procNotReminder( HashMap codeDef, String batchDate) throws Exception{
		List<SettleoutData> settleoutlist = new ArrayList();
		
		Statement stmt = null;
        ResultSet rs = null;
        SettleoutData settleout = null;
        MemberData memberData = null;
        StringBuffer seqnoSql = new StringBuffer();
        
        seqnoSql.append(" SELECT ACQ_MEM_ID, SUM(TXN_AMT)*-1, TB_CARD.CARD_PRODUCT");
        seqnoSql.append(" FROM TB_AUTO_RELOAD_RESP, TB_CARD");
        seqnoSql.append(" WHERE TB_AUTO_RELOAD_RESP.APRV_DATE = ").append(StringUtil.toSqlValueWithSQuote(BatchUtil.getSomeDay(batchDate,-1)));
        seqnoSql.append(" AND TB_AUTO_RELOAD_RESP.STATUS = '9' ");
        seqnoSql.append(" AND P_CODE IN ('5717','5737')");
        seqnoSql.append(" AND TB_CARD.CARD_NO = TB_AUTO_RELOAD_RESP.CARD_NO");
        seqnoSql.append(" AND TB_CARD.EXPIRY_DATE = TB_AUTO_RELOAD_RESP.EXPIRY_DATE");
        seqnoSql.append(" GROUP BY ACQ_MEM_ID, TB_CARD.CARD_PRODUCT");
        
        log.debug("procSettleResultSql: "+seqnoSql.toString());
        
        try {
        	stmt = conn.createStatement();

        	rs = stmt.executeQuery(seqnoSql.toString());
        	while (rs.next()) {
        		
        		settleout = new SettleoutData();

        		settleout.setAccountCode("S113");		
        		settleout.setTermSettleDate("00000000");
        		settleout.setProcDate(batchDate);
        		settleout.setAcqMemId(rs.getString(1));
        		settleout.setAmt(rs.getString(2).toString());
        		
        		memberData = (MemberData) memberDatalist.get(settleout.getAcqMemId());
        		String Agency = StringUtil.isEmpty(memberData.getAgency()) ? "Z" : memberData.getAgency();
        		
        		settleout.setAgency(Agency);
        		settleout.setIndustryId(memberData.getIndustryId());
        		settleout.setCardCatId(rs.getString(3).substring(0,2)+"000");
        		settleout.setProductTypeId(rs.getString(3).substring(2,5) );
        		settleout.setExpPayDate("00000000");
        		settleoutlist.add(settleout);
        	}
        }
        catch (SQLException e) {
            log.warn("Settleout action warn:" + e.getMessage());
            throw e;
        }
        finally {
              ReleaseResource.releaseDB(null, stmt, rs);
        }
        
        if ( settleoutlist.size() != 0 ){
	        for (int i = 0; i < settleoutlist.size(); i++){
	        	executeUpdate(conn, getInsertHeaderSQL( ), getparameterValues(settleoutlist.get(i),batchDate));
	        	
	        	if ( i%100 ==0 )
	        		conn.commit();
	        }
	        conn.commit();
        }
	}
	
	private void procTrafficPenalty(HashMap codeDefMonthly, String batchDate, String memId) throws Exception{
		List<SettleoutData> settleoutlist = new ArrayList();
		
		Statement stmt = null;
        ResultSet rs = null;
        SettleoutData settleout = null;
        MemberData memberData = null;
        StringBuffer seqnoSql = new StringBuffer();
        seqnoSql.append(" SELECT ACQ_MEM_ID, SUM(PENALTY_AMT) AS PENALTY_AMT, COUNT(1) AS NUM");
        seqnoSql.append(" FROM TB_TRAFFIC_TXN, TB_SETTLE_RESULT");
        seqnoSql.append(" WHERE PROC_DATE BETWEEN ").append(StringUtil.toSqlValueWithSQuote(procPeriod.getStartDate()));
        seqnoSql.append(" AND " +StringUtil.toSqlValueWithSQuote(procPeriod.getEndDate()));
        seqnoSql.append(" AND ACQ_MEM_ID = ").append(StringUtil.toSqlValueWithSQuote(memId));
        //S404 需撈取有設定S206之帳務交易
        seqnoSql.append(" AND ACCOUNT_CODE = 'S206'" );
        seqnoSql.append(" AND TB_SETTLE_RESULT.CARD_NO = TB_TRAFFIC_TXN.CARD_NO");
        seqnoSql.append(" AND TB_SETTLE_RESULT.LMS_INVOICE_NO = TB_TRAFFIC_TXN.LMS_INVOICE_NO");
        seqnoSql.append(" GROUP BY ACQ_MEM_ID");
        
        log.debug("procTrafficPenalty: "+seqnoSql.toString());
        
        try {
        	stmt = conn.createStatement();

        	rs = stmt.executeQuery(seqnoSql.toString());
        	while (rs.next()) {
        		settleout = new SettleoutData();
        		
        		settleout.setAccountCode("S404");
        		settleout.setAcqMemId(rs.getString(1));
        		settleout.setProcDate(batchDate);
        		settleout.setNum(rs.getString(3));
        		settleout.setCardCatId("99999");
        		settleout.setProductTypeId("999");
        		settleout.setExpPayDate("00000000");
        		settleout.setTermSettleDate("00000000");
        		
        		memberData = (MemberData) memberDatalist.get(settleout.getAcqMemId());
        		
        		//#493  未稅金額為清算金額    稅額一律為0000000000000
        		String amtNotax = String.valueOf(new BigDecimal(BatchUtils.div(
    					Double.parseDouble(rs.getString(2)), 1.05)).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
        		settleout.setAmt(amtNotax);
        		
        		String txnFee = String.valueOf(new BigDecimal(BatchUtils.mul(
	        					Double.parseDouble(settleout.getAmtNotax()), 0.05)).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
        		settleout.setTaxFee(txnFee);
        		
        		settleout.setAmtWithtax(String.valueOf(BatchUtils.add(
        				Double.parseDouble(settleout.getAmtNotax()), Double.parseDouble(settleout.getTaxFee()))));
        		
        		
        		String Agency = StringUtil.isEmpty(memberData.getAgency()) ? "Z" : memberData.getAgency();
        		settleout.setAgency(Agency);
        		settleout.setIndustryId(memberData.getIndustryId());
        		
        		settleoutlist.add(settleout);
        	}
        }
        catch (SQLException e) {
            log.warn("Settleout action warn:" + e.getMessage());
            throw e;
        }
        finally {
              ReleaseResource.releaseDB(null, stmt, rs);
        }
        
        for (int i = 0; i < settleoutlist.size(); i++){
        	executeUpdate(conn, getInsertHeaderSQL( ), getparameterValues(settleoutlist.get(i),batchDate));
        	
        	if ( i%100 ==0 )
        		conn.commit();
        }
        conn.commit();		
	}
	
	private void procCommission(HashMap codeDefMonthly, String batchDate, String memId) throws Exception{
		List<SettleoutData> settleoutlist = new ArrayList();
		
		Statement stmt = null;
        ResultSet rs = null;
        SettleoutData settleout = null;
        MemberData memberData = null;
        StringBuffer seqnoSql = new StringBuffer();
        seqnoSql.append("SELECT ACCOUNT_CODE,SUM(FEE_AMT*(CASE WHEN DEBIT_ID = '").append(memId).append("' THEN 1 ELSE -1 END)) AS FSUM, ");
        seqnoSql.append("SUM(NUM_OF_TXN) AS NUM ");
        seqnoSql.append("FROM TB_FEE_RESULT ");
        seqnoSql.append("WHERE PROC_DATE BETWEEN ").append(StringUtil.toSqlValueWithSQuote(procPeriod.getStartDate()));
        seqnoSql.append(" AND ").append("" ).append(StringUtil.toSqlValueWithSQuote(procPeriod.getEndDate()));
        seqnoSql.append(" AND ACCOUNT_CODE = '" ).append(codeDefMonthly.get("CODE_ID") ).append("' ");
        seqnoSql.append("AND (CREDIT_ID = ").append( StringUtil.toSqlValueWithSQuote(memId) );
        seqnoSql.append(" OR DEBIT_ID = ").append(StringUtil.toSqlValueWithSQuote(memId) ).append(")");
        seqnoSql.append("GROUP BY ACCOUNT_CODE ");
        seqnoSql.append("ORDER BY ACCOUNT_CODE");
        
        log.debug("procFeeResultSql: "+seqnoSql.toString());
        
        try {
        	stmt = conn.createStatement();

        	rs = stmt.executeQuery(seqnoSql.toString());
        	while (rs.next()) {
        		settleout = new SettleoutData();
        		
        		settleout.setAccountCode(rs.getString(1));
        		settleout.setProcDate(batchDate);
        		settleout.setAmt(rs.getString(2));
        		settleout.setNum(rs.getString(3));
        		settleout.setAcqMemId(memId);
        		settleout.setCardCatId("99999");
        		settleout.setProductTypeId("999");
        		settleout.setExpPayDate("00000000");
        		settleout.setTermSettleDate("00000000");
        		
        		memberData = (MemberData) memberDatalist.get(settleout.getAcqMemId());
        		
        		//#493  未稅金額為清算金額    稅額一律為0000000000000
        		String amtNotax = String.valueOf(new BigDecimal(BatchUtils.div(
    					Double.parseDouble(rs.getString(2)), 1.05)).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
        		
        		settleout.setAmt(amtNotax);
        		
        		String txnFee = String.valueOf(new BigDecimal(BatchUtils.mul(
	        					Double.parseDouble(settleout.getAmtNotax()), 0.05)).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
        		settleout.setTaxFee(txnFee);
        		
        		settleout.setAmtWithtax(String.valueOf(BatchUtils.add(
        				Double.parseDouble(settleout.getAmtNotax()), Double.parseDouble(settleout.getTaxFee()))));
        		
        		
        		String Agency = StringUtil.isEmpty(memberData.getAgency()) ? "Z" : memberData.getAgency();
        		settleout.setAgency(Agency);
        		settleout.setIndustryId(memberData.getIndustryId());
        		
        		settleoutlist.add(settleout);
        	}
        }
        catch (SQLException e) {
            log.warn("Settleout action warn:" + e.getMessage());
            throw e;
        }
        finally {
              ReleaseResource.releaseDB(null, stmt, rs);
        }
        
        for (int i = 0; i < settleoutlist.size(); i++){
        	executeUpdate(conn, getInsertHeaderSQL( ), getparameterValues(settleoutlist.get(i),batchDate));
        	
        	if ( i%100 ==0 )
        		conn.commit();
        }
        conn.commit();		
	}
	
	private void procFeeResult(HashMap codeDefMonthly, String batchDate, String memId) throws Exception{
		List<SettleoutData> settleoutlist = new ArrayList();
		
		Statement stmt = null;
        ResultSet rs = null;
        SettleoutData settleout = null;
        MemberData memberData = null;
        StringBuffer seqnoSql = new StringBuffer();
        seqnoSql.append("SELECT ACCOUNT_CODE,SUM(FEE_AMT*(CASE WHEN DEBIT_ID = '").append(memId).append("' THEN 1 ELSE -1 END)) AS FSUM, ");
        seqnoSql.append("SUM(NUM_OF_TXN) AS NUM ");
        seqnoSql.append("FROM TB_FEE_RESULT ");
        seqnoSql.append("WHERE PROC_DATE BETWEEN ").append(StringUtil.toSqlValueWithSQuote(procPeriod.getStartDate()));
        seqnoSql.append(" AND ").append("" ).append(StringUtil.toSqlValueWithSQuote(procPeriod.getEndDate()));
        seqnoSql.append(" AND ACCOUNT_CODE = '" ).append(codeDefMonthly.get("CODE_ID") ).append("' ");
        seqnoSql.append("AND (CREDIT_ID = ").append( StringUtil.toSqlValueWithSQuote(memId) );
        seqnoSql.append(" OR DEBIT_ID = ").append(StringUtil.toSqlValueWithSQuote(memId) ).append(")");
        seqnoSql.append("GROUP BY ACCOUNT_CODE ");
        seqnoSql.append("ORDER BY ACCOUNT_CODE");
        
        log.debug("procFeeResultSql: "+seqnoSql.toString());
        
        try {
        	stmt = conn.createStatement();

        	rs = stmt.executeQuery(seqnoSql.toString());
        	while (rs.next()) {
        		settleout = new SettleoutData();
        		
        		settleout.setAccountCode(rs.getString(1));
        		settleout.setProcDate(batchDate);
        		settleout.setAmt(rs.getString(2));
        		settleout.setNum(rs.getString(3));
        		settleout.setAcqMemId(memId);
        		settleout.setCardCatId("99999");
        		settleout.setProductTypeId("999");
        		settleout.setExpPayDate("00000000");
        		settleout.setTermSettleDate("00000000");
        		
        		memberData = (MemberData) memberDatalist.get(settleout.getAcqMemId());
        		
        		String amtNotax = settleout.getAmt();
        		if ( memberData.getTaxContainFlag() != null ){
	        		if (memberData.getTaxContainFlag().equals("Y")){
	        			//四捨五入到小數點第二位
	        			amtNotax = String.valueOf(new BigDecimal(BatchUtils.div(
	        					Double.parseDouble(settleout.getAmt()), 1.05)).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
	        		}
        		}
        		settleout.setAmtNotax(amtNotax);
        		//20171113需求500 稅額改為0
        		//String txnFee = String.valueOf(new BigDecimal(BatchUtils.mul(
	        					//Double.parseDouble(settleout.getAmtNotax()), 0.05)).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
        		settleout.setTaxFee("0");
        		
        		settleout.setAmtWithtax(String.valueOf(BatchUtils.add(
        				Double.parseDouble(settleout.getAmtNotax()), Double.parseDouble(settleout.getTaxFee()))));
        		
        		
        		String Agency = StringUtil.isEmpty(memberData.getAgency()) ? "Z" : memberData.getAgency();
        		settleout.setAgency(Agency);
        		settleout.setIndustryId(memberData.getIndustryId());
        		
        		settleoutlist.add(settleout);
        	}
        }
        catch (SQLException e) {
            log.warn("Settleout action warn:" + e.getMessage());
            throw e;
        }
        finally {
              ReleaseResource.releaseDB(null, stmt, rs);
        }
        
        for (int i = 0; i < settleoutlist.size(); i++){
        	executeUpdate(conn, getInsertHeaderSQL( ), getparameterValues(settleoutlist.get(i),batchDate));
        	
        	if ( i%100 ==0 )
        		conn.commit();
        }
        conn.commit();		
	}
	
	private void procRegCard(HashMap codeDefMonthly, String batchDate, String memId,String regfees) throws Exception{
		List<SettleoutData> settleoutlist = new ArrayList();
		
		Statement stmt = null;
        ResultSet rs = null;
        SettleoutData settleout = null;
        MemberData memberData = null;
        StringBuffer seqnoSql = new StringBuffer();
        seqnoSql.append("SELECT MBR_REG_DATE,CARD_PRODUCT,COUNT(*) CNT ");
        seqnoSql.append("FROM TB_CARD c, TB_MERCH m ");
        seqnoSql.append("WHERE c.MBR_REG_DATE BETWEEN ").append(StringUtil.toSqlValueWithSQuote(procPeriod.getStartDate()));
        seqnoSql.append(" AND " +StringUtil.toSqlValueWithSQuote(procPeriod.getEndDate()));
        seqnoSql.append(" AND c.CARD_OPEN_OWNER IS NOT NULL");
        seqnoSql.append(" AND c.CARD_OPEN_OWNER = m.MERCH_ID");
        seqnoSql.append(" AND m.MEM_ID = ").append(StringUtil.toSqlValueWithSQuote(memId));
        seqnoSql.append(" GROUP BY MBR_REG_DATE,CARD_PRODUCT ORDER BY MBR_REG_DATE,CARD_PRODUCT");

        log.debug("procRegCardSql: "+seqnoSql.toString());
        
        try {
        	stmt = conn.createStatement();

        	rs = stmt.executeQuery(seqnoSql.toString());
        	while (rs.next()) {
        		settleout = new SettleoutData();
        		
        		settleout.setAccountCode((String) codeDefMonthly.get("CODE_ID"));
        		settleout.setAcqMemId(memId);
        		settleout.setTermSettleDate(rs.getString(1));
        		settleout.setCardCatId(rs.getString(2).substring(0,2)+"000");
        		settleout.setProductTypeId(rs.getString(2).substring(2,5) );
        		settleout.setProcDate(batchDate);
        		BigDecimal b1 = new BigDecimal(rs.getString(3));
        		BigDecimal b2 = new BigDecimal(regfees);
        		settleout.setAmt(b1.multiply(b2).toString());

        		memberData = (MemberData) memberDatalist.get(settleout.getAcqMemId());
        		String Agency = StringUtil.isEmpty(memberData.getAgency()) ? "Z" : memberData.getAgency();
        		
        		settleout.setAgency(Agency);
        		settleout.setIndustryId(memberData.getIndustryId());
        		
        		settleoutlist.add(settleout);
        	}
        }
        catch (SQLException e) {
            log.warn("Settleout action warn:" + e.getMessage());
            throw e;
        }
        finally {
              ReleaseResource.releaseDB(null, stmt, rs);
        }
        
        for (int i = 0; i < settleoutlist.size(); i++){
        	executeUpdate(conn, getInsertHeaderSQL( ), getparameterValues(settleoutlist.get(i),batchDate));
        	
        	if ( i%100 ==0 )
        		conn.commit();
        }
        conn.commit();		
	}
	
//	private void procCardFee(HashMap codeDefMonthly, String batchDate, String memId, String cardFee, String strTerm) throws Exception{
//		List<SettleoutData> settleoutlist = new ArrayList();
//		
//		Statement stmt = null;
//        ResultSet rs = null;
//        SettleoutData settleout = null;
//        MemberData memberData = null;
//        StringBuffer seqnoSql = new StringBuffer();
//        seqnoSql.append("SELECT ISSUE_DATE, CARD_PRODUCT, COUNT(*) CNT ");
//        seqnoSql.append("FROM TB_CARD ");
//        seqnoSql.append("WHERE ISSUE_DATE BETWEEN "+StringUtil.toSqlValueWithSQuote(procPeriod.getStartDate()));
//        seqnoSql.append(" AND " +StringUtil.toSqlValueWithSQuote(procPeriod.getEndDate()));
//        seqnoSql.append(" AND MEM_ID = "+StringUtil.toSqlValueWithSQuote(memId));
//        seqnoSql.append(strTerm);
//        seqnoSql.append(" GROUP BY ISSUE_DATE, CARD_PRODUCT");
//        seqnoSql.append(" ORDER BY ISSUE_DATE, CARD_PRODUCT");
//
//        log.debug("procRegCardSql: "+seqnoSql.toString());
//        
//        try {
//        	stmt = conn.createStatement();
//
//        	rs = stmt.executeQuery(seqnoSql.toString());
//        	while (rs.next()) {
//        		settleout = new SettleoutData();
//        		
//        		settleout.setAccountCode((String) codeDefMonthly.get("CODE_ID"));
//        		settleout.setAcqMemId(memId);
//        		settleout.setTermSettleDate(rs.getString(1));
//        		settleout.setCardCatId(rs.getString(2).substring(0,2)+"000");
//        		settleout.setProductTypeId(rs.getString(2).substring(2,5) );
//        		settleout.setNum(rs.getString(3));
//        		settleout.setProcDate(batchDate);
//        		BigDecimal b1 = new BigDecimal(rs.getString(3));
//        		BigDecimal b2 = new BigDecimal(cardFee);
//        		settleout.setAmt(b1.multiply(b2).toString());
//
//        		memberData = (MemberData) memberDatalist.get(settleout.getAcqMemId());
//        		String Agency = StringUtil.isEmpty(memberData.getAgency()) ? "Z" : memberData.getAgency();
//        		
//        		settleout.setAgency(Agency);
//        		settleout.setIndustryId(memberData.getIndustryId());
//        		
//        		settleoutlist.add(settleout);
//        	}
//        }
//        catch (SQLException e) {
//            log.warn("Settleout action warn:" + e.getMessage());
//            throw e;
//        }
//        finally {
//              ReleaseResource.releaseDB(null, stmt, rs);
//        }
//        
//        for (int i = 0; i < settleoutlist.size(); i++){
//        	executeUpdate(conn, getInsertHeaderSQL( ), getparameterValues(settleoutlist.get(i),batchDate));
//        	
//        	if ( i%100 ==0 )
//        		conn.commit();
//        }
//        conn.commit();		
//	}
	
	private void procExpPrepaidBal(HashMap codeDefMonthly, String batchDate, String memId) throws Exception{
		List<SettleoutData> settleoutlist = new ArrayList();
		
		Statement stmt = null;
        ResultSet rs = null;
        SettleoutData settleout = null;
        MemberData memberData = null;
        StringBuffer seqnoSql = new StringBuffer();
        seqnoSql.append("SELECT PROC_MON, ACQ_MEM_ID, PREPAID_BAL ");
        seqnoSql.append("FROM TB_PREPAID_BAL ");
        seqnoSql.append("WHERE PROC_MON =  ").append(StringUtil.toSqlValueWithSQuote(DateUtil.addMonth(batchDate, -1).substring(0, 6)));
        seqnoSql.append(" AND ACQ_MEM_ID = ").append(StringUtil.toSqlValueWithSQuote(memId));
        seqnoSql.append(" AND PREPAID_BAL<0 ");
        
        log.debug("procFeeResultSql: "+seqnoSql.toString());
        
        try {
        	stmt = conn.createStatement();

        	rs = stmt.executeQuery(seqnoSql.toString());
        	while (rs.next()) {
        		settleout = new SettleoutData();
        		
        		settleout.setAccountCode((String) codeDefMonthly.get("CODE_ID"));
        		settleout.setAcqMemId(memId);
        		settleout.setTermSettleDate(rs.getString(1)+"00");
        		settleout.setCardCatId("99999");
        		settleout.setProductTypeId("999");
        		settleout.setProcDate(batchDate);
        		settleout.setAmt(rs.getString(3));

        		memberData = (MemberData) memberDatalist.get(settleout.getAcqMemId());
        		String Agency = StringUtil.isEmpty(memberData.getAgency()) ? "Z" : memberData.getAgency();
        		
        		settleout.setAgency(Agency);
        		settleout.setIndustryId(memberData.getIndustryId());
        		
        		settleoutlist.add(settleout);
        	}
        }
        catch (SQLException e) {
            log.warn("Settleout action warn:" + e.getMessage());
            throw e;
        }
        finally {
        	ReleaseResource.releaseDB(null, stmt, rs);
        }
        
        for (int i = 0; i < settleoutlist.size(); i++){
        	executeUpdate(conn, getInsertHeaderSQL( ), getparameterValues(settleoutlist.get(i),batchDate));
        	
        	if ( i%100 ==0 )
        		conn.commit();
        }
        conn.commit();		
	}
	
	private Object[] getparameterValues(SettleoutData settleoutData, String batchDate) {
		Object[] parameterValues = new Object[16];
		
		System.arraycopy(new Object[] { 
				settleoutData.getAccountCode(),
				settleoutData.getTermSettleDate(),
				settleoutData.getProcDate(),
				settleoutData.getAgency(),
				settleoutData.getAcqMemId(),
				settleoutData.getCardCatId(),
				settleoutData.getProductTypeId(),
				settleoutData.getIndustryId(),
				settleoutData.getAmt(),
				settleoutData.getNum(),
				batchDate+StringUtils.leftPad(Integer.toString(expSeqNo++),7,'0'),
				settleoutData.getCardNo(),
				settleoutData.getExpPayDate(),
				settleoutData.getAmtNotax(),
				settleoutData.getTaxFee(),
				settleoutData.getAmtWithtax()
				
		}, 0, parameterValues, 0, 16);
		return parameterValues;
	}

	private String getInsertHeaderSQL() {
		
		String insertSQL = 
				"INSERT INTO TB_SETTLEOUT (ACCOUT_CODE,TERM_SETTLE_DATE,PROC_DATE," +
				"AGENCY,ACQ_MEM_ID,CARD_CAT_ID,PRODUCT_TYPE_ID,INDUSTRY_ID,AMT,NUM,EXP_SEQ_NO," +
				"CARD_NO,EXP_PAY_DATE,AMT_NOTAX,TAX_FEE,AMT_WITHTAX) " +
				"VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		return insertSQL;
	}
	
	public void setProcPeriod(DateRange procPeriod)
    {
        if ( StringUtil.isEmpty(procPeriod.getEndDate())||procPeriod.getEndDate()==null) 
        { //如果是ProcCycle是 Dxx, endDate會是null -> 令 endDate = startDate
            procPeriod.setEndDate(procPeriod.getStartDate());
        }
        this.procPeriod = procPeriod;
    }
	
	//如果失敗則刪掉當日所有TB_SETTLEOUT
	public void recoverData(String batchDate ) throws SQLException {
		log.warn("recoverData TB_SETTLEOUT WHERE PROC_DATE = " + batchDate);
		Connection conn = null;
		try{
			conn = DBService.getDBService().getConnection(Layer1Constants.DSNAME_BATCH);
	        String sql = "DELETE FROM TB_SETTLEOUT WHERE PROC_DATE = " + StringUtil.toSqlValueWithSQuote(batchDate);
	        DBService.getDBService().sqlAction(sql, conn, false);
		}
		 catch (SQLException e) {
			 log.warn("recoverData action warn:" + e.getMessage());
			 throw e;
		 }
		finally {
			ReleaseResource.releaseDB(conn);
		}
	}
	
	private static DateRange handleMXXCycle(String baseDate, String cycle)
    {
        DateRange ret = null;
        int cday = Integer.parseInt(cycle.substring(1));
        int bday = DateUtil.getDayOfMonth(baseDate);
        if (cday==99)
        {
            ret = new DateRange();
            //如果是ProcCycle是 Dxx, endDate會是null -> 令 endDate = startDate
            ret.setStartDate(baseDate.substring(0,6)+"01");
            ret.setEndDate(baseDate.substring(0,6)+String.valueOf(DateUtil.getLastDayOfMonth(baseDate)));
        }
        else if (bday == cday)
        {
            ret = new DateRange();
            // 上個月
            ret.setStartDate(DateUtil.addDate(DateUtil.addMonth(baseDate, -1), 1));
            ret.setEndDate(baseDate);
        }
        else
        {
            ret = null;
        }
        return ret;
    }
}
