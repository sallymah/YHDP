/**
 * changelog
 * --------------------
 * 20080403
 * duncan
 * add checkTotalRecords property, for HouseKeeping
 * --------------------
 * 20071207
 * duncan
 * 修正取得 totalRecords 的規則
 * --------------------
 */
package tw.com.hyweb.svc.yhdp.batch.expfiles.ExpCardVR;

import org.apache.log4j.Logger;

import java.sql.*;
import java.util.List;
import java.util.ArrayList;
import java.io.BufferedReader;

import tw.com.hyweb.util.ReleaseResource;

/**
 * <pre>
 * ExpFileResult javabean
 * </pre>
 * author:duncan
 */
public class ExpFileResult {
    private static Logger log = Logger.getLogger(ExpFileResult.class);
    // current run expFileInfo
    private ExpFileInfo expFileInfo = null;

    // this query total records
    private int totalRecords = 0;
    private int fieldCount = 0;
    // handle how many records writing to file
    private int recordCount = 0;

    private Connection conn = null;
    private Statement stmt = null;
    private ResultSet rs = null;
    private ResultSetMetaData rsmd = null;
    private boolean checkTotalRecords = true;

    public ExpFileResult() {
    }

    public boolean isCheckTotalRecords() {
        return checkTotalRecords;
    }

    public void setCheckTotalRecords(boolean checkTotalRecords) {
        this.checkTotalRecords = checkTotalRecords;
    }

    public Connection getConnection() {
        return conn;
    }

    public void setConnection(Connection conn) {
        this.conn = conn;
    }

    public ExpFileInfo getExpFileInfo() {
        return expFileInfo;
    }

    public void setExpFileInfo(ExpFileInfo expFileInfo) {
        this.expFileInfo = expFileInfo;
    }

    public int getFieldCount() {
        return fieldCount;
    }

    public void setFieldCount(int fieldCount) {
        this.fieldCount = fieldCount;
    }

    public int getRecordCount() {
        return recordCount;
    }

    public void setRecordCount(int recordCount) {
        this.recordCount = recordCount;
    }

    public int getTotalRecords() {
        return totalRecords;
    }

    public void setTotalRecords(int totalRecords) {
        this.totalRecords = totalRecords;
    }

    public void checkParameters() throws SQLException {
        // check conn and expFileInfo properties
        if (conn  == null) {
            throw new SQLException("conn is null!");
        }
        if (expFileInfo == null) {
            throw new SQLException("expFileInfo is null!");
        }
    }

    public void checkCounts() throws SQLException {
        // get totalRecords and check totalRecords
//        int fromIdx = -1;
//        fromIdx = expFileInfo.getSelectSQL().toUpperCase().indexOf("FROM");
//        if (fromIdx == -1) {
//            // should not happen
//            throw new SQLException("invalid selectSQL '" + expFileInfo.getSelectSQL() + "'!");
//        }
//        String countSQL = "SELECT COUNT(*) " + expFileInfo.getSelectSQL().substring(fromIdx);
        String countSQL = "SELECT COUNT(*) FROM (" + expFileInfo.getSelectSQL() + ")";
        // use another Statement, ResultSet
        Statement countStmt = null;
        ResultSet countRs = null;
        try {
            countStmt = conn.createStatement();
            countRs = countStmt.executeQuery(countSQL);
            if (countRs.next()) {
                totalRecords = countRs.getInt(1);
            }
        }
        catch (SQLException ignore) {
            log.warn("query totalCount fail for '" + countSQL + "':" + ignore.getMessage(), ignore);
            throw ignore;
        }
        finally {
            ReleaseResource.releaseDB(null, countStmt, countRs);
        }
        // TB_OUTCTL.TOT_REC 限制
        // 20140606 頂新因要求全檔匯出，修改TB_OUTCTL.TOT_REC最大值 (6->8)
        if (checkTotalRecords && totalRecords >= 100000000) {
            throw new SQLException("totalRecords '" + totalRecords + "' > 100000000 for countSQL '" + countSQL + "'!");
        }
    }

    public void executeSQL() throws SQLException {
        // execute expFileInfo.selectSQL
        try {
            stmt = conn.createStatement();
            rs = stmt.executeQuery(expFileInfo.getSelectSQL());
            rsmd = rs.getMetaData();
            fieldCount = rsmd.getColumnCount();
            // set columnTypes by rsmd
            setColumnTypes();
        }
        catch (SQLException ignore) {
            log.warn("executeSQL fail for '" + expFileInfo.getSelectSQL() + "':" + ignore.getMessage(), ignore);
            throw ignore;
        }
    }

    public void startProcess() throws SQLException {
        // check conn and expFileInfo properties
        checkParameters();
        // get totalRecords and check totalRecords
        checkCounts();
        // execute expFileInfo.selectSQL
        executeSQL();
    }

    public void closeResource() {
        ReleaseResource.releaseDB(null, stmt, rs);
    }

    protected void finalize() throws Throwable {
        super.finalize();
        closeResource();
    }

