package tw.com.hyweb.cp.ws.enduser;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.jws.WebService;
import javax.sql.DataSource;

import org.apache.log4j.Logger;
                                                                                              
@WebService(endpointInterface = "tw.com.hyweb.cp.ws.enduser.TxnService",serviceName="TxnService")

public class TxnServiceImpl implements TxnService{

	private static final Logger log = Logger.getLogger(TxnServiceImpl.class);

	private final ServiceObject txnService;
	private final ServiceObject txnDtlService;
    private final String table = "TB_ONL_TXN";
    private final String tableDtl = "TB_ONL_TXN_DTL";
    private Connection connection = null;
	
	public TxnServiceImpl(Map<String, String> Fields, Map<String, String> DtlFields, DataSource dataSource) throws SQLException
	{		
		this.txnService = new ServiceObject(table, Fields);
		this.txnDtlService = new ServiceObject(tableDtl, DtlFields);
		connection = dataSource.getConnection();
	}

//	public String QueryCardTxn(String cardNo) throws Exception {
//		
//		return txnService.queryString(connection, EndUserUtil.getCardPk(cardNo), "STATUS in ('1','C','R')", "txn_date desc, txn_time desc", 10);
//	}

    public String QueryCardTxn(String cardNo, String expiryDate) throws Exception {
        
        return txnService.queryString(connection, EndUserUtil.getCardPk(cardNo, expiryDate), "STATUS in ('1','C','R')", "txn_date desc, txn_time desc", 10);
    }
    
	public String QueryTxnDtl(String cardNo, String expiryDate,String lmsInvoiceNo) throws Exception {
		return txnDtlService.queryString(connection, genTxnPkMap(cardNo,expiryDate,lmsInvoiceNo),"", "PAR_MON desc, PAR_DAY desc", 10);
	}
	
	
	private Map<String, String> genTxnPkMap(String cardNo, String expiryDate,String lmsInvoiceNo)
	{
		Map<String, String> pkMap = new HashMap<String, String>();
		
		if(EndUserUtil.isNullOrEmpty(expiryDate) == true)
			expiryDate="99991231";
		
		pkMap.put("CARD_NO", cardNo);
		pkMap.put("EXPIRY_DATE", expiryDate);
		pkMap.put("LMS_INVOICE_NO", lmsInvoiceNo);
		
		return pkMap;
	}
}
