package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpJcicCust;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;

import oracle.net.aso.i;
import tw.com.hyweb.core.cp.batch.util.DateUtils;
import tw.com.hyweb.util.date.DateUtil;
import tw.com.hyweb.service.db.info.TbCustInfo;
import tw.com.hyweb.service.db.info.TbCustUptInfo;
import tw.com.hyweb.service.db.mgr.TbCustMgr;
import tw.com.hyweb.util.BatchUtils;
import tw.com.hyweb.util.DbUtil;
import tw.com.hyweb.util.ReleaseResource;

public class JcicCustData
{
    private static Logger log = Logger.getLogger(JcicCustData.class);

    private final Map<String, String> fileData;
    private final String sysDate = DateUtils.getSystemDate();
    private final String sysTime = DateUtils.getSystemTime();
    private Vector<TbCustInfo> custResult = new Vector<TbCustInfo>();
    private String personId;
    private int custCount;
    private TbCustInfo custInfo;

       
    public JcicCustData(Connection connection, Map<String, String> fileData) throws Exception
    {
    	this.fileData = fileData;
    	this.personId=BatchUtils.encript((String)fileData.get("PERSON_ID"));
    	this.custCount = getCustInfo(connection , personId);

    }
    
    private int getCustInfo(Connection connection, String personId) throws SQLException
    {
         int custCount = DbUtil.getInteger("SELECT 1 FROM TB_CUST where PERSON_ID ='"+personId+"' ", connection);
        return custCount;
    }

	public Map<String, String> getFileData() {
		return fileData;
	}

	public int getCustInfoCount() {
		return custCount;
	}



    public List handleCust(Connection connection, String batchDate, String fileDate, String fullfilename) throws Exception {
    	List sqls = new ArrayList();  	

    	String where = " person_Id = '" + personId + "'";
		Vector<TbCustInfo> result = new Vector<TbCustInfo>();
		new TbCustMgr(connection).queryMultiple(where, result);

		for (int i = 0; i < result.size(); i++) {
			custInfo = (TbCustInfo) result.get(i);
			sqls.add(UpdateCustSQL(batchDate,fullfilename));
			sqls.add(makeInsertCustUptSql(fullfilename));
		}
        return sqls;
    }   
    
    private String makeInsertCustUptSql(String fullfilename) {

		TbCustUptInfo custUptInfo = new TbCustUptInfo();
		custUptInfo.setCustId(custInfo.getCustId());	
		custUptInfo.setRegionId(custInfo.getRegionId());
		custUptInfo.setCustLevel(custInfo.getCustLevel());
		custUptInfo.setPersonId(personId);
		custUptInfo.setLocName(custInfo.getLocName());
		custUptInfo.setEngName(custInfo.getEngName());
		custUptInfo.setMarriage(custInfo.getMarriage());
		custUptInfo.setAddress(custInfo.getAddress());
		custUptInfo.setBirthday(custInfo.getBirthday());
		custUptInfo.setContact(custInfo.getContact());
		custUptInfo.setTelHome(custInfo.getTelHome());
		custUptInfo.setTelOffice(custInfo.getTelOffice());
		custUptInfo.setMobile(custInfo.getMobile());
		custUptInfo.setZipCode(custInfo.getZipCode());
		custUptInfo.setEducation(custInfo.getEducation());
		custUptInfo.setEmail(custInfo.getEmail());
		custUptInfo.setGender(custInfo.getGender());
		custUptInfo.setIndustry(custInfo.getIndustry());
		custUptInfo.setOccupation(custInfo.getOccupation());
		custUptInfo.setAnnualIncome(custInfo.getAnnualIncome());
		custUptInfo.setVerifyDate(fileData.get("VERIFY_DATE").toString());
		custUptInfo.setVerifyCode(fileData.get("VERIFY_CODE").toString());
		custUptInfo.setVerifyCodeDesc(fileData.get("VERIFY_CODE_DESC").toString());
		custUptInfo.setImpFileName(fullfilename);
		custUptInfo.setAprvStatus("1");
		custUptInfo.setAprvDate(sysDate);
		custUptInfo.setAprvTime(sysTime);
		custUptInfo.setAprvUserid("BATCH");
		custUptInfo.setUptDate(sysDate);
		custUptInfo.setUptTime(sysTime);
		custUptInfo.setUptUserid("BATCH");
		custUptInfo.setUptStatus("2");
		return custUptInfo.toInsertSQL();

	}
    private String UpdateCustSQL(String batchDate, String fullfilename) throws SQLException {
		
		String sql = "UPDATE TB_CUST set VERIFY_DATE='"+fileData.get("VERIFY_DATE")+"'"
				+ ",VERIFY_CODE='"+fileData.get("VERIFY_CODE")+"'"
				+ ",VERIFY_CODE_DESC='"+fileData.get("VERIFY_CODE_DESC")+"'"
				+ ",Upt_Date='"+batchDate+"'"
				+ ",Upt_Time='"+DateUtil.getTodayString().substring(8,14)+"'"
				+ ",Upt_Userid='BATCH'"
				+ ",Aprv_Date='"+batchDate+"'"
				+ ",aprv_time='"+DateUtil.getTodayString().substring(8,14)+"'"
				+ ",aprv_userId='BATCH' "
				+ ", imp_file_name = '"+fullfilename+"'"
				+ "where person_id = '"+personId+"'";

	    	
    	return sql;
	}      
}
