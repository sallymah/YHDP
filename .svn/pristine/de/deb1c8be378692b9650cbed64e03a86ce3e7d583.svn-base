package tw.com.hyweb.svc.yhdp.batch.perso;

import org.apache.log4j.Logger;

import tw.com.hyweb.svc.cp.batch.perso.CardNumberGenerator;

public class YhdpCardNumberGenerator implements CardNumberGenerator{
	
	private static final Logger LOGGER = Logger.getLogger(YhdpCardNumberGenerator.class);

	public String inqueryNextCardNo(String cardNumber, String cardProduct) {
		
		String cardNoPrefix =cardNumber.substring(0,8); 
		
		int cardNoSeq = Integer.parseInt(cardNumber.substring(8, 15))+1;
		
		return getFullCardNoWithCheck(cardNoPrefix+get7Seq(cardNoSeq));
	}
	
	
	private String get7Seq( int seq){
    	
    	String seqStr = ("0000000"+seq);
    	
    	seqStr = seqStr.substring(seqStr.length()-7, seqStr.length());
    	
    	return seqStr;
    	
    }
	
	 private String getFullCardNoWithCheck(String cardNoPrefixSeq){
	    	
	    	return cardNoPrefixSeq+this.getCardNoCheckCode(cardNoPrefixSeq);
	    }
	    
	    private String getCardNoCheckCode(String cardNo15)
	    {
	        int checkSum = 0;
	        int result = 0;
	        for(int i= 0;i<cardNo15.length();i++)
	        {
	        int checkUnit =0;
	         
	         if(cardNo15.length()% 2 != 0)
	        	 cardNo15="0" + cardNo15;
	          
	            if (i % 2 == 0)
	            {
	                checkUnit = Integer.parseInt(cardNo15.substring(i, (i+1))) * 1;
	            }
	            else
	            {
	                checkUnit = Integer.parseInt(cardNo15.substring(i, (i+1))) * 2;
	            }
	 
	            if (checkUnit > 9)
	            {
	                checkSum += (checkUnit / 10) + (checkUnit % 10);
	            }
	            else
	            {
	                checkSum += checkUnit;
	            }
	        }
	        if (checkSum % 10 != 0)
	        {
	            result = 10 - (checkSum % 10);
	        }
	 
	        return String.valueOf(result);
	    }

}
