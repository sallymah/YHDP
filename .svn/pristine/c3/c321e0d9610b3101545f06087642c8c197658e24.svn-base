package tw.com.hyweb.svc.yhdp.batch.framework.traffics;

import java.util.HashMap;

import tw.com.hyweb.service.db.info.TbMemberInfo;
import tw.com.hyweb.service.db.info.TbZipLogInfo;

public class dhlrt extends FilenameBean{

	//[_.]
	//TXN_{SOURCEID}_{TERMINALID}_YYYYMMDDHHMMSS_NN.dat_YYYYMMDDHHMMSS
	@Override
	public void initial(String fullFileName, HashMap members) throws Exception {
		
		String[] AfterSplit = fullFileName.split("["+getSeparatorString()+"]");
		
		String transFileAllas = AfterSplit[1];
		String DateTimeSeqno = AfterSplit[2];
		String seqno = DateTimeSeqno.substring(3);
		
		String unzipDateTime = AfterSplit[4];
		
		
		/*String DateTime = AfterSplit[3];
		String seqno = AfterSplit[4];
		String unzipDateTime = AfterSplit[5];*/
		
		TbMemberInfo tbMemberInfo = (TbMemberInfo) members.get(transFileAllas);
		
		if ( tbMemberInfo != null ){
			setSeqno(seqno);
			setMemGroupId(tbMemberInfo.getMemGroupId());
			setMemId(tbMemberInfo.getMemId());
			setExpSeqno(getOutctlSeqno());
			setFullFileNameR(getFileNameR() + "_" + DateTimeSeqno + ".DAT_" + unzipDateTime );
		}
	}

	@Override
	public void clear( ) {
		// TODO Auto-generated method stub
		setSeqno("");
	    setMemGroupId("");
	    setMemId("");
	    setFullFileNameR("");
	    setFileDate("");
	}
	
	@Override
	public String getFullFileNameRZip(TbZipLogInfo zipLogInfo) {
		// TODO Auto-generated method stub

		String fileZipName = getFileNameZip().split("["+getSeparatorString()+"]")[0];
		String fileZipNameR = getFileNameRZip().split("["+getSeparatorString()+"]")[0];
		String fullFileNameZip = zipLogInfo.getZipName();
		String fullFileNameRZip =  fullFileNameZip.replaceAll(fileZipName, fileZipNameR);
		
		return fullFileNameRZip;
	}
	
	@Override
	public String getFullFileNameR(TbZipLogInfo zipLogInfo) {
		// TODO Auto-generated method stub
		
		//rename 去掉_YYYYMMDDHHMMSS
		String fullFileNameR = zipLogInfo.getExpName().substring(0,zipLogInfo.getExpName().length()-15);
		
		return fullFileNameR;
	}

	public void initialZip(String zipFullFileName, HashMap members)
			throws Exception {
		
		String[] AfterSplit = zipFullFileName.split("["+getSeparatorString()+"]");
		
		String transFileAllas = AfterSplit[1];
		String dateTimeSeqno = AfterSplit[2];
		
		TbMemberInfo tbMemberInfo = (TbMemberInfo) members.get(transFileAllas);
		
		if ( tbMemberInfo != null ){
			setFileDate(dateTimeSeqno.substring(0,8));
			setSeqno(dateTimeSeqno.substring(dateTimeSeqno.length()-3, dateTimeSeqno.length()));
			setMemGroupId(tbMemberInfo.getMemGroupId());
			setMemId(tbMemberInfo.getMemId());
		}
		
	}
}
