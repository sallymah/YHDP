/*
 * Version: 3.0.0
 * Date: 2007-01-29
 */

package tw.com.hyweb.svc.yhdp.batch.balance;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.apache.log4j.Logger;

import tw.com.hyweb.core.cp.common.misc.Constants;

import tw.com.hyweb.core.cp.batch.framework.AbstractBatchBasic;
import tw.com.hyweb.core.cp.batch.util.BatchUtil;
import tw.com.hyweb.core.cp.common.misc.Layer2Util;
import tw.com.hyweb.service.db.DBService;
import tw.com.hyweb.util.ReleaseResource;

/**
 * <pre>
 * BalanceProcessor
 * </pre>
 * 
 * author: Anny
 */
public abstract class BalanceProcessor
{
    private static final Logger logger = Logger.getLogger(BalanceProcessor.class);

    private String batchDate;

    private String bonusBase;

    private String balanceType;

    private String transBalIdField;

    private String transBalKeyField;

    private String balanceTable;

    private String balanceIdField;

    private String balanceKeyField;
    
    private int cutDay;

    private AbstractBatchBasic batch;
    
    private String rcode;
    
    public String getRcode()
    {
        return rcode;
    }

    public void setRcode(String rcode)
    {
        this.rcode = rcode;
    }

    public BalanceProcessor(AbstractBatchBasic batch, int cutDay)
    {
        this.batch = batch;
        this.cutDay = cutDay;
    }

    public void setBatch(AbstractBatchBasic batch)
    {
        this.batch = batch;
    }

    protected String getBalanceIdField()
    {
        return balanceIdField;
    }

    protected void setBalanceIdField(String balanceIdField)
    {
        this.balanceIdField = balanceIdField;
    }

    protected String getBalanceKeyField()
    {
        return balanceKeyField;
    }

    protected void setBalanceKeyField(String balanceKeyField)
    {
        this.balanceKeyField = balanceKeyField;
    }

    protected String getBalanceTable()
    {
        return balanceTable;
    }

    protected void setBalanceTable(String balanceTable)
    {
        this.balanceTable = balanceTable;
    }

    protected String getBalanceType()
    {
        return balanceType;
    }

    protected void setBalanceType(String balanceType)
    {
        this.balanceType = balanceType;
    }

    protected String getBonusBase()
    {
        return bonusBase;
    }

    protected void setBonusBase(String bonusBase)
    {
        this.bonusBase = bonusBase;
    }

    protected String getTransBalIdField()
    {
        return transBalIdField;
    }

    protected void setTransBalIdField(String transBalIdField)
    {
        this.transBalIdField = transBalIdField;
    }

    protected String getTransBalKeyField()
    {
        return transBalKeyField;
    }

    protected void setTransBalKeyField(String transBalKeyField)
    {
        this.transBalKeyField = transBalKeyField;
    }

    protected String getBatchDate()
    {
        return batchDate;
    }

    protected void setBatchDate(String batchDate)
    {
        this.batchDate = batchDate;
    }

    /**
     * 初始設定<br/>
     * 
     * @throws Exception
     */
    protected void init() throws Exception
    {
        logger.debug("set init");

        if (!BatchUtil.checkChristianDate(getBatchDate()))
            throw new Exception("Date Format Error:Please Input YYYYMMDD");

        if (getBonusBase() == null && !"".equals(getBonusBase()))
            throw new Exception("Null Bonuse Base");

        if (getBalanceType() == null && !"".equals(getBalanceType()))
            throw new Exception("Null Balance Type");

        if (getBalanceTable() == null && !"".equals(getBalanceTable()))
            throw new Exception("Null Balance Table");

        if (getBalanceIdField() == null && !"".equals(getBalanceIdField()))
            throw new Exception("Null BalanceId Field, ex: card_no, acct_id, cust_id...");

        if (getBalanceKeyField() == null && !"".equals(getBalanceKeyField()))
            throw new Exception("Null BalanceKey Field, ex: region_id, exprity_date...");

        if (getTransBalIdField() == null && !"".equals(getTransBalIdField()))
            throw new Exception("Null Trans BalanceId Field, ex: card_no, acct_id, cust_id...");

        if (getTransBalKeyField() == null && !"".equals(getTransBalKeyField()))
            throw new Exception("Null Trans BalanceKey Field, ex: region_id, exprity_date...");

    }

