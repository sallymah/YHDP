package tw.com.hyweb.core.yhdp.common.misc;

import tw.com.hyweb.util.ReleaseResource;
import tw.com.hyweb.util.string.StringUtil;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;

/**
 * <pre>
 * SequenceGenerator
 * </pre>
 * author:duncan
 */
public class SequenceGenerator
{
    private static Logger log = Logger.getLogger(SequenceGenerator.class);

    private SequenceGenerator() {
        ;
    }
    
    // oracle sequences
    // reference: http://www.psoug.org/reference/sequences.html

    public static String pendingKey(long value, int length) {
        String ret = "" + value;
        int len = length - ret.length();
        for (int i = 0; i < len; i++) {
            ret = "0" + ret;
        }
        return ret;
    }

    private static long getMaxMinus1(int numDigits) {
        long ret = 1;
        for (int i = 0; i < numDigits; i++) {
            ret *= 10;
        }
        return ret - 1;
    }

    private static boolean isSequenceExists(Connection conn, String seqName) {
        boolean ret = false;
        Statement stmt = null;
        ResultSet rs = null;
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT COUNT(*) FROM USER_SEQUENCES");
        sql.append(" WHERE SEQUENCE_NAME = ");
        sql.append(StringUtil.toSqlValueWithSQuote(seqName));
        try {
            int count = 0;
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql.toString());
            while (rs.next()) {
                count = rs.getInt(1);
            }
            if (count > 0) {
                ret = true;
            }
            else {
                ret = false;
            }
        }
        catch (SQLException se) {
            // should not happen here!
            log.warn("isSequenceExists error:" + se.getMessage(), se);
        }
        finally {
            ReleaseResource.releaseDB(null, stmt, rs);
        }
        return ret;
    }

    public static void createSequence(Connection conn, String seqName, int seqLength) {
        if (conn == null) {
            throw new IllegalArgumentException("conn is null!");
        }
        if (StringUtil.isEmpty(seqName)) {
            throw new IllegalArgumentException("seqName is empty!");
        }
        if (seqLength <= 0 || seqLength > 18) {
            throw new IllegalArgumentException("seqLength invalid [1~18]!");
        }
        if (!isSequenceExists(conn, seqName)) {
            Statement stmt = null;
            StringBuffer sql = new StringBuffer();
            sql.append("CREATE SEQUENCE ");
            sql.append(seqName);
            sql.append(" MINVALUE 1");
            sql.append(" MAXVALUE " + getMaxMinus1(seqLength));
            sql.append(" INCREMENT BY 1");
            sql.append(" START WITH 1");
            sql.append(" NOCACHE");
            sql.append(" CYCLE");
            try {
                stmt = conn.createStatement();
                stmt.executeUpdate(sql.toString());
            }
            catch (SQLException se) {
                // should not happen here!
                log.warn("createSequence error:" + se.getMessage(), se);
            }
            finally {
                ReleaseResource.releaseDB(null, stmt, null);
            }
        }
    }

    public static String getSequenceString(Connection conn, String seqName, int seqLength) throws SQLException {
//        createSequence(conn, seqName, seqLength);
        String ret = "";
        long nextSeq = -1;
        Statement stmt = null;
        ResultSet rs = null;
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT " + seqName + ".NEXTVAL FROM DUAL");
        try {
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql.toString());
            while (rs.next()) {
                nextSeq = rs.getBigDecimal(1).longValue();
            }
            ret = pendingKey(nextSeq, seqLength);
        }
        finally {
            ReleaseResource.releaseDB(null, stmt, rs);
        }
        return ret;
    }

    public static void dropSequence(Connection conn, String seqName) {
        if (conn == null) {
            throw new IllegalArgumentException("conn is null!");
        }
        if (StringUtil.isEmpty(seqName)) {
            throw new IllegalArgumentException("seqName is empty!");
        }
        Statement stmt = null;
        StringBuffer sql = new StringBuffer();
        sql.append("DROP SEQUENCE " + seqName);
        try {
            stmt = conn.createStatement();
            stmt.executeUpdate(sql.toString());
        }
        catch (SQLException se) {
            // should not happen here!
            log.warn("dropSequence error:" + se.getMessage(), se);
        }
        finally {
            ReleaseResource.releaseDB(null, stmt, null);
        }
    }

    private static final String SEQ_BATCH_LMSINVOICENO = "SEQ_BATCH_LMSINVOICENO";
    public static String getLmsInvoiceNo(Connection conn, String batchDate) throws SQLException {
        if (conn == null) {
            throw new IllegalArgumentException("conn is null!");
        }
        if (!DateUtil.isValidDate(batchDate)) {
            throw new IllegalArgumentException("batchDate invalid!");
        }
        String yy = batchDate.substring(2, 4);
        String jjj = StringUtil.pendingKey(DateUtil.getDayOfYear(batchDate), 3);
        String nnnnnn = getSequenceString(conn, SEQ_BATCH_LMSINVOICENO, 6);
        return "B" + yy + jjj + nnnnnn;
    }

    public static boolean isInArray(String data, String[] datas) {
        for (int i = 0; i < datas.length; i++) {
            if (data.equals(datas[i])) {
                return true;
            }
        }
        return false;
    }

    // batchNo sequence names
    private static final String SEQ_CAPTXN_BATCHNO = "SEQ_CAPTXN_BATCHNO";
    private static final String SEQ_ADJUSTTXN_BATCHNO = "SEQ_ADJUSTTXN_BATCHNO";
    private static final String SEQ_BALTRANSFER_BATCHNO = "SEQ_BALTRANSFER_BATCHNO";
    private static final String SEQ_APPOINTLIST_BATCHNO = "SEQ_APPOINTLIST_BATCHNO";
    private static final String SEQ_APPOINTRELOAD_BATCHNO = "SEQ_APPOINTRELOAD_BATCHNO";
    private static final String SEQ_BATCHTRANS_BATCHNO = "SEQ_BATCHTRANS_BATCHNO";
    private static final String SEQ_BONUSEXTEND_BATCHNO = "SEQ_BONUSEXTEND_BATCHNO";
    private static final String SEQ_ERRTXN_BATCHNO = "SEQ_ERRTXN_BATCHNO";
    private static final String SEQ_BADBONUS_BATCHNO = "SEQ_BADBONUS_BATCHNO";
    private static final String SEQ_WEBTXN_BATCHNO = "SEQ_WEBTXN_BATCHNO";
    // batchNo type
    public static final String TYPE_CAPTURE = "C";
    public static final String TYPE_ADJUST = "A";
    public static final String TYPE_TRANSFER = "T";
    public static final String TYPE_APPOINT = "L";
    public static final String TYPE_APPLOAD = "P";
    public static final String TYPE_BATCH = "B";
    public static final String TYPE_BONUSEXTEND = "X";
    public static final String TYPE_ERRTXN = "R";
    public static final String TYPE_BADBONUS = "N";
    public static final String TYPE_WEBTXN = "I";

    public static String getBatchNoByType(Connection conn, String type) throws SQLException {
        if (StringUtil.isEmpty(type)) {
            throw new IllegalArgumentException("type is empty!");
        }
        if (!isInArray(type, new String[] {
                TYPE_CAPTURE,
                TYPE_ADJUST,
                TYPE_TRANSFER,
                TYPE_APPOINT,
                TYPE_APPLOAD,
                TYPE_BATCH,
                TYPE_BONUSEXTEND,
                TYPE_ERRTXN,
                TYPE_BADBONUS,
                TYPE_WEBTXN
        })) {
            throw new IllegalArgumentException("invalid type!");
        }
        String seqName = "";
        if (TYPE_CAPTURE.equals(type)) {
            seqName = SEQ_CAPTXN_BATCHNO;
        }
        else if (TYPE_ADJUST.equals(type)) {
            seqName = SEQ_ADJUSTTXN_BATCHNO;
        }
        else if (TYPE_TRANSFER.equals(type)) {
            seqName = SEQ_BALTRANSFER_BATCHNO;
        }
        else if (TYPE_APPOINT.equals(type)) {
            seqName = SEQ_APPOINTLIST_BATCHNO;
        }
        else if (TYPE_APPLOAD.equals(type)) {
            seqName = SEQ_APPOINTRELOAD_BATCHNO;
        }
        else if (TYPE_BATCH.equals(type)) {
            seqName = SEQ_BATCHTRANS_BATCHNO;
        }
        else if (TYPE_BONUSEXTEND.equals(type)) {
            seqName = SEQ_BONUSEXTEND_BATCHNO;
        }
        else if (TYPE_ERRTXN.equals(type)) {
            seqName = SEQ_ERRTXN_BATCHNO;
        }
        else if (TYPE_BADBONUS.equals(type)) {
            seqName = SEQ_BADBONUS_BATCHNO;
        }
        else if (TYPE_WEBTXN.equals(type)) {
            seqName = SEQ_WEBTXN_BATCHNO;
        }
        String nnnnn = getSequenceString(conn, seqName, 5);
        return type + nnnnn;
    }

    private static final String SEQ_APPOINTRELOAD_ARSERNO = "SEQ_APPOINTRELOAD_ARSERNO";
    public static String getArSerno(Connection conn, String batchDate) throws SQLException {
        if (conn == null) {
            throw new IllegalArgumentException("conn is null!");
        }
        if (!DateUtil.isValidDate(batchDate)) {
            throw new IllegalArgumentException("batchDate invalid!");
        }
        String yymmdd = batchDate.substring(2, 8);
        String nnnnnn = getSequenceString(conn, SEQ_APPOINTRELOAD_ARSERNO, 6);
        return yymmdd + nnnnnn;
    }

    private static final String SEQ_PERSO_BATCHNO = "SEQ_PERSO_BATCHNO";
    public static String getPersoBatchNo(Connection conn, String batchDate) throws SQLException {
        if (conn == null) {
            throw new IllegalArgumentException("conn is null!");
        }
        if (!DateUtil.isValidDate(batchDate)) {
            throw new IllegalArgumentException("batchDate invalid!");
        }
        String nnn = getSequenceString(conn, SEQ_PERSO_BATCHNO, 3);
        return batchDate + nnn;
    }

    private static final String SEQ_APPOINTLIST_ALSERNO = "SEQ_APPOINTLIST_ALSERNO";
    public static String getAlSerno(Connection conn, String batchDate) throws SQLException {
        if (conn == null) {
            throw new IllegalArgumentException("conn is null!");
        }
        if (!DateUtil.isValidDate(batchDate)) {
            throw new IllegalArgumentException("batchDate invalid!");
        }
        String nnnnnn = getSequenceString(conn, SEQ_APPOINTLIST_ALSERNO, 6);
        return batchDate.substring(2, 8) + nnnnnn;
    }
}
