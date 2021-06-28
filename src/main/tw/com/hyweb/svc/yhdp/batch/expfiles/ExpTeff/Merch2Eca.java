package tw.com.hyweb.svc.yhdp.batch.expfiles.ExpTeff;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Vector;

import org.apache.log4j.Logger;

import tw.com.hyweb.core.cp.batch.framework.Layer1Constants;
import tw.com.hyweb.service.db.DBService;
import tw.com.hyweb.service.db.info.TbMerchInfo;
import tw.com.hyweb.service.db.mgr.TbMerchMgr;
import tw.com.hyweb.svc.yhdp.batch.framework.traffics.Members;
import tw.com.hyweb.util.ReleaseResource;

public class Merch2Eca {
	
	private static Logger log = Logger.getLogger(Members.class);

	private HashMap merch2Eca = new HashMap();
	
	
	public Merch2Eca() throws SQLException {
		
		Connection conn = null;
		Vector tbMerchMgrs = new Vector();
		
		try {
			conn = DBService.getDBService().getConnection(Layer1Constants.DSNAME_BATCH);
			TbMerchMgr tbMerchMgr = new TbMerchMgr(conn);
			tbMerchMgr.queryMultiple(" ECA_MERCH_ID IS NOT NULL ", tbMerchMgrs);
	    	
			for (int i = 0; i < tbMerchMgrs.size(); i++) {
				TbMerchInfo info = (TbMerchInfo) tbMerchMgrs.get(i);
				merch2Eca.put(info.getMerchId(), info);
            }
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			throw new SQLException(e.getMessage());
		}
		finally {
			//log.info("merch2Eca: " + merch2Eca);
            ReleaseResource.releaseDB(conn, null, null);
		}
    	
	}


	public HashMap getMerch2Eca() {
		return merch2Eca;
	}
	public void setMerch2Eca(HashMap merch2Eca) {
		this.merch2Eca = merch2Eca;
	}
	
}
