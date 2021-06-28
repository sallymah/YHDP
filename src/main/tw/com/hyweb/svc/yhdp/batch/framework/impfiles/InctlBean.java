package tw.com.hyweb.svc.yhdp.batch.framework.impfiles;

import org.apache.log4j.Logger;
import tw.com.hyweb.service.db.info.TbBatchResultInfo;
import tw.com.hyweb.service.db.info.TbInctlInfo;
import tw.com.hyweb.service.db.mgr.TbInctlMgr;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * <pre>
 * InctlBean javabean
 * batchResultInfo:TbBatchResultInfo
 * related:boolean
 * inctlInfo:TbInctlInfo
 * </pre>
 * author:duncan
 */
public class InctlBean {
    private static Logger log = Logger.getLogger(InctlBean.class);
    private TbBatchResultInfo batchResultInfo = null;
    private boolean related = false;
    private TbInctlInfo inctlInfo = null;

    public InctlBean() {
    }

    public TbBatchResultInfo getBatchResultInfo() {
        return batchResultInfo;
    }

    public void setBatchResultInfo(TbBatchResultInfo batchResultInfo) {
        this.batchResultInfo = batchResultInfo;
    }

    public boolean isRelated() {
        return related;
    }

    public void setRelated(boolean related) {
        this.related = related;
    }

    public TbInctlInfo getInctlInfo() {
        return inctlInfo;
    }

    public void setInctlInfo(TbInctlInfo inctlInfo) {
        this.inctlInfo = inctlInfo;
    }

    public TbInctlInfo makeInctl(TbInctlInfo inctlInfo) {
        if (inctlInfo == null) {
            throw new IllegalArgumentException("inctlInfo is null!");
        }
        if (related && batchResultInfo == null) {
            throw new IllegalArgumentException("related is true but batchResultInfo is null!");
        }
        this.inctlInfo = (TbInctlInfo) inctlInfo.clone();
        if (related) {
            // inctlInfo 與 batchResultInfo 有相關
            this.inctlInfo.setProgramName(batchResultInfo.getProgramName());
            this.inctlInfo.setStartDate(batchResultInfo.getStartDate());
            this.inctlInfo.setStartTime(batchResultInfo.getStartTime());
        }
        return this.inctlInfo;
    }

    public boolean insertInctl(Connection conn, boolean isCommit, TbInctlInfo inctlInfo) throws SQLException {
        if (conn == null) {
            throw new IllegalArgumentException("conn is null!");
        }
        if (inctlInfo == null) {
            throw new IllegalArgumentException("inctlInfo is null!");
        }
        boolean ret = false;
        try {
            TbInctlMgr mgr = new TbInctlMgr(conn);
            mgr.insert(inctlInfo);
            if (isCommit) {
                conn.commit();
            }
            ret = true;
        }
        catch (Exception ignore) {
            ret = false;
            log.warn("insertInctl fail:" + ignore.getMessage(), ignore);
            throw new SQLException(ignore.getMessage());
        }
        return ret;
    }

    public boolean updateInctl(Connection conn, boolean isCommit, TbInctlInfo inctlInfo) throws SQLException {
        if (conn == null) {
            throw new IllegalArgumentException("conn is null!");
        }
        if (inctlInfo == null) {
            throw new IllegalArgumentException("inctlInfo is null!");
        }
        boolean ret = false;
        try {
            TbInctlMgr mgr = new TbInctlMgr(conn);
            mgr.update(inctlInfo);
            if (isCommit) {
                conn.commit();
            }
            ret = true;
        }
        catch (Exception ignore) {
            ret = false;
            log.warn("updateInctl fail:" + ignore.getMessage(), ignore);
            throw new SQLException(ignore.getMessage());
        }
        return ret;
    }
}
