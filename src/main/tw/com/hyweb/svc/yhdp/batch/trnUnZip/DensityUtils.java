package tw.com.hyweb.svc.yhdp.batch.trnUnZip;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.log4j.Logger;


public class DensityUtils {
	private static Logger log = Logger.getLogger(DensityUtils.class);
	
	/**
	 * GZip it
	 * @param zipFile output GZip file location
	 */
	public static void GzFile( String input, String output, boolean isDelete ){
	
		 byte[] buffer = new byte[1024];
		
		 try{
		
			GZIPOutputStream gzos = 
				new GZIPOutputStream(new FileOutputStream(output));
		
		    FileInputStream in = 
		        new FileInputStream(input);
		
		    int len;
		    while ((len = in.read(buffer)) > 0) {
		    	gzos.write(buffer, 0, len);
		    }
		
		    in.close();
		
			gzos.finish();
			gzos.close();
		
			log.debug("[" + input + "] gz --> ["+ output +"]");
			
			if (isDelete){
				File file = new File(input);
				file.delete();
				log.debug("Delete file: [" + input + "]");
			}
		
		}
		 catch(IOException ex){
		   ex.printStackTrace();   
		}
	}
	
	/*public static void UZipFile( String input, String output, boolean isDelete){
		
		try{
			File outputFile = new File(output);
			
			//開啟欲解壓縮的檔案
			ZipInputStream zIn = new ZipInputStream (
			new BufferedInputStream(new FileInputStream(input)) );

			//取得壓縮檔內的下一個項目(一個壓縮的檔案或目錄，為壓縮時所建立的)
			ZipEntry zipEntry = null;
			
			//先判斷上一層資料夾是否存在，若不存在則先建立資料夾，再解壓縮檔案
			while ( (zipEntry = zIn.getNextEntry()) != null ) 
			{
				if(!zipEntry.isDirectory()) {
					
//					if ( zipEntry.getName().equals(outputFile.getName()) ){
//						
//						File parent = outputFile.getParentFile();
//						if(!parent.exists()) {
//							parent.mkdirs(); 
//						}
//						//開啟解壓縮欲寫入的檔案
//						FileOutputStream fOut = new FileOutputStream(outputFile);
//						//以byte讀取解壓縮後的資料再寫入檔案
//						int byteNo1;
//						byte[] b1 = new byte[64];
//						while ((byteNo1=zIn.read(b1))>0) {
//							fOut.write(b1, 0, byteNo1);
//						}
//						fOut.close();
//						log.debug("[" + input + "] uzip --> ["+ output +"]");
//					}
					
					File parent = outputFile.getParentFile();
					if(!parent.exists()) {
						parent.mkdirs(); 
					}
					//開啟解壓縮欲寫入的檔案
					FileOutputStream fOut = new FileOutputStream(outputFile);
					//以byte讀取解壓縮後的資料再寫入檔案
					int byteNo1;
					byte[] b1 = new byte[64];
					while ((byteNo1=zIn.read(b1))>0) {
						fOut.write(b1, 0, byteNo1);
					}
					fOut.close();
					log.debug("[" + input + "] uzip --> ["+ output +"]");
					
				}
			}
			zIn.close();
			if (isDelete){
				File file = new File(input);
				file.delete();
				log.debug("Delete file: [" + input + "]");
			}
	
		}
		catch(IOException ex){
			   ex.printStackTrace();   
			}
	}*/
	public static List<File> UZipFile( String input,  boolean isDelete){
		
		List unZipFiles = new ArrayList();
		
		try{
			File inputFile = new File(input);
			
			//開啟欲解壓縮的檔案
			ZipInputStream zIn = new ZipInputStream (
			new BufferedInputStream(new FileInputStream(input)) );

			//取得壓縮檔內的下一個項目(一個壓縮的檔案或目錄，為壓縮時所建立的)
			ZipEntry zipEntry = null;
			
			//先判斷上一層資料夾是否存在，若不存在則先建立資料夾，再解壓縮檔案
			while ( (zipEntry = zIn.getNextEntry()) != null ) 
			{
				if(!zipEntry.isDirectory()) {

					File parent = inputFile.getParentFile();
					if(!parent.exists()) {
						parent.mkdirs(); 
					}
					//開啟解壓縮欲寫入的檔案
					File output = new File(inputFile.getParentFile() + File.separator + zipEntry.getName());
					if ( !output.isFile() ){
						FileOutputStream fOut = new FileOutputStream( output );
						//以byte讀取解壓縮後的資料再寫入檔案
						int byteNo1;
						byte[] b1 = new byte[64];
						while ((byteNo1=zIn.read(b1))>0) {
							fOut.write(b1, 0, byteNo1);
						}
						fOut.close();
						log.debug("[" + input + "] uzip --> ["+ output.getAbsoluteFile() +"]");
						
						unZipFiles.add(output);
					}
					else{
						log.warn("[" + output.getAbsoluteFile() + "] is exist, unzip fail. ");
					}
				}
			}
			zIn.close();
			if (isDelete){
				File file = new File(input);
				file.delete();
				log.debug("Delete file: [" + input + "]");
			}
	
		}
		catch(IOException ex){
			ex.printStackTrace();   
		}
		
		return unZipFiles;
	}
	
	
	public static void ZipFile( ZipOutputStream zOut, File physicalFile, String ZipEntryName, boolean isDelete) throws Exception{
		
		FileInputStream file = new FileInputStream(physicalFile);
		/*在壓縮檔內建立一個項目(表示一個壓縮的檔案或目錄，可以目錄結構的方式表示，
		    解壓縮後可以設定的目錄結構放置檔案)*/
		zOut.putNextEntry(new ZipEntry(ZipEntryName));
		
		//設定壓縮的程度0~9
		//zOut.setLevel(0); 
		 
		//以byte的方式讀取檔案並寫入壓縮檔
		int byteNo;
		byte[] b = new byte[64];
		while( (byteNo = file.read(b)) > 0){
			zOut.write(b,0,byteNo);//將檔案寫入壓縮檔
		}
		log.debug("[" + physicalFile + "] zip. ");
		file.close();
		zOut.flush();
		
		if (isDelete){
			physicalFile.delete();
			log.debug("Delete file: [" + physicalFile.getAbsolutePath() + "]");
		}
	}
}
