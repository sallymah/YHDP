package tw.com.hyweb.core.yhdp.common.misc;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;

import tw.com.hyweb.core.yhdp.common.misc.Layer2Util;
import tw.com.hyweb.service.db.info.TbAcctBalInfo;
import tw.com.hyweb.util.ReleaseResource;
import tw.com.hyweb.util.string.StringUtil;

/**
 * author:anny
 */
public class AcctBalAction
{
    protected TbAcctBalInfo info = null;
    private Connection conn;
    private String m_sTable = "TB_ACCT_BAL";
    private static Logger log = Logger.getLogger(AcctBalAction.class);

    public AcctBalAction(Connection conn, TbAcctBalInfo info)
    {
        this.conn = conn;
        this.info = info;  
    }
 
    public void init()
    {
        info.setCrBonusQty(0);
        info.setDbBonusQty(0);
        info.setBalBonusQty(0);  
    }
    
    public String getSql(String txnCode, double bonusQty) throws SQLException
    {        
        init();
        
        if (getCount()>0)
            return getUpdateSql(txnCode, bonusQty);
        else
            return getInsertSql(txnCode, bonusQty);
    }
    
    public TbAcctBalInfo getAcctBalInfo()
    {
        return info;
    }

    public void setAcctBalInfo(TbAcctBalInfo acctBalInfo)
    {
        if (acctBalInfo != null) {
            this.info = (TbAcctBalInfo) acctBalInfo.clone();
        }
    }

    public String getUpdateSql(String txnCode, double modifyQty)
    {
        String sql = info.toUpdateSQL();
        
        if (Layer2Util.isPlusSign(txnCode))
        {
            log.info("replace");
            sql=sql.replaceAll("CR_BONUS_QTY = 0", "CR_BONUS_QTY = CR_BONUS_QTY + "+modifyQty);
            sql=sql.replaceAll("DB_BONUS_QTY = 0", "DB_BONUS_QTY = DB_BONUS_QTY");
            sql=sql.replaceAll("BAL_BONUS_QTY = 0", "BAL_BONUS_QTY = BAL_BONUS_QTY");
            log.info(sql);
        }
        else if (Layer2Util.isMinusSign(txnCode))
        {
            sql=sql.replaceAll("CR_BONUS_QTY = 0", "CR_BONUS_QTY = CR_BONUS_QTY");
            sql=sql.replaceAll("DB_BONUS_QTY = 0", "DB_BONUS_QTY = DB_BONUS_QTY + "+modifyQty);
            sql=sql.replaceAll("BAL_BONUS_QTY = 0", "BAL_BONUS_QTY = BAL_BONUS_QTY");
        }
        else
        {
            sql=sql.replaceAll("CR_BONUS_QTY = 0", "CR_BONUS_QTY = CR_BONUS_QTY");
            sql=sql.replaceAll("DB_BONUS_QTY = 0", "DB_BONUS_QTY = DB_BONUS_QTY");
            sql=sql.replaceAll("BAL_BONUS_QTY = 0", "BAL_BONUS_QTY = BAL_BONUS_QTY");            
        }
        
        return sql;
    }
    
    public String getInsertSql(String txnCode, double modifyQty)
    {             
        if (Layer2Util.isPlusSign(txnCode))
        {
            info.setCrBonusQty(modifyQty);
            info.setDbBonusQty(0);
        }
        else if (Layer2Util.isMinusSign(txnCode))
        {
            info.setCrBonusQty(0);
            info.setDbBonusQty(modifyQty);
        }
        
        return info.toInsertSQL();
    }
    
    protected String getCond() throws NullPointerException {

        int iCond = 0;
        StringBuffer sql = new StringBuffer();
        if (!StringUtil.isEmpty(info.getAcctId())) {
          sql.append(iCond > 0 ? " AND " : " WHERE ");
          sql.append("ACCT_ID = ");
          sql.append(StringUtil.toSqlValueWithSQuote(info.getAcctId()));
          iCond++;
        }

        if (!StringUtil.isEmpty(info.getRegionId())) {
          sql.append(iCond > 0 ? " AND " : " WHERE ");
          sql.append("REGION_ID = ");
          sql.append(StringUtil.toSqlValueWithSQuote(info.getRegionId()));
          iCond++;
        }

        if (!StringUtil.isEmpty(info.getBonusId())) {
          sql.append(iCond > 0 ? " AND " : " WHERE ");
          sql.append("BONUS_ID = ");
          sql.append(StringUtil.toSqlValueWithSQuote(info.getBonusId()));
          iCond++;
        }

        if (!StringUtil.isEmpty(info.getBonusSdate())) {
          sql.append(iCond > 0 ? " AND " : " WHERE ");
          sql.append("BONUS_SDATE = ");
          sql.append(StringUtil.toSqlValueWithSQuote(info.getBonusSdate()));
          iCond++;
        }

        if (!StringUtil.isEmpty(info.getBonusEdate())) {
          sql.append(iCond > 0 ? " AND " : " WHERE ");
          sql.append("BONUS_EDATE = ");
          sql.append(StringUtil.toSqlValueWithSQuote(info.getBonusEdate()));
          iCond++;
        }

        return sql.toString();
      }
    
    /**
     * <pre>
     * 依 <em>info</em> 的屬性 (之間以 AND 連接) 來取得目前 TB_CARD_BAL table 目前總共有幾筆資炓,
     * 失敗會丟出 SQLException.
     * </pre>
     * @return 取得目前 TB_CARD_BAL table 目前總共有幾筆資炓.
     * @throws java.sql.SQLException
     */
    public int getCount() throws SQLException {
      int ret = 0;
      Statement stmt = null;
      ResultSet rs = null;
      StringBuffer sql = new StringBuffer();
      sql.append("SELECT COUNT(*) FROM " + m_sTable);
      sql.append(getCond());
      try {
        log.debug("sql: " + sql.toString());
        stmt = conn.createStatement();
        rs = stmt.executeQuery(sql.toString());
        while (rs.next()) {
          ret = rs.getInt(1);
        }
        log.debug(m_sTable + ": " + ret + " records exist");
      }
      catch (SQLException se) {
        log.warn(m_sTable + ": " + se.getMessage(), se);
        throw new SQLException(m_sTable + ": query failed! (" + se.getMessage() + ")");
      }
      finally {
        ReleaseResource.releaseDB(null, stmt, rs);
      }
      return ret;
    }
}