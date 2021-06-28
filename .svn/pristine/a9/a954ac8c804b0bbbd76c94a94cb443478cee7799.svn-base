/*
 * (版權及授權描述)
 * Copyright 2007 (C) Hyweb Technology Corporation.
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
public class BatchHandleException extends Exception
{
    private static final long serialVersionUID = 1L;

    private final String rcode;

    /**
     * @param cause
     */
    public BatchHandleException(Exception cause)
    {
        super(cause);

        this.rcode = Layer1Constants.RCODE_2999_ALLDATAERROR;
    }

    /**
     * @param cause
     * @param rcode
     */
    public BatchHandleException(Exception cause, String rcode)
    {
        super(cause);

        this.rcode = rcode;
    }

    /**
     * @param message
     * @param rcode
     */
    public BatchHandleException(String message, String rcode)
    {
        super(message);

        this.rcode = rcode;
    }

    /**
     * @return
     */
    public String getRcode()
    {
        return rcode;
    }
}
