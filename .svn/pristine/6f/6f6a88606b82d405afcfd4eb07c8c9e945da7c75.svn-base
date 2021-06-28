package tw.com.hyweb.svc.yhdp.batch.daycut;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Vector;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import tw.com.hyweb.core.cp.batch.framework.AbstractBatchBasic;
import tw.com.hyweb.core.cp.batch.framework.IBatchProcess;
import tw.com.hyweb.core.cp.batch.framework.IBatchResult;
import tw.com.hyweb.core.cp.batch.util.BatchUtil;
import tw.com.hyweb.service.db.DBService;
import tw.com.hyweb.util.string.StringUtil;

public class CheckLateUploadTxn extends AbstractBatchBasic implements IBatchResult, IBatchProcess
{
    protected final Logger logger = Logger.getLogger(this.getClass().getName());

    protected Connection conn = BatchUtil.getConnection();

    protected String batchDate; //process date
    
    protected int sleepTime; 
    
    public Vector<HashMap> preCondition()
    {
        StringBuffer sql = new StringBuffer();
        sql.append("select CARD_NO, EXPIRY_DATE, LMS_INVOICE_NO from TB_TRANS ");
        sql.append(" where CUT_DATE='").append(getBatchDate()).append("'");
        sql.append(" and CARD_NO||EXPIRY_DATE in (Select CARD_NO||EXPIRY_DATE from TB_CARD where STATUS='T')");
        logger.debug(sql);
        Vector<HashMap> vtr = BatchUtil.getInfoListHashMap(sql.toString());
        
        return vtr;
    }
    
    public void process(String[] args) throws Exception
    {
        if (StringUtil.isEmpty(getBatchDate())) {
            BatchUtil.getNow();
            setBatchDate(BatchUtil.sysDay);
        }
        
        Vector<HashMap> vtr = new Vector<HashMap>();
        logger.debug(getBatchDate());
        
        vtr = preCondition();
        
        for ( HashMap<String, String> hm : vtr )
        {
            StringBuffer sql = new StringBuffer();
            sql.append("update TB_TRANS set");
            sql.append(" late_upload_flag= '1', ");
            sql.append(" where CARD_NO='").append(hm.get("CARD_NO")).append("'");
            sql.append(" and EXPIRY_DATE='").append(hm.get("EXPIRY_DATE")).append("'");
            sql.append(" and LMS_INVOICE_NO='").append(hm.get("LMS_INVOICE_NO")).append("'");
            logger.debug(sql);
            try
            {
                DBService.getDBService().sqlAction(sql.toString(), conn, false);
                conn.commit();
                Thread.sleep(getSleepTime());
            }
            catch (SQLException e)
            {
                logger.warn(" sql:"+sql);
            }
        }
        
    }

    
    public static void main(String[] args)
    {
        String springFile = FilenameUtils.separatorsToSystem("config/batch/daycut/CheckLateUploadTxn/spring.xml");

        ApplicationContext context = new FileSystemXmlApplicationContext(springFile);

        CheckLateUploadTxn instance = (CheckLateUploadTxn) context.getBean("CheckLateUploadTxn");
        instance.setBatchDate(System.getProperty("date"));
        instance.run(null);
    }

    public String getBatchDate()
    {
        return batchDate;
    }

    public void setBatchDate(String batchDate)
    {
        this.batchDate = batchDate;
    }

    public int getSleepTime()
    {
        return sleepTime;
    }

    public void setSleepTime(int sleepTime)
    {
        this.sleepTime = sleepTime;
    }

}
