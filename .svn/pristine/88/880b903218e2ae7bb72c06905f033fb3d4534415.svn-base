/*
 * Version: 1.0.0
 * Date: 2007-01-03
 */
/*
 * (版權及授權描述)
 *
 * Copyright 2006 (C) Hyweb Technology Co., Ltd. All Rights Reserved.
 *
 * $History: $
 * 
 */
package tw.com.hyweb.core.yhdp.batch.util;

import java.io.File;

import org.apache.log4j.Logger;

public class FilesUtil
{
    private final static Logger logger = Logger.getLogger(FilesUtil.class);
    
    private static final FilesUtil filesUtil = new FilesUtil();
    
    /**
     * Creates a new FilesUtil
     */
    private FilesUtil()
    {
    }

    /**
     * get instance
     *
     * @return FilesUtil
     */
    public static final FilesUtil getInstance()
    {
        return FilesUtil.filesUtil;
    }
    
    /**
     * 檢查檔案大小是否正確
     *
     * @param file
     *        檔案內容
     * @param fileName
     *        檔名大小
     * @return 檢查結果
     */
    public static boolean checkSize(File file, String fileSize)
    {
        return file.length() == Long.parseLong(fileSize);
    }
    
    /**
     * Rename to formal name(replace existing file).
     *
     * @param srcFile
     *        檔案內容
     * @param newFileName
     *        新檔名
     * @return boolean
     */
    public static boolean renameFile(File srcFile, String newFileName)
                              throws Exception
    {
        boolean isRename = false;
        String dir = newFileName.substring(0, newFileName.lastIndexOf(File.separator) + 1);

        try
        {
            if (new File(dir).exists())
            {
                //do nothing
            }
            else
            {
                if (! new File(dir).mkdirs())
                {
                    throw new Exception("renameFile: fail " + "\nFrom: " + srcFile.getPath()
                                        + "\nTo: " + newFileName);
                }
            }

            //檔案已存在,先刪除
            if (new File(newFileName).exists())
            {
                if (! deleteFile(new File(newFileName)))
                {
                    throw new Exception("renameFile: fail " + "\nFrom: " + srcFile.getPath()
                                        + "\nTo: " + newFileName);
                }
            }

            //rename file
            isRename = srcFile.renameTo(new File(newFileName));
        }
        catch (Exception e)
        {
            throw new Exception("renameFile: fail " + "\nFrom: " + srcFile.getPath() + "\nTo: "
                                + newFileName);
        }

        return isRename;
    }
    
    /**
     * Delete the file.
     *
     * @param file File
     * @return boolean
     */
    public static boolean deleteFile(File file) throws Exception
    {
        boolean isOk = false;

        try
        {
            isOk = file.delete();
            isOk = true;
        }
        catch (Exception e)
        {
            logger.warn("deleteFile error:" + e.fillInStackTrace());
            throw new Exception("deleteFile: fail " + "\nFile: " + file.getPath() + file.getName());
        }

        return isOk;
    }
    
    /**
     * List files in a directory.
     *
     * @param dir File
     * 
     * @return String[]
     */
    public static String[] listFiles(File dir) throws Exception
    {
        String[] children = null;

        try
        {
            if (dir.isDirectory())
            {
                children = dir.list();
            }
        }
        catch (Exception e)
        {
            throw new Exception("listFiles: fail " + "\nDir: " + dir.getPath());
        }

        return children;
    }
}