    /**
     * Balance成功,必須註記成功<br/>
     * 
     * @param conn
     * @param balanceInfo
     * @throws Exception
     */
    protected void remarkSuccess(Connection conn, BalanceInfo balanceInfo) throws Exception
    {
        logger.info("remarkSuccess");

        StringBuffer sqlCmd = new StringBuffer("update tb_trans_dtl");
        sqlCmd.append(" set BAL_PROC_DATE ='").append(getBatchDate()).append("'");
        sqlCmd.append(" where ");
        sqlCmd.append("CUT_DATE BETWEEN '");
        sqlCmd.append(BatchUtil.getSomeDay(getBatchDate(),-cutDay)).append("' ");
        sqlCmd.append("AND '");
        sqlCmd.append(getBatchDate()).append("' ");
        sqlCmd.append(" AND balance_id='").append(balanceInfo.getBalanceId()).append("'");
        sqlCmd.append(" AND bonus_base ='").append(getBonusBase()).append("'");
        sqlCmd.append(" AND balance_type='").append(getBalanceType()).append("'");
        sqlCmd.append(" AND ").append(getBalanceKeyField()).append("='").append(balanceInfo.getBalanceKey())
                        .append("'");
        sqlCmd.append(" AND BAL_PROC_DATE ='00000000'");

        try
        {
            DBService.getDBService().sqlAction(sqlCmd.toString(), conn);
            conn.commit();
        }
        catch (SQLException sql)
        {
            logger.warn("sql Exception", sql);
        }
        catch (Exception e)
        {
            logger.warn("Exception", e);
        }
    }

    /**
     * Balance發生錯誤,必須註記失敗<br/>
     * 
     * @param conn
     * @param balanceInfo
     * @throws Exception
     */
    protected void remarkFail(Connection conn, BalanceInfo balanceInfo) throws Exception
    {
        batch.setRcode(Constants.RCODE_2001_WARN);
        
        if  (getRcode() == Constants.RCODE_0000_OK)
            setRcode(Constants.RCODE_2200_BAL_ERR);
        
        logger.warn("remarkFail");

        StringBuffer sqlCmd = new StringBuffer("update tb_trans_dtl");
        sqlCmd.append(" set BAL_PROC_DATE ='").append(getBatchDate()).append("',");
        sqlCmd.append(" BAL_RCODE='").append(getRcode()).append("'");
        sqlCmd.append(" where ");
        sqlCmd.append("CUT_DATE BETWEEN '");
        sqlCmd.append(BatchUtil.getSomeDay(getBatchDate(),-cutDay)).append("' ");
        sqlCmd.append("AND '");
        sqlCmd.append(getBatchDate()).append("' ");
        sqlCmd.append(" AND balance_id='").append(balanceInfo.getBalanceId()).append("'");
        sqlCmd.append(" AND ").append(getBalanceKeyField()).append("='").append(balanceInfo.getBalanceKey())
                        .append("'");
        sqlCmd.append(" AND bonus_base ='").append(getBonusBase()).append("'");
        sqlCmd.append(" AND balance_type='").append(getBalanceType()).append("'");
        sqlCmd.append(" AND BAL_PROC_DATE ='00000000'");

        logger.warn(sqlCmd.toString());

        try
        {
            DBService.getDBService().sqlAction(sqlCmd.toString(), conn);
            conn.commit();
        }
        catch (SQLException sql)
        {
            logger.warn("sql Exception", sql);
        }
        catch (Exception e)
        {
            logger.warn("Exception", e);
        }
    }

