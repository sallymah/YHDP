package tw.com.hyweb.svc.yhdp.batch.framework.pathMatche;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;

import tw.com.hyweb.core.ftp.batch.framework.TransferUtil;
import tw.com.hyweb.core.ftp.batch.framework.ftp.TransferFileInfo;
import tw.com.hyweb.core.ftp.batch.framework.pathMatche.UploadPathMatcheWay;
import tw.com.hyweb.service.db.info.TbFileInfoInfo;
import tw.com.hyweb.service.db.info.TbOutctlInfo;
import tw.com.hyweb.service.db.info.TbPersoFactoryInfo;
import tw.com.hyweb.service.db.mgr.TbPersoFactoryMgr;
import tw.com.hyweb.util.ReleaseResource;
import tw.com.hyweb.util.string.StringUtil;

public class HGCardGroupUpPath extends UploadPathMatcheWay{

	private static final Logger LOG = Logger.getLogger(HGCardGroupUpPath.class);
	
	private static final String HG_CARD_GROUP_RREMOTE = "HG_CARD_GROUP";
	@Override
	public boolean matches(String remotePath) {
		// TODO Auto-generated method stub
		if ( !StringUtil.isEmpty(remotePath) ){
			return remotePath.contains(HG_CARD_GROUP_RREMOTE);
		}
		return false;
	}
	
	@Override
	public List<TransferFileInfo> getTransferFileInfo(Connection connection, String batchDate, 
			TbFileInfoInfo fileInfo, String remotePaths, String localPath, TbOutctlInfo outControl) throws Exception {
		// TODO Auto-generated method stub
		List<TransferFileInfo> transferFileInfos = new ArrayList<TransferFileInfo>();

		String remotePath = TransferUtil.arrangeFile(remotePaths.replaceAll(HG_CARD_GROUP_RREMOTE, outControl.getMemId()));
		TransferFileInfo transferFileInfo = new TransferFileInfo();
        transferFileInfo.setRemotePath(remotePath);
        if ( !transferFileInfo.getRemotePath().endsWith("/") ){
        	transferFileInfo.setRemotePath(TransferUtil.arrangeFile(transferFileInfo.getRemotePath()));
        }
        transferFileInfo.setLocalPath(TransferUtil.arrangeFile(localPath.replaceAll(HG_CARD_GROUP_RREMOTE, outControl.getMemId())));
        transferFileInfo.setName(outControl.getFullFileName());
        transferFileInfo.setSize(new File(transferFileInfo.getLocalPath() + outControl.getFullFileName()).length());
        transferFileInfo.setSubFileName(fileInfo.getSubFileName());
        
        transferFileInfos.add(transferFileInfo);
    	
    	return transferFileInfos;
	}
	
	@Override
	public String getFile(Connection connection, TbOutctlInfo outctlInfo,
			String relativePath) throws SQLException {
		// TODO Auto-generated method stub
		return relativePath.replaceAll(HG_CARD_GROUP_RREMOTE, outctlInfo.getMemId());
	}
}
