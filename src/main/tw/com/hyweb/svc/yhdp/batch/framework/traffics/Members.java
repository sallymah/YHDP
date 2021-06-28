package tw.com.hyweb.svc.yhdp.batch.framework.traffics;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Vector;

import org.apache.log4j.Logger;

import tw.com.hyweb.core.cp.batch.framework.Layer1Constants;
import tw.com.hyweb.service.db.DBService;
import tw.com.hyweb.service.db.info.TbMemberInfo;
import tw.com.hyweb.service.db.mgr.TbMemberMgr;
import tw.com.hyweb.util.ReleaseResource;
import tw.com.hyweb.util.string.StringUtil;

public class Members {
	
	private static Logger log = Logger.getLogger(Members.class);

	private HashMap sourdeId2Member = new HashMap();
	
	
	public Members() throws SQLException {
		
		Connection conn = null;
		Vector tbMemberMgrs = new Vector();
		
		try {
			conn = DBService.getDBService().getConnection(Layer1Constants.DSNAME_BATCH);
			TbMemberMgr tbMemberMgr = new TbMemberMgr(conn);
			tbMemberMgr.queryMultiple(" ACQ_TYPE = '2' ", tbMemberMgrs);
	    	
			for (int i = 0; i < tbMemberMgrs.size(); i++) {
				TbMemberInfo info = (TbMemberInfo) tbMemberMgrs.get(i);
				
				if ( !StringUtil.isEmpty(info.getTransFileAlias()) ){
					sourdeId2Member.put(info.getTransFileAlias(), info);
				}
				else{
					sourdeId2Member.put(info.getMemId(), info);
				}
            }
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			throw new SQLException(e.getMessage());
		}
		finally {
			log.info("sourdeId2Member: " + sourdeId2Member);
            ReleaseResource.releaseDB(conn, null, null);
		}
    	
	}

	public HashMap getSourdeId2Member() {
		return sourdeId2Member;
	}

	public void setSourdeId2Member(HashMap sourdeId2Member) {
		this.sourdeId2Member = sourdeId2Member;
	}
		
}