    /**
     * 更新餘額，若不存在即Insert，已存在則Update<br/>
     * 
     * @param conn
     * @param balanceInfo
     * @param cinfo
     * @param bonusId
     * @param bonusSDate
     * @param bonusEDate
     * @throws Exception
     */
    protected void handleBalanceTable(Connection conn, BalanceInfo balanceInfo, ComputeInfo cinfo, String bonusId,
                    String bonusSDate, String bonusEDate) throws Exception
    {
        StringBuffer whereCond = new StringBuffer(" WHERE ");
        whereCond.append(getBalanceIdField()).append("='").append(balanceInfo.getBalanceId()).append("' ");
        whereCond.append("AND ").append(getBalanceKeyField()).append("='").append(balanceInfo.getBalanceKey());
        whereCond.append("' ").append("AND bonus_id='").append(bonusId).append("' ").append("AND bonus_SDate='");
        whereCond.append(bonusSDate).append("' ").append("AND bonus_EDate='").append(bonusEDate).append("' ");

        logger.debug("=handleBalanceTable=");

        // 判斷要insert or update
        StringBuffer sqlCnt = new StringBuffer("SELECT count(*) from " + getBalanceTable());
        sqlCnt.append(whereCond);

        logger.debug(sqlCnt);
        int cnt = DBService.getDBService().count(sqlCnt.toString(), conn);
        logger.debug("筆數:" + cnt);

        StringBuffer sqlCmd = new StringBuffer("");
        if (cnt == 0)
        {
            setRcode("2202");
            throw new Exception("No Found Balance");
        }
        else
        {
            // update

            logger.debug("update " + getBalanceTable());
            sqlCmd.append("update ").append(getBalanceTable()).append(" set proc_date='").append(batchDate).append("'");
            sqlCmd.append(",cr_bonus_qty=cr_bonus_qty-").append(cinfo.getCrValue());
            sqlCmd.append(",db_bonus_qty=db_bonus_qty-").append(cinfo.getDbValue());
            sqlCmd.append(",bal_bonus_qty=bal_bonus_qty+").append(cinfo.getBalValue());
            sqlCmd.append(whereCond);
            

            logger.debug(sqlCmd);
            DBService.getDBService().sqlAction(sqlCmd.toString(), conn);
        }
    }

    /**
     * 更新餘額，若不存在即Insert，已存在則Update<br/> 更新餘額成功，必須註記成功<br/> 更新餘額失敗，必須註記失敗<br/>
     * 
     * @param bwbi
     * @return
     * @throws Exception
     */
    protected boolean updateOneBal(BalWithBonusInfos bwbi, Connection conn) throws Exception
    {
        logger.debug("Start update One Bal");
        boolean ret = false;
        boolean autoCommit = true;

        try
        {
            autoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            HashMap key2ComputeInfo = bwbi.getKey2ComputeInfo();
            logger.debug("[key2ComputeInfo]" + key2ComputeInfo.toString());
            Set keys = key2ComputeInfo.keySet();
            Iterator iter = keys.iterator();
            while (iter.hasNext())
            {
                String key = (String) iter.next();
                ComputeInfo cinfo = (ComputeInfo) key2ComputeInfo.get(key);
                logger.debug("[cinfo]" + cinfo);
                String[] tokens = key.split(":");
                if (tokens.length < 3)
                {
                    continue;
                }
                String bonusId = tokens[0];
                String bonusSDate = tokens[1];
                String bonusEDate = tokens[2];
                handleBalanceTable(conn, bwbi.getBalanceInfo(), cinfo, bonusId, bonusSDate, bonusEDate);
            }
            remarkSuccess(conn, bwbi.getBalanceInfo());
            conn.commit();
            ret = true;
        }
        catch (Exception ignore)
        {
            ignore.printStackTrace();
            ret = false;
            if (conn != null)
            {
                try
                {
                    conn.rollback();
                    remarkFail(conn, bwbi.getBalanceInfo());
                    conn.commit();
                }
                catch (Exception ignore2)
                {
                    throw new Exception(ignore2);
                }
            }
        }
        finally
        {
            if (conn != null)
            {
                try
                {
                    conn.setAutoCommit(autoCommit);
                }
                catch (Exception ignore)
                {
                    throw new Exception(ignore);
                }
            }
        }
        return ret;
    }

