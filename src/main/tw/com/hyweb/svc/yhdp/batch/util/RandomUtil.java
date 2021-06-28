package tw.com.hyweb.svc.yhdp.batch.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class RandomUtil {
	
	
	public static void main(String[] args) throws Exception
	{
		
		int[] temp = RandomUtil.GenerateNonDuplicateRan2(10,11);
		
		for(int i = 0 ; i < temp.length ; i++){
			System.out.println(temp[i]);
		}
	}
	
	public static int[] GenerateNonDuplicateRan2(int range,int count)  
    {  
        Random rand = new Random();  
        int rdm[] = new int[count];
        
        List holeList = new ArrayList();  
        
        for(int i=0; i < count; i++){
        	
        	if(i>=range){
        		holeList = new ArrayList();
        	}
        	
        	int pv = rand.nextInt(range);
        	//System.out.println("pv:"+pv);
        	while(holeList.contains(pv)){
        		
        		pv = rand.nextInt(range);
        		//System.out.println("pv_I:"+pv);
        	}
        	holeList.add(pv);
            rdm[i] = pv;
        	  
        }
        return rdm;  
    }
	
}
