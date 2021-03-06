/**
 * changelog
 * --------------------
 * 20080529
 * duncan
 * rename PROC_DATE, PROC_TIME to SYS_DATE, SYS_TIME
 * --------------------
 */
/*
 * (版權及授權描述)
 * Copyright 2006 (C) Hyweb Technology Coperation.
 * All Rights Reserved.
 *
 * 2007/12/7
 */
package tw.com.hyweb.svc.yhdp.batch.framework.ftp;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;
import java.util.List;
import java.util.Vector;

import javax.sql.DataSource;

import org.apache.log4j.Logger;

import tw.com.hyweb.core.cp.batch.framework.AbstractBatchBasic;
import tw.com.hyweb.core.ftp.batch.framework.ftp.TransferFileInfo;
import tw.com.hyweb.core.ftp.batch.framework.ftp.TransferStrategy;
import tw.com.hyweb.core.ftp.batch.util.FTPSUtil;
import tw.com.hyweb.core.ftp.batch.util.FTPUtil;
import tw.com.hyweb.core.ftp.batch.util.SFTPUtil;
import tw.com.hyweb.core.cp.common.misc.Constants;
import tw.com.hyweb.core.cp.common.misc.Layer2Util;
import tw.com.hyweb.service.db.info.TbFileInfoInfo;
import tw.com.hyweb.service.db.info.TbFileInfoPK;
import tw.com.hyweb.service.db.info.TbFtpInfoInfo;
import tw.com.hyweb.service.db.mgr.TbFileInfoMgr;
import tw.com.hyweb.service.db.mgr.TbFtpInfoMgr;
import tw.com.hyweb.util.ReleaseResource;
import tw.com.hyweb.util.date.DateUtil;
import tw.com.hyweb.util.string.StringUtil;

/**
 * @author Clare
 * 
 */
public class RefileDeleteProcessor extends AbstractBatchBasic
{
    private static final Logger LOG = Logger.getLogger(RefileDeleteProcessor.class);

    private static final String SUCCESS_TRANSFER_STATUS = "1";
    private static final String FAILURE_TRANSFER_STATUS = "0";
    private static final String FTP_RETRY_SLEEP = "FTP_RETRY_SLEEP";
    private static final String FTP_RETRY_COUNT = "FTP_RETRY_COUNT";
    private static final String FTP_REFILE_DEL_DAYS = "FTP_REFILE_DEL_DAYS";

    private boolean passiveMode = true;

    private final TransferStrategy transferStrategy;

    private DataSource dataSource;

    private String batchDate;
    private String batchTime = DateUtil.getTodayString().substring(8);
    
    private int retryCount = 0;
    private int retrySleep = 0;
    private int RefileDeleteDays = 0;
    
    // 須檢查刪除的TB_FILE_INFO.FILE_NAME
    private List fileNames = null;

    public RefileDeleteProcessor(TransferStrategy transferStrategy)
    {
        this.transferStrategy = transferStrategy;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * tw.com.hyweb.core.cp.batch.framework.IBatchProcess#process(java.lang.
     * String[])
     */
    public void process(String[] argv) throws Exception
    {
        Connection connection = null;

        try
        {
            connection = dataSource.getConnection();
            
            //retry 次數
            String sysRetryCount = Layer2Util.getBatchConfig(FTP_RETRY_COUNT);
            retryCount = !isBlankOrNull(sysRetryCount) ? Integer.parseInt(sysRetryCount) : 0;
            
            //retry 每次間隔
            String sysRetrySleep = Layer2Util.getBatchConfig(FTP_RETRY_SLEEP);
            retrySleep = !isBlankOrNull(sysRetrySleep) ? Integer.parseInt(sysRetrySleep) : 0;
            
            //刪除重複上傳天數
            String sysRefileDeleteDays = Layer2Util.getBatchConfig(FTP_REFILE_DEL_DAYS);
            RefileDeleteDays = !isBlankOrNull(sysRefileDeleteDays) ? Integer.parseInt(sysRefileDeleteDays) : 0;
            
            LOG.info("retryCount: "+retryCount);

            for (TbFtpInfoInfo ftpInfo : getFtpInfos(connection))
            {
            	if ( fileNames.contains(ftpInfo.getFileName()) ){
	            	LOG.info("ftpInfo: "+ ftpInfo.getFileName());
	                Savepoint savepoint = connection.setSavepoint();
	
	                try
	                {
	                    transferFiles(connection, ftpInfo, getFileInfo(connection, ftpInfo));
	                }
	                catch (Exception e)
	                {
	                    LOG.warn("exception when handle this ftp info:" + ftpInfo, e);
	                    connection.rollback(savepoint);
	
	                    setRcode(Constants.RCODE_2001_WARN);
	                }
	
	                connection.commit();
            	}
            }

            connection.commit();
        }
        catch (Exception e)
        {
            LOG.warn("exception when ftp in", e);
            connection.rollback();

            throw e;
        }
        finally
        {
        	String errorDesc = getErrorDesc();
        	if ( getErrorDesc() != null && getErrorDesc() != ""){
	        	if ( errorDesc.length() > 280 )
	        		setErrorDesc(errorDesc.substring(0, 280));
        	}
            ReleaseResource.releaseDB(connection);
        }
    }

