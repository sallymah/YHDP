package tw.com.hyweb.core.yhdp.batch.util;

import org.apache.log4j.Logger;

import tw.com.hyweb.core.yhdp.common.misc.Constants;
import tw.com.hyweb.core.yhdp.common.misc.SequenceGenerator;
import tw.com.hyweb.service.db.DBService;
import tw.com.hyweb.util.ReleaseResource;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * <pre>
 * SequenceGenerator
 * </pre>
 * author:duncan
 */
public class BatchSequenceGenerator {
    private static Logger log = Logger.getLogger(BatchSequenceGenerator.class);

    private BatchSequenceGenerator() {
        ;
    }

    public static void createSequence(String seqName, int seqLength) {
        Connection conn = null;
        try
        {
            conn = DBService.getDBService().getConnection(Constants.DSNAME_BATCH);
            SequenceGenerator.createSequence(conn, seqName, seqLength);
        }
        catch (Exception ignore) {
            log.warn("createSequence error:" + ignore.getMessage(), ignore);
        }
        finally
        {
            ReleaseResource.releaseDB(conn);
        }
    }

    public static String getSequenceString(String seqName, int seqLength) throws SQLException {
        Connection conn = null;
        String ret = "";
        try
        {
            conn = DBService.getDBService().getConnection(Constants.DSNAME_BATCH);
            ret = SequenceGenerator.getSequenceString(conn, seqName, seqLength);
        }
        finally
        {
            ReleaseResource.releaseDB(conn);
        }
        return ret;
    }

    public static void dropSequence(String seqName) {
        Connection conn = null;
        try
        {
            conn = DBService.getDBService().getConnection(Constants.DSNAME_BATCH);
            SequenceGenerator.dropSequence(conn, seqName);
        }
        catch (Exception ignore) {
            log.warn("dropSequence error:" + ignore.getMessage(), ignore);
        }
        finally
        {
            ReleaseResource.releaseDB(conn);
        }
    }

    public static String getLmsInvoiceNo(String batchDate) throws SQLException {
        Connection conn = null;
        String ret = "";
        try
        {
            conn = DBService.getDBService().getConnection(Constants.DSNAME_BATCH);
            ret = SequenceGenerator.getLmsInvoiceNo(conn, batchDate);
        }
        finally
        {
            ReleaseResource.releaseDB(conn);
        }
        return ret;
    }

    public static String getBatchNoByType(String type) throws SQLException {
        Connection conn = null;
        String ret = "";
        try
        {
            conn = DBService.getDBService().getConnection(Constants.DSNAME_BATCH);
            ret = SequenceGenerator.getBatchNoByType(conn, type);
        }
        finally
        {
            ReleaseResource.releaseDB(conn);
        }
        return ret;
    }

    public static String getArSerno(String batchDate) throws SQLException {
        Connection conn = null;
        String ret = "";
        try
        {
            conn = DBService.getDBService().getConnection(Constants.DSNAME_BATCH);
            ret = SequenceGenerator.getArSerno(conn, batchDate);
        }
        finally
        {
            ReleaseResource.releaseDB(conn);
        }
        return ret;
    }

    public static String getPersoBatchNo(String batchDate) throws SQLException {
        Connection conn = null;
        String ret = "";
        try
        {
            conn = DBService.getDBService().getConnection(Constants.DSNAME_BATCH);
            ret = SequenceGenerator.getPersoBatchNo(conn, batchDate);
        }
        finally
        {
            ReleaseResource.releaseDB(conn);
        }
        return ret;
    }

    public static String getAlSerno(String batchDate) throws SQLException {
        Connection conn = null;
        String ret = "";
        try
        {
            conn = DBService.getDBService().getConnection(Constants.DSNAME_BATCH);
            ret = SequenceGenerator.getAlSerno(conn, batchDate);
        }
        finally
        {
            ReleaseResource.releaseDB(conn);
        }
        return ret;
    }
}
