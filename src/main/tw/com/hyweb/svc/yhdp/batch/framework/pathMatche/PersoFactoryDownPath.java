package tw.com.hyweb.svc.yhdp.batch.framework.pathMatche;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import tw.com.hyweb.core.ftp.batch.framework.TransferUtil;
import tw.com.hyweb.core.ftp.batch.framework.ftp.TransferFileInfo;
import tw.com.hyweb.core.ftp.batch.framework.pathMatche.DownloadPathMatcheWay;
import tw.com.hyweb.core.ftp.batch.util.BatchUtil;
import tw.com.hyweb.service.db.info.TbBatchResultInfo;
import tw.com.hyweb.service.db.info.TbFileInfoInfo;
import tw.com.hyweb.service.db.info.TbFtpInfoInfo;
import tw.com.hyweb.service.db.info.TbPersoFactoryInfo;
import tw.com.hyweb.service.db.mgr.TbPersoFactoryMgr;
import tw.com.hyweb.util.string.StringUtil;

public class PersoFactoryDownPath extends DownloadPathMatcheWay{
	
	private static final Logger LOG = Logger.getLogger(PersoFactoryDownPath.class);
	
	private static final String PERSO_FACTORY_RREMOTE = "PERSO_FACTORY";

	@Override
	public boolean matches(String remotePath) {
		// TODO Auto-generated method stub
		if ( !StringUtil.isEmpty(remotePath) ){
			return remotePath.contains(PERSO_FACTORY_RREMOTE);
		}
		return false;
	}

	@Override
	public List<TransferFileInfo> getTransferFileInfos(Connection connection, TbFileInfoInfo fileInfo, 
			TbFtpInfoInfo ftpInfo, Object util, TbBatchResultInfo batchResultInfo, List jobMemIds ) throws Exception
	{
		// TODO Auto-generated method stub
		List<TransferFileInfo> transferFileInfos = new ArrayList<TransferFileInfo>();
		
		for (TbPersoFactoryInfo persoFactoryInfo : getTbPersoFactoryInfos(connection))
		{	
			String localPath = TransferUtil.arrangeFile((BatchUtil.getTempDirectory()  + File.separator) + (fileInfo.getLocalPath().replaceAll(PERSO_FACTORY_RREMOTE, persoFactoryInfo.getRemoteFolder())));
			String remotePath = TransferUtil.arrangeFile(ftpInfo.getRemotePath().replaceAll(PERSO_FACTORY_RREMOTE, persoFactoryInfo.getRemoteFolder()));
			//LOG.debug("remotePath: "+remotePath + " /   localPath: " + localPath);
		
			transferFileInfos.addAll(TransferUtil.getTransferFileInfos(fileInfo, ftpInfo, util, remotePath, localPath));
		}
		
		return transferFileInfos;
	}
	
	private List<TbPersoFactoryInfo> getTbPersoFactoryInfos(Connection connection) throws SQLException
	{
		Vector<TbPersoFactoryInfo> result = new Vector<TbPersoFactoryInfo>();
		new TbPersoFactoryMgr(connection).queryAll(result);
		return result;
	}

	@Override
	public File[] getFiles(Connection connection, TbFileInfoInfo fileInfo, String tempDir) throws SQLException {
		// TODO Auto-generated method stub
		
		File[] filesAll = new File[0];
		File[] files = new File[0];
		
		String parentLocalPath = FilenameUtils.separatorsToSystem((tempDir + File.separator) + (fileInfo.getLocalPath().substring(0, fileInfo.getLocalPath().indexOf(PERSO_FACTORY_RREMOTE))));
		String InnermostPath = fileInfo.getLocalPath().substring(fileInfo.getLocalPath().indexOf(PERSO_FACTORY_RREMOTE) + PERSO_FACTORY_RREMOTE.length())  + File.separator;
		for (TbPersoFactoryInfo persoFactoryInfo : getTbPersoFactoryInfos(connection)){
			File parentLocaldir = new File( parentLocalPath + persoFactoryInfo.getRemoteFolder() + InnermostPath );
			if (parentLocaldir.isDirectory() && parentLocaldir.listFiles() != null) {
                
				files = filesAll;
				filesAll = new File[parentLocaldir.listFiles().length + files.length];
				
				for ( int i =0; i< files.length; i++ ){
					filesAll[i] = files[i];
				}
				for ( int i =files.length; i< filesAll.length; i++ ){
					filesAll[i] = parentLocaldir.listFiles()[i-files.length];
				}
				
            }
		}
		
		return filesAll;
	}

}
