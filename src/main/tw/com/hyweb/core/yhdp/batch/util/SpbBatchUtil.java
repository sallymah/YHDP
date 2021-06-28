/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tw.com.hyweb.core.yhdp.batch.util;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * CardNoChecker
 * @author Jeff Kung
 * @version 
 */
public class SpbBatchUtil {

    public SpbBatchUtil() {
    	
    }
    
    //傳入卡號(前15碼),算出卡號檢查碼(第16碼)
    public static int CardNoChecker(String Data) 
    {
        int sum = 0;
        for (int i = 0; i < Data.length(); ++i)
        {
            int temp = Integer.valueOf(Data.substring(i, i + 1)) * (i % 2 == 0 ? 2 : 1);
            sum += temp < 10 ? temp : temp - 9;
        }
        return (10 - (sum % 10)) % 10;
    }
}
