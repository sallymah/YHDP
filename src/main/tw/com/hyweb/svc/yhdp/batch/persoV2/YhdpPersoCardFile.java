package tw.com.hyweb.svc.yhdp.batch.persoV2;

import static tw.com.hyweb.core.cp.batch.util.PreparedStatementUtils.executeQuerySingleValue;
import static tw.com.hyweb.core.cp.batch.util.PreparedStatementUtils.executeUpdate;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Vector;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import tw.com.hyweb.core.cp.batch.framework.Layer1Constants;
import tw.com.hyweb.core.cp.batch.util.BatchUtil;
import tw.com.hyweb.core.cp.batch.util.DateUtils;
import tw.com.hyweb.core.cp.batch.util.StringUtils;
import tw.com.hyweb.service.db.info.TbFileInfoInfo;
import tw.com.hyweb.service.db.info.TbFileInfoPK;
import tw.com.hyweb.service.db.info.TbHgCardMapInfo;
import tw.com.hyweb.service.db.info.TbMemberInfo;
import tw.com.hyweb.service.db.info.TbOutctlInfo;
import tw.com.hyweb.service.db.info.TbPersoFactoryInfo;
import tw.com.hyweb.service.db.info.TbPersoInfo;
import tw.com.hyweb.service.db.mgr.TbFileInfoMgr;
import tw.com.hyweb.service.db.mgr.TbMemberMgr;
import tw.com.hyweb.service.db.mgr.TbOutctlMgr;
import tw.com.hyweb.service.db.mgr.TbPersoFactoryMgr;
import tw.com.hyweb.svc.yhdp.batch.framework.perso.CgYhdpSsf;
import tw.com.hyweb.svc.yhdp.batch.framework.perso.HsmAdapterECB;
import tw.com.hyweb.util.DbUtil;
import tw.com.hyweb.util.ISOUtil;
import tw.com.hyweb.util.ReleaseResource;
import tw.com.hyweb.util.string.StringUtil;

public class YhdpPersoCardFile {
	private static final Logger logger = Logger.getLogger(YhdpPersoCardFile.class);
	
	private final int HSM_MAX_LENGTH = 65512;
	
	private final String PERSO_FACTORY_RREMOTE = "PERSO_FACTORY";
	protected final String MEMGROUPID_SPECIAL = "22222";
    protected final String MEMID_SPECIAL = "00000000";
    
	private final String PERSO_FILE_NAME = "PERSO2";
	private final String BANK_FILE_NAME = "PERSO_BANK";
	
    private TbFileInfoInfo fileInfo;
    //private TbCardProductInfo cardProductInfo;
    private String filePath;
    private String fullFileName;
    private String tempFile;
    private String file;
    private BufferedOutputStream bw = null;
//    private BufferedInputStream br = null;

    private final HsmAdapterECB hsmAdapterECB;
    private final CgYhdpSsf cgYhdpSsf;
    
    public YhdpPersoCardFile (HsmAdapterECB hsmAdapterECB, CgYhdpSsf cgYhdpSsf){
    	this.hsmAdapterECB = hsmAdapterECB;
    	this.cgYhdpSsf = cgYhdpSsf;
    }
	/**
     * 
     * 
     * @see tw.com.hyweb.core.cp.batch.framework.generic.CursorBatchJobFactory#init(java.sql.Connection,
     *      java.lang.String)
     */
    public void init(Connection connection, String batchDate,TbPersoInfo persoInfo) throws Exception
    {
    	// 20140609 cvvArray,pinArray清空，以免造成執行makePerso不會 new 新的陣列。
    	/*cvvArray = null;
        pinArray = null;*/
    	
    	TbPersoFactoryMgr mgr = new TbPersoFactoryMgr(connection);
    	TbPersoFactoryInfo persoFactoryInfo =  mgr.querySingle(persoInfo.getCardFactoryId());
    	
    	//PERSO、PERSO_BANK
    	fileInfo = getPersoFileInfo(connection, persoInfo);
    	
    	TbMemberInfo tbMemberInfo = null;
        String serialNumber = getSerialNumber(connection, batchDate, persoInfo.getMemId());
        String isamNumber = "01";
        String LocalPath = fileInfo.getLocalPath();
        
        //自家製卡
    	if (persoInfo.getCardFactoryId().equals(ProcessPersoJob.OWN_CARD_FACTORY)){
	    	Vector<TbMemberInfo> result = new Vector<TbMemberInfo>();        	
	    	String where = "BANK_ID = "+ StringUtil.toSqlValueWithSQuote(persoInfo.getBankId());
	    	
	    	new TbMemberMgr(connection).queryMultiple(where, result);
	    	
	    	if (result.size() != 1){
	    		throw new Exception("(PersoBatchNo: " +persoInfo.getPersoBatchNo() + ", BankId:" + persoInfo.getBankId() + ") member error");
	    	}
	    	tbMemberInfo = result.get(0);
	    	fullFileName = getBankPersoFileName(fileInfo, tbMemberInfo.getMemId(), persoInfo.getPersoBatchNo(), isamNumber);
	    	
	    	//只允許銀行卡且製卡廠為00可以設為 收單下資料夾
	    	if (fileInfo.getLocalPath().indexOf(MEMGROUPID_SPECIAL) != -1 || fileInfo.getLocalPath().indexOf(MEMID_SPECIAL) != -1){
	        	
		        if (fileInfo.getLocalPath().indexOf(MEMGROUPID_SPECIAL) != -1) {
		            // find "22222"
		        	LocalPath = LocalPath.replaceFirst(MEMGROUPID_SPECIAL, tbMemberInfo.getMemGroupId());
		        }
		        if (fileInfo.getLocalPath().indexOf(MEMID_SPECIAL) != -1) {
		            // find "00000000"
		        	LocalPath = LocalPath.replaceFirst(MEMID_SPECIAL, tbMemberInfo.getMemId());
		        }
	        }
    	}
    	else{
    		fullFileName = getPersoFileName(fileInfo, persoInfo, persoInfo.getPersoBatchNo(), isamNumber);
    	}
    	
        if (LocalPath.contains(PERSO_FACTORY_RREMOTE)){
        	LocalPath = LocalPath.replaceFirst(PERSO_FACTORY_RREMOTE, persoFactoryInfo.getRemoteFolder());
        }

        filePath = FilenameUtils.separatorsToSystem(BatchUtil.getWorkDirectory() + LocalPath + "/");
        //cardProductInfo =  new TbCardProductMgr(connection).querySingle(persoSetting.getCardProduct());
	        
        new File(filePath).mkdirs();
	
        new File(filePath + fullFileName).delete();
        
        this.file = filePath + fullFileName;
        
        this.tempFile = file + ".tmp";
        
        bw = new BufferedOutputStream(new FileOutputStream(new File(tempFile)));
        
        if (persoInfo.getCardFactoryId().equals(ProcessPersoJob.OWN_CARD_FACTORY)){
        	insertOutControl(connection, batchDate, tbMemberInfo.getMemId(), tbMemberInfo.getMemGroupId(), 
        			getSerialNumber(connection, batchDate, tbMemberInfo.getMemId()),persoInfo.getPersoQty().intValue());
        }
        else{
        	insertOutControl(connection, batchDate, persoInfo.getMemId(), null, 
        			getSerialNumber(connection, batchDate, persoInfo.getMemId()) ,persoInfo.getPersoQty().intValue());
        }
    }
    
