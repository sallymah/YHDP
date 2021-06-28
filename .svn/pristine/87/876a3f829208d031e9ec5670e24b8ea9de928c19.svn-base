/*
 * (版權及授權描述)
 * Copyright 2007 (C) Hyweb Technology Coperation.
 * All Rights Reserved.
 *
 * 2008/9/30
 */
package tw.com.hyweb.svc.yhdp.batch.postoperation;

import static tw.com.hyweb.core.cp.batch.util.PreparedStatementUtils.executeQuerySingleData;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;

import tw.com.hyweb.core.cp.batch.framework.generic.BatchJobException;
import tw.com.hyweb.core.cp.batch.framework.generic.GenericBatchJob;
import tw.com.hyweb.util.date.DateUtil;
import tw.com.hyweb.gui.CfgInfo;
import tw.com.hyweb.gui.NotifySms;
import tw.com.hyweb.service.db.info.TbIssPbmInfo;
import tw.com.hyweb.service.db.info.TbMemberInfo;
import tw.com.hyweb.service.db.mgr.TbIssPbmMgr;
import tw.com.hyweb.service.db.mgr.TbMemberMgr;
import tw.com.hyweb.svc.cp.batch.postoperation.CheckQuotaAlertJob;
import tw.com.hyweb.util.string.StringUtil;

/**
 * @author Anny
 * 履約保證字眼改成 -> 加值總額度控管
 * 20110221:將mail改由簡訊發送
 */
public class SystexCheckQuotaAlertJob extends GenericBatchJob
{
    private static final Logger LOGGER = Logger.getLogger(CheckQuotaAlertJob.class);

    private final TbIssPbmInfo issuer;

    /**
     * @param issuer
     */
    public SystexCheckQuotaAlertJob(TbIssPbmInfo issuer)
    {
	    this.issuer = issuer;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * tw.com.hyweb.core.cp.batch.framework.generic.GenericBatchJob#action(java
     * .sql.Connection, java.lang.String)
     */
    @Override
    public void action(Connection connection, String batchDate) throws Exception
    {
//        Properties props = System.getProperties();
//        props.put("mail.host", mailServer);
//        props.put("mail.transport.protocol", "smtp");
//
//        String mailTo = getMailTo();
//
//        Message message = new MimeMessage(Session.getDefaultInstance(props, null));
//        message.setSentDate(new Date());
//        message.setFrom(new InternetAddress(mailFrom));
//        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(mailTo, false));
//        message.setSubject("Alert parameter of Issued Quota");
//        message.setContent(getMessageContent(connection), "text/plain" + ";charset=UTF-8");
//
//        LOGGER.info("send mail to:" + mailTo);
//
//        Transport.send(message);
    	
    	 String tel = this.getTel(connection, issuer.getIssMemId());
    	 String body = this.getMessageContent(connection);

         CfgInfo cfgInfo = new CfgInfo();
         NotifySms notifySms = new NotifySms(cfgInfo, tel);
         String ret = notifySms.doNotify(body);
         LOGGER.debug(ret);
         String[] rets = ret.split("\n");
         String retcode=""; 
         for (String var:rets)
         {
             if (var.startsWith("msgid"))
             {
                 retcode = var.substring(1+var.indexOf('='));
                 break;
             }
         }
         if (retcode.indexOf('-')>=0)
         {
        	 LOGGER.warn("retcode="+retcode);
         }
    }

    /**
     * @return
     */
//    private String getMailTo()
//    {
//        StringBuilder mailTo = new StringBuilder();
//        mailTo.append(StringUtil.isEmpty(issuer.getPbChargeEmail1()) ? "" : issuer.getPbChargeEmail1());
//        mailTo.append(mailTo.length() == 0 || StringUtil.isEmpty(issuer.getPbChargeEmail2()) ? "" : ",");
//        mailTo.append(StringUtil.isEmpty(issuer.getPbChargeEmail2()) ? "" : issuer.getPbChargeEmail2());
//        mailTo.append(mailTo.length() == 0 || StringUtil.isEmpty(issuer.getPbChargeEmail3()) ? "" : ",");
//        mailTo.append(StringUtil.isEmpty(issuer.getPbChargeEmail3()) ? "" : issuer.getPbChargeEmail3());
//
//        return mailTo.toString();
//    }
    
