package tw.com.hyweb.core.yhdp.common.misc;

import org.apache.log4j.Logger;
import tw.com.hyweb.util.ReleaseResource;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;

/**
 * <pre>
 * ExecuteSqlsUtil
 * </pre>
 * author:duncan
 */
public class ExecuteSqlsUtil
{
    private static Logger log = Logger.getLogger(ExecuteSqlsUtil.class);

    private ExecuteSqlsUtil()
    {
    }

    /**
     * <pre>
     * 依 <em>sqlsInfo</em> 使用 <em>conn</em> 來執行 SQLs
     * 可設 savepoint 和 commit,
     * 成功, return true, 失敗會丟 exceptions
     * </pre>
     * @param conn conn
     * @param sqlsInfo sqlsInfo
     * @return true/false
     * @throws SQLException when occurs exceptions
     */
    public static boolean executeSqls(Connection conn, ExecuteSqlsInfo sqlsInfo) throws SQLException {
        if (conn == null) {
            throw new IllegalArgumentException("conn is null!");
        }
        if (sqlsInfo == null) {
            throw new IllegalArgumentException("sqlsInfo is null!");
        }
        if (sqlsInfo.getSqls().size() == 0) {
            // no sqls
            return true;
        }
        Savepoint savepoint = null;
        Statement stmt = null;
        boolean autoCommit = false;
        try {
            autoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            stmt = conn.createStatement();
            if (sqlsInfo.isSavepoint()) {
                // savepoint enable, create one savepoint
                savepoint = conn.setSavepoint();
            }
            for (int i = 0; i < sqlsInfo.getSqls().size(); i++)
            {
                String sql = (String) sqlsInfo.getSqls().get(i);
                stmt.executeUpdate(sql);
            }
            if (sqlsInfo.isCommit()) {
                conn.commit();
            }
        }
        catch (Exception ignore) {
            log.warn("executeSqls error:" + ignore.getMessage(), ignore);
            if (sqlsInfo.isSavepoint() && savepoint != null) {
                // savepoint enable, rollback to savepoint
                if (conn != null) {
                    try {
                        conn.rollback(savepoint);
                    }
                    catch (Exception ignore2) {
                        ;
                    }
                }
            }
            throw new SQLException(ignore.getMessage());
        }
        finally {
            if (conn != null)
            {
                try
                {
                    conn.setAutoCommit(autoCommit);
                }
                catch (Exception ignore)
                {
                    ;
                }
            }
            ReleaseResource.releaseDB(null, stmt, null);
        }
        return true;
    }
}
