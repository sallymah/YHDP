package tw.com.hyweb.svc.yhdp.batch.persoOTA;

import static tw.com.hyweb.core.cp.batch.util.PreparedStatementUtils.executeQuerySingleValue;
import static tw.com.hyweb.core.cp.batch.util.PreparedStatementUtils.executeUpdate;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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
import tw.com.hyweb.service.db.info.TbPersoInfo;
import tw.com.hyweb.service.db.mgr.TbFileInfoMgr;
import tw.com.hyweb.service.db.mgr.TbMemberMgr;
import tw.com.hyweb.service.db.mgr.TbOutctlMgr;
import tw.com.hyweb.service.db.mgr.TbPersoFactoryMgr;
import tw.com.hyweb.util.DbUtil;
import tw.com.hyweb.util.ISOUtil;
import tw.com.hyweb.util.ReleaseResource;
import tw.com.hyweb.util.string.StringUtil;

public class YhdpPersoCardFile {
	private static final Logger logger = Logger.getLogger(YhdpPersoCardFile.class);
	
    private TbFileInfoInfo fileInfo;
    //private TbCardProductInfo cardProductInfo;
    private String filePath;
    private String fullFileName;
    private String file;
    private BufferedOutputStream bw = null;

    private String fileName = "PERSO_OTA";
    
    public YhdpPersoCardFile (){
    }
	/**
     * 
     * 
     * @see tw.com.hyweb.core.cp.batch.framework.generic.CursorBatchJobFactory#init(java.sql.Connection,
     *      java.lang.String)
     */
    public void init(Connection connection, String batchDate,TbPersoInfo persoInfo) throws Exception
    {
    	// 20140609 cvvArray,pinArray???????????????????????????makePerso?????? new ???????????????
    	/*cvvArray = null;
        pinArray = null;*/
    	
    	TbPersoFactoryMgr mgr = new TbPersoFactoryMgr(connection);
    	
    	TbFileInfoPK pk = new TbFileInfoPK();
        pk.setFileName(fileName);
        pk.setInOut("O");
        fileInfo = new TbFileInfoMgr(connection).querySingle(pk);
        
        String LocalPath = fileInfo.getLocalPath();

        //?????????(00000000) ?????????(MMMMMMMM)
        String bankMemId = "00000000";
        if (persoInfo.getPersoType().equals("3")){
        	//?????????
        	TbMemberInfo bankMemberInfo = null;
	    	Vector<TbMemberInfo> result = new Vector<TbMemberInfo>();        	
	    	String where = "BANK_ID = "+ StringUtil.toSqlValueWithSQuote(persoInfo.getBankId());
	    	
	    	new TbMemberMgr(connection).queryMultiple(where, result);
	    	
	    	if (result.size() != 1){
	    		throw new Exception("(PersoBatchNo: " +persoInfo.getPersoBatchNo() + ", BankId:" + persoInfo.getBankId() + ") member error");
	    	}
	    	bankMemberInfo = result.get(0);
	    	bankMemId = bankMemberInfo.getMemId();
        }
        
        //OTA???????????????
        Statement stmt = null;
        ResultSet rs = null;
        String seqno = "01";
        String seqnoSql = "SELECT LPAD(NVL(TO_CHAR(TO_NUMBER(MAX(SEQNO)) + 1), '01'), 2, '0') FROM TB_OUTCTL " +
                		"WHERE MEM_ID = " + StringUtil.toSqlValueWithSQuote(persoInfo.getMemId()) + " " +
                		"AND FILE_NAME = " + StringUtil.toSqlValueWithSQuote(fileInfo.getFileName()) + " " +
                		"AND FILE_DATE = " + StringUtil.toSqlValueWithSQuote(batchDate);
        
        logger.debug(seqnoSql);
        
        try {
        	stmt = connection.createStatement();
            rs = stmt.executeQuery(seqnoSql);
            while (rs.next()) {
            	seqno = rs.getString(1);
             }
        } catch (SQLException ignore) {
        	logger.warn("SQLException:" + ignore.getMessage(), ignore);
        } finally {
        	ReleaseResource.releaseDB(null, stmt, rs);
        }
        
        /*OTA_P3
		??????(1) + ??????(8) + YYYYMMDDNN
        00000000 ?????????
        MMMMMMMM ?????????*/
        fullFileName = "OTA_P3." + persoInfo.getTelecomCode() + "." + bankMemId + "." + batchDate + seqno;
        filePath = FilenameUtils.separatorsToSystem(BatchUtil.getWorkDirectory() + LocalPath + "/");
        new File(filePath).mkdirs();
        new File(filePath + fullFileName).delete();
        
        this.file = filePath + fullFileName;
        
        bw = new BufferedOutputStream(new FileOutputStream(new File(file)));
        
        insertOutControl(connection, batchDate, persoInfo.getMemId(), null, seqno ,persoInfo.getPersoQty().intValue());
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
    		convDataInfo("????????????", "H0", true, false);
    	}
    	   	
