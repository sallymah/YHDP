package tw.com.hyweb.svc.yhdp.batch.impfiles;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.log4j.Logger;

import tw.com.hyweb.core.yhdp.batch.framework.Layer1Constants;
import tw.com.hyweb.core.yhdp.batch.framework.impfiles.ErrorDescInfo;
import tw.com.hyweb.core.yhdp.batch.framework.impfiles.FieldInfo;
import tw.com.hyweb.core.yhdp.batch.framework.impfiles.ImpFilesUtil;
import tw.com.hyweb.core.yhdp.common.misc.*;
import tw.com.hyweb.service.db.info.*;
import tw.com.hyweb.service.db.mgr.*;
import tw.com.hyweb.svc.yhdp.batch.impfiles.ApploadData;
import tw.com.hyweb.util.string.StringUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * <pre>
 * ImpAppload(For THIG, Only 以卡歸戶,主機紅利)
 * </pre>
 * author:Anny
 */
public class ApploadChecker
{
    private static Logger log = Logger.getLogger(ApploadChecker.class);
    
	private final ApploadData apploadData;
	private final Map<String, FieldInfo> apploadFieldInfos;

	private List descInfos = new ArrayList();

    public ApploadChecker(ApploadData apploadData, Map<String, FieldInfo> apploadFieldInfos)
    {
    	this.apploadData = apploadData; 
    	this.apploadFieldInfos = apploadFieldInfos;
    }

    private void checkCardInfo(Connection connection) throws SQLException
    {
        if(apploadData.getCardInfo() == null)
        {
        	addErrorDescInfo(connection, Constants.RCODE_2710_INVALID_ERR, apploadFieldInfos.get("CARD_NO") ,"cardInfo is null:" + apploadData.getFileData().get("CARD_NO"));
        }
        else if (!"1".equals(apploadData.getCardInfo().getStatus()) && !"2".equals(apploadData.getCardInfo().getStatus()) && !"3".equals(apploadData.getCardInfo().getStatus()))
        {
        	addErrorDescInfo(connection, Constants.RCODE_2710_INVALID_ERR, apploadFieldInfos.get("CARD_NO"), "card status is not valid:" + apploadData.getFileData().get("CARD_NO"));
        }
    }
    
    private void checkSponsorInfo(Connection connection) throws SQLException
    {	
       if (apploadData.getCardInfo()!= null)
       {
	       if (!apploadData.getFileData().get("ACQ_MEM_ID").equals(apploadData.getCardInfo().getMemId()) && !checkAcqDefInfo(connection, apploadData.getCardInfo().getMemId(), apploadData.getFileData().get("ACQ_MEM_ID")))
	       {
	    	   addErrorDescInfo(connection, Constants.RCODE_2710_INVALID_ERR, apploadFieldInfos.get("ACQ_MEM_ID"), "Sponsor Id is not valid:" + apploadData.getFileData().get("ACQ_MEM_ID"));
	       }
       }
    }
    
    private void checkMerchInfo(Connection conn) throws SQLException
    {	
    	TbMerchMgr mgr = new TbMerchMgr(conn);
    	TbMerchInfo merchInfo = mgr.querySingle(apploadData.getFileData().get("MERCH_ID"));
    	
    	if (merchInfo == null)
    	{
    		addErrorDescInfo(conn, Constants.RCODE_2710_INVALID_ERR, apploadFieldInfos.get("MERCH_ID"), "merchInfo is null:" + apploadData.getFileData().get("MERCH_ID"));
    	}
    }
    
    private boolean checkAcqDefInfo(Connection connection, String issMemId, String acqMemId) throws SQLException {
    	
    	TbAcqDefPK pk = new TbAcqDefPK();
    	pk.setAcqMemId(acqMemId);
    	pk.setIssMemId(issMemId);
    	
        TbAcqDefInfo acqDefInfo = new TbAcqDefMgr(connection).querySingle(pk);
        
        if (acqDefInfo != null)
        	return true;
        else
        	return false;    	

    }
    
    private void checkBonusInfo(Connection conn) throws SQLException {

    	TbBonusMgr mgr = new TbBonusMgr(conn);
    	TbBonusInfo bonusInfo = mgr.querySingle(apploadData.getFileData().get("BONUS_ID"));
    	
    	if (bonusInfo == null)
    	{
    		addErrorDescInfo(conn, Constants.RCODE_2710_INVALID_ERR, apploadFieldInfos.get("BONUS_ID"), "bonusInfo is null:" + apploadData.getFileData().get("BONUS_ID"));
    	}
    	/*2010.2.5 Wendy說Coupon也不一定要填*/
    	/*else
    	{
            if (StringUtil.isEmpty(apploadData.getFileData().get("BONUS_SDATE")) && StringUtil.isEmpty(apploadData.getFileData().get("BONUS_EDATE"))) {
                // 只有點數才可以不給效期
                if (!Constants.BONUSNATURE_POINT.equals(bonusInfo.getBonusNature())) {
                	addErrorDescInfo(conn, Constants.RCODE_2710_INVALID_ERR, apploadFieldInfos.get("BONUS_ID"), "bonus_nature:" + bonusInfo.getBonusNature());
                }  
            }
    	}*/
    
    }
    
    /*履約保證處理*/
    /*private boolean handlePBM(Connection conn)
    {
    	try
        {
            TbMemberInfo bean = new TbMemberInfo();
            BeanUtils.getProperty(bean, "pbmFlag"); //try try看是否有pb相關欄位

            Savepoint savepoint = conn.setSavepoint();

            String rcode = PbUtils.pbMaintain(conn, apploadData.getFileData().get("CARD_NO"), apploadData.getFileData().get("EXPIRY_DATE"), apploadData.getFileData().get("BONUS_ID"), Double.valueOf(apploadData.getFileData().get("BONUS_QTY")));

            if (!Layer1Constants.RCODE_0000_OK.equals(rcode))
            {
            	addErrorDescInfo(conn, rcode, null ,"maintain pb failure!");
            	
                try
                {
                    conn.rollback(savepoint);
                    
                }
                catch (Exception e)
                {

                }
                
                return false;
            }
            
            return true;
        }
        catch(Exception e)
        {
            //沒有PB相關欄位，故不處理
        	return true;
        }

    }*/
    
    private void addErrorDescInfo(Connection connection, String rcode, FieldInfo info, String content) throws SQLException
    {
		ErrorDescInfo descInfo = ImpFilesUtil.getErrorDescInfo(rcode, info, content);   
        descInfos.add(descInfo);
    }
    
    public List<ErrorDescInfo> checker(Connection connection) throws Exception
    {	
        /*檢查主檔*/
        checkCardInfo(connection);

        /* 檢查Bonus id(field05)*/
        checkBonusInfo(connection);
        
        /*檢查出資單位是否合法*/
        checkSponsorInfo(connection);
        
        /*檢查指定加值特店是否合法*/
        checkMerchInfo(connection);
        
        /*履約保證處理*/
        //handlePBM(connection);
          
        return descInfos;
        
    }
    
}
