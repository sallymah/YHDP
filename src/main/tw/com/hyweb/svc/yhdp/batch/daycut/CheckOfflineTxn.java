/*
 * Version: 1.0.0
 * Date: 2007-01-03
 */

/*
 * (版權及授權描述)
 *
 * Copyright 2007 (C) Hyweb. All Rights Reserved.
 *
 * $History: CheckOfflineTxn.java $
 * 
 * 
 * *****************  Version 2  *****************
 * User: Anny     Date: 2007/01/22     Time:17:40
 * modfiy makeWhereCondition()
 * => moidfy CUT_DATE = {batchDate} ->CUT_DATE < = {batchDate}
 * => add CHECK_DATE is null 
 * 
 * *****************  Version 1  *****************
 * User: duncan    Date: 2007/01/03     Time:14:00
 * 
 */

package tw.com.hyweb.svc.yhdp.batch.daycut;

import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import tw.com.hyweb.core.cp.common.misc.Constants;
import tw.com.hyweb.util.date.DateUtil;
import tw.com.hyweb.core.cp.batch.framework.AbstractBatchBasic;
import tw.com.hyweb.core.cp.batch.util.BatchUtil;
import tw.com.hyweb.service.db.info.TbTransInfo;
import tw.com.hyweb.service.db.mgr.TbTransMgr;
import tw.com.hyweb.util.ReleaseResource;
import tw.com.hyweb.util.string.StringUtil;

/**
 * <pre>
 *  CheckOfflineTxn
 *  fetchRecord:int
 *  recordsPerCommit:int
 *  checkConditionInfos:List, each element is CheckOfflineInfo object
 *  batchDate:String
 * 
 *  依據 fetchRecord, recordsPerCommit, checkConditionInfos, batchDate 的值
 *  來針對離線交易檢查交易合法性
 *  rule:
 *  1. 根據 batchDate 和 checkConditionInfos.getTxnSrc 來抓 offline 的資料來檢查
 *  2. 每次取 fetchRecord 筆 TB_TRANS 出來處理
 *  3. recordsPerCommit / fetchRecord 必須要整除, 否則會跟 fetchRecord 用一樣的值
 *  4. 依據 checkConditionInfos 的設定來對 offline 的 TB_TRANS 資料做檢查
 *  5. 根據檢查的結果, 寫到 TB_TRANS_CHECK_ERR 與更新 TB_TRANS.CHECK_FLAG
 *  6. 每 recordsPerCommit commit 一次
 * </pre>
 * 
 * author: duncan
 */
public class CheckOfflineTxn extends AbstractBatchBasic
{
    private static Logger log = Logger.getLogger(CheckOfflineTxn.class);

    private static final String SPRING_PATH = "config"+File.separator+"batch"+File.separator+"CheckOfflineTxn"+File.separator+"spring.xml";
    
    private int fetchRecord = 100;

    private int recordsPerCommit = -1;

    private List checkConditionInfos = null;

    private String batchDate = "";

    private Connection conn = null;

    public CheckOfflineTxn()
    {
    }

    /**
     * fetchRecord getter
     * 
     * @return fetchRecord
     */
    public int getFetchRecord()
    {
        return fetchRecord;
    }

    /**
     * fetchRecord setter
     * 
     * @param fetchRecord fetchRecord
     */
    public void setFetchRecord(int fetchRecord)
    {
        this.fetchRecord = fetchRecord;
    }

    /**
     * recordsPerCommit getter
     * 
     * @return recordsPerCommit
     */
    public int getRecordsPerCommit()
    {
        return recordsPerCommit;
    }

    /**
     * recordsPerCommit setter
     * 
     * @param recordsPerCommit recordsPerCommit
     */
    public void setRecordsPerCommit(int recordsPerCommit)
    {
        this.recordsPerCommit = recordsPerCommit;
    }

    /**
     * checkConditionInfos getter
     * 
     * @return checkConditionInfos
     */
    public List getCheckConditionInfos()
    {
        return checkConditionInfos;
    }

    /**
     * checkConditionInfos setter
     * 
     * @param checkConditionInfos checkConditionInfos
     */
    public void setCheckConditionInfos(List checkConditionInfos)
    {
        this.checkConditionInfos = checkConditionInfos;
    }

    /**
     * batchDate getter
     * 
     * @return batchDate
     */
    public String getBatchDate()
    {
        return batchDate;
    }

    /**
     * batchDate setter
     * 
     * @param batchDate batchDate
     */
    public void setBatchDate(String batchDate)
    {
        this.batchDate = batchDate;
    }

    private String makeWhereCondition(String txnSrc)
    {
        StringBuffer where = new StringBuffer();
        where.append("TXN_SRC = ");
        where.append(StringUtil.toSqlValueWithSQuote(txnSrc));
        where.append(" AND ");
        where.append("ONLINE_FLAG = ");
        where.append(StringUtil.toSqlValueWithSQuote(Constants.ONLINEFLAG_OFFLINE));
        where.append(" AND ");
        where.append("CUT_DATE < = ");
        where.append(StringUtil.toSqlValueWithSQuote(batchDate));
        where.append(" AND ");
        where.append("CHECK_DATE is null ");
        return where.toString();
    }

