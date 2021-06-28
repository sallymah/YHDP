/**
 * changelog
 * --------------------
 * 20070528
 * duncan
 * 加上換行, 讓印出來的 sql 較好查看
 * --------------------
 */
package tw.com.hyweb.core.yhdp.common.misc;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * <pre>
 * ExecuteSqlsInfo javabean
 * commit:boolean
 * savepoint:boolean
 * sqls:List object, each element is String object
 * </pre>
 * author:duncan
 */
public class ExecuteSqlsInfo implements Serializable
{
    private static final String lineSep = System.getProperty("line.separator", "\n");

    private boolean commit = false;
    private boolean savepoint = false;
    private List sqls = new ArrayList();

    public ExecuteSqlsInfo()
    {
    }

    public List getSqls()
    {
        return sqls;
    }

    public void setSqls(List sqls)
    {
        this.sqls = sqls;
    }

    public void addSql(String sql) {
        if (sql == null || "".equals(sql)) {
            return;
        }
        // only insert, update, delete sql command can be added
        try {
            StringTokenizer st = new StringTokenizer(sql);
            if (st.hasMoreTokens()) {
                String cmd = st.nextToken();
                if ("insert".equalsIgnoreCase(cmd) || "update".equalsIgnoreCase(cmd) || "delete".equalsIgnoreCase(cmd)) {
                    sqls.add(sql);
                }
            }
        }
        catch (Exception ignore) {
            ;
        }
    }

    public boolean isCommit()
    {
        return commit;
    }

    public void setCommit(boolean commit)
    {
        this.commit = commit;
    }

    public boolean isSavepoint()
    {
        return savepoint;
    }

    public void setSavepoint(boolean savepoint)
    {
        this.savepoint = savepoint;
    }

    public ExecuteSqlsInfo clone() {
        ExecuteSqlsInfo cloneInfo = new ExecuteSqlsInfo();
        cloneInfo.setCommit(commit);
        cloneInfo.setSavepoint(savepoint);
        // deep copy, avoid refer to the same object
        List copyList = new ArrayList();
        for (int i = 0; i < sqls.size(); i++)
        {
            String sql = (String) sqls.get(i);
            copyList.add(sql);
        }
        cloneInfo.setSqls(copyList);
        return cloneInfo;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("[ExecuteSqlsInfo: ");
        sb.append(" commit:" + commit);
        sb.append(" savepoint:" + savepoint);
        sb.append(" sqls:" + lineSep);
        for (int i = 0; i < sqls.size(); i++) {
            String sql = (String) sqls.get(i);
            sb.append(sql + lineSep);
        }
        sb.append("]");
        return sb.toString();
    }
}
