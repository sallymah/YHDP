package tw.com.hyweb.svc.yhdp.batch.util;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import net.sf.jftp.system.logging.Log;

import org.apache.log4j.Logger;

import tw.com.hyweb.core.cp.batch.framework.expfiles.ExpFileInfo;
import tw.com.hyweb.core.cp.batch.util.StringUtils;
import tw.com.hyweb.util.ISOUtil;
import tw.com.hyweb.util.ReleaseResource;

public class BatchUtils {
	
	private final static Logger logger = Logger.getLogger(BatchUtils.class); 

	//unixTime時間 基準點
	private static final String basedUnixTime = "19700101000000";
	
	
	public static void main(String[] argv) throws Exception{
		
		//System.out.println(new String (convertHexToString(getUnixTime("20101123180000")), "UTF-8"));
		File file = new File("C:/Users/Hyweb-B00/Desktop/test/001.txt");
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));
		String A = getUnixTime("20101123180000");
		System.out.println("A:" + A);
		bw.write(new String (convertHexToString(getUnixTime("20101123180000")), "UTF-8"));
		bw.close();
	}
	
	public static byte[] convertHexToString(String hex){
     	 
    	byte[] nameByteArray = ISOUtil.hex2byte(hex);
		ByteBuffer byteBuffer = ByteBuffer.allocate(nameByteArray.length + 8);
		byteBuffer.put(nameByteArray);
		
		return nameByteArray;
    }
	
	public static byte[] reverseByte(byte[] byteDate){
    	 
		logger.debug(byteDate.length);
    	byte[] rByte = new byte[byteDate.length];

    	for ( int i = 0; i < byteDate.length; i++ ){
    		rByte[byteDate.length-1-i] = byteDate[i];
    	}
		
		return rByte;
    }
	
	public static String getUnixTime(String Datetime) throws Exception
    {
		long basedUnixTimeLong = TimeConversion(basedUnixTime);
		long datetimeLong = TimeConversion(Datetime);
		
		long unixTimeD = datetimeLong-basedUnixTimeLong;
 
	    return String.valueOf(unixTimeD);
    }
	/**
	 * unixTime時間格式
	 * @param Datetime 日期時間
	 * @return 
	*/
	public static long TimeConversion(String Datetime) throws Exception
    {
    	Date termDate = parseDateTime(Datetime, "yyyyMMddHHmmss");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(termDate);
        long calendarMillis = calendar.getTimeInMillis();
        long unixTime = calendarMillis / 1000L;

	    return unixTime;
    }
	/**
	 * 時間格式檢查
	 * @param dateString 日期時間
	 * @return pattern 日期時間格式
	*/
	public static Date parseDateTime(String dateString, String pattern)
    {
        DateFormat dateformat = new SimpleDateFormat(pattern);
        Date date=null;
        try
        {
            date = dateformat.parse(dateString);
        }
        catch (ParseException e)
        {
        	logger.error(e);
        }
        return date;
       
    }
	
	public static byte encryptForImp(ExpFileInfo expFileInfo) throws Exception {
    	
		byte[] lineByte = handleFile(expFileInfo.getExpTempFile());
		System.out.println(new String (lineByte, expFileInfo.getFileEncoding()));
		byte xorData = XorData(lineByte);
		
		logger.debug("xorData: " + xorData);
		
		return xorData;
	}
	
	/**
	 * 提供Xor演算。
	 * @param bytes 需演算的值
	 * @return Xor後結果
	*/
	public static byte XorData(byte[] bytes)
    {
		byte tmpByte = 0;
		
		for(int b=0; b<bytes.length; b++)
		{
			if(b==0) {
				tmpByte = bytes[b];
				continue;
			}
			
			tmpByte ^= bytes[b];
			
		}
		return tmpByte;
    }
	
	public static boolean isBlankOrNull(String value) {
		return (value == null || value.trim().equals(""));
	}
	
	/**
	 * 以byte方式讀檔。
	 * @param bytes 需演算的值
	 * @return Xor後結果
	*/
	private static byte[] handleFile(File file){
    	
		
    	BufferedInputStream bis = null;
    	byte[] lineByte = new byte[0];
    	byte[] lineByte2 = new byte[0];
        try {
            bis = new BufferedInputStream(new FileInputStream(file));
            int bytesRead = 0;
            int idx=0;
            
            while(bis.available()>0){
            	
            	bytesRead = bis.read();
            	
            	lineByte2 =  new byte[idx];
            	System.arraycopy(lineByte, 0, lineByte2, 0, lineByte2.length);
            	
            	idx++;
            	
            	lineByte = new byte[idx];
        		
        		System.arraycopy(lineByte2, 0, lineByte, 0, lineByte2.length);
        		
        		lineByte[idx-1] = (byte) bytesRead;
            }
        }
        catch (Exception ignore) {
        	
        }
        finally {
            ReleaseResource.releaseIO(bis);
        }
        return lineByte;
    }
	
	/**
	 * 提供精确的加法运算。
	 * @param v1 被加数
	 * @param v2 加数
	 * @return 两个参数的和
	*/
	public static double add(double v1,double v2){
    	BigDecimal b1 = new BigDecimal(Double.toString(v1));
    	BigDecimal b2 = new BigDecimal(Double.toString(v2));
    	return b1.add(b2).doubleValue();
	}

	/**
	 * 提供精确的减法运算。
	 * @param v1 被减数
	 * @param v2 减数
	 * @return 两个参数的差
	 */
	public static double sub(double v1,double v2){
		BigDecimal b1 = new BigDecimal(Double.toString(v1));
    	BigDecimal b2 = new BigDecimal(Double.toString(v2));
    	return b1.subtract(b2).doubleValue();
	}

	/**
	 * 提供精确的乘法运算。
	 * @param v1 被乘数
	 * @param v2 乘数
	 * @return 两个参数的积
	 */
	public static double mul(double v1,double v2){
		BigDecimal b1 = new BigDecimal(Double.toString(v1));
		BigDecimal b2 = new BigDecimal(Double.toString(v2));
		return b1.multiply(b2).doubleValue();
	}
	/**
	 * 提供精确的除法运算。
	 * @param v1 被除数
	 * @param v2 除数
	 * @return 两个参数的商
	 */
	public static double div(double v1,double v2){
		BigDecimal b1 = new BigDecimal(Double.toString(v1));
		BigDecimal b2 = new BigDecimal(Double.toString(v2));
		return b1.divide(b2,10,BigDecimal.ROUND_HALF_UP).doubleValue();

	}
	
}