    private void convDataInfo(String desc, String convertData, boolean plainCodeFlag, boolean numberFlag) 
    {
    	byte[] convert;
    	
    	if(numberFlag) {
    		String hexString = String2HexString(convertData);
    		convert = convertHexToString(hexString);
    	}else {
    		if(plainCodeFlag == true) {
        		convert = convertData.getBytes();
        	}else {
        		convert = convertHexToString(convertData);
        	}
    	}
    	
    	logger.info(desc + ": [" + convertData +"]");
    	logger.info("convert: [" + ISOUtil.hexString(convert) +"]");	
    }
    
    /**
     * @param connection
     * @param cardProduct
     * @param persoDetails
     * @param file
     * @throws Exception
     * @see tw.com.hyweb.core.cp.batch.perso.PersoFileFactory#makePersoHeader(java.sql.Connection,
     *      tw.com.hyweb.service.db.info.TbCardProductInfo, java.util.List,
     *      java.lang.String)
     */
    public void makePersoHeader(String batchDate, TbPersoInfo persoInfo, String seqNo) throws Exception
    {
    	bw.write("H0".getBytes());
    	if(logger.isDebugEnabled()) {
    		convDataInfo("檔頭標籤", "H0", true, false);
    	}
    	   	
    	bw.write(batchDate.getBytes());
    	if(logger.isDebugEnabled()) {
    		convDataInfo("處理日期", batchDate, true, false);
    	}
    	
       	bw.write(StringUtils.paddingLeftString(persoInfo.getPersoQty().toString(), '0', 8).getBytes());
       	if(logger.isDebugEnabled()) {
       		convDataInfo("資料筆數", StringUtils.paddingLeftString(persoInfo.getPersoQty().toString(), '0', 8), true, false);
       	}
       	
    	bw.write(persoInfo.getPersoBatchNo().getBytes());
    	if(logger.isDebugEnabled()) {
    		convDataInfo("申請編號", persoInfo.getPersoBatchNo(), true, false);
    	}
    	
    	bw.write(StringUtils.paddingLeftString(persoInfo.getLayoutId(), '0', 4).getBytes());
    	if(logger.isDebugEnabled()) {
    		convDataInfo("卡面設計編號", StringUtils.paddingLeftString(persoInfo.getLayoutId(), '0', 4), true, false);
    	}
    	
    	bw.write(StringUtils.paddingLeftString(persoInfo.getCardFactoryId(),'0', 2).getBytes());
    	if(logger.isDebugEnabled()) {
    		convDataInfo("發行批號(製卡廠商)", StringUtils.paddingLeftString(persoInfo.getCardFactoryId(),'0', 2), true, false);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString(persoInfo.getCardCatId(),'0',2)));
    	if(logger.isDebugEnabled()) {
    		convDataInfo("卡別", StringUtils.paddingLeftString(persoInfo.getCardCatId(),'0',2), false, false);
    	}
    	
    	bw.write(StringUtils.paddingLeftString(persoInfo.getPurchaseOrderNo(), ' ',10).getBytes());
    	if(logger.isDebugEnabled()) {
    		convDataInfo("採購單號", StringUtils.paddingLeftString(persoInfo.getPurchaseOrderNo(), ' ',10), true, false);
    	}
    	
