/*
 * Version: 1.0.0
 * Date: 2007-01-03
 */
/*
 * (版權及授權描述)
 *
 * Copyright 2006 (C) Financial Information Service Coperation. All Rights Reserved.
 *
 * $History: BatchException.java $
 * 
 * *****************  Version 6  *****************
 * User: Tracy        Date: 05/10/04   Time: 5:44p
 * Updated in $/CARD_CAFE/cc_src/src/main/tw/com/fisc/framework/batch
 *
 * *****************  Version 5  *****************
 * User: Tracy        Date: 05/09/29   Time: 1:48p
 * Updated in $/CARD_CAFE/cc_src/src/main/tw/com/fisc/framework/batch
 *
 * *****************  Version 4  *****************
 * User: Tracy        Date: 05/09/09   Time: 3:29p
 * Updated in $/CARD_CAFE/cc_src/src/main/tw/com/fisc/framework/batch
 * 更新註解
 *
 * *****************  Version 3  *****************
 * User: Alex_lin     Date: 05/06/08   Time: 11:26a
 * Updated in $/CARD_CAFE/cc_src/src/main/tw/com/fisc/framework/batch
 * 變更說明: 加入檔頭及檔案歷程
 */
package tw.com.hyweb.core.yhdp.batch.framework;

/**
 * Exception of Batch<br>
 *
 * @author Alex Lin
 */
public class BatchException extends RuntimeException
{
    /**
     * Creates a new BatchException object.
     */
    public BatchException()
    {
        super();
    }

    /**
     * Creates a new BatchException object.
     *
     * @param message DOCUMENT ME!
     */
    public BatchException(String message)
    {
        super(message);
    }

    /**
     * Creates a new BatchException object.
     *
     * @param message DOCUMENT ME!
     * @param cause DOCUMENT ME!
     */
    public BatchException(String message, Throwable cause)
    {
        super(message, cause);
    }

    /**
     * Creates a new BatchException object.
     *
     * @param cause DOCUMENT ME!
     */
    public BatchException(Throwable cause)
    {
        super(cause);
    }
}