    /**
     * 傳送此筆ftp info及file info對應的所有檔案
     * 
     * @param connection
     * @param ftpInfo
     * @param fileInfo
     * @throws Exception
     */
    private void transferFiles(Connection connection, TbFtpInfoInfo ftpInfo, TbFileInfoInfo fileInfo) throws Exception
    {
    	if ( ftpInfo.getFtpProtocol().equals("0") ){
    		FTPUtil ftpUtil = new FTPUtil(ftpInfo.getFtpIp(), Integer.parseInt(ftpInfo.getFtpPort()), ftpInfo.getFtpLoginId(), ftpInfo.getFtpLoginPwd());
            transferFiles (connection, ftpInfo, fileInfo, ftpUtil);
    	}
    	else if ( ftpInfo.getFtpProtocol().equals("1") ){
    		SFTPUtil sftpUtil = new SFTPUtil(ftpInfo.getFtpIp(), Integer.parseInt(ftpInfo.getFtpPort()), ftpInfo.getFtpLoginId(), ftpInfo.getFtpLoginPwd());
            transferFiles (connection, ftpInfo, fileInfo, sftpUtil);
    	}
    	else if ( ftpInfo.getFtpProtocol().equals("2") ){
    		FTPSUtil ftpsUtil = new FTPSUtil(ftpInfo.getFtpIp(), Integer.parseInt(ftpInfo.getFtpPort()), ftpInfo.getFtpLoginId(), ftpInfo.getFtpLoginPwd());
            transferFiles (connection, ftpInfo, fileInfo, ftpsUtil);
    	}
    	else{
    		LOG.error(ftpInfo.getFileName() + " is Error: type=" + ftpInfo.getFtpProtocol());
    	}
    	
    }
    
    private void transferFiles(Connection connection, TbFtpInfoInfo ftpInfo, TbFileInfoInfo fileInfo, FTPUtil ftpUtil) throws Exception
    {
    	LOG.info("transferFiles.FTPUtil: "+ftpInfo.getFileName());
    	try
        {
            LOG.info("ftp login:" + ftpInfo.getFtpIp());

            if (passiveMode)
            {
                ftpUtil.ftpClient.enterLocalPassiveMode();
            }


            ftpUtil.connect();

            for (TransferFileInfo transferFileInfo : transferStrategy.getTransferFileInfos(connection, batchDate, ftpInfo, fileInfo, ftpUtil, null, null))
            {
            	boolean isTransfered = isTransfered(connection, ftpInfo, transferFileInfo);
            	
            	if (isTransfered)
                {
            		deleteFile(connection, ftpInfo, fileInfo, ftpUtil, transferFileInfo);
                }
                else
                {
                    LOG.warn(transferFileInfo.getName() + " already transfered! local path:" + transferFileInfo.getLocalPath() + " remote path:" + transferFileInfo.getRemotePath() + " direction:" + transferStrategy.getTransferDirction());
                }
            }
        }
        finally
        {
            try
            {
                LOG.info("ftp logout and disconnect:" + ftpInfo.getFtpIp());

                ftpUtil.logout();
                ftpUtil.disconnect();
            }
            catch (Exception e)
            {
                LOG.warn("exception when logout or disconnect ftp", e);
            }
        }
    }
    
