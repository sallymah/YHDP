package tw.com.hyweb.svc.yhdp.batch.framework.cursorThread;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import tw.com.hyweb.core.cp.batch.framework.generic.BatchJob;
import tw.com.hyweb.service.db.info.TbBatchResultInfo;
import tw.com.hyweb.util.DbUtil;
import tw.com.hyweb.util.ReleaseResource;


public abstract class CursorBatchJobFactory implements BatchJobFactory{
	private final static Logger LOGGER = Logger.getLogger(CursorBatchJobFactory.class);

    private Statement statement;
    private ResultSet resultSet;
    private String[] columnNames;

    private boolean hasNext;
    
    private int fetchCount;
    private int fetchTimes;
    private int batchNuber;
    
    public void divideBatch(Connection connection, String batchDate, int fetchCount, TbBatchResultInfo tbBatchResultInfo) throws Exception
    {
    	this.fetchCount = fetchCount;
    	this.batchNuber = 0;
    	
    	if ( fetchCount > 0 ){
	    	String sql =  "SELECT COUNT(*) FROM (" + getSQL(batchDate, tbBatchResultInfo) + ")";
	    	
	    	int dataCount = DbUtil.getInteger(sql, connection);
	    	
	    	LOGGER.info("redeem txn total record: " + dataCount);
	
	        fetchTimes = dataCount / fetchCount;
	        if(dataCount % fetchCount != 0){
	        	fetchTimes++;
			}
    	}
    	else {
    		fetchTimes = 1;
    	}
    	LOGGER.info("Divide " +fetchTimes+ " Batch.");
    }
    
    public boolean hasBatchNext()
    {
        return batchNuber < fetchTimes;
    }
    
    /**
     * 使用statement執行sql並取得result set及meta data
     * 
     * @see tw.com.hyweb.core.cp.batch.framework.generic.BatchJobFactory#init(java.sql.Connection,
     *      java.lang.String)
     */
    public void init(Connection connection, String batchDate, TbBatchResultInfo tbBatchResultInfo) throws Exception
    {
        String sql = "";
        
        //需分批撈
        if ( fetchTimes > 1 ){
        	sql = getlistMultipleSql(batchDate, batchNuber, fetchCount);
        }
        //不須分批或只有一批
        else{
        	sql = getSQL(batchDate, tbBatchResultInfo);
        }
        
        LOGGER.debug("execute sql:" + sql);

        statement = connection.createStatement();
        resultSet = statement.executeQuery(sql);

        ResultSetMetaData metaData = resultSet.getMetaData();
        columnNames = new String[metaData.getColumnCount()];

        for (int i = 0; i < columnNames.length; ++i)
        {
            columnNames[i] = metaData.getColumnName(i + 1);
        }

        batchNuber ++;
        hasNext = resultSet.next();
    }

    /**
     * 根據resultSet.hasNext的結果來判斷是否還有下一筆資料
     * 
     * @see tw.com.hyweb.core.cp.batch.framework.generic.BatchJobFactory#hasNext()
     */
    public boolean hasNext()
    {
        return hasNext;
    }

    /**
     * 回傳下一個batch job
     * 
     * @see tw.com.hyweb.core.cp.batch.framework.generic.BatchJobFactory#next(java.sql.Connection,
     *      java.lang.String)
     */
    public BatchJob next(Connection connection, String batchDate) throws Exception
    {
        BatchJob job = makeBatchJob(getResultMap());
        hasNext = resultSet.next();

        return job;
    }

    /**
     * 組成一個result map，value皆為String格式
     * 
     * @return
     * @throws SQLException
     */
    private Map<String, String> getResultMap() throws SQLException
    {
        Map<String, String> result = new HashMap<String, String>();

        for (int i = 0; i < columnNames.length; ++i)
        {
            result.put(columnNames[i].toUpperCase(), resultSet.getString(i + 1));
        }

        return result;
    }

    /**
     * release statement and resultSet
     * 
     * @see tw.com.hyweb.core.cp.batch.framework.generic.BatchJobFactory#destroy()
     */
    public void destroy()
    {
        ReleaseResource.releaseDB(null, statement, resultSet);
    }

    /**
     * preCondition SQL command
     * 
     * @param batchDate
     * @return
     * @throws Exception
     */
    protected abstract String getlistMultipleSql(String batchDate, int batchNuber, int fetchCount) throws Exception;
    
    /**
     * preCondition SQL command
     * 
     * @param batchDate
     * @return
     * @throws Exception
     */
    protected abstract String getSQL(String batchDate, TbBatchResultInfo tbBatchResultInfo) throws Exception;

    /**
     * 根據result map建立batch job
     * 
     * @param resultMap
     * @return
     * @throws Exception
     */
    protected abstract BatchJob makeBatchJob(Map<String, String> resultMap) throws Exception;
}
