package tw.com.hyweb.util;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import tw.com.hyweb.core.yhdp.batch.util.BatchUtil;
import tw.com.hyweb.service.db.info.TbSysConfigInfo;
import tw.com.hyweb.service.db.mgr.TbSysConfigMgr;
import tw.com.hyweb.svc.yhdp.online.util.CipherUtil;
import tw.com.hyweb.util.string.StringUtil;
import javax.crypto.Cipher;

public class BatchUtils {
	
	private final static Logger logger = Logger.getLogger(BatchUtils.class); 
	private static Connection conn;
	private static HashMap<String, TbSysConfigInfo> sysConfigInfos = new HashMap<String, TbSysConfigInfo>();
	
	public BatchUtils() {
	}
	
	static {
		conn = BatchUtil.getConnection();
		
		TbSysConfigMgr mgr = new TbSysConfigMgr(conn);
        Vector<TbSysConfigInfo> results = new Vector<TbSysConfigInfo>();
        try {
			mgr.queryAll(results, "PARM");
			for (TbSysConfigInfo sysConfigInfo : results) {
	            sysConfigInfos.put(sysConfigInfo.getParm(), sysConfigInfo);
	        }
		} catch (SQLException e) {
			e.printStackTrace();
		}    
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			DOMConfigurator.configureAndWatch("config/batch/log4j.xml", 600000);
			logger.info("decript: " + encript("我是周儀亭周儀亭周儀亭周儀亭周儀亭周儀亭周儀亭周儀亭周儀亭周儀亭周儀亭周儀亭周儀亭周儀亭周儀亭周儀亭周儀亭周儀亭周儀亭周儀亭周儀亭周儀亭周儀亭周儀亭周儀亭周儀亭周儀亭周儀亭周儀亭周儀亭周儀亭周儀亭周哈"));
			logger.info("decript: " + decript("pxBkSvAEplN5ADYBX46K+5J2/2f+LX/pozUoL3sULQyvr/sibHAxf78RhhRsbJPyGA8I5EZq6NJ+ok9i7hmFL3quBY1wqTCReVkW4gag+LucOoxrJzfnFYB1XXFdHppePuo3XC3qzJj9t7qCUEEDqjBqb0uYkp88DduN4yvXgcjGF7cLBpTjNPenD4WUtAnsnfnHTfAgQp7KSsEJpyJ/20ANWP5VkbKQtAk/yVOs/UnbIJnwr2ZUumLWE/PutxILzpuSvT7QA7TxNX0zFSmQ8id4uBDJipptOheI27JC1LHp0NEC7xwDf3vPkESCfReS7yn9vKRZj8lsLIDmQMGz5MYnkIL+M55Uyyyx2R3oaMcjUZkuY2tbLCnoWajJ3JipPuDoDn2jHd6GDbhhgLYNBA=="));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static String getSysConfigValue(String parm) throws Exception
    {   
		return sysConfigInfos.get(parm).getValue();
    }
	
	public static String encript(String data) throws Exception
    {   
    	boolean encryptFlag = false;
    	String custKey="";
    	String custIv="";
    	String isCustEncript="";
    	
    	if (sysConfigInfos != null)
    	{
    		custKey = getSysConfigValue("CUST_KEY");
    		logger.info("custKey: " + custKey);
   	  
    		custIv = getSysConfigValue("CUST_IV");
    		logger.info("custIv: " + custIv);
    	   	  
    		isCustEncript = getSysConfigValue("IS_CUST_ENCRIPT");
    		logger.info("isCustEncript: " + isCustEncript);
    	   	  
    		encryptFlag = (Integer.valueOf(isCustEncript).intValue() == 1);
    	}
    	else {
    		logger.warn("sysConfigInfos is null !");
    	}
      
    	String encript = data;
    	if ((encryptFlag) && (!StringUtil.isEmpty(data)))
    	{
    		encript = CipherUtil.encryptApi(true, Cipher.ENCRYPT_MODE, data, custKey, custIv);
    	}
    	return encript;
    }
	
	public static String decript(String data) throws Exception
    {   
    	boolean encryptFlag = false;
    	String custKey="";
    	String custIv="";
    	String isCustEncript="";
    	
    	if (sysConfigInfos != null)
    	{
    		custKey = getSysConfigValue("CUST_KEY");
    		logger.info("custKey: " + custKey);
   	  
    		custIv = getSysConfigValue("CUST_IV");
    		logger.info("custIv: " + custIv);
    	   	  
    		isCustEncript = getSysConfigValue("IS_CUST_ENCRIPT");
    		logger.info("isCustEncript: " + isCustEncript);
    	   	  
    		encryptFlag = (Integer.valueOf(isCustEncript).intValue() == 1);
    	}
    	else {
    		logger.warn("sysConfigInfos is null !");
    	}
      
    	String decript = data;
    	if ((encryptFlag) && (!StringUtil.isEmpty(data)))
    	{
    		decript = CipherUtil.encryptApi(true, Cipher.DECRYPT_MODE, data, custKey, custIv);
    	}
    	return decript;
    }
	
	public static String getNextSec(String time, int n) throws ParseException
    {
    	SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HHmmss");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(TIME_FORMAT.parse(time));
        calendar.add(Calendar.SECOND, n);

        return TIME_FORMAT.format(calendar.getTime());
    }
	
	public static int getNextValFromSeq(Connection conn, String seqName) throws SQLException
	{
		if (!StringUtil.isEmpty(seqName))
		{
			StringBuffer cmd = new StringBuffer();
			cmd.append("select ");
			cmd.append(seqName);
			cmd.append(".NEXTVAL from DUAL");

			return DbUtil.getInteger(cmd.toString(), conn);
		}
		return 0;
	}
}
