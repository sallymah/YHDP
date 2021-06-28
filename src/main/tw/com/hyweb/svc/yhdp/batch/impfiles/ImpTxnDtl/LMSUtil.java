package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpTxnDtl;

import java.sql.Connection;
import java.sql.SQLException;

import tw.com.hyweb.util.date.DateUtil;
import tw.com.hyweb.core.cp.common.misc.SequenceGenerator;
import tw.com.hyweb.util.string.StringUtil;


public class LMSUtil
{
    public static final String SEQ_BATCH_LMSINVOICENO = "SEQ_BATCH_LMSINVOICENO";
    public static final String CACHE_UNKNOWN_TERM = "UNKNOWN_TERM";
    public static final String CACHE_UNKNOWN_MERCH = "UNKNOWN_MERCH";
    public static final String DEFAULT_PROC_CODE = "888888";   
    
    public static String getLmsInvoiceNo(Connection conn, String batchDate) throws SQLException {
        if (conn == null) {
            throw new IllegalArgumentException("conn is null!");
        }
       /* if (!DateUtil.isValidDate(batchDate)) {
            throw new IllegalArgumentException("batchDate invalid!");
        }*/
        String yy = batchDate.substring(2, 4);
        String jjj = StringUtil.pendingKey(DateUtil.getDayOfYear(batchDate), 3);
        String nnnnnn = SequenceGenerator.getSequenceString(conn, SEQ_BATCH_LMSINVOICENO, 6);
        return "B" + yy + jjj + nnnnnn;
    }
}