    private void transferFiles(Connection connection, TbFtpInfoInfo ftpInfo, TbFileInfoInfo fileInfo, SFTPUtil sftpUtil) throws Exception
    {
    	LOG.info("transferFiles.SFTPUtil: "+ftpInfo.getFileName());
    	try
        {
            LOG.info("ftp login:" + ftpInfo.getFtpIp());

            sftpUtil.connect();
            
            if ( !sftpUtil.sftpClient.isConnected() ){
            	for( int i = 0; i < retryCount; i++ ){
            		LOG.info("Sleep: " + getRetrySleep() + "(ms)");
            		Thread.sleep(getRetrySleep());
            		LOG.info("Connect retry: " + (i+1));
            		sftpUtil.connect();
            		if ( sftpUtil.sftpClient.isConnected() )
            			break;
            	}
            	if ( !sftpUtil.sftpClient.isConnected() )
            		throw new Exception("ftp connect fail:" + ftpInfo.getFtpIp());
            }
            
            for (TransferFileInfo transferFileInfo : transferStrategy.getTransferFileInfos(connection, batchDate, ftpInfo, fileInfo, sftpUtil, null, null))
            {
            	boolean isTransfered = isTransfered(connection, ftpInfo, transferFileInfo);
            	
            	if (isTransfered)
                {
            		deleteFile(connection, ftpInfo, fileInfo, sftpUtil, transferFileInfo);
                }
            }
        }
        finally
        {
            try
            {
                if ( sftpUtil.sftpClient.isConnected() ){
                	LOG.info("ftp logout and disconnect:" + ftpInfo.getFtpIp());
	                sftpUtil.logout();
	                sftpUtil.disconnect();
                }
            }
            catch (Exception e)
            {
                LOG.warn("exception when logout or disconnect ftp", e);
            }
        }
    }
    
    private void transferFiles(Connection connection, TbFtpInfoInfo ftpInfo, TbFileInfoInfo fileInfo, FTPSUtil ftpsUtil) throws Exception
    {
    	LOG.info("transferFiles.FTPSUtil: "+ftpInfo.getFileName());
    	try
        {
            LOG.info("ftp login:" + ftpInfo.getFtpIp()+":"+ftpInfo.getFtpPort());


            ftpsUtil.connect();
            
            if (passiveMode)
            {
            	ftpsUtil.ftpsClient.execPBSZ(0);		// 協商保護緩衝區 SSL/TLS模式中必須為0，因為SSL/TLS有自己的安全緩衝區設定，所以要關閉一般的協商保護緩衝區
            	ftpsUtil.ftpsClient.execPROT("P");		// "C"未保護級別，"P"保護級別
            	//ftpsUtil.ftpsClient.execAUTH("SSL");	// 指定增強認證方法,SSL或TLS
            	ftpsUtil.ftpsClient.enterLocalPassiveMode();
            	ftpsUtil.ftpsClient.setUseEPSVwithIPv4(true);
            }

            for (TransferFileInfo transferFileInfo : transferStrategy.getTransferFileInfos(connection, batchDate, ftpInfo, fileInfo, ftpsUtil, null, null))
            {
            	boolean isTransfered = isTransfered(connection, ftpInfo, transferFileInfo);
            	
            	if (isTransfered)
                {
            		deleteFile(connection, ftpInfo, fileInfo, ftpsUtil, transferFileInfo);
                }
                else
                {
                    LOG.warn(transferFileInfo.getName() + " already transfered! local path:" + transferFileInfo.getLocalPath() + " remote path:" + transferFileInfo.getRemotePath() + " direction:" + transferStrategy.getTransferDirction());
                }
            }
        }
        finally
        {
            try
            {
            	LOG.info("ftp Reply:" + ftpsUtil.getReplyCode() + ftpsUtil.getReplyString());
                LOG.info("ftp logout and disconnect:" + ftpInfo.getFtpIp());

                ftpsUtil.logout();
                ftpsUtil.disconnect();
            }
            catch (Exception e)
            {
                LOG.warn("exception when logout or disconnect ftp", e);
            }
        }
    }
    
    /**
     * 從TB_FTP_LOG檢查此筆檔案是否已成功傳送過
     * 
     * @param connection
     * @param ftpInfo
     * @param transferFileInfo
     * @return True if log exists, false if not.
     * @throws SQLException
     */
    private boolean isTransfered(Connection connection, TbFtpInfoInfo ftpInfo, TransferFileInfo transferFileInfo) throws SQLException
    {
        Statement stmt = null;
        ResultSet rs = null;
        int count = 0;
        String seqnoSql = "SELECT COUNT(1) FROM TB_FTP_LOG " +
        		"WHERE FTP_IP = '" + ftpInfo.getFtpIp() + "' " +
        		"AND REMOTE_PATH = '" + transferFileInfo.getRemotePath() + "' " +
        		"AND FULL_FILE_NAME = '" + transferFileInfo.getName() + "' " +
        		"AND IN_OUT = '" + transferStrategy.getTransferDirction() + "' " +
        		"AND STATUS = '" + SUCCESS_TRANSFER_STATUS + "' " +
        		"AND SYS_DATE <= TO_CHAR(TO_DATE( '"+batchDate+"', 'YYYYMMDD') - " + RefileDeleteDays +", 'YYYYMMDD')" ;
        LOG.info(seqnoSql);
        try {
        	stmt = connection.createStatement();
              rs = stmt.executeQuery(seqnoSql);
              while (rs.next()) {
            	  count = rs.getInt(1);
             }
        }
        finally {
              ReleaseResource.releaseDB(null, stmt, rs);
        }
        
        
        return count > 0;
    }

