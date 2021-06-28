package tw.com.hyweb.svc.yhdp.batch.preoperation.RejectApploadExpiryModify;

import java.sql.Connection;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;

import tw.com.hyweb.core.cp.batch.framework.generic.BatchJob;
import tw.com.hyweb.core.cp.batch.framework.generic.BatchJobException;
import tw.com.hyweb.core.cp.batch.util.BatchUtil;
import tw.com.hyweb.service.db.info.TbAppointReloadInfo;
import tw.com.hyweb.service.db.info.TbAppointReloadUptInfo;
import tw.com.hyweb.service.db.info.TbAppointReloadUptPK;
import tw.com.hyweb.service.db.mgr.TbAppointReloadMgr;
import tw.com.hyweb.service.db.mgr.TbAppointReloadUptMgr;
import tw.com.hyweb.util.date.DateUtil;
import tw.com.hyweb.util.string.StringUtil;

public class RejectApploadExpiryModifyJob implements BatchJob {
	
	private static final Logger logger = Logger.getLogger(RejectApploadExpiryModifyJob.class);
	
	private final TbAppointReloadInfo info;
	private final String sysTime;
	private final String batchNo;

	public RejectApploadExpiryModifyJob(TbAppointReloadInfo info, String sysTime, String batchNo) {
		// TODO Auto-generated constructor stub
		this.info = info;
		this.sysTime = sysTime;
		this.batchNo = batchNo;
	}

	@Override
	public void action(Connection conn, String batchDate) throws Exception {
		// TODO Auto-generated method stub
		String isUpt = info.getIsUpt();
		
		info.setRejectSrc("B");
		info.setRejectDate(batchDate);
		info.setIsUpt("0");
		//YHDP特殊要求狀態update 9
		info.setStatus("9");
		
		TbAppointReloadUptMgr appUptMgr = new TbAppointReloadUptMgr(conn);
		// IS_UPT = 1 修改中
		if (isUpt.equalsIgnoreCase("1")){
			//update upt

			Vector<TbAppointReloadUptInfo> result = new Vector<TbAppointReloadUptInfo>();

			StringBuffer where = new StringBuffer();
			where.append(" BONUS_BASE = ").append(StringUtil.toSqlValueWithSQuote(info.getBonusBase()));
			where.append(" AND BALANCE_TYPE = ").append(StringUtil.toSqlValueWithSQuote(info.getBalanceType()));
			where.append(" AND BALANCE_ID = ").append(StringUtil.toSqlValueWithSQuote(info.getBalanceId()));
			where.append(" AND AR_SERNO = ").append(StringUtil.toSqlValueWithSQuote(info.getArSerno()));
			where.append(" AND UPT_STATUS = '2'");
			where.append(" AND APRV_STATUS = '0'");
			where.append(" AND UI_SRC = '2'");

			new TbAppointReloadUptMgr(conn).queryMultiple(where.toString(), result);
			
			if (result.size() != 1){
				//UPT不等於一筆皆為有問題
				throw new Exception
						("UPT COUNT <> 1 " +
						"BONUS_BASE = " + info.getBonusBase() + 
						",BALANCE_TYPE = " + info.getBalanceType() + 
						",BALANCE_ID = " + info.getBalanceId() + 
						",AR_SERNO = " + info.getArSerno());
			}
			
			TbAppointReloadUptInfo uptInfo = result.get(0);
			
			uptInfo.setAprvUserid("batch");
			uptInfo.setAprvDate(batchDate);
			uptInfo.setAprvTime(sysTime);
			uptInfo.setAprvStatus("2");
			
			appUptMgr.update(uptInfo);
			
		}
		else{
			
			//insert upt
			TbAppointReloadUptInfo uptInfo = new TbAppointReloadUptInfo();

			uptInfo.setBonusBase(info.getBonusBase());
			uptInfo.setBalanceType(info.getBalanceType());
			uptInfo.setBalanceId(info.getBalanceId());
			uptInfo.setExpiryDate(info.getExpiryDate());
			uptInfo.setArSerno(info.getArSerno());
			uptInfo.setArSrc(info.getArSrc());
			uptInfo.setAcqMemId(info.getAcqMemId());
			uptInfo.setMerchId(info.getMerchId());
			uptInfo.setValidSdate(info.getValidSdate());
			uptInfo.setValidEdate(info.getValidEdate());
			uptInfo.setStatus(info.getStatus());
			uptInfo.setDwTxnDate(info.getDwTxnDate());
			uptInfo.setDwTxnTime(info.getDwTxnTime());
			uptInfo.setDwLmsInvoiceNo(info.getDwLmsInvoiceNo());
			uptInfo.setInfile(info.getInfile());
			uptInfo.setBatchRcode(info.getBatchRcode());
			uptInfo.setParMon(info.getParMon());
			uptInfo.setParDay(info.getParDay());
			uptInfo.setRegionId(info.getRegionId());
			uptInfo.setProcDate(info.getProcDate());
			uptInfo.setUptUserid("batch");
			uptInfo.setUptDate(batchDate);
			uptInfo.setUptTime(sysTime);
			uptInfo.setAprvUserid("batch");
			uptInfo.setAprvDate(batchDate);
			uptInfo.setAprvTime(sysTime);
			uptInfo.setUptStatus("2");
			uptInfo.setAprvStatus("2");
			uptInfo.setCardNo(info.getCardNo());
			uptInfo.setApprldDesc(info.getApprldDesc());
			uptInfo.setBatchNo(batchNo);
			uptInfo.setHgOrderNo(info.getHgOrderNo());
			uptInfo.setExchangeSeqno(info.getExchangeSeqno());
			uptInfo.setExchangeDate(info.getExchangeDate());
			uptInfo.setProcFlag(info.getProcFlag());
			uptInfo.setErrorDesc(info.getErrorDesc());
			uptInfo.setProductId(info.getProductId());
			uptInfo.setLineNo(info.getLineNo());
			uptInfo.setOutputDate(info.getOutputDate());
			uptInfo.setExMemId(info.getExMemId());
//			uptInfo.setRejectDate(info.getRejectDate());
			uptInfo.setUiSrc("2");
			uptInfo.setIsUpt(info.getIsUpt());
			
			logger.debug(uptInfo.toInsertSQL());
			
			appUptMgr.insert(uptInfo);
		}
		TbAppointReloadMgr appMgr = new TbAppointReloadMgr(conn);
		appMgr.update(info);
	}

	@Override
	public void remarkFailure(Connection conn, String batchDate,
			BatchJobException e) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void remarkSuccess(Connection conn, String batchDate) throws Exception {
		// TODO Auto-generated method stub
		
	}
}
