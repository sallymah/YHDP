/*
 * (版權及授權描述)
 * Copyright 2006 (C) Hyweb Technology Coperation.
 * All Rights Reserved.
 *
 * 2007/12/10
 */
package tw.com.hyweb.svc.yhdp.batch.framework.ftp.strategy;

import tw.com.hyweb.core.ftp.batch.framework.ftp.DelegateTransferStrategy;
import tw.com.hyweb.core.ftp.batch.framework.ftp.TransferFileInfo;
import tw.com.hyweb.core.ftp.batch.framework.ftp.TransferStrategy;
import tw.com.hyweb.core.ftp.batch.framework.ftp.strategy.FileFilter;
import tw.com.hyweb.core.ftp.batch.framework.ftp.strategy.FileFilterImpl;
import tw.com.hyweb.service.db.info.TbFileInfoInfo;
import tw.com.hyweb.service.db.info.TbFtpInfoInfo;

/**
 * @author Clare
 * 
 */
public class ReOKFileDelete extends DelegateTransferStrategy
{
    private static final String ENABLE_OK_FLAG = "1";

    public ReOKFileDelete(TransferStrategy transferStrategy)
    {
        super(transferStrategy);
    }

    /**
     * 如果OK_FLAG為1的話，OK檔也要刪除
     * 
     * @see tw.com.hyweb.core.cp.batch.framework.ftp.DelegateTransferStrategy#transfer(tw.com.hyweb.service.db.info.TbFtpInfoInfo,
     *      tw.com.hyweb.service.db.info.TbFileInfoInfo,
     *      tw.com.hyweb.core.cp.batch.util.FTPUtil,
     *      tw.com.hyweb.core.cp.batch.framework.ftp.TransferFileInfo)
     */
    @Override
    public void delete(TbFtpInfoInfo ftpInfo, TbFileInfoInfo fileInfo, Object util, TransferFileInfo transferFileInfo) throws Exception
    {
        transferStrategy.delete(ftpInfo, fileInfo, util, transferFileInfo);

        if (ENABLE_OK_FLAG.equals(fileInfo.getOkFlag()))
        {
            transferStrategy.delete(ftpInfo, fileInfo, util, getTransferOKFileInfo(transferFileInfo));
        }
    }
    /**
     * 傳回此檔案對應的OK檔刪除資訊
     * 
     * @param transferFileInfo
     * @return
     */
    private TransferFileInfo getTransferOKFileInfo(TransferFileInfo transferFileInfo)
    {
        TransferFileInfo okFileInfo = new TransferFileInfo();
        okFileInfo.setRemotePath(transferFileInfo.getRemotePath());
        okFileInfo.setLocalPath(transferFileInfo.getLocalPath());
        okFileInfo.setSize(0);
        okFileInfo.setSubFileName(transferFileInfo.getSubFileName());
        
        String subFileName = "";
    	String fileName = "";
    	if (transferFileInfo.getSubFileName().contains("/")){
    		subFileName = transferFileInfo.getSubFileName().replaceAll("/", "");
    		fileName = transferFileInfo.getName().substring(0,transferFileInfo.getName().lastIndexOf(".")) + subFileName;
    	}
    	else{
    		subFileName = transferFileInfo.getSubFileName();
    		fileName = transferFileInfo.getName() + subFileName;
    	}
        
        okFileInfo.setName(fileName);

        return okFileInfo;
    }

}