    protected String getBonusInfoSQL(BalanceInfo balanceInfo)
    {
        //20080425 將撈online_flag取消,因為不管哪種都要算餘額,減少join
        StringBuffer sqlCmd = new StringBuffer(" ");
        sqlCmd.append("select");
        sqlCmd.append(" tb_trans_dtl.TXN_CODE,");
        sqlCmd.append(" tb_trans_dtl.BONUS_ID,tb_trans_dtl.BONUS_SDATE,tb_trans_dtl.BONUS_EDATE,");
        sqlCmd.append(" SUM(tb_trans_dtl.BONUS_QTY) as BONUS_QTYSUM");
        sqlCmd.append(" from tb_trans_dtl ");
        sqlCmd.append(" where ");
        sqlCmd.append("CUT_DATE BETWEEN '");
        sqlCmd.append(BatchUtil.getSomeDay(getBatchDate(),-cutDay)).append("' ");
        sqlCmd.append("AND '");
        sqlCmd.append(getBatchDate()).append("' ");
        sqlCmd.append(" AND tb_trans_dtl.").append(getTransBalIdField()).append(" = '").append(balanceInfo.getBalanceId()).append("'");
        sqlCmd.append(" AND tb_trans_dtl.").append(getTransBalKeyField()).append(" = '").append(balanceInfo.getBalanceKey()).append("'");
        sqlCmd.append(" AND tb_trans_dtl.bonus_base ='").append(getBonusBase()).append("'");
        sqlCmd.append(" AND tb_trans_dtl.balance_type ='").append(getBalanceType()).append("'");
        sqlCmd.append(" AND tb_trans_dtl.txn_code in ( select txn_code from tb_txn_def where bal_flag='1')");
        sqlCmd.append(" AND tb_trans_dtl.BAL_PROC_DATE ='00000000'");
        sqlCmd.append(" group by ");
        sqlCmd.append(" tb_trans_dtl.TXN_CODE,");
        sqlCmd.append(" tb_trans_dtl.BONUS_ID,tb_trans_dtl.BONUS_SDATE,tb_trans_dtl.BONUS_EDATE");

        logger.debug(sqlCmd);
        return sqlCmd.toString();
    }

    protected BonusInfo getBonusInfoFormat(HashMap info)
    {
        BonusInfo bonusInfo = new BonusInfo();
        String txnCode = (String) info.get("TXN_CODE");
        String bonusId = (String) info.get("BONUS_ID");
        String bonusSDate = (String) info.get("BONUS_SDATE");
        String bonusEDate = (String) info.get("BONUS_EDATE");
        double bonusQtySum = ((Number) info.get("BONUS_QTYSUM")).doubleValue();

        bonusInfo.setTxnCode(txnCode);
        bonusInfo.setBonusId(bonusId);
        bonusInfo.setBonusSDate(bonusSDate);
        bonusInfo.setBonusEDate(bonusEDate);
        bonusInfo.setBonusQtySum(bonusQtySum);

        return bonusInfo;
    }

    /**
     * 依條件傳回 List object, each element is BonusInfo object <br/>
     * 
     * @param balanceInfo
     * @return bonusInfos
     * @throws SQLException
     */
    protected List getBonusInfosByGeneral(BalanceInfo balanceInfo, Connection conn) throws SQLException
    {
        List bonusInfos = new ArrayList();

        logger.debug("=getBonusInfosByGeneral=");

        Vector linkList = new Vector();

        linkList = BatchUtil.getInfoListHashMap(getBonusInfoSQL(balanceInfo), conn);

        for (int i = 0; i < linkList.size(); i++)
        {
            BonusInfo bonusInfo = getBonusInfoFormat((HashMap) linkList.get(i));
            bonusInfos.add(bonusInfo);
        }
        return bonusInfos;
    }