    public List getFieldMetaData(int idx) throws SQLException {
//        0:columnName:String
//        1:columnTypeName:String
//        2:columnClassName:String
//        3:columnType:Number
//        4:precision:Number
//        5:scale:Number
//        6:columnDisplaySize
        // idx is 1-based
        List ret = new ArrayList();
        ret.add(rsmd.getColumnName(idx));
        ret.add(rsmd.getColumnTypeName(idx));
        ret.add(rsmd.getColumnClassName(idx));
        ret.add(new Integer(rsmd.getColumnType(idx)));
        ret.add(new Integer(rsmd.getPrecision(idx)));
        ret.add(new Integer(rsmd.getScale(idx)));
        ret.add(new Integer(rsmd.getColumnDisplaySize(idx)));
        return ret;
    }

    private int[] columnTypes = null;
    public void setColumnTypes() throws SQLException {
        // set columnTypes by rsmd
        columnTypes = new int[fieldCount + 1];
        for (int i = 1; i <= fieldCount; i++) {
            List metadata = getFieldMetaData(i);
            columnTypes[i] = ((Number) metadata.get(3)).intValue();
        }
    }

    /**
     * Types.BOOLEAN:
     * Types.DATE:
     * Types.TIME:
     * Types.TIMESTAMP:
     * Types.CHAR:
     * Types.VARCHAR:
     * Types.CLOB:
     * non-null will map to java.lang.String
     * null will map to empty string
     *
     * Types.BIT:
     * Types.DECIMAL:
     * Types.DOUBLE:
     * Types.FLOAT:
     * Types.INTEGER:
     * Types.BIGINT:
     * Types.NUMERIC:
     * Types.REAL:
     * Types.SMALLINT:
     * Types.TINYINT:
     * non-null will map to java.lang.Number
     * null will map to null
     *
     * not support
     * Types.ARRAY:
     * Types.BINARY:
     * Types.BLOB:
     * Types.DATALINK:
     * Types.DISTINCT:
     * Types.JAVA_OBJECT:
     * Types.LONGVARBINARY:
     * Types.LONGVARCHAR:
     * Types.NULL:
     * Types.OTHER:
     * Types.REF:
     * Types.STRUCT:
     * Types.VARBINARY:
     * default:
     * non-null will map to java.lang.String(using Object.toString())
     * null will map to empty string
     * @param obj
     * @param columnType
     * @return see above
     */
    private Object map2Object(Object obj, int columnType) {
        Object ret = null;
        String str = "";
        switch (columnType) {
            // handle known types
            // boolean, datetime, char, varchar...
            case Types.BOOLEAN:
            case Types.DATE:
            case Types.TIME:
            case Types.TIMESTAMP:
            case Types.CHAR:
            case Types.VARCHAR:
                if (obj == null) {
                    str = "";
                }
                else {
                    str = obj.toString();
                }
                ret = str;
                break;
            // number type
            case Types.BIT:
            case Types.DECIMAL:
            case Types.DOUBLE:
            case Types.FLOAT:
            case Types.INTEGER:
            case Types.BIGINT:
            case Types.NUMERIC:
            case Types.REAL:
            case Types.SMALLINT:
            case Types.TINYINT:
                if (obj == null) {
                    ret = null;
                }
                else {
                    if (obj instanceof Number) {
                        ret = obj;
                    }
                    else {
                        // should not happen!!
                        ret = null;
                    }
                }
                break;
            case Types.CLOB:
                Clob clob = (Clob) obj;
                if (obj == null) {
                    str = "";
                }
                else {
                    BufferedReader br = null;
                    try {
                        br = new BufferedReader(clob.getCharacterStream());
                        StringBuffer sb = new StringBuffer();
                        String line = "";
                        while ((line = br.readLine()) != null) {
                            sb.append(line + expFileInfo.getLineSeparator());
                        }
                        str = sb.toString();
                    }
                    catch (Exception ignore) {
                        str = "";
                    }
                    finally {
                        if (br != null) {
                            try {
                                br.close();
                            }
                            catch (Exception ignore) {
                                ;
                            }
                        }
                    }
                }
                ret = str;
                break;
            // unhandle
            case Types.ARRAY:
            case Types.BINARY:
            case Types.BLOB:
            case Types.DATALINK:
            case Types.DISTINCT:
            case Types.JAVA_OBJECT:
            case Types.LONGVARBINARY:
            case Types.LONGVARCHAR:
            case Types.NULL:
            case Types.OTHER:
            case Types.REF:
            case Types.STRUCT:
            case Types.VARBINARY:
            default:
                if (obj == null) {
                    str = "";
                }
                else {
                    str = obj.toString();
                }
                ret = str;
                break;
        }
        return ret;
    }

    public List getRecord() throws SQLException {
        List ret = new ArrayList();
        if (rs.next()) {
            for (int i = 1; i <= fieldCount; i++) {
                Object o = map2Object(rs.getObject(i), columnTypes[i]);
                ret.add(o);
            }
        }
        return ret;
    }
}