    /**
     * @return
     * @throws SQLException
     */
    private String getMessageContent(Connection connection) throws SQLException
    {
        double percent = issuer.getIssCurrentQuota().doubleValue() / issuer.getIssMaxQuota().doubleValue() * 100;

        String memberName = "";
        StringBuilder content = new StringBuilder();
//        content.append("加值總額度控管參加單位:" + getMemberName(connection) + ",");
//        content.append("加值總額度控管有效起始/結束日期:" + issuer.getPbValidSdate() + "~" + issuer.getPbValidEdate() + ",");
//        content.append("加值總額度控管總額度:" + issuer.getIssMaxQuota() + "。");
//        content.append(String.format("尚可發行額度:%.2f\n", (issuer.getIssMaxQuota().doubleValue() * issuer.getQuotaLimit().doubleValue() / 100 - issuer.getIssCurrentQuota().doubleValue())));
//        content.append("已發行額度警示通知參數:" + issuer.getQuotaAlert() + "%\n");
//        content.append("已發行額度上限參數:" + issuer.getQuotaLimit() + "%\n");
//        content.append("已發行額度:" + issuer.getIssCurrentQuota() + "\n");
//        content.append(String.format("已發行額度百分比:%.2f", percent)).append("%\n");
//        content.append("未發行額度:" + (issuer.getIssMaxQuota().doubleValue() - issuer.getIssCurrentQuota().doubleValue()) + "\n");
//        content.append(String.format("未發行額度百分比:%.2f", (100 - percent))).append("%\n");
        content.append("上限警示通知").append("   ");
        
        //20111222 判斷長度>5才取前六碼
        
        memberName = getMemberName(connection);
        
        if(!isNullOrEmpty(memberName)){
        	if(memberName.length()>5){
        		memberName = memberName.substring(0, 6);
        	}else{        		
        		for(int i= 0;i<(6-memberName.length());i++){
        		   memberName =  memberName+" ";                                 
        		}
        	}
        	
        }
        content.append("企業" + memberName + ",");        
        content.append("到期日" + issuer.getPbValidEdate() + ",");
        content.append("總額度" + issuer.getIssMaxQuota() + ",");
        content.append("已發行" + issuer.getIssCurrentQuota() + ",");
        content.append(String.format("%.0f", percent)).append("％,");
        content.append("未發行" + (issuer.getIssMaxQuota().intValue() - issuer.getIssCurrentQuota().intValue()) + ",");
        content.append(String.format("%.0f", (100 - percent))).append("％");
        LOGGER.info(content.toString().length()+","+content.toString());
        return content.toString();
    }
    
    public static boolean isNullOrEmpty(String value)
    {
        if (value==null || value.trim().equals(""))
       	 return true;

        return false;
    }
    /**
     * @param connection
     * @return
     * @throws SQLException
     */
    private String getMemberName(Connection connection) throws SQLException
    {
    	TbMemberInfo tbMemberInfo = null;
    	Vector<TbMemberInfo> result = new Vector<TbMemberInfo>();
        new TbMemberMgr(connection).queryMultiple("MEM_ID = '" + issuer.getIssMemId() + "'", result);
        for(int i=0; i<result.size(); i++) 
    	{
        	tbMemberInfo = result.get(0);
    	}
        
        return tbMemberInfo.getMemName();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * tw.com.hyweb.core.cp.batch.framework.generic.GenericBatchJob#remarkSuccess
     * (java.sql.Connection, java.lang.String)
     */
    @Override
    public void remarkSuccess(Connection connection, String batchDate) throws Exception
    {
        issuer.setMailFlag("1");
        issuer.setMailRcode("0000");
        issuer.setMailDate(DateUtil.getTodayString().substring(0, 8));

        new TbIssPbmMgr(connection).update(issuer);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * tw.com.hyweb.core.cp.batch.framework.generic.GenericBatchJob#remarkFailure
     * (java.sql.Connection, java.lang.String,
     * tw.com.hyweb.core.cp.batch.framework.generic.BatchJobException)
     */
    @Override
    public void remarkFailure(Connection connection, String batchDate, BatchJobException batchJobException) throws Exception
    {
        issuer.setMailFlag("1");
        issuer.setMailRcode("2801");
        issuer.setMailDate(DateUtil.getTodayString().substring(0, 8));

        new TbIssPbmMgr(connection).update(issuer);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return "check quota alter for issuer pbm:" + issuer.getIssMemId() + ", proc_date:" + issuer.getProcDate();
    }
    
    private String getTel(Connection connection, String iss_mem_id) throws SQLException
    {
    	String sql = "select PB_CHARGE_MOBILE1, PB_CHARGE_MOBILE2, PB_CHARGE_MOBILE3 from TB_MEMBER where MEM_ID='"+iss_mem_id+"'";

    	Map<String, String> memberMap = executeQuerySingleData(connection, sql);
    	
    	StringBuilder sendTo = new StringBuilder();
    	sendTo.append(StringUtil.isEmpty(memberMap.get("PB_CHARGE_MOBILE1")) ? "" : memberMap.get("PB_CHARGE_MOBILE1"));
    	sendTo.append(sendTo.length() == 0 || StringUtil.isEmpty(memberMap.get("PB_CHARGE_MOBILE2")) ? "" : ",");
    	sendTo.append(StringUtil.isEmpty(memberMap.get("PB_CHARGE_MOBILE2")) ? "" : memberMap.get("PB_CHARGE_MOBILE2"));
    	sendTo.append(sendTo.length() == 0 || StringUtil.isEmpty(memberMap.get("PB_CHARGE_MOBILE3")) ? "" : ",");
    	sendTo.append(StringUtil.isEmpty(memberMap.get("PB_CHARGE_MOBILE3")) ? "" : memberMap.get("PB_CHARGE_MOBILE3"));
    	
    	return sendTo.toString();
    }
}