    /**
     * 依據 fetchRecord, recordsPerCommit, checkConditionInfos, batchDate 的值
     * 來針對離線交易檢查交易合法性 rule: 1. 根據 batchDate 和 checkConditionInfos.getTxnSrc 來抓
     * offline 的資料來檢查 2. 每次取 fetchRecord 筆 TB_TRANS 出來處理 3. recordsPerCommit /
     * fetchRecord 必須要整除, 否則會跟 fetchRecord 用一樣的值 4. 依據 checkConditionInfos 的設定來對
     * offline 的 TB_TRANS 資料做檢查 5. 根據檢查的結果, 寫到 TB_TRANS_CHECK_ERR 與更新
     * TB_TRANS.CHECK_FLAG 6. 每 recordsPerCommit commit 一次
     * 
     * @param args args
     * @throws Exception
     */
    public void checkOfflineTxnProcess() throws Exception
    {
        if (recordsPerCommit <= 0)
        {
            recordsPerCommit = fetchRecord;
        }
        else
        {
            if (fetchRecord % recordsPerCommit == 0)
            {
                ;
            }
            else
            {
                this.recordsPerCommit = this.fetchRecord;
            }
        }

        try
        {
            conn = BatchUtil.getConnection();
            for (int i = 0; i < checkConditionInfos.size(); i++)
            {
                TbTransMgr tmgr = new TbTransMgr(conn);
                CheckConditionInfo conditionInfo = (CheckConditionInfo) checkConditionInfos.get(i);
                String where = makeWhereCondition(conditionInfo.getTxnSrc());
                int txnSrcCount = tmgr.getCount(where);
                int pages;
                if (txnSrcCount % fetchRecord == 0)
                {
                    pages = txnSrcCount / fetchRecord;
                }
                else
                {
                    pages = (txnSrcCount / fetchRecord) + 1;
                }
                for (int j = 0; j < pages; j++)
                {
                    int start = (j * fetchRecord) + 1;
                    int end = (j + 1) * fetchRecord;
                    if (end > txnSrcCount)
                    {
                        end = txnSrcCount;
                    }
                    Vector transInfos = new Vector();
                    tmgr.queryMultiple(where, start, end, transInfos);
                    List checkInfos = new ArrayList();
                    for (int k = 0; k < transInfos.size(); k++)
                    {
                        TbTransInfo transInfo = (TbTransInfo) transInfos.get(k);
                        CheckOfflineInfo checkInfo = CheckOfflineTxnUtil.makeCheckOfflineInfo(conn, transInfo,
                                                                                              conditionInfo);
                        checkInfos.add(checkInfo);
                        if (checkInfos.size() == recordsPerCommit)
                        {
                            CheckOfflineTxnUtil.handleCheckOfflineInfos(conn, checkInfos);
                            checkInfos.clear();
                        }
                    }
                    if (checkInfos.size() > 0)
                    {
                        CheckOfflineTxnUtil.handleCheckOfflineInfos(conn, checkInfos);
                        checkInfos.clear();
                    }
                }
            }
        }
        catch (Exception ignore)
        {
            log.warn("process error:" + ignore.getMessage(), ignore);
            throw ignore;
        }
        finally
        {
            ReleaseResource.releaseDB(conn);
        }
    }

    /**
     * get instance by spring <br/>
     * @return instance
     */
    public static CheckOfflineTxn getInstance()
    {
        ApplicationContext apContext = new FileSystemXmlApplicationContext(SPRING_PATH);
        CheckOfflineTxn instance = (CheckOfflineTxn) apContext.getBean("checkOfflineTxn");
        return instance;
    }
    
    public static void main(String[] args)
    {
        try
        {
            String batchDate = System.getProperty("date");
            // Gets Spring Setting

            CheckOfflineTxn cot = getInstance();
            cot.setBatchDate(batchDate);
            cot.run(args);
        }
        catch (Exception e)
        {
            log.warn("get spring bean error:" + e.getMessage(), e);
        }
    }
    

    public void process(String[] arg0) throws Exception
    {
        
        if (StringUtil.isEmpty(batchDate))
        {
            batchDate = DateUtil.getTodayString().substring(0, 8);
        }
        
        init();

        log.info("=====Start checkOfflineTxn Process=====");
        checkOfflineTxnProcess();
    }
    
    /**
     * 初始設定
     * 
     * @throws Exception
     */
    protected void init() throws Exception
    {
        log.info("set init");
        
        if (StringUtil.isEmpty(getBatchDate()))
        {
            throw new Exception(getBatchDate()+"Recover Must Input Date");
        }


    }

}