    	bw.write(StringUtils.paddingRightString("", ' ', 9).getBytes());
    	if(logger.isDebugEnabled()) {
    		convDataInfo("空白", StringUtils.paddingRightString("", ' ', 9), true, false);
    	}	
    	bw.write(convertHexToString("0D0A"));
    }
    
    /**
     * @param connection
     * @param cardProduct
     * @param persoDetails
     * @param file
     * @throws Exception
     * @see tw.com.hyweb.core.cp.batch.perso.PersoFileFactory#makePerso(java.sql.Connection,
     *      tw.com.hyweb.service.db.info.TbCardProductInfo, java.util.List,
     *      java.lang.String)
     */
    public void makePerso(Connection connection,String cardNumber, TbPersoInfo persoInfo, TbHgCardMapInfo hgCardMapInfo) throws Exception
    {	
    	bw.write("DT".getBytes());
    	if(logger.isDebugEnabled()) {
    		convDataInfo("檔頭標籤", "DT", true, false);
    	}
    	
    	bw.write(convertHexToString(cardNumber));
    	if(logger.isDebugEnabled()) {
    		convDataInfo("儲值檔識別碼(PID)", cardNumber, false, false);
    	}
    	
    	bw.write(StringUtils.paddingLeftString(persoInfo.getBalMaxAmt().toString(), '0', 8).getBytes());
    	if(logger.isDebugEnabled()) {
    		convDataInfo("餘額上限 (BIL)", StringUtils.paddingLeftString(persoInfo.getBalMaxAmt().toString(), '0', 8), true, false);
    	}
    	
    	if(persoInfo.getMinBalAmt().intValue()<0) {
    		Number natuNumber = 0 - persoInfo.getMinBalAmt().intValue();
    		String minBalAmt = StringUtils.paddingLeftString(natuNumber.toString(), '0', 5).replaceFirst("0", "-");
    		bw.write(minBalAmt.getBytes());
    		if(logger.isDebugEnabled()) {
	    		convDataInfo("餘額下限 (BLL)", minBalAmt, true, false);
	    	}
		}
		else {
			bw.write(StringUtils.paddingLeftString(persoInfo.getMinBalAmt().toString(), '0', 5).getBytes());
	    	if(logger.isDebugEnabled()) {
	    		convDataInfo("餘額下限 (BLL)", StringUtils.paddingLeftString(persoInfo.getMinBalAmt().toString(), '0', 5), true, false);
	    	}
		}

    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'0',8)));
    	if(logger.isDebugEnabled()) {
    		convDataInfo("卡片保固期", StringUtils.paddingLeftString("",'0',8), false, true);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'0',2)));
    	//bw.write(StringUtils.paddingRightString("",' ',1).getBytes());
    	if(logger.isDebugEnabled()) {
    		convDataInfo("首次消費", StringUtils.paddingLeftString("",'0',2), false, true);
    		//convDataInfo("首次消費", StringUtils.paddingRightString("",' ',1), true, false);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'0',16)));
    	if(logger.isDebugEnabled()) {
    		convDataInfo("累積離線消費金額上限", StringUtils.paddingLeftString("",'0',16), false, true);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'0',2)));
    	if(logger.isDebugEnabled()) {
    		convDataInfo("累積離線消費次數上限", StringUtils.paddingLeftString("",'0',2), false, true);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString(persoInfo.getOfflineAutoloadCnt().toString(),'0',2)));
    	if(logger.isDebugEnabled()) {
    		convDataInfo("連續離線自動加值次數上限", StringUtils.paddingLeftString(persoInfo.getOfflineAutoloadCnt().toString(),'0',2), false, true);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'0',2)));
    	if(logger.isDebugEnabled()) {
    		convDataInfo("連續自動加值次數上限", StringUtils.paddingLeftString("",'0',2), false, true);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'0',2)));
    	if(logger.isDebugEnabled()) {
    		convDataInfo("連續指定加值次數上限", StringUtils.paddingLeftString("",'0',2), false, true);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'F',222)));
    	if(logger.isDebugEnabled()) {
    		convDataInfo("保留", StringUtils.paddingLeftString("",'F',222), false, false);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'F',360)));
    	if(logger.isDebugEnabled()) {
    		convDataInfo("交易資料", StringUtils.paddingLeftString("",'F',360), false, false);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'0',8)));
    	if(logger.isDebugEnabled()) {
    		convDataInfo("Mifare晶片序號", StringUtils.paddingLeftString("",'0',8), false, false);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'0',24)));
    	if(logger.isDebugEnabled()) {
    		convDataInfo("廠商批號Maufacturer code", StringUtils.paddingLeftString("",'0',24), false, false);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString(persoInfo.getFirstAidGroup(),'F',32)));
    	if(logger.isDebugEnabled()) {
    		convDataInfo("目錄服務指標(1)", StringUtils.paddingLeftString(persoInfo.getFirstAidGroup(),'F',32), false, false);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString(persoInfo.getSecondAidGroup(),'F',32)));
    	if(logger.isDebugEnabled()) {
    		convDataInfo("目錄服務指標(2)", StringUtils.paddingLeftString(persoInfo.getSecondAidGroup(),'F',32), false, false);
    	}
    	
    	if(persoInfo.getChipVersion().equals("2")) {
    		bw.write(convertHexToString("0c"));
        	if(logger.isDebugEnabled()) {
        		convDataInfo("發卡單位編號", "0c", false, false);
        	}
    	}
    	else {
    		bw.write(convertHexToString("0a"));
        	if(logger.isDebugEnabled()) {
        		convDataInfo("發卡單位編號", "0a", false, false);
        	}
    	}

    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'0',4)));
    	if(logger.isDebugEnabled()) {
    		convDataInfo("發卡設備編號", StringUtils.paddingLeftString("",'0',4), false, false);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString(NumberStr2HexString(persoInfo.getPersoBatchNo().substring(14,18)), '0', 4)));
    	if(logger.isDebugEnabled()) {
    		convDataInfo("發行批號", persoInfo.getPersoBatchNo().substring(14,18), false, true);
    	}
    	
    	logger.info((DateUtils.getSystemDate()+"000000"));
    	bw.write(convertHexToString(String2HexString(String.valueOf(TimeConversion(DateUtils.getSystemDate()+"000000")))));
    	if(logger.isDebugEnabled()) {
    		convDataInfo("發出日期", String.valueOf(TimeConversion(DateUtils.getSystemDate()+"000000")), false, true);
    	}
    	
    	bw.write(convertHexToString(String2HexString(String.valueOf(TimeConversion("20991231000000")))));
    	if(logger.isDebugEnabled()) {
    		convDataInfo("有效日期", String.valueOf(TimeConversion("20991231000000")), false, true);
    	}
    	
    	bw.write(convertHexToString("01"));
    	if(logger.isDebugEnabled()) {
    		convDataInfo("卡片格式版本", "01", false, true);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString(persoInfo.getCardStatus(),'0',2)));
    	if(logger.isDebugEnabled()) {
    		convDataInfo("卡片狀態", StringUtils.paddingLeftString(persoInfo.getCardStatus(),'0',2), false, true);
    	}
    	
    	String issCardUnitNo = "";
    	if(persoInfo.getChipVersion().equals("2")) {
    		issCardUnitNo = ISOUtil.hexString(convertHexToString(StringUtils.paddingLeftString("0c",'0',2)));
		}
		else {
			issCardUnitNo = ISOUtil.hexString(convertHexToString(StringUtils.paddingLeftString("0a",'0',2)));
		}
    	
    	logger.info("發卡單位編號: " + ISOUtil.hexString(convertHexToString(StringUtils.paddingLeftString(issCardUnitNo,'0',2))));
    	logger.info("發卡設備編號: " + ISOUtil.hexString(convertHexToString(StringUtils.paddingLeftString("",'0',4))));
    	logger.info("發行批號: " + ISOUtil.hexString(convertHexToString(StringUtils.paddingLeftString(NumberStr2HexString(persoInfo.getPersoBatchNo().substring(14,18)), '0', 4))));
    	logger.info("發出日期: " + ISOUtil.hexString(convertHexToString(String2HexString(String.valueOf(TimeConversion(DateUtils.getSystemDate()+"000000"))))));
    	logger.info("有效日期: " + ISOUtil.hexString(convertHexToString(String2HexString(String.valueOf(TimeConversion("20991231000000"))))));
    	logger.info("卡片格式版本: " + ISOUtil.hexString(convertHexToString("01")));
    	logger.info("卡片狀態: " + ISOUtil.hexString(convertHexToString(StringUtils.paddingLeftString(persoInfo.getCardStatus(),'0',2))));

    	String checkCode1 =	issCardUnitNo +
							ISOUtil.hexString(convertHexToString(StringUtils.paddingLeftString("",'0',4))) + 
							ISOUtil.hexString(convertHexToString(StringUtils.paddingLeftString(NumberStr2HexString(persoInfo.getPersoBatchNo().substring(14,18)), '0', 4))) + 
							ISOUtil.hexString(convertHexToString(String2HexString(String.valueOf(TimeConversion(DateUtils.getSystemDate()+"000000"))))) +  
							ISOUtil.hexString(convertHexToString(String2HexString(String.valueOf(TimeConversion("20991231000000"))))) +
							ISOUtil.hexString(convertHexToString("01")) + 
							ISOUtil.hexString(convertHexToString(StringUtils.paddingLeftString(persoInfo.getCardStatus(),'0',2)));
    	
    	logger.info("Before XOR: [" + checkCode1 +"]");
    	logger.info("檢查碼: [" + XorData(checkCode1) + "]");
    	
    	byte[] tmpByte = new byte[1];
    	tmpByte[0] = XorData(checkCode1);
    	logger.info("檢查碼: [" + ISOUtil.hexString(tmpByte) + "]");
    	
    	bw.write(XorData(checkCode1));
    	
    	String autoReloadFlag = "";
    	if(persoInfo.getAutoReloadFlag().equals("N")){
    		autoReloadFlag = "01";
    	}
    	else {
    		autoReloadFlag = "02";
    	}

    	bw.write(convertHexToString(StringUtils.paddingLeftString(autoReloadFlag,'0',2)));
    	if(logger.isDebugEnabled()) {
    		convDataInfo("自動加值設定", StringUtils.paddingLeftString(autoReloadFlag,'0',2), false, false);
    	}
    	
    	//bw.write(convertHexToString(StringUtils.paddingLeftString(persoInfo.getAutoReloadValue().toString(),'0',4)));
    	bw.write(convertHexToString(StringUtils.paddingLeftString(String2HexString(persoInfo.getAutoReloadValue().toString()),'0',4)));
    	if(logger.isDebugEnabled()) {
    		convDataInfo("自動加值票值數額", StringUtils.paddingLeftString(persoInfo.getAutoReloadValue().toString(),'0',4), false, true);
    	}
    	
    	bw.write(convertHexToString(String2HexString(persoInfo.getCardBalLimit().toString())));
    	if(logger.isDebugEnabled()) {
    		convDataInfo("儲存最大票值數額(餘額上限)", persoInfo.getCardBalLimit().toString(), false, false);
    	}
    	
    	bw.write(convertHexToString(String2HexString(persoInfo.getMaxConsumeAmt().toString())));
    	if(logger.isDebugEnabled()) {
    		convDataInfo("每筆可扣減最大票值數額", persoInfo.getMaxConsumeAmt().toString(), false, false);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'F',2)));
    	if(logger.isDebugEnabled()) {
    		convDataInfo("保留(指定加值設定)", StringUtils.paddingLeftString("",'F',2), false, false);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'F',4)));
    	if(logger.isDebugEnabled()) {
    		convDataInfo("保留(指定加值票值數額)", StringUtils.paddingLeftString("",'F',4), false, false);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'0',4)));
    	if(logger.isDebugEnabled()) {
    		convDataInfo("自動加值日期", StringUtils.paddingLeftString("",'0',4), false, false);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString(persoInfo.getOfflineAutoloadCnt().toString(),'0',2)));
    	if(logger.isDebugEnabled()) {
    		convDataInfo("連續離線自動加值次數上限", StringUtils.paddingLeftString(persoInfo.getOfflineAutoloadCnt().toString(),'0',2), false, false);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'0',2)));
    	if(logger.isDebugEnabled()) {
    		convDataInfo("連續自動加值次數上限", StringUtils.paddingLeftString("",'0',2), false, false);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'0',2)));
    	if(logger.isDebugEnabled()) {
    		convDataInfo("連續指定加值次數上限", StringUtils.paddingLeftString("",'0',2), false, false);
    	}
    	
    	logger.info("自動加值設定: " + ISOUtil.hexString(convertHexToString(StringUtils.paddingLeftString(autoReloadFlag,'0',2))));
    	logger.info("自動加值票值數額: " + ISOUtil.hexString(convertHexToString(StringUtils.paddingLeftString(persoInfo.getAutoReloadValue().toString(),'0',4))));
    	logger.info("儲存最大票值數額(餘額上限): " + ISOUtil.hexString(convertHexToString(String2HexString(persoInfo.getCardBalLimit().toString()))));
    	logger.info("每筆可扣減最大票值數額: " + ISOUtil.hexString(convertHexToString(persoInfo.getMaxConsumeAmt().toString())));
    	logger.info("保留(指定加值設定): " + ISOUtil.hexString(convertHexToString(StringUtils.paddingLeftString("",'F',2))));
    	logger.info("保留(指定加值票值數額): " + ISOUtil.hexString(convertHexToString(StringUtils.paddingLeftString("",'F',4))));
    	logger.info("自動加值日期: " + ISOUtil.hexString(convertHexToString(StringUtils.paddingLeftString("",'0',4))));
    	logger.info("連續離線自動加值次數上限: " + ISOUtil.hexString(convertHexToString(StringUtils.paddingLeftString(persoInfo.getOfflineAutoloadCnt().toString(),'0',2))));
    	logger.info("連續自動加值次數上限: " + ISOUtil.hexString(convertHexToString(StringUtils.paddingLeftString("",'0',2))));
    	logger.info("連續指定加值次數上限: " + ISOUtil.hexString(convertHexToString(StringUtils.paddingLeftString("",'0',2))));
    	
    	String checkCode2 = ISOUtil.hexString(convertHexToString(StringUtils.paddingLeftString(autoReloadFlag,'0',2))) + 
    						//ISOUtil.hexString(convertHexToString(StringUtils.paddingLeftString(persoInfo.getAutoReloadValue().toString(),'0',4))) +
    						ISOUtil.hexString(convertHexToString(StringUtils.paddingLeftString(String2HexString(persoInfo.getAutoReloadValue().toString()),'0',4))) +
    						ISOUtil.hexString(convertHexToString(String2HexString(persoInfo.getCardBalLimit().toString()))) + 
    						ISOUtil.hexString(convertHexToString(String2HexString(persoInfo.getMaxConsumeAmt().toString()))) +
    						ISOUtil.hexString(convertHexToString(StringUtils.paddingLeftString("",'F',2))) + 
    						ISOUtil.hexString(convertHexToString(StringUtils.paddingLeftString("",'F',4))) + 
    						ISOUtil.hexString(convertHexToString(StringUtils.paddingLeftString("",'0',4))) +
    						ISOUtil.hexString(convertHexToString(StringUtils.paddingLeftString(persoInfo.getOfflineAutoloadCnt().toString(),'0',2))) +
    						ISOUtil.hexString(convertHexToString(StringUtils.paddingLeftString("",'0',2))) +
    						ISOUtil.hexString(convertHexToString(StringUtils.paddingLeftString("",'0',2)));
    	
    	logger.info("Before XOR: [" + checkCode2 +"]");
    	logger.info("檢查碼: [" + XorData(checkCode2) + "]");
    	bw.write(XorData(checkCode2));
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'0',32)));
    	if(logger.isDebugEnabled()) {
    		convDataInfo("防偽驗證資料", StringUtils.paddingLeftString("",'0',32), false, false);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString(persoInfo.getPreloadAmt().toString(), '0', 32)));
    	if(logger.isDebugEnabled()) {
    		convDataInfo("主要票值", StringUtils.paddingLeftString(persoInfo.getPreloadAmt().toString(), '0', 32), false, false);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'0',32)));
    	if(logger.isDebugEnabled()) {
    		convDataInfo("票值備份", StringUtils.paddingLeftString("",'0',32), false, false);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'0',32)));
    	if(logger.isDebugEnabled()) {
    		convDataInfo("票值加值記錄", StringUtils.paddingLeftString("",'0',32), false, false);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'0',32)));
    	if(logger.isDebugEnabled()) {
    		convDataInfo("卡片交易狀態資料", StringUtils.paddingLeftString("",'0',32), false, false);
    	}
    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'0',32)));
    	if(logger.isDebugEnabled()) {
    		convDataInfo("最近兩筆閘門交易記錄(1)", StringUtils.paddingLeftString("",'0',32), false, false);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'0',32)));
    	if(logger.isDebugEnabled()) {
    		convDataInfo("最近兩筆閘門交易記錄(2)", StringUtils.paddingLeftString("",'0',32), false, false);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'0',32)));
    	if(logger.isDebugEnabled()) {
    		convDataInfo("最近六筆交易記錄(1)", StringUtils.paddingLeftString("",'0',32), false, false);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'0',32)));
    	if(logger.isDebugEnabled()) {
    		convDataInfo("最近六筆交易記錄(2)", StringUtils.paddingLeftString("",'0',32), false, false);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'0',32)));
    	if(logger.isDebugEnabled()) {
    		convDataInfo("最近六筆交易記錄(3)", StringUtils.paddingLeftString("",'0',32), false, false);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'0',32)));
    	if(logger.isDebugEnabled()) {
    		convDataInfo("最近六筆交易記錄(4)", StringUtils.paddingLeftString("",'0',32), false, false);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'0',32)));
    	if(logger.isDebugEnabled()) {
    		convDataInfo("最近六筆交易記錄(5)", StringUtils.paddingLeftString("",'0',32), false, false);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'0',32)));
    	if(logger.isDebugEnabled()) {
    		convDataInfo("最近六筆交易記錄(6)", StringUtils.paddingLeftString("",'0',32), false, false);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString(persoInfo.getCardTypeId(),'0',2)));
    	if(logger.isDebugEnabled()) {
    		convDataInfo("使用者型態(卡種)", StringUtils.paddingLeftString(persoInfo.getCardTypeId(),'0',2), false, false);
    	}
    	    	
    	bw.write(convertHexToString(String2HexString(String.valueOf(TimeConversion("20991231000000")))));
    	if(logger.isDebugEnabled()) {
    		convDataInfo("使用者截止日期", String2HexString(String.valueOf(TimeConversion("20991231000000"))), false, false);
    	}
    	   	
    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'0',12)));
    	if(logger.isDebugEnabled()) {
    		convDataInfo("使用者序號User id", StringUtils.paddingLeftString("",'0',12), false, false);
    	}
    	
    	String ptaUnitNo = "";
    	if(persoInfo.getPtaUnitNo().equals("")){
    		ptaUnitNo = "FF";
    	}
    	else {
    		ptaUnitNo = persoInfo.getPtaUnitNo();
    	}
    	bw.write(convertHexToString(StringUtils.paddingLeftString(ptaUnitNo,'0',2)));
    	if(logger.isDebugEnabled()) {
    		convDataInfo("發卡企業編號", StringUtils.paddingLeftString(ptaUnitNo,'0',2), false, false);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'F',4)));
    	if(logger.isDebugEnabled()) {
    		convDataInfo("保留", StringUtils.paddingLeftString("",'F',4), false, false);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString(NumberStr2HexString(persoInfo.getCardFee().toString()), '0', 4)));
    	if(logger.isDebugEnabled()) {
    		convDataInfo("卡片押金", persoInfo.getCardFee().toString(), false, true);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString(persoInfo.getCardCatId(),'0',2)));
    	if(logger.isDebugEnabled()) {
    		convDataInfo("卡別", StringUtils.paddingLeftString(persoInfo.getCardCatId(),'0',2), false, false);
    	}
    	
    	bw.write(convertHexToString("00"));
    	if(logger.isDebugEnabled()) {
    		convDataInfo("記名註記", "00", false, false);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'0',4)));
    	if(logger.isDebugEnabled()) {
    		convDataInfo("生日MMDD", StringUtils.paddingLeftString("",'0',4), false, true);
    	}
    	
    	bw.write(convertHexToString(String2HexString(persoInfo.getCardProduct().substring(2, 5).toString())));
    	if(logger.isDebugEnabled()) {
    		convDataInfo("產品別", persoInfo.getCardProduct().substring(2, 5).toString(), false, true);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'F',22)));
    	if(logger.isDebugEnabled()) {
    		convDataInfo("保留", StringUtils.paddingLeftString("",'F',22), false, false);
    	}
    	
    	bw.write(convertHexToString(cardNumber));
    	if(logger.isDebugEnabled()) {
    		convDataInfo("儲值檔識別碼(PID)", cardNumber, false, false);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'F',16)));	
    	if(logger.isDebugEnabled()) {
    		convDataInfo("保留", StringUtils.paddingLeftString("",'F',16), false, false);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'0',32)));	
    	if(logger.isDebugEnabled()) {
    		convDataInfo("保留-台鐵", StringUtils.paddingLeftString("",'0',32), false, false);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'0',32)));	
    	if(logger.isDebugEnabled()) {
    		convDataInfo("保留-台鐵", StringUtils.paddingLeftString("",'0',32), false, false);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'0',32)));	
    	if(logger.isDebugEnabled()) {
    		convDataInfo("保留-台鐵", StringUtils.paddingLeftString("",'0',32), false, false);
    	}

    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'0',32)));	
    	if(logger.isDebugEnabled()) {
    		convDataInfo("保留-客運", StringUtils.paddingLeftString("",'0',32), false, false);
    	}

    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'0',32)));	
    	if(logger.isDebugEnabled()) {
    		convDataInfo("保留-客運", StringUtils.paddingLeftString("",'0',32), false, false);
    	}

    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'0',32)));	
    	if(logger.isDebugEnabled()) {
    		convDataInfo("保留-客運", StringUtils.paddingLeftString("",'0',32), false, false);
    	}

    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'0',32)));	
    	if(logger.isDebugEnabled()) {
    		convDataInfo("保留-台北捷運", StringUtils.paddingLeftString("",'0',32), false, false);
    	}

    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'0',32)));	
    	if(logger.isDebugEnabled()) {
    		convDataInfo("保留-台北捷運", StringUtils.paddingLeftString("",'0',32), false, false);
    	}

    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'0',32)));	
    	if(logger.isDebugEnabled()) {
    		convDataInfo("保留-台北捷運", StringUtils.paddingLeftString("",'0',32), false, false);
    	}

    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'0',32)));	
    	if(logger.isDebugEnabled()) {
    		convDataInfo("保留-資料備份區", StringUtils.paddingLeftString("",'0',32), false, false);
    	}

    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'0',32)));	
    	if(logger.isDebugEnabled()) {
    		convDataInfo("保留-資料備份區", StringUtils.paddingLeftString("",'0',32), false, false);
    	}

    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'0',32)));	
    	if(logger.isDebugEnabled()) {
    		convDataInfo("保留-資料備份區", StringUtils.paddingLeftString("",'0',32), false, false);
    	}

    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'0',32)));	
    	if(logger.isDebugEnabled()) {
    		convDataInfo("保留-段次客運", StringUtils.paddingLeftString("",'0',32), false, false);
    	}

    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'0',32)));	
    	if(logger.isDebugEnabled()) {
    		convDataInfo("保留-段次客運", StringUtils.paddingLeftString("",'0',32), false, false);
    	}

    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'0',32)));	
    	if(logger.isDebugEnabled()) {
    		convDataInfo("保留-段次客運", StringUtils.paddingLeftString("",'F',32), false, false);
    	}

    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'0',32)));	
    	if(logger.isDebugEnabled()) {
    		convDataInfo("保留-停車場", StringUtils.paddingLeftString("",'0',32), false, false);
    	}

    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'0',32)));	
    	if(logger.isDebugEnabled()) {
    		convDataInfo("保留-停車場", StringUtils.paddingLeftString("",'0',32), false, false);
    	}

    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'0',32)));	
    	if(logger.isDebugEnabled()) {
    		convDataInfo("保留-停車場", StringUtils.paddingLeftString("",'0',32), false, false);
    	}

    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'0',32)));	
    	if(logger.isDebugEnabled()) {
    		convDataInfo("保留-機場捷運", StringUtils.paddingLeftString("",'0',32), false, false);
    	}

    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'0',32)));	
    	if(logger.isDebugEnabled()) {
    		convDataInfo("保留-機場捷運", StringUtils.paddingLeftString("",'0',32), false, false);
    	}

    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'0',32)));	
    	if(logger.isDebugEnabled()) {
    		convDataInfo("保留-機場捷運", StringUtils.paddingLeftString("",'0',32), false, false);
    	}

    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'0',32)));	
    	if(logger.isDebugEnabled()) {
    		convDataInfo("保留-機場捷運", StringUtils.paddingLeftString("",'0',32), false, false);
    	}

    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'0',32)));	
    	if(logger.isDebugEnabled()) {
    		convDataInfo("保留-機場捷運", StringUtils.paddingLeftString("",'0',32), false, false);
    	}

    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'0',32)));	
    	if(logger.isDebugEnabled()) {
    		convDataInfo("保留-機場捷運", StringUtils.paddingLeftString("",'0',32), false, false);
    	}

    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'0',32)));	
    	if(logger.isDebugEnabled()) {
    		convDataInfo("保留-機場捷運", StringUtils.paddingLeftString("",'0',32), false, false);
    	}

    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'0',32)));	
    	if(logger.isDebugEnabled()) {
    		convDataInfo("保留-機場捷運", StringUtils.paddingLeftString("",'0',32), false, false);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'0',32)));	
    	if(logger.isDebugEnabled()) {
    		convDataInfo("保留-機場捷運", StringUtils.paddingLeftString("",'0',32), false, false);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'0',256)));	
    	if(logger.isDebugEnabled()) {
    		convDataInfo("控管資料", StringUtils.paddingLeftString("",'0',256), false, false);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'F',360)));	
    	if(logger.isDebugEnabled()) {
    		convDataInfo("交易資料", StringUtils.paddingLeftString("",'F',360), false, false);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'0',256)));	
    	if(logger.isDebugEnabled()) {
    		convDataInfo("控管資料", StringUtils.paddingLeftString("",'0',256), false, false);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'F',360)));	
    	if(logger.isDebugEnabled()) {
    		convDataInfo("交易資料", StringUtils.paddingLeftString("",'F',360), false, false);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'0',256)));	
    	if(logger.isDebugEnabled()) {
    		convDataInfo("控管資料", StringUtils.paddingLeftString("",'0',256), false, false);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'F',360)));	
    	if(logger.isDebugEnabled()) {
    		convDataInfo("交易資料", StringUtils.paddingLeftString("",'F',360), false, false);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'0',256)));	
    	if(logger.isDebugEnabled()) {
    		convDataInfo("控管資料", StringUtils.paddingLeftString("",'0',256), false, false);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'F',360)));	
    	if(logger.isDebugEnabled()) {
    		convDataInfo("交易資料", StringUtils.paddingLeftString("",'F',360), false, false);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'0',256)));	
    	if(logger.isDebugEnabled()) {
    		convDataInfo("控管資料", StringUtils.paddingLeftString("",'0',256), false, false);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'F',360)));	
    	if(logger.isDebugEnabled()) {
    		convDataInfo("交易資料", StringUtils.paddingLeftString("",'F',360), false, false);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'0',256)));	
    	if(logger.isDebugEnabled()) {
    		convDataInfo("控管資料", StringUtils.paddingLeftString("",'0',256), false, false);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'F',360)));	
    	if(logger.isDebugEnabled()) {
    		convDataInfo("交易資料", StringUtils.paddingLeftString("",'F',360), false, false);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'0',256)));	
    	if(logger.isDebugEnabled()) {
    		convDataInfo("控管資料", StringUtils.paddingLeftString("",'0',256), false, false);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'F',360)));	
    	if(logger.isDebugEnabled()) {
    		convDataInfo("交易資料", StringUtils.paddingLeftString("",'F',360), false, false);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'0',256)));	
    	if(logger.isDebugEnabled()) {
    		convDataInfo("控管資料", StringUtils.paddingLeftString("",'0',256), false, false);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'F',360)));	
    	if(logger.isDebugEnabled()) {
    		convDataInfo("交易資料", StringUtils.paddingLeftString("",'F',360), false, false);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'0',256)));	
    	if(logger.isDebugEnabled()) {
    		convDataInfo("控管資料", StringUtils.paddingLeftString("",'0',256), false, false);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'F',360)));	
    	if(logger.isDebugEnabled()) {
    		convDataInfo("交易資料", StringUtils.paddingLeftString("",'F',360), false, false);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'0',256)));	
    	if(logger.isDebugEnabled()) {
    		convDataInfo("控管資料", StringUtils.paddingLeftString("",'0',256), false, false);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'F',360)));	
    	if(logger.isDebugEnabled()) {
    		convDataInfo("交易資料", StringUtils.paddingLeftString("",'F',360), false, false);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'0',256)));	
    	if(logger.isDebugEnabled()) {
    		convDataInfo("控管資料", StringUtils.paddingLeftString("",'0',256), false, false);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'F',360)));	
    	if(logger.isDebugEnabled()) {
    		convDataInfo("交易資料", StringUtils.paddingLeftString("",'F',360), false, false);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'0',256)));	
    	if(logger.isDebugEnabled()) {
    		convDataInfo("控管資料", StringUtils.paddingLeftString("",'0',256), false, false);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'F',360)));	
    	if(logger.isDebugEnabled()) {
    		convDataInfo("交易資料", StringUtils.paddingLeftString("",'F',360), false, false);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'0',256)));	
    	if(logger.isDebugEnabled()) {
    		convDataInfo("控管資料", StringUtils.paddingLeftString("",'0',256), false, false);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'F',360)));	
    	if(logger.isDebugEnabled()) {
    		convDataInfo("交易資料", StringUtils.paddingLeftString("",'F',360), false, false);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'0',256)));	
    	if(logger.isDebugEnabled()) {
    		convDataInfo("控管資料", StringUtils.paddingLeftString("",'0',256), false, false);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'F',360)));	
    	if(logger.isDebugEnabled()) {
    		convDataInfo("交易資料", StringUtils.paddingLeftString("",'F',360), false, false);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'0',256)));	
    	if(logger.isDebugEnabled()) {
    		convDataInfo("控管資料", StringUtils.paddingLeftString("",'0',256), false, false);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'F',360)));	
    	if(logger.isDebugEnabled()) {
    		convDataInfo("交易資料", StringUtils.paddingLeftString("",'F',360), false, false);
    	}
    	
    	//20170620 改為判斷HgCardGroupId是否有填
    	if(!StringUtil.isEmpty(persoInfo.getHgCardGroupId()))  {
    		//bw.write(convertHexToString(StringUtils.paddingRightString(hgCardMapInfo.getBarcode1(),'0',32)));
    		bw.write(StringUtils.paddingRightString(hgCardMapInfo.getBarcode1(),' ',16).getBytes());
    		if(logger.isDebugEnabled()) {
    			//convDataInfo("HG磁條一軌", StringUtils.paddingRightString(hgCardMapInfo.getBarcode1(),'0',32), false, false);
    			convDataInfo("HG磁條一軌", StringUtils.paddingRightString(hgCardMapInfo.getBarcode1(),' ',16), true, false);
    		}
    		
    		//bw.write(convertHexToString(StringUtils.paddingRightString(hgCardMapInfo.getBarcode2(),'0',74)));
    		bw.write(StringUtils.paddingRightString(hgCardMapInfo.getBarcode2(),' ',37).getBytes());
    		if(logger.isDebugEnabled()) {
    			//convDataInfo("HG磁條二軌", StringUtils.paddingRightString(hgCardMapInfo.getBarcode2(),'0',74), false, false);
    			convDataInfo("HG磁條二軌", StringUtils.paddingRightString(hgCardMapInfo.getBarcode2(),' ',37), true, false);
    		}
    		
    		//bw.write(convertHexToString(StringUtils.paddingRightString(hgCardMapInfo.getBarcode1().substring(3, 16),'0',26)));
    		bw.write(StringUtils.paddingRightString(hgCardMapInfo.getBarcode1().substring(3, 16),' ',13).getBytes());
    		if(logger.isDebugEnabled()) {
    			//convDataInfo("HG條碼", StringUtils.paddingRightString(hgCardMapInfo.getBarcode1().substring(3, 16),'0',26), false, false);
    			convDataInfo("HG條碼", StringUtils.paddingRightString(hgCardMapInfo.getBarcode1().substring(3, 16),' ',13), true, false);
    		}
    	}else {   		
    		//bw.write(convertHexToString(StringUtils.paddingRightString("",'0',32)));
    		bw.write(StringUtils.paddingRightString("",' ',16).getBytes());
    		if(logger.isDebugEnabled()) {
    			//convDataInfo("HG磁條一軌", StringUtils.paddingRightString("",'0',32), false, false);
    			convDataInfo("HG磁條一軌", StringUtils.paddingRightString("",' ',16), true, false);
    		}
    		
    		//bw.write(convertHexToString(StringUtils.paddingRightString("",'0',74)));
    		bw.write(StringUtils.paddingRightString("",' ',37).getBytes());
    		if(logger.isDebugEnabled()) {
    			//convDataInfo("HG磁條二軌", StringUtils.paddingRightString("",'0',74), false, false);
    			convDataInfo("HG磁條二軌", StringUtils.paddingRightString("",' ',37), true, false);
    		}
    		
    		//bw.write(convertHexToString(StringUtils.paddingRightString("",'0',26)));
    		bw.write(StringUtils.paddingRightString("",' ',13).getBytes());
    		if(logger.isDebugEnabled()) {
    			//convDataInfo("HG條碼", StringUtils.paddingRightString("",'0',26), false, false);
    			convDataInfo("HG條碼", StringUtils.paddingRightString("",' ',13), true, false);
    		}
    	}

    	bw.write(StringUtils.paddingLeftString(persoInfo.getCardMaterialNo(), ' ', 18).getBytes());
    	if(logger.isDebugEnabled()) {
    		convDataInfo("卡的物料號碼", StringUtils.paddingLeftString(persoInfo.getCardMaterialNo(), ' ', 18), true, false);
    	}	
    	
    	bw.write(StringUtils.paddingRightString("", ' ', 3).getBytes());
    	if(logger.isDebugEnabled()) {
    		convDataInfo("空白", StringUtils.paddingRightString("", ' ', 3), true, false);
    	}	
    	bw.write(convertHexToString("0D0A"));
    	
    	logger.info("generate card Number: " + cardNumber);
    }
    
    public byte XorData(String data)
    {
    	if(data.length() % 2 != 0) {
    		data += "F";
    	}
    	
    	byte[] bytes = ISOUtil.hex2byte(data);
		byte tmpByte = 0;
		
		for(int b=0; b<bytes.length; b++)
		{
			if(b==0) {
				tmpByte = bytes[b];
				continue;
			}
			tmpByte ^= bytes[b];
		}
		return tmpByte;
    }
    
    public String NumberStr2HexString(String value) {
    	String hex = Long.toHexString(Long.valueOf(value));
    	if(hex.length() % 2 !=0) {
    		hex = "0" + hex;
    	}
    	return hex;
    }
    
    public String String2HexString(String value) {
    	String hex = Long.toHexString(Long.valueOf(value));
    	if(hex.length() % 2 !=0) {
    		//hex = hex + "F";
    		hex = "0" + hex;
    	}
    	return hex;
    }
    
    public String PackString(String hexString) { 	
    	byte[] bytes = ISOUtil.hex2byte(hexString);
    	
        StringBuilder sb = new StringBuilder(bytes.length);
        for (int i = 0; i < bytes.length; ++ i) {
            sb.append((char) bytes[i]);
        }
        return sb.toString();
    } 
    
    public byte[] convertHexToString(String hex){
      	 
    	byte[] nameByteArray = ISOUtil.hex2byte(hex);
		ByteBuffer byteBuffer = ByteBuffer.allocate(nameByteArray.length + 8);
		byteBuffer.put(nameByteArray);
		
		return nameByteArray;
    }
    
    public static Date parseDateTime(String dateString, String pattern)
    {
        DateFormat dateformat = new SimpleDateFormat(pattern);
        Date date=null;
        try
        {
            date = dateformat.parse(dateString);
        }
        catch (ParseException e)
        {
            System.out.println(e);
        }
        return date;
       
    }
    
    public long TimeConversion(String time) throws Exception
    {
    	Date termDate = parseDateTime(time, "yyyyMMddHHmmss");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(termDate);
        long calendarMillis = calendar.getTimeInMillis();
        long unixTime = calendarMillis / 1000L;

	    return unixTime;
    }
    
    public void makePersoDone() throws Exception
    {
        try
        {
        	bw.flush();
        }
        finally
        {
            if (bw != null)
            {
            	bw.close();
            }
        }
    }
    
    public String getPersoFileName(TbFileInfoInfo fileInfo, TbPersoInfo persoInfo, String persoBatchNo, String isamNumber)
    {
        return "P3." + persoInfo.getCardFactoryId() +  "." + persoBatchNo + isamNumber;
    }
    public String getBankPersoFileName(TbFileInfoInfo fileInfo, String bankMemId, String persoBatchNo, String isamNumber)
    {
        return "P3." + bankMemId +  "." + persoBatchNo + isamNumber;
    }

    /**
     * 
     * 
     * @param connection
     * @param batchDate
     * @param memberId
     * @return
     * @throws SQLException
     */
    private String getSerialNumber(Connection connection, String batchDate, String memberId) throws SQLException
    {
        String sql = "select max(SEQNO) as SEQNO from TB_OUTCTL where MEM_ID=? and FILE_NAME=? and FILE_DATE=?";

        String maxSequence = executeQuerySingleValue(connection, sql, memberId, fileInfo.getFileName(), batchDate);

        if (StringUtil.isEmpty(maxSequence))
        {
            return "01";
        }
        else
        {
            return StringUtils.int2String(Integer.parseInt(maxSequence) + 1, 2);
        }
    }

    /**
     * 
     * 
     * @param connection
     * @param batchDate
     * @param memberId
     * @param serialNumber
     * @throws SQLException
     */
    private void insertOutControl(Connection connection, String batchDate, String memberId, String memGroupId, String serialNumber, int totalRecord) throws SQLException
    {
        TbOutctlInfo outControl = new TbOutctlInfo();
        outControl.setMemId(memberId);
        outControl.setMemGroupId(memGroupId);
        outControl.setFileName(fileInfo.getFileName());
        outControl.setFileDate(batchDate);
        outControl.setSeqno(serialNumber);
        outControl.setFileType("P");
        outControl.setWorkFlag(Layer1Constants.OWORKFLAG_INWORK);
        outControl.setTotRec(totalRecord);
        outControl.setFileSize(new File(filePath + fullFileName).length());
        outControl.setFullFileName(fullFileName);
        outControl.setProgramName("ExpPerso");
        outControl.setStartDate(DateUtils.getSystemDate());
        outControl.setStartTime(DateUtils.getSystemTime());
        outControl.setParMon(batchDate.substring(4, 6));
        outControl.setParDay(batchDate.substring(6));

        new TbOutctlMgr(connection).insert(outControl);
    }
   
    /**
     * 
     * 
     * @param connection
     * @return
     * @throws SQLException
     */
    private TbFileInfoInfo getPersoFileInfo(Connection connection, TbPersoInfo persoInfo) throws SQLException
    {
        TbFileInfoPK pk = new TbFileInfoPK();
        if (persoInfo.getCardFactoryId().equals(ProcessPersoJob.OWN_CARD_FACTORY)){
        	pk.setFileName(BANK_FILE_NAME);
        }
        else{
        	pk.setFileName(PERSO_FILE_NAME);
        }
        pk.setInOut("O");

        return new TbFileInfoMgr(connection).querySingle(pk);
    }
    
	public void updateOutControl(Connection connection) throws Exception {
		String sqlStr = "update TB_OUTCTL set FILE_SIZE= ? WHERE FULL_FILE_NAME = ?";
		executeUpdate(connection, sqlStr, new File(filePath + fullFileName).length() , fullFileName);
	}

	public void encryptPerso2File(TbPersoInfo persoInfo, Connection connection) throws Exception {
		String enString1 = null;
		String enString2 = null;
		
		BufferedInputStream br = new BufferedInputStream(new FileInputStream(new File(tempFile)));
		BufferedOutputStream bw = new BufferedOutputStream(new FileOutputStream(new File(file)));
		
		int times = br.available() / HSM_MAX_LENGTH;
		logger.info("times: " + times);
		int bytes = br.available() % HSM_MAX_LENGTH;
		logger.info("bytes: " + bytes);
		
		byte[] buffer1 = new byte[HSM_MAX_LENGTH];
		byte[] buffer2 = new byte[bytes];

		//自行製卡需先設定KeyId
		//if (persoInfo.getPersoType().equals(ProcessPersoJob.PERSO_TYPE_BANK_CARD)){
		if (persoInfo.getCardFactoryId().equals(ProcessPersoJob.OWN_CARD_FACTORY)){
			String sqlCommand = "SELECT MEM_ID FROM TB_MEMBER WHERE BANK_ID = " + StringUtil.toSqlValueWithSQuote(persoInfo.getBankId());
			String memId = DbUtil.getString(sqlCommand, connection);
			if (memId == null || memId.length() <= 0 ){
				throw new Exception("MemberId is not exist, bankId [" + persoInfo.getBankId() + "]");
			}
			cgYhdpSsf.setKeyId(memId+"KEY");
		}
		
		if(times >0) {
			
			for(int i=0; i<times; i++)
			{
				br.read(buffer1);
				//if (!persoInfo.getPersoType().equals(ProcessPersoJob.PERSO_TYPE_BANK_CARD)){
				if (!persoInfo.getCardFactoryId().equals(ProcessPersoJob.OWN_CARD_FACTORY)){
					enString1 = hsmAdapterECB.encryptECB(ISOUtil.hexString(buffer1), false);
				}
				else{//自行製卡加密方式
					enString1 = cgYhdpSsf.encryptDES3(ISOUtil.hexString(buffer1), false);
				}
				
				logger.info("encrypt String1: " + enString1);
				bw.write(ISOUtil.hex2byte(enString1));
			}
		}
		
		if(bytes != 0) {
			br.read(buffer2);
			//if (!persoInfo.getPersoType().equals(ProcessPersoJob.PERSO_TYPE_BANK_CARD)){
			if (!persoInfo.getCardFactoryId().equals(ProcessPersoJob.OWN_CARD_FACTORY)){
				enString2 = hsmAdapterECB.encryptECB(ISOUtil.hexString(buffer2), true);
			}
			else{//自行製卡加密方式
				enString2 = cgYhdpSsf.encryptDES3(ISOUtil.hexString(buffer2), true);
			}
			logger.info("encrypt String2: " + enString2);
			bw.write(ISOUtil.hex2byte(enString2));
		}
		
		/*String deString = decryptCBC(enString2);
		logger.info("decrypt: " + deString);*/
		
		bw.flush();
		ReleaseResource.releaseIO(br);	
		ReleaseResource.releaseIO(bw);	
	}  
}