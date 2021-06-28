/*
 * $$Id: AcctBalance.java 203 2009-11-09 06:44:52Z 94068 $$
 * 
 * Copyright ${year} Hyweb Technology Corporation.
 * All Rights Reserved.
 */
package tw.com.hyweb.cp.ws.enduser;

/**
 * VNPTRcode:
 *
 * @author Anny
 */
public class EndUserRcode
{
    /**
     * default ok rcode
     */
    public static final String OK = "0000";

    /**
     * default fail rcode
     */
    public static final String SYSTEM_ERROR = "1000";

    public static final String SQL_FAIL = "1001";

    /**
     * 批次處理完畢(部份資料有誤)
     */
    public static final String S2001 = "2001";


    /**
     * 執行批次失敗
     */
    public static final String RUN_BATCH_FAIL = "2999";
    
    /*
     * End User Rcode
     */
    
    public static final String NOSUPPORT_REGCUST = "9001";
    
    public static final String DOUBLE_REGCUST = "9002";
    
    public static final String NOFOUND_MASTERDATA = "9011";
    
    public static final String PK_ERR = "9012";
    
    public static final String FIELD_UNREGULAR = "9013";
    

}
