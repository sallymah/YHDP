package tw.com.hyweb.svc.yhdp.batch.framework.traffics;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

import org.apache.log4j.Logger;

import tw.com.hyweb.core.cp.batch.framework.Layer1Constants;
import tw.com.hyweb.service.db.DBService;
import tw.com.hyweb.util.ReleaseResource;

public class TrnRcodes {
	
	private static Logger log = Logger.getLogger(TrnRcodes.class);

	private HashMap rcode2RespCode = new HashMap();
	
	
	public TrnRcodes() throws SQLException {
		
		Connection conn = null;
		Statement stmt = null;
        ResultSet rs = null;
        
		String seqnoSql = "SELECT RCODE, RESP_CODE FROM TB_TRN_RCODE";
        //String seqnoSql = "SELECT PARM, VALUE FROM TB_SYS_CONFIG";
		
		try {
			conn = DBService.getDBService().getConnection(Layer1Constants.DSNAME_BATCH);
			stmt = conn.createStatement();
	    	log.debug("seqnoSql: "+seqnoSql);
	    	rs = stmt.executeQuery(seqnoSql);
	    	while (rs.next())
	    		rcode2RespCode.put(rs.getString(1), rs.getString(2));
	    	
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			throw new SQLException(e.getMessage());
		}
		finally {
			log.info("rcode2RespCode: " + rcode2RespCode);
            ReleaseResource.releaseDB(conn, stmt, rs);
		}
    	
	}

	public HashMap getRcode2RespCode() {
		return rcode2RespCode;
	}

	public void setRcode2RespCode(HashMap rcode2RespCode) {
		this.rcode2RespCode = rcode2RespCode;
	}
	
}
