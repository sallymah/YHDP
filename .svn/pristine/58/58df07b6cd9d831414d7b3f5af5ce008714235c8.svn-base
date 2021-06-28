package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpTerm;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;

import tw.com.hyweb.core.cp.batch.util.DateUtils;
import tw.com.hyweb.service.db.info.TbMerchInfo;
import tw.com.hyweb.service.db.info.TbTermInfo;
import tw.com.hyweb.service.db.info.TbTermUptInfo;
import tw.com.hyweb.service.db.mgr.TbMerchMgr;
import tw.com.hyweb.util.ReleaseResource;

public class TermData
{
    private static Logger log = Logger.getLogger(TermData.class);

    private final Map<String, String> fileData;
    private final String sysDate = DateUtils.getSystemDate();
    private final String sysTime = DateUtils.getSystemTime();
    private Vector<TbMerchInfo> merchResult = new Vector<TbMerchInfo>();
    private String memId;
    private String merchId;
    private String termId;
    private int merchCount;

       
    public TermData(Connection connection, Map<String, String> fileData) throws SQLException
    {
    	this.fileData = fileData;
    	this.merchCount = getMerchInfo(connection , (String)fileData.get("MEM_ID"), (String)fileData.get("MERCH_LOC_NAME"));
    	if (getMerchInfoCount()!=0){
	    	this.memId = getMerchInfo().getMemId();
	    	this.merchId = getMerchInfo().getMerchId();
	    	this.termId = getTermId(connection);
    	}
    }
    
    private int getMerchInfo(Connection connection, String memId, String merchLocName) throws SQLException
    {
        TbMerchInfo info = new TbMerchInfo();
        info.setMemId(memId);
        info.setMerchLocName(merchLocName);

        return new TbMerchMgr(connection).queryMultiple(info, merchResult);
    }
    
    private String getTermId( Connection connection ) throws SQLException
    {
    	Statement stmt = null;
        ResultSet rs = null;
        String termId = "00000001";
        String seqnoSql = "SELECT LPAD(NVL(TO_CHAR(TO_NUMBER(MAX(TERM_ID)) + 1), '00000001'), 8, '0') FROM "+
        				"(SELECT TERM_ID FROM TB_TERM UNION SELECT TERM_ID FROM TB_TERM_UPT)";
        try {
        	stmt = connection.createStatement();
              log.warn("seqnoSql: "+seqnoSql);
              rs = stmt.executeQuery(seqnoSql);
              while (rs.next()) {
            	  termId = rs.getString(1);
            	  log.info("termId: "+termId);
             }
        }
        finally {
              ReleaseResource.releaseDB(null, stmt, rs);
        }
    	return termId;
    }

	public Map<String, String> getFileData() {
		return fileData;
	}

	public int getMerchInfoCount() {
		return merchCount;
	}
	
	public TbMerchInfo getMerchInfo() {
		return merchResult.get(0);
	}
	
	public String getTermId() {
		return termId;
	}

    public List handleCust(Connection connection, String batchDate, String fileDate) throws Exception {
    	
    	List sqls = new ArrayList();
    	
    	TbTermInfo insertTermSQL = insertTermInfo(batchDate);
    	sqls.add(insertTermSQL.toInsertSQL());
    	
    	TbTermUptInfo insertTermUptSQL = insertTermUptInfo(fileDate);    	
    	sqls.add(insertTermUptSQL.toInsertSQL());
        
        return sqls;
    }   
    
    private TbTermInfo insertTermInfo(String batchDate) {
    	TbTermInfo termInfo = new TbTermInfo();
    	termInfo.setMemId(memId);
    	termInfo.setMerchId(merchId);
    	termInfo.setTermId(termId);
    	termInfo.setStoreCounterId((String)fileData.get("STORE_COUNTER_ID"));
    	termInfo.setEcrId((String)fileData.get("ECR_ID"));
    	termInfo.setStatus((String)fileData.get("STATUS"));
    	termInfo.setEffectiveDate((String)fileData.get("EFFECTIVE_DATE"));
    	termInfo.setTerminationDate((String)fileData.get("TERMINATION_DATE"));
    	termInfo.setTermVendor((String)fileData.get("TERM_VENDOR"));
    	termInfo.setTermType((String)fileData.get("TERM_TYPE"));
    	termInfo.setFunc((String)fileData.get("FUNC"));
    	termInfo.setUptUserid("BATCH");
    	termInfo.setUptDate(sysDate);
    	termInfo.setUptTime(sysTime);
    	termInfo.setAprvUserid("BATCH");
    	termInfo.setAprvDate(termInfo.getUptDate());
    	termInfo.setAprvTime(termInfo.getUptTime());
    	
    	return termInfo;
    }
    
    private TbTermUptInfo insertTermUptInfo(String batchDate) {
    	TbTermUptInfo termUptInfo = new TbTermUptInfo();
    	
    	termUptInfo.setMemId(memId);
    	termUptInfo.setMerchId(merchId);
    	termUptInfo.setTermId(termId);
    	termUptInfo.setStoreCounterId((String)fileData.get("STORE_COUNTER_ID"));
    	termUptInfo.setEcrId((String)fileData.get("ECR_ID"));
    	termUptInfo.setStatus((String)fileData.get("STATUS"));
    	termUptInfo.setEffectiveDate((String)fileData.get("EFFECTIVE_DATE"));
    	termUptInfo.setTerminationDate((String)fileData.get("TERMINATION_DATE"));
    	termUptInfo.setTermVendor((String)fileData.get("TERM_VENDOR"));
    	termUptInfo.setTermType((String)fileData.get("TERM_TYPE"));
    	termUptInfo.setFunc((String)fileData.get("FUNC"));
    	termUptInfo.setUptUserid("BATCH");
    	termUptInfo.setUptDate(sysDate);
    	termUptInfo.setUptTime(sysTime);
    	termUptInfo.setAprvUserid("BATCH");
    	termUptInfo.setAprvDate(termUptInfo.getUptDate());
    	termUptInfo.setAprvTime(termUptInfo.getUptTime());
    	termUptInfo.setUptStatus("1");
    	termUptInfo.setAprvStatus("1");
    	
    	return termUptInfo;
    }
}
