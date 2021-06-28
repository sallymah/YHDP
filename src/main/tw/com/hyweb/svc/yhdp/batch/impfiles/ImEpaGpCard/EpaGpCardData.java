package tw.com.hyweb.svc.yhdp.batch.impfiles.ImEpaGpCard;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;

import tw.com.hyweb.service.db.info.TbCardPK;
import tw.com.hyweb.service.db.info.TbEpaGpCardInfo;
import tw.com.hyweb.util.DbUtil;

public class EpaGpCardData
{
    private static Logger log = Logger.getLogger(EpaGpCardData.class);

    private final Map<String, String> fileData;
    private int cardCount;
    private String cardNo;
       
    public EpaGpCardData(Connection connection, Map<String, String> fileData) throws SQLException
    {
    	this.fileData = fileData;
    	this.cardNo = fileData.get("CARD_PHYSICAL_ID");
    	this.cardCount = getCardInfoCnt(connection , cardNo);
    }
    
    private int getCardInfoCnt(Connection connection, String cardNo) throws SQLException
    {
    	int count=0;
        Vector v=DbUtil.select("select card_no from TB_CARD WHERE CARD_NO='"+cardNo+"'",connection);
        if(v != null && v.size() > 0)
        {
        	log.info("getCardInfoCnt:"+v.size());
        	count=v.size();
        }
        return count;
    }

	public Map<String, String> getFileData() {
		return fileData;
	}

	public int getCardInfoCount() {
		return cardCount;
	}
	
	public String getCardNo() {
		return cardNo;
	}

    public List handleGpCard(Connection conn, String batchDate,String batchTime) throws Exception 
    {    	
    	Vector v=DbUtil.select("select card_no,cancel_date,cancel_time from TB_EPA_GP_CARD where CARD_NO='"+cardNo+"'", conn);
    	
    	TbCardPK pk=new TbCardPK();
    	pk.setCardNo(cardNo);
    	TbEpaGpCardInfo info=new TbEpaGpCardInfo();
    	String sql=null;
    	if(v != null && v.size() > 0)
    	{
        	log.info("handleGpCard:"+v.size());
    		Vector row = (Vector)v.get(0);

    		info.setCardNo(cardNo);
			info.setEpaGpStatus("1");
    		info.setReceiveDate(fileData.get("RECEIVE_DATE").substring(0,8));
    		info.setReceiveTime(fileData.get("RECEIVE_DATE").substring(8));
    		info.setReceiveContent(fileData.get("RECEIVE_CONTENT"));
    		info.setCancelDate(String.valueOf(row.get(1)));
    		info.setCancelTime(String.valueOf(row.get(2)));
			sql=info.toUpdateSQL();
    	}else { 
    		info.setCardNo(cardNo);
    		info.setEpaGpStatus("1");
    		info.setReceiveDate(fileData.get("RECEIVE_DATE").substring(0,8));
    		info.setReceiveTime(fileData.get("RECEIVE_DATE").substring(8));
    		info.setReceiveContent(fileData.get("RECEIVE_CONTENT"));
    		sql=info.toInsertSQL();
		}
    	    	
        List sqls = new ArrayList();
        log.info("sql: " +sql);
        sqls.add(sql);
        
        return sqls;
    }   
}