    /**
     * 1.來產生一個 BalWithBonusInfos object<br>
     * 2.計算<br/> 3.updateOneBal<br/>
     * 
     * @param balanceInfo
     * @throws Exception
     */
    protected void action(BalanceInfo balanceInfo, Connection conn) throws Exception
    {
        logger.info("[balanceInfo]:" + balanceInfo);

        try
        {
            BalWithBonusInfos bwbi = new BalWithBonusInfos();
            bwbi.setBalanceInfo(balanceInfo);
            List bonusInfoList = getBonusInfosByGeneral(balanceInfo, conn);
            if (bonusInfoList != null)
            {
                for (int i = 0; i < bonusInfoList.size(); i++)
                {
                    BonusInfo info = (BonusInfo) bonusInfoList.get(i);
                    bwbi.addBonusInfo(info);
                }
            }
            // computing
            bwbi.compute();
            logger.info("BonusInfos:" + bwbi);
            // update One Balance
            updateOneBal(bwbi, conn);
        }
        catch (Exception e)
        {
            throw new Exception(e);
        }
    }

    protected String getBalanceInfoSQL()
    {
        StringBuffer sqlCmd = new StringBuffer("");
        sqlCmd.append("SELECT " + getTransBalIdField() + "," + getTransBalKeyField() + " ");
        sqlCmd.append("FROM TB_TRANS_DTL ");
        sqlCmd.append("WHERE ");
        sqlCmd.append("CUT_DATE BETWEEN '");
        sqlCmd.append(BatchUtil.getSomeDay(getBatchDate(),-cutDay)).append("' ");
        sqlCmd.append("AND '");
        sqlCmd.append(getBatchDate()).append("' ");
        sqlCmd.append("AND TB_TRANS_DTL.bonus_base=").append("'").append(getBonusBase()).append("' ");
        sqlCmd.append("AND TB_TRANS_DTL.balance_type=").append("'").append(getBalanceType()).append("' ");
        sqlCmd.append("AND TB_TRANS_DTL.BAL_PROC_DATE = '00000000' ");
        //sqlCmd.append("AND EXISTS (SELECT TXN_CODE FROM TB_TXN_DEF DEF WHERE TXN_CODE=DEF.TXN_CODE AND BAL_FLAG='1') ");
        sqlCmd.append("GROUP BY ").append(getTransBalIdField()).append(",");
        sqlCmd.append(getTransBalKeyField());

        logger.info(sqlCmd);

        return sqlCmd.toString();
    }

    protected BalanceInfo getBalanceInfoFormat(ResultSet rs) throws SQLException
    {
        BalanceInfo balanceInfo = new BalanceInfo();

        balanceInfo.setBalanceId(rs.getString(1));
        balanceInfo.setBalanceKey(rs.getString(2));

        return balanceInfo;
    }

    /**
     * Balance主流成控制<br>
     */
    protected void balanceProcess() throws Exception
    {
        logger.info(Layer2Util.getTxnDefInfos());

        logger.info("=====Start Balance Process=====");
        init();

        Connection conn = null;

        try
        {
            conn = BatchUtil.getConnection();
            conn.setAutoCommit(true);

            Statement stmt = null;
            ResultSet rs = null;

            try
            {
                stmt = conn.createStatement();
                rs = stmt.executeQuery(getBalanceInfoSQL());

                logger.info("===action start===");
                while (rs.next())
                {
                    action(getBalanceInfoFormat(rs), conn);
                }
                logger.info("===action end===");
            }
            finally
            {
                ReleaseResource.releaseDB(null, stmt, rs);
            }

            conn.commit();
        }
        catch (Exception e)
        {
            logger.warn("exception when balanceprocssor", e);
            conn.rollback();

            throw e;
        }
        finally
        {
            ReleaseResource.releaseDB(conn);
        }

    }

}
