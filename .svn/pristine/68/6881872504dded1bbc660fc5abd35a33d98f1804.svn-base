package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpTxnDtl;

import org.apache.log4j.Logger;

import tw.com.hyweb.svc.yhdp.batch.framework.impfiles.DataLineInfo;

public class TxnBeanInfo
{
    private static final Logger logger = Logger.getLogger(TxnBeanInfo.class);
    
    int pCode = 0;       //交易代碼
    String cashierShopNo = null;//門店代碼
    String posID = null;        //收款機號
    String txnDate =  null;     //收銀小票交易日期
    String txnTime =  null;      //收銀小票交易時間
    String cardNo = null;       //會員卡號
    String posSemo  = null;      //收銀機交易序號
    double txnAmt  = 0;         //需回饋的支付金額
    double rewardBonusQty  = 0; //前端回饋積分
    double saleCardAmt  = 0;    //售卡金額
    String acqMenId = null;
    String termId = null;
    String rawCashierShopNo = null; //原交易門店代碼
    String rawPosID = null;         //原交易收款機號
    String rawPosSemo  = null;      //原交易收銀機交易序號
    
    public TxnBeanInfo(DataLineInfo lineInfo)
    {
        pCode = Integer.valueOf((String) lineInfo.getFieldData("field01"));        //交易代碼
        cashierShopNo = (String) lineInfo.getFieldData("field02");//門店代碼
        posID = (String) lineInfo.getFieldData("field03");        //收款機號
        txnDate = (String) lineInfo.getFieldData("field04");      //收銀小票交易日期
        txnTime = (String) lineInfo.getFieldData("field05");      //收銀小票交易時間
        cardNo = (String) lineInfo.getFieldData("field06");       //會員卡號
        posSemo =(String)  lineInfo.getFieldData("field07");      //收銀機交易序號
        txnAmt = ((Number) lineInfo.getFieldData("field08")).doubleValue();         //需回饋的支付金額
        rewardBonusQty =((Number) lineInfo.getFieldData("field09")).doubleValue(); //前端回饋積分
        saleCardAmt = ((Number) lineInfo.getFieldData("field10")).doubleValue();    //售卡金額
        rawCashierShopNo = (String) lineInfo.getFieldData("field11");//門店代碼
        rawPosID = (String) lineInfo.getFieldData("field12");        //收款機號
        rawPosSemo = (String)  lineInfo.getFieldData("field13");      //收銀機交易序號
    }
    
    public String toString()
    {
        StringBuffer strb = new StringBuffer();
        
        strb.append("pCode:").append(pCode).append(",");
        strb.append("cashierShopNo:").append(cashierShopNo).append(",");
        strb.append("posID:").append(posID).append(",");
        strb.append("txnDate:").append(txnDate).append(",");
        strb.append("txnTime:").append(txnTime).append(",");
        strb.append("cardNo:").append(cardNo).append(",");
        strb.append("posSemo:").append(posSemo).append(",");
        strb.append("txnAmt:").append(String.valueOf(txnAmt)).append(",");
        strb.append("rewardBonusQty:").append(String.valueOf(rewardBonusQty)).append(",");
        strb.append("saleCardAmt:").append(String.valueOf(saleCardAmt)).append(",");
        strb.append("rawCashierShopNo:").append(rawCashierShopNo).append(",");
        strb.append("rawPosID:").append(rawPosID).append(",");
        strb.append("rawPosSemo:").append(rawPosSemo).append(",");
        return strb.toString();
    }
    
    public void setSaleCardAmt(double saleCardAmt)
    {
        this.saleCardAmt = saleCardAmt;
    }
    
    public double getSaleCardAmt()
    {
        return this.saleCardAmt;
    }
    
    public void setRewardBonusQty(double rewardBonusQty)
    {
        this.rewardBonusQty = rewardBonusQty;
    }
    
    public double getRewardBonusQty()
    {
        return this.rewardBonusQty;
    }
    
    public void setTxnAmt(double txnAmt)
    {
        this.txnAmt = txnAmt;
    }
    
    public double getTxnAmt()
    {
        return this.txnAmt;
    }
    
    public void setPosSemo(String posSemo)
    {
        this.posSemo = posSemo;
    }
    
    public String getPosSemo()
    {
        return this.posSemo;
    }
    
    public void setCardNo(String cardNo)
    {
        this.cardNo = cardNo;
    }
    
    public String getCardNo()
    {
        return this.cardNo;
    }
    
    public void setTxnTime(String txnTime)
    {
        this.txnTime = txnTime;
    }
    
    public String getTxnTime()
    {
        return this.txnTime;
    }
    
    public void setTxnDate(String txnDate)
    {
        this.txnDate = txnDate;
    }
    
    public String getTxnDate()
    {
        return this.txnDate;
    }
    
    public void setPosID(String posID)
    {
        this.posID = posID;
    }
    
    public String getPosID()
    {
        return this.posID;
    }
    
    public void setCashierShopNo(String cashierShopNo)
    {
        this.cashierShopNo = cashierShopNo;
    }
    
    public String getCashierShopNo()
    {
        return this.cashierShopNo;
    }
    
    public void setPCode(int pCode)
    {
        this.pCode = pCode;
    }
    
    public int getPCode()
    {
        return this.pCode;
    }
    
    public void setAcqMenId(String acqMenId)
    {
        this.acqMenId = acqMenId;
    }
    
    public String getAcqMenId()
    {
        return this.acqMenId;
    }
    
    public void setTermId(String termId)
    {
        this.termId = termId;
    }
    
    public String getTermId()
    {
        return this.termId;
    }
    
    public void setRawPosID(String rawPosID)
    {
        this.rawPosID = rawPosID;
    }
    
    public String getRawPosID()
    {
        return this.rawPosID;
    }
    
    public void setRawCashierShopNo(String rawCashierShopNo)
    {
        this.rawCashierShopNo = rawCashierShopNo;
    }
    
    public String getRawCashierShopNo()
    {
        return this.rawCashierShopNo;
    }
    
    public void setRawPosSemo(String rawPosSemo)
    {
        this.rawPosSemo = rawPosSemo;
    }
    
    public String getRawPosSemo()
    {
        return this.rawPosSemo;
    }
}
