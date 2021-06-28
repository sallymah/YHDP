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

public class PersoFactoryUpPath extends UploadPathMatcheWay{

	private static final Logger LOG = Logger.getLogger(PersoFactoryUpPath.class);
	
	private static final String PERSO_FACTORY_RREMOTE = "PERSO_FACTORY";
	@Override
	public boolean matches(String remotePath) {
		// TODO Auto-generated method stub
		if ( !StringUtil.isEmpty(remotePath) ){
			return remotePath.contains(PERSO_FACTORY_RREMOTE);
		}
		return false;
	}

	private List<TbPersoFactoryInfo> getTbPersoFactoryInfos(String persoFactoryId, Connection connection) throws SQLException
	{
		Vector<TbPersoFactoryInfo> result = new Vector<TbPersoFactoryInfo>();
        new TbPersoFactoryMgr(connection).queryMultiple("PERSO_FACTORY_ID = '"+persoFactoryId+"'", result);
        return result;
	}
	
	@Override
	public List<TransferFileInfo> getTransferFileInfo(Connection connection, String batchDate, 
			TbFileInfoInfo fileInfo, String remotePaths, String localPath, TbOutctlInfo outControl) throws Exception {
		// TODO Auto-generated method stub
		List<TransferFileInfo> transferFileInfos = new ArrayList<TransferFileInfo>();
		String persoFactoryId = outControl.getFullFileName().substring(3, 5);
    	for (TbPersoFactoryInfo persoFactoryInfo : getTbPersoFactoryInfos(persoFactoryId, connection))
        {
    		String remotePath = TransferUtil.arrangeFile(remotePaths.replaceAll(PERSO_FACTORY_RREMOTE, persoFactoryInfo.getRemoteFolder()));
    		TransferFileInfo transferFileInfo = new TransferFileInfo();
            transferFileInfo.setRemotePath(remotePath);
            if ( !transferFileInfo.getRemotePath().endsWith("/") ){
            	transferFileInfo.setRemotePath(TransferUtil.arrangeFile(transferFileInfo.getRemotePath()));
            }
            transferFileInfo.setLocalPath(TransferUtil.arrangeFile(localPath.replaceAll(PERSO_FACTORY_RREMOTE, persoFactoryInfo.getRemoteFolder())));
            transferFileInfo.setName(outControl.getFullFileName());
            transferFileInfo.setSize(new File(transferFileInfo.getLocalPath() + outControl.getFullFileName()).length());
            transferFileInfo.setSubFileName(fileInfo.getSubFileName());
            
            transferFileInfos.add(transferFileInfo);
        }
    	
    	return transferFileInfos;
	}
	
	@Override
	public String getFile(Connection connection, TbOutctlInfo outctlInfo,
			String relativePath) throws SQLException {
		// TODO Auto-generated method stub
		
    	String persoFactoryId = outctlInfo.getFullFileName().substring(3, 5);
    	String remoteFolder = "";
    	// get all TB_PERSO_FACTORY
        String sql = "SELECT REMOTE_FOLDER FROM TB_PERSO_FACTORY WHERE PERSO_FACTORY_ID = '" +persoFactoryId+"'";
        Statement stmt = null;
        ResultSet rs = null;
        stmt = connection.createStatement();
        rs = stmt.executeQuery(sql);
        while (rs.next()) {
        	remoteFolder = rs.getString(1);
        }
        ReleaseResource.releaseDB(null, stmt, rs);
    	
		return relativePath.replaceAll(PERSO_FACTORY_RREMOTE, remoteFolder);
	}
}