    	bw.write(batchDate.getBytes());
    	if(logger.isDebugEnabled()) {
    		convDataInfo("????????????", batchDate, true, false);
    	}
    	
       	bw.write(StringUtils.paddingLeftString(persoInfo.getPersoQty().toString(), '0', 8).getBytes());
       	if(logger.isDebugEnabled()) {
       		convDataInfo("????????????", StringUtils.paddingLeftString(persoInfo.getPersoQty().toString(), '0', 8), true, false);
       	}
       	
    	bw.write(persoInfo.getPersoBatchNo().getBytes());
    	if(logger.isDebugEnabled()) {
    		convDataInfo("????????????", persoInfo.getPersoBatchNo(), true, false);
    	}
    	
    	bw.write(StringUtils.paddingLeftString(persoInfo.getLayoutId(), '0', 4).getBytes());
    	if(logger.isDebugEnabled()) {
    		convDataInfo("??????????????????", StringUtils.paddingLeftString(persoInfo.getLayoutId(), '0', 4), true, false);
    	}
    	
    	bw.write(StringUtils.paddingLeftString(persoInfo.getCardFactoryId(),'0', 2).getBytes());
    	if(logger.isDebugEnabled()) {
    		convDataInfo("????????????(????????????)", StringUtils.paddingLeftString(persoInfo.getCardFactoryId(),'0', 2), true, false);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString(persoInfo.getCardCatId(),'0',2)));
    	if(logger.isDebugEnabled()) {
    		convDataInfo("??????", StringUtils.paddingLeftString(persoInfo.getCardCatId(),'0',2), false, false);
    	}
    	
    	bw.write(StringUtils.paddingLeftString(persoInfo.getPurchaseOrderNo(), ' ',10).getBytes());
    	if(logger.isDebugEnabled()) {
    		convDataInfo("????????????", StringUtils.paddingLeftString(persoInfo.getPurchaseOrderNo(), ' ',10), true, false);
    	}
    	
    	bw.write(StringUtils.paddingRightString("", ' ', 9).getBytes());
    	if(logger.isDebugEnabled()) {
    		convDataInfo("??????", StringUtils.paddingRightString("", ' ', 9), true, false);
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
    		convDataInfo("????????????", "DT", true, false);
    	}
    	
    	bw.write(convertHexToString(cardNumber));
    	if(logger.isDebugEnabled()) {
    		convDataInfo("??????????????????(PID)", cardNumber, false, false);
    	}
    	
    	bw.write(StringUtils.paddingLeftString(persoInfo.getBalMaxAmt().toString(), '0', 8).getBytes());
    	if(logger.isDebugEnabled()) {
    		convDataInfo("???????????? (BIL)", StringUtils.paddingLeftString(persoInfo.getBalMaxAmt().toString(), '0', 8), true, false);
    	}
    	
    	if(persoInfo.getMinBalAmt().intValue()<0) {
    		Number natuNumber = 0 - persoInfo.getMinBalAmt().intValue();
    		String minBalAmt = StringUtils.paddingLeftString(natuNumber.toString(), '0', 5).replaceFirst("0", "-");
    		bw.write(minBalAmt.getBytes());
    		if(logger.isDebugEnabled()) {
	    		convDataInfo("???????????? (BLL)", minBalAmt, true, false);
	    	}
		}
		else {
			bw.write(StringUtils.paddingLeftString(persoInfo.getMinBalAmt().toString(), '0', 5).getBytes());
	    	if(logger.isDebugEnabled()) {
	    		convDataInfo("???????????? (BLL)", StringUtils.paddingLeftString(persoInfo.getMinBalAmt().toString(), '0', 5), true, false);
	    	}
		}

    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'0',8)));
    	if(logger.isDebugEnabled()) {
    		convDataInfo("???????????????", StringUtils.paddingLeftString("",'0',8), false, true);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'0',2)));
    	//bw.write(StringUtils.paddingRightString("",' ',1).getBytes());
    	if(logger.isDebugEnabled()) {
    		convDataInfo("????????????", StringUtils.paddingLeftString("",'0',2), false, true);
    		//convDataInfo("????????????", StringUtils.paddingRightString("",' ',1), true, false);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'0',16)));
    	if(logger.isDebugEnabled()) {
    		convDataInfo("??????????????????????????????", StringUtils.paddingLeftString("",'0',16), false, true);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'0',2)));
    	if(logger.isDebugEnabled()) {
    		convDataInfo("??????????????????????????????", StringUtils.paddingLeftString("",'0',2), false, true);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString(persoInfo.getOfflineAutoloadCnt().toString(),'0',2)));
    	if(logger.isDebugEnabled()) {
    		convDataInfo("????????????????????????????????????", StringUtils.paddingLeftString(persoInfo.getOfflineAutoloadCnt().toString(),'0',2), false, true);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'0',2)));
    	if(logger.isDebugEnabled()) {
    		convDataInfo("??????????????????????????????", StringUtils.paddingLeftString("",'0',2), false, true);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'0',2)));
    	if(logger.isDebugEnabled()) {
    		convDataInfo("??????????????????????????????", StringUtils.paddingLeftString("",'0',2), false, true);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'F',222)));
    	if(logger.isDebugEnabled()) {
    		convDataInfo("??????", StringUtils.paddingLeftString("",'F',222), false, false);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'F',360)));
    	if(logger.isDebugEnabled()) {
    		convDataInfo("????????????", StringUtils.paddingLeftString("",'F',360), false, false);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'0',8)));
    	if(logger.isDebugEnabled()) {
    		convDataInfo("Mifare????????????", StringUtils.paddingLeftString("",'0',8), false, false);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'0',24)));
    	if(logger.isDebugEnabled()) {
    		convDataInfo("????????????Maufacturer code", StringUtils.paddingLeftString("",'0',24), false, false);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString(persoInfo.getFirstAidGroup(),'F',32)));
    	if(logger.isDebugEnabled()) {
    		convDataInfo("??????????????????(1)", StringUtils.paddingLeftString(persoInfo.getFirstAidGroup(),'F',32), false, false);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString(persoInfo.getSecondAidGroup(),'F',32)));
    	if(logger.isDebugEnabled()) {
    		convDataInfo("??????????????????(2)", StringUtils.paddingLeftString(persoInfo.getSecondAidGroup(),'F',32), false, false);
    	}
    	
    	bw.write(convertHexToString("0c"));
    	if(logger.isDebugEnabled()) {
    		convDataInfo("??????????????????", "0c", false, false);
    	}

    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'0',4)));
    	if(logger.isDebugEnabled()) {
    		convDataInfo("??????????????????", StringUtils.paddingLeftString("",'0',4), false, false);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString(NumberStr2HexString(persoInfo.getPersoBatchNo().substring(14,18)), '0', 4)));
    	if(logger.isDebugEnabled()) {
    		convDataInfo("????????????", persoInfo.getPersoBatchNo().substring(14,18), false, true);
    	}
    	
    	logger.info((DateUtils.getSystemDate()+"000000"));
    	bw.write(convertHexToString(String2HexString(String.valueOf(TimeConversion(DateUtils.getSystemDate()+"000000")))));
    	if(logger.isDebugEnabled()) {
    		convDataInfo("????????????", String.valueOf(TimeConversion(DateUtils.getSystemDate()+"000000")), false, true);
    	}
    	
    	bw.write(convertHexToString(String2HexString(String.valueOf(TimeConversion("20991231000000")))));
    	if(logger.isDebugEnabled()) {
    		convDataInfo("????????????", String.valueOf(TimeConversion("20991231000000")), false, true);
    	}
    	
    	bw.write(convertHexToString("01"));
    	if(logger.isDebugEnabled()) {
    		convDataInfo("??????????????????", "01", false, true);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString(persoInfo.getCardStatus(),'0',2)));
    	if(logger.isDebugEnabled()) {
    		convDataInfo("????????????", StringUtils.paddingLeftString(persoInfo.getCardStatus(),'0',2), false, true);
    	}
    	
    	String issCardUnitNo = ISOUtil.hexString(convertHexToString(StringUtils.paddingLeftString("0c",'0',2)));
    	
    	logger.info("??????????????????: " + ISOUtil.hexString(convertHexToString(StringUtils.paddingLeftString(issCardUnitNo,'0',2))));
    	logger.info("??????????????????: " + ISOUtil.hexString(convertHexToString(StringUtils.paddingLeftString("",'0',4))));
    	logger.info("????????????: " + ISOUtil.hexString(convertHexToString(StringUtils.paddingLeftString(NumberStr2HexString(persoInfo.getPersoBatchNo().substring(14,18)), '0', 4))));
    	logger.info("????????????: " + ISOUtil.hexString(convertHexToString(String2HexString(String.valueOf(TimeConversion(DateUtils.getSystemDate()+"000000"))))));
    	logger.info("????????????: " + ISOUtil.hexString(convertHexToString(String2HexString(String.valueOf(TimeConversion("20991231000000"))))));
    	logger.info("??????????????????: " + ISOUtil.hexString(convertHexToString("01")));
    	logger.info("????????????: " + ISOUtil.hexString(convertHexToString(StringUtils.paddingLeftString(persoInfo.getCardStatus(),'0',2))));

    	String checkCode1 =	issCardUnitNo +
							ISOUtil.hexString(convertHexToString(StringUtils.paddingLeftString("",'0',4))) + 
							ISOUtil.hexString(convertHexToString(StringUtils.paddingLeftString(NumberStr2HexString(persoInfo.getPersoBatchNo().substring(14,18)), '0', 4))) + 
							ISOUtil.hexString(convertHexToString(String2HexString(String.valueOf(TimeConversion(DateUtils.getSystemDate()+"000000"))))) +  
							ISOUtil.hexString(convertHexToString(String2HexString(String.valueOf(TimeConversion("20991231000000"))))) +
							ISOUtil.hexString(convertHexToString("01")) + 
							ISOUtil.hexString(convertHexToString(StringUtils.paddingLeftString(persoInfo.getCardStatus(),'0',2)));
    	
    	logger.info("Before XOR: [" + checkCode1 +"]");
    	logger.info("?????????: [" + XorData(checkCode1) + "]");
    	
    	byte[] tmpByte = new byte[1];
    	tmpByte[0] = XorData(checkCode1);
    	logger.info("?????????: [" + ISOUtil.hexString(tmpByte) + "]");
    	
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
    		convDataInfo("??????????????????", StringUtils.paddingLeftString(autoReloadFlag,'0',2), false, false);
    	}
    	
    	//bw.write(convertHexToString(StringUtils.paddingLeftString(persoInfo.getAutoReloadValue().toString(),'0',4)));
    	bw.write(convertHexToString(StringUtils.paddingLeftString(String2HexString(persoInfo.getAutoReloadValue().toString()),'0',4)));
    	if(logger.isDebugEnabled()) {
    		convDataInfo("????????????????????????", StringUtils.paddingLeftString(persoInfo.getAutoReloadValue().toString(),'0',4), false, true);
    	}
    	
    	bw.write(convertHexToString(String2HexString(persoInfo.getCardBalLimit().toString())));
    	if(logger.isDebugEnabled()) {
    		convDataInfo("????????????????????????(????????????)", persoInfo.getCardBalLimit().toString(), false, false);
    	}
    	
    	bw.write(convertHexToString(String2HexString(persoInfo.getMaxConsumeAmt().toString())));
    	if(logger.isDebugEnabled()) {
    		convDataInfo("?????????????????????????????????", persoInfo.getMaxConsumeAmt().toString(), false, false);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'F',2)));
    	if(logger.isDebugEnabled()) {
    		convDataInfo("??????(??????????????????)", StringUtils.paddingLeftString("",'F',2), false, false);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'F',4)));
    	if(logger.isDebugEnabled()) {
    		convDataInfo("??????(????????????????????????)", StringUtils.paddingLeftString("",'F',4), false, false);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'0',4)));
    	if(logger.isDebugEnabled()) {
    		convDataInfo("??????????????????", StringUtils.paddingLeftString("",'0',4), false, false);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString(persoInfo.getOfflineAutoloadCnt().toString(),'0',2)));
    	if(logger.isDebugEnabled()) {
    		convDataInfo("????????????????????????????????????", StringUtils.paddingLeftString(persoInfo.getOfflineAutoloadCnt().toString(),'0',2), false, false);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'0',2)));
    	if(logger.isDebugEnabled()) {
    		convDataInfo("??????????????????????????????", StringUtils.paddingLeftString("",'0',2), false, false);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'0',2)));
    	if(logger.isDebugEnabled()) {
    		convDataInfo("??????????????????????????????", StringUtils.paddingLeftString("",'0',2), false, false);
    	}
    	
    	logger.info("??????????????????: " + ISOUtil.hexString(convertHexToString(StringUtils.paddingLeftString(autoReloadFlag,'0',2))));
    	logger.info("????????????????????????: " + ISOUtil.hexString(convertHexToString(StringUtils.paddingLeftString(persoInfo.getAutoReloadValue().toString(),'0',4))));
    	logger.info("????????????????????????(????????????): " + ISOUtil.hexString(convertHexToString(String2HexString(persoInfo.getCardBalLimit().toString()))));
    	logger.info("?????????????????????????????????: " + ISOUtil.hexString(convertHexToString(persoInfo.getMaxConsumeAmt().toString())));
    	logger.info("??????(??????????????????): " + ISOUtil.hexString(convertHexToString(StringUtils.paddingLeftString("",'F',2))));
    	logger.info("??????(????????????????????????): " + ISOUtil.hexString(convertHexToString(StringUtils.paddingLeftString("",'F',4))));
    	logger.info("??????????????????: " + ISOUtil.hexString(convertHexToString(StringUtils.paddingLeftString("",'0',4))));
    	logger.info("????????????????????????????????????: " + ISOUtil.hexString(convertHexToString(StringUtils.paddingLeftString(persoInfo.getOfflineAutoloadCnt().toString(),'0',2))));
    	logger.info("??????????????????????????????: " + ISOUtil.hexString(convertHexToString(StringUtils.paddingLeftString("",'0',2))));
    	logger.info("??????????????????????????????: " + ISOUtil.hexString(convertHexToString(StringUtils.paddingLeftString("",'0',2))));
    	
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
    	logger.info("?????????: [" + XorData(checkCode2) + "]");
    	bw.write(XorData(checkCode2));
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'0',32)));
    	if(logger.isDebugEnabled()) {
    		convDataInfo("??????????????????", StringUtils.paddingLeftString("",'0',32), false, false);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString(persoInfo.getPreloadAmt().toString(), '0', 32)));
    	if(logger.isDebugEnabled()) {
    		convDataInfo("????????????", StringUtils.paddingLeftString(persoInfo.getPreloadAmt().toString(), '0', 32), false, false);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'0',32)));
    	if(logger.isDebugEnabled()) {
    		convDataInfo("????????????", StringUtils.paddingLeftString("",'0',32), false, false);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'0',32)));
    	if(logger.isDebugEnabled()) {
    		convDataInfo("??????????????????", StringUtils.paddingLeftString("",'0',32), false, false);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'0',32)));
    	if(logger.isDebugEnabled()) {
    		convDataInfo("????????????????????????", StringUtils.paddingLeftString("",'0',32), false, false);
    	}
    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'0',32)));
    	if(logger.isDebugEnabled()) {
    		convDataInfo("??????????????????????????????(1)", StringUtils.paddingLeftString("",'0',32), false, false);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'0',32)));
    	if(logger.isDebugEnabled()) {
    		convDataInfo("??????????????????????????????(2)", StringUtils.paddingLeftString("",'0',32), false, false);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'0',32)));
    	if(logger.isDebugEnabled()) {
    		convDataInfo("????????????????????????(1)", StringUtils.paddingLeftString("",'0',32), false, false);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'0',32)));
    	if(logger.isDebugEnabled()) {
    		convDataInfo("????????????????????????(2)", StringUtils.paddingLeftString("",'0',32), false, false);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'0',32)));
    	if(logger.isDebugEnabled()) {
    		convDataInfo("????????????????????????(3)", StringUtils.paddingLeftString("",'0',32), false, false);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'0',32)));
    	if(logger.isDebugEnabled()) {
    		convDataInfo("????????????????????????(4)", StringUtils.paddingLeftString("",'0',32), false, false);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'0',32)));
    	if(logger.isDebugEnabled()) {
    		convDataInfo("????????????????????????(5)", StringUtils.paddingLeftString("",'0',32), false, false);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'0',32)));
    	if(logger.isDebugEnabled()) {
    		convDataInfo("????????????????????????(6)", StringUtils.paddingLeftString("",'0',32), false, false);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString(persoInfo.getCardTypeId(),'0',2)));
    	if(logger.isDebugEnabled()) {
    		convDataInfo("???????????????(??????)", StringUtils.paddingLeftString(persoInfo.getCardTypeId(),'0',2), false, false);
    	}
    	    	
    	bw.write(convertHexToString(String2HexString(String.valueOf(TimeConversion("20991231000000")))));
    	if(logger.isDebugEnabled()) {
    		convDataInfo("?????????????????????", String2HexString(String.valueOf(TimeConversion("20991231000000"))), false, false);
    	}
    	   	
    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'0',12)));
    	if(logger.isDebugEnabled()) {
    		convDataInfo("???????????????User id", StringUtils.paddingLeftString("",'0',12), false, false);
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
    		convDataInfo("??????????????????", StringUtils.paddingLeftString(ptaUnitNo,'0',2), false, false);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'F',4)));
    	if(logger.isDebugEnabled()) {
    		convDataInfo("??????", StringUtils.paddingLeftString("",'F',4), false, false);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString(NumberStr2HexString(persoInfo.getCardFee().toString()), '0', 4)));
    	if(logger.isDebugEnabled()) {
    		convDataInfo("????????????", persoInfo.getCardFee().toString(), false, true);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString(persoInfo.getCardCatId(),'0',2)));
    	if(logger.isDebugEnabled()) {
    		convDataInfo("??????", StringUtils.paddingLeftString(persoInfo.getCardCatId(),'0',2), false, false);
    	}
    	
    	bw.write(convertHexToString("00"));
    	if(logger.isDebugEnabled()) {
    		convDataInfo("????????????", "00", false, false);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'0',4)));
    	if(logger.isDebugEnabled()) {
    		convDataInfo("??????MMDD", StringUtils.paddingLeftString("",'0',4), false, true);
    	}
    	
    	bw.write(convertHexToString(String2HexString(persoInfo.getCardProduct().substring(2, 5).toString())));
    	if(logger.isDebugEnabled()) {
    		convDataInfo("?????????", persoInfo.getCardProduct().substring(2, 5).toString(), false, true);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'F',22)));
    	if(logger.isDebugEnabled()) {
    		convDataInfo("??????", StringUtils.paddingLeftString("",'F',22), false, false);
    	}
    	
    	bw.write(convertHexToString(cardNumber));
    	if(logger.isDebugEnabled()) {
    		convDataInfo("??????????????????(PID)", cardNumber, false, false);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'F',16)));	
    	if(logger.isDebugEnabled()) {
    		convDataInfo("??????", StringUtils.paddingLeftString("",'F',16), false, false);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'0',32)));	
    	if(logger.isDebugEnabled()) {
    		convDataInfo("??????-??????", StringUtils.paddingLeftString("",'0',32), false, false);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'0',32)));	
    	if(logger.isDebugEnabled()) {
    		convDataInfo("??????-??????", StringUtils.paddingLeftString("",'0',32), false, false);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'0',32)));	
    	if(logger.isDebugEnabled()) {
    		convDataInfo("??????-??????", StringUtils.paddingLeftString("",'0',32), false, false);
    	}

    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'0',32)));	
    	if(logger.isDebugEnabled()) {
    		convDataInfo("??????-??????", StringUtils.paddingLeftString("",'0',32), false, false);
    	}

    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'0',32)));	
    	if(logger.isDebugEnabled()) {
    		convDataInfo("??????-??????", StringUtils.paddingLeftString("",'0',32), false, false);
    	}

    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'0',32)));	
    	if(logger.isDebugEnabled()) {
    		convDataInfo("??????-??????", StringUtils.paddingLeftString("",'0',32), false, false);
    	}

    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'0',32)));	
    	if(logger.isDebugEnabled()) {
    		convDataInfo("??????-????????????", StringUtils.paddingLeftString("",'0',32), false, false);
    	}

    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'0',32)));	
    	if(logger.isDebugEnabled()) {
    		convDataInfo("??????-????????????", StringUtils.paddingLeftString("",'0',32), false, false);
    	}

    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'0',32)));	
    	if(logger.isDebugEnabled()) {
    		convDataInfo("??????-????????????", StringUtils.paddingLeftString("",'0',32), false, false);
    	}

    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'0',32)));	
    	if(logger.isDebugEnabled()) {
    		convDataInfo("??????-???????????????", StringUtils.paddingLeftString("",'0',32), false, false);
    	}

    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'0',32)));	
    	if(logger.isDebugEnabled()) {
    		convDataInfo("??????-???????????????", StringUtils.paddingLeftString("",'0',32), false, false);
    	}

    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'0',32)));	
    	if(logger.isDebugEnabled()) {
    		convDataInfo("??????-???????????????", StringUtils.paddingLeftString("",'0',32), false, false);
    	}

    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'0',32)));	
    	if(logger.isDebugEnabled()) {
    		convDataInfo("??????-????????????", StringUtils.paddingLeftString("",'0',32), false, false);
    	}

    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'0',32)));	
    	if(logger.isDebugEnabled()) {
    		convDataInfo("??????-????????????", StringUtils.paddingLeftString("",'0',32), false, false);
    	}

    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'0',32)));	
    	if(logger.isDebugEnabled()) {
    		convDataInfo("??????-????????????", StringUtils.paddingLeftString("",'F',32), false, false);
    	}

    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'0',32)));	
    	if(logger.isDebugEnabled()) {
    		convDataInfo("??????-?????????", StringUtils.paddingLeftString("",'0',32), false, false);
    	}

    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'0',32)));	
    	if(logger.isDebugEnabled()) {
    		convDataInfo("??????-?????????", StringUtils.paddingLeftString("",'0',32), false, false);
    	}

    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'0',32)));	
    	if(logger.isDebugEnabled()) {
    		convDataInfo("??????-?????????", StringUtils.paddingLeftString("",'0',32), false, false);
    	}

    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'0',32)));	
    	if(logger.isDebugEnabled()) {
    		convDataInfo("??????-????????????", StringUtils.paddingLeftString("",'0',32), false, false);
    	}

    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'0',32)));	
    	if(logger.isDebugEnabled()) {
    		convDataInfo("??????-????????????", StringUtils.paddingLeftString("",'0',32), false, false);
    	}

    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'0',32)));	
    	if(logger.isDebugEnabled()) {
    		convDataInfo("??????-????????????", StringUtils.paddingLeftString("",'0',32), false, false);
    	}

    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'0',32)));	
    	if(logger.isDebugEnabled()) {
    		convDataInfo("??????-????????????", StringUtils.paddingLeftString("",'0',32), false, false);
    	}

    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'0',32)));	
    	if(logger.isDebugEnabled()) {
    		convDataInfo("??????-????????????", StringUtils.paddingLeftString("",'0',32), false, false);
    	}

    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'0',32)));	
    	if(logger.isDebugEnabled()) {
    		convDataInfo("??????-????????????", StringUtils.paddingLeftString("",'0',32), false, false);
    	}

    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'0',32)));	
    	if(logger.isDebugEnabled()) {
    		convDataInfo("??????-????????????", StringUtils.paddingLeftString("",'0',32), false, false);
    	}

    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'0',32)));	
    	if(logger.isDebugEnabled()) {
    		convDataInfo("??????-????????????", StringUtils.paddingLeftString("",'0',32), false, false);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'0',32)));	
    	if(logger.isDebugEnabled()) {
    		convDataInfo("??????-????????????", StringUtils.paddingLeftString("",'0',32), false, false);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'0',256)));	
    	if(logger.isDebugEnabled()) {
    		convDataInfo("????????????", StringUtils.paddingLeftString("",'0',256), false, false);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'F',360)));	
    	if(logger.isDebugEnabled()) {
    		convDataInfo("????????????", StringUtils.paddingLeftString("",'F',360), false, false);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'0',256)));	
    	if(logger.isDebugEnabled()) {
    		convDataInfo("????????????", StringUtils.paddingLeftString("",'0',256), false, false);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'F',360)));	
    	if(logger.isDebugEnabled()) {
    		convDataInfo("????????????", StringUtils.paddingLeftString("",'F',360), false, false);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'0',256)));	
    	if(logger.isDebugEnabled()) {
    		convDataInfo("????????????", StringUtils.paddingLeftString("",'0',256), false, false);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'F',360)));	
    	if(logger.isDebugEnabled()) {
    		convDataInfo("????????????", StringUtils.paddingLeftString("",'F',360), false, false);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'0',256)));	
    	if(logger.isDebugEnabled()) {
    		convDataInfo("????????????", StringUtils.paddingLeftString("",'0',256), false, false);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'F',360)));	
    	if(logger.isDebugEnabled()) {
    		convDataInfo("????????????", StringUtils.paddingLeftString("",'F',360), false, false);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'0',256)));	
    	if(logger.isDebugEnabled()) {
    		convDataInfo("????????????", StringUtils.paddingLeftString("",'0',256), false, false);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'F',360)));	
    	if(logger.isDebugEnabled()) {
    		convDataInfo("????????????", StringUtils.paddingLeftString("",'F',360), false, false);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'0',256)));	
    	if(logger.isDebugEnabled()) {
    		convDataInfo("????????????", StringUtils.paddingLeftString("",'0',256), false, false);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'F',360)));	
    	if(logger.isDebugEnabled()) {
    		convDataInfo("????????????", StringUtils.paddingLeftString("",'F',360), false, false);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'0',256)));	
    	if(logger.isDebugEnabled()) {
    		convDataInfo("????????????", StringUtils.paddingLeftString("",'0',256), false, false);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'F',360)));	
    	if(logger.isDebugEnabled()) {
    		convDataInfo("????????????", StringUtils.paddingLeftString("",'F',360), false, false);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'0',256)));	
    	if(logger.isDebugEnabled()) {
    		convDataInfo("????????????", StringUtils.paddingLeftString("",'0',256), false, false);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'F',360)));	
    	if(logger.isDebugEnabled()) {
    		convDataInfo("????????????", StringUtils.paddingLeftString("",'F',360), false, false);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'0',256)));	
    	if(logger.isDebugEnabled()) {
    		convDataInfo("????????????", StringUtils.paddingLeftString("",'0',256), false, false);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'F',360)));	
    	if(logger.isDebugEnabled()) {
    		convDataInfo("????????????", StringUtils.paddingLeftString("",'F',360), false, false);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'0',256)));	
    	if(logger.isDebugEnabled()) {
    		convDataInfo("????????????", StringUtils.paddingLeftString("",'0',256), false, false);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'F',360)));	
    	if(logger.isDebugEnabled()) {
    		convDataInfo("????????????", StringUtils.paddingLeftString("",'F',360), false, false);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'0',256)));	
    	if(logger.isDebugEnabled()) {
    		convDataInfo("????????????", StringUtils.paddingLeftString("",'0',256), false, false);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'F',360)));	
    	if(logger.isDebugEnabled()) {
    		convDataInfo("????????????", StringUtils.paddingLeftString("",'F',360), false, false);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'0',256)));	
    	if(logger.isDebugEnabled()) {
    		convDataInfo("????????????", StringUtils.paddingLeftString("",'0',256), false, false);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'F',360)));	
    	if(logger.isDebugEnabled()) {
    		convDataInfo("????????????", StringUtils.paddingLeftString("",'F',360), false, false);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'0',256)));	
    	if(logger.isDebugEnabled()) {
    		convDataInfo("????????????", StringUtils.paddingLeftString("",'0',256), false, false);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'F',360)));	
    	if(logger.isDebugEnabled()) {
    		convDataInfo("????????????", StringUtils.paddingLeftString("",'F',360), false, false);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'0',256)));	
    	if(logger.isDebugEnabled()) {
    		convDataInfo("????????????", StringUtils.paddingLeftString("",'0',256), false, false);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'F',360)));	
    	if(logger.isDebugEnabled()) {
    		convDataInfo("????????????", StringUtils.paddingLeftString("",'F',360), false, false);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'0',256)));	
    	if(logger.isDebugEnabled()) {
    		convDataInfo("????????????", StringUtils.paddingLeftString("",'0',256), false, false);
    	}
    	
    	bw.write(convertHexToString(StringUtils.paddingLeftString("",'F',360)));	
    	if(logger.isDebugEnabled()) {
    		convDataInfo("????????????", StringUtils.paddingLeftString("",'F',360), false, false);
    	}
    	
    	//20170620 ????????????HgCardGroupId????????????
    	if(!StringUtil.isEmpty(persoInfo.getHgCardGroupId()))  {
    		//bw.write(convertHexToString(StringUtils.paddingRightString(hgCardMapInfo.getBarcode1(),'0',32)));
    		bw.write(StringUtils.paddingRightString(hgCardMapInfo.getBarcode1(),' ',16).getBytes());
    		if(logger.isDebugEnabled()) {
    			//convDataInfo("HG????????????", StringUtils.paddingRightString(hgCardMapInfo.getBarcode1(),'0',32), false, false);
    			convDataInfo("HG????????????", StringUtils.paddingRightString(hgCardMapInfo.getBarcode1(),' ',16), true, false);
    		}
    		
    		//bw.write(convertHexToString(StringUtils.paddingRightString(hgCardMapInfo.getBarcode2(),'0',74)));
    		bw.write(StringUtils.paddingRightString(hgCardMapInfo.getBarcode2(),' ',37).getBytes());
    		if(logger.isDebugEnabled()) {
    			//convDataInfo("HG????????????", StringUtils.paddingRightString(hgCardMapInfo.getBarcode2(),'0',74), false, false);
    			convDataInfo("HG????????????", StringUtils.paddingRightString(hgCardMapInfo.getBarcode2(),' ',37), true, false);
    		}
    		
    		//bw.write(convertHexToString(StringUtils.paddingRightString(hgCardMapInfo.getBarcode1().substring(3, 16),'0',26)));
    		bw.write(StringUtils.paddingRightString(hgCardMapInfo.getBarcode1().substring(3, 16),' ',13).getBytes());
    		if(logger.isDebugEnabled()) {
    			//convDataInfo("HG??????", StringUtils.paddingRightString(hgCardMapInfo.getBarcode1().substring(3, 16),'0',26), false, false);
    			convDataInfo("HG??????", StringUtils.paddingRightString(hgCardMapInfo.getBarcode1().substring(3, 16),' ',13), true, false);
    		}
    	}else {   		
    		//bw.write(convertHexToString(StringUtils.paddingRightString("",'0',32)));
    		bw.write(StringUtils.paddingRightString("",' ',16).getBytes());
    		if(logger.isDebugEnabled()) {
    			//convDataInfo("HG????????????", StringUtils.paddingRightString("",'0',32), false, false);
    			convDataInfo("HG????????????", StringUtils.paddingRightString("",' ',16), true, false);
    		}
    		
    		//bw.write(convertHexToString(StringUtils.paddingRightString("",'0',74)));
    		bw.write(StringUtils.paddingRightString("",' ',37).getBytes());
    		if(logger.isDebugEnabled()) {
    			//convDataInfo("HG????????????", StringUtils.paddingRightString("",'0',74), false, false);
    			convDataInfo("HG????????????", StringUtils.paddingRightString("",' ',37), true, false);
    		}
    		
    		//bw.write(convertHexToString(StringUtils.paddingRightString("",'0',26)));
    		bw.write(StringUtils.paddingRightString("",' ',13).getBytes());
    		if(logger.isDebugEnabled()) {
    			//convDataInfo("HG??????", StringUtils.paddingRightString("",'0',26), false, false);
    			convDataInfo("HG??????", StringUtils.paddingRightString("",' ',13), true, false);
    		}
    	}

    	bw.write(StringUtils.paddingLeftString(persoInfo.getCardMaterialNo(), ' ', 18).getBytes());
    	if(logger.isDebugEnabled()) {
    		convDataInfo("??????????????????", StringUtils.paddingLeftString(persoInfo.getCardMaterialNo(), ' ', 18), true, false);
    	}	
    	
    	bw.write(StringUtils.paddingRightString("", ' ', 3).getBytes());
    	if(logger.isDebugEnabled()) {
    		convDataInfo("??????", StringUtils.paddingRightString("", ' ', 3), true, false);
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
        outControl.setProgramName("ExpPersoOTA");
        outControl.setStartDate(DateUtils.getSystemDate());
        outControl.setStartTime(DateUtils.getSystemTime());
        outControl.setParMon(batchDate.substring(4, 6));
        outControl.setParDay(batchDate.substring(6));

        new TbOutctlMgr(connection).insert(outControl);
    }
    
	public void updateOutControl(Connection connection) throws Exception {
		String sqlStr = "update TB_OUTCTL set FILE_SIZE= ? WHERE FULL_FILE_NAME = ?";
		executeUpdate(connection, sqlStr, new File(filePath + fullFileName).length() , fullFileName);
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
}
