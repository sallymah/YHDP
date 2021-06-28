package tw.com.hyweb.svc.yhdp.batch.framework.pathMatche;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;

import tw.com.hyweb.core.ftp.batch.framework.TransferUtil;
import tw.com.hyweb.core.ftp.batch.framework.ftp.TransferFileInfo;
import tw.com.hyweb.core.ftp.batch.framework.pathMatche.UploadPathMatcheWay;
import tw.com.hyweb.service.db.info.TbFileInfoInfo;
import tw.com.hyweb.service.db.info.TbMemberInfo;
import tw.com.hyweb.service.db.info.TbOutctlInfo;
import tw.com.hyweb.service.db.info.TbPersoFactoryInfo;
import tw.com.hyweb.service.db.mgr.TbMemberMgr;
import tw.com.hyweb.service.db.mgr.TbPersoFactoryMgr;
import tw.com.hyweb.util.string.StringUtil;

public class AllMemberUpPath extends UploadPathMatcheWay{
	
	private static final Logger LOG = Logger.getLogger(AllMemberUpPath.class);
	
	private static final String ALLMEMBER = "ALLMEMBER";
	
	@Override
	public boolean matches(String remotePath) {
		// TODO Auto-generated method stub
		if ( !StringUtil.isEmpty(remotePath) ){
			return remotePath.contains(ALLMEMBER);
		}
		return false;
	}
	
	private List<TbMemberInfo> getMemberInfos(Connection connection) throws SQLException
    {
        Vector<TbMemberInfo> result = new Vector<TbMemberInfo>();
        new TbMemberMgr(connection).queryMultiple("", result);

        return result;
    }
	
	@Override
	public List<TransferFileInfo> getTransferFileInfo(Connection connection,
			String batchDate, TbFileInfoInfo fileInfo, String remotePaths,
			String localPath, TbOutctlInfo outControl) throws Exception {
		// TODO Auto-generated method stub
		List<TransferFileInfo> transferFileInfos = new ArrayList<TransferFileInfo>();
		
		for (TbMemberInfo memberInfo : getMemberInfos(connection))
        {
    		String remotePath = remotePaths.replaceAll(ALLMEMBER, memberInfo.getMemGroupId()+"/"+memberInfo.getMemId()+ "/");
    		TransferFileInfo transferFileInfo = new TransferFileInfo();
            transferFileInfo.setRemotePath(remotePath);
            if ( !transferFileInfo.getRemotePath().endsWith("/") ){
            	transferFileInfo.setRemotePath(TransferUtil.arrangeFile(transferFileInfo.getRemotePath()));
            }
            transferFileInfo.setLocalPath(localPath);
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
		return null;
	}
}
