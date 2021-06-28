/*
 * (版權及授權描述)
 * Copyright 2007 (C) Hyweb Technology Coperation.
 * All Rights Reserved.
 *
 * 2008/5/19
 */
package tw.com.hyweb.core.yhdp.batch.framework.generic;

import tw.com.hyweb.core.yhdp.batch.framework.Layer1Constants;

/**
 * @author Clare
 * 
 */
public class BatchHandleResult
{
    public static final BatchHandleResult DEFAULT_SUCCESS_RESULT = new BatchHandleResult("", Layer1Constants.RCODE_0000_OK);

    private final String errorDescribe;
    private final String rcode;

    public BatchHandleResult(String errorDescribe, String rcode)
    {
        this.errorDescribe = errorDescribe;
        this.rcode = rcode;
    }

    /**
     * @return the errorDescribe
     */
    public String getErrorDescribe()
    {
        return errorDescribe;
    }

    /**
     * @return the rcode
     */
    public String getRcode()
    {
        return rcode;
    }
}
