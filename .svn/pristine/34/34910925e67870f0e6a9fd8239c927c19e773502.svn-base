/*
 * (版權及授權描述)
 * Copyright 2007 (C) Hyweb Technology Coperation.
 * All Rights Reserved.
 *
 * 2008/12/31
 */
package tw.com.hyweb.core.yhdp.batch.framework.generic;

/**
 * @author Clare
 * 
 */
public class RcodeBaseException extends Exception
{
    private static final long serialVersionUID = -1059440206822073506L;

    private final String rcode;

    /**
     * @param message
     * @param rcode
     */
    public RcodeBaseException(String message, String rcode)
    {
        super(message);

        this.rcode = rcode;
    }

    /**
     * @param throwable
     * @param rcode
     */
    public RcodeBaseException(Throwable throwable, String rcode)
    {
        super(throwable);

        this.rcode = rcode;
    }

    /**
     * @return the rcode
     */
    public String getRcode()
    {
        return rcode;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Throwable#getMessage()
     */
    @Override
    public String getMessage()
    {
        return super.getMessage() + ", rcode:" + rcode;
    }
}
