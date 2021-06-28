package tw.com.hyweb.svc.yhdp.batch.impfiles.ImpTxnDtl;

public class TrafficDataFormat
{
    /* 0,AN / 1, is BIN */
    int format = 0;
    /* 0,not lsb / 1, is lsb */
    int lsb = 0;
    int length = 0;
    String fieldName;
    boolean isNecessary = false;
    int convertFunc = 0;
    
    public void setConvertFunc(int convertFunc)
    {
        this.convertFunc = convertFunc;
    }
    
    public int getConvertFunc()
    {
        return this.convertFunc;
    }
    
    public void setIsNecessary(boolean isNecessary)
    {
        this.isNecessary = isNecessary;
    }
    
    public boolean getIsNecessary()
    {
        return this.isNecessary;
    }
    
    public void setFieldName(String fieldName)
    {
        this.fieldName = fieldName;
    }
    
    public String getFieldName()
    {
        return this.fieldName;
    }
    
    public void setFormat(int format)
    {
        this.format = format;
    }
    
    public int getFormat()
    {
        return this.format;
    }
    
    public void setLsb(int lsb)
    {
        this.lsb = lsb;
    }
    
    public int getLsb()
    {
        return this.lsb;
    }
    
    public void setLength(int length)
    {
        this.length = length;
    }
    
    public int getLength()
    {
        return this.length;
    }
}