    private void deleteFile(Connection connection, TbFtpInfoInfo ftpInfo, TbFileInfoInfo fileInfo, FTPUtil ftpUtil, TransferFileInfo transferFileInfo) throws SQLException
    {
        try
        {
            transferStrategy.delete(ftpInfo, fileInfo, ftpUtil, transferFileInfo);
        }
        catch (Exception e)
        {
            LOG.warn("exception when delete file:", e);
            setRcode(Constants.RCODE_2001_WARN);
            setErrorDesc(e.getMessage());
        }
    }
    
    private void deleteFile(Connection connection, TbFtpInfoInfo ftpInfo, TbFileInfoInfo fileInfo, SFTPUtil sftpUtil, TransferFileInfo transferFileInfo) throws SQLException
    {
        try
        {
            transferStrategy.delete(ftpInfo, fileInfo, sftpUtil, transferFileInfo);
        }
        catch (Exception e)
        {
            LOG.warn("exception when delete file:", e);
            setRcode(Constants.RCODE_2001_WARN);
            setErrorDesc(e.getMessage());
        }
    }
    
    private void deleteFile(Connection connection, TbFtpInfoInfo ftpInfo, TbFileInfoInfo fileInfo, FTPSUtil ftpsUtil, TransferFileInfo transferFileInfo) throws SQLException
    {
        try
        {
            transferStrategy.delete(ftpInfo, fileInfo, ftpsUtil, transferFileInfo);
        }
        catch (Exception e)
        {
            LOG.warn("exception when delete file:", e);
            setRcode(Constants.RCODE_2001_WARN);
            setErrorDesc(e.getMessage());
        }
    }
    
    /**
     * 根據ftp info傳回對應的file info
     * 
     * @param connection
     * @param info
     * @return fileInfo of this ftpInfo
     * @throws SQLException
     */
    private TbFileInfoInfo getFileInfo(Connection connection, TbFtpInfoInfo info) throws SQLException
    {
        TbFileInfoPK pk = new TbFileInfoPK();
        pk.setFileName(info.getFileName());
        pk.setInOut(transferStrategy.getTransferDirction());

        return new TbFileInfoMgr(connection).querySingle(pk);
    }

    /**
     * 取出所有ftp info設定
     * 
     * @param connection
     * @return all valid ftpInfos
     * @throws SQLException
     */
    private List<TbFtpInfoInfo> getFtpInfos(Connection connection) throws SQLException
    {
        Vector<TbFtpInfoInfo> result = new Vector<TbFtpInfoInfo>();
        new TbFtpInfoMgr(connection).queryMultiple("in_out='" + transferStrategy.getTransferDirction() + "' and " + getFileNameCondition(), result);

        for (TbFtpInfoInfo info : result)
        {
            if (!info.getRemotePath().endsWith("/"))
            {
                info.setRemotePath(info.getRemotePath() + "/");
            }
        }

        return result;
    }

    /**
     * 判斷是否要抓取所有的file或是參數指定的file
     * 
     * @return
     */
    private String getFileNameCondition()
    {
        if (!StringUtil.isEmpty(System.getProperty("fileName")))
        {
            return "file_name='" + System.getProperty("fileName") + "'";
        }

        return "file_name in (select file_name from tb_file_info where in_out='" + transferStrategy.getTransferDirction() + "')";
    }

    public static boolean isBlankOrNull(String value) {
		return (value == null || value.trim().equals(""));
	}

    /**
     * @param isPassiveMode
     *            the isPassiveMode to set
     */
    public void setPassiveMode(boolean passiveMode)
    {
        this.passiveMode = passiveMode;
    }

    /**
     * @param dataSource
     *            the dataSource to set
     */
    public void setDataSource(DataSource dataSource)
    {
        this.dataSource = dataSource;
    }

    
    public int getRetrySleep() {
		return retrySleep;
	}

	public void setRetrySleep(int retrySleep) {
		this.retrySleep = retrySleep;
	}

	/**
     * @param batchDate
     *            the batchDate to set
     */
    public void setBatchDate(String batchDate)
    {
        this.batchDate = batchDate;
    }

	public List getFileNames() {
		return fileNames;
	}

	public void setFileNames(List fileNames) {
		this.fileNames = fileNames;
	}
    
    
}
