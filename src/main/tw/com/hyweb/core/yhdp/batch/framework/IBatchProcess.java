/*
 * Version: 1.0.0
 * Date: 2007-01-03
 */
/*
 * (版權及授權描述)
 *
 * Copyright 2006 (C) Hyweb Technology Co., Ltd. All Rights Reserved.
 *
 * $History: IBatchProcess.java $
 * 
 * 
 * 
 * *****************  Version 1  *****************
 * User: Rock         2006/09/15 (YYYY/MM/DD) Time: 10:20
 * Updated in $/JCP/cc_src/src/main/tw/com/hyweb/batch
 * 新增例外於process()
 * 
 * *****************  Version 1  *****************
 * User: Robert       2006/08/29 (YYYY/MM/DD) Time: 15:41
 * 
 */

package tw.com.hyweb.core.yhdp.batch.framework;

/**
 * @author Robert
 */
public interface IBatchProcess
{

    /**
     * 批次作業核心邏輯介面函式
     * 
     * @throws Exception
     */
    public abstract void process(String[] argv) throws Exception;
}
