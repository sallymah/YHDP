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
 * *****************  Version 2  *****************
 * User: Rock         2006/11/07 (YYYY/MM/DD) Time: 14:30
 * 新增建構子 由使用者傳入ftp參數
 * 
 */
package tw.com.hyweb.core.yhdp.batch.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.Vector;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.log4j.Logger;

import tw.com.hyweb.util.CipherUtil;

/**
 * FTPUtil: 提供FTP的工具<br>
 * 功能:<br>
 * 1.upload<br>
 * 2.download<br>
 * <br>
 * FTP utility<br>
 * 
 * @author Sonys
 */
public class FTPUtil
{
    private final static Logger logger = Logger.getLogger(FTPUtil.class);

    private static String FTP_CONFIG = "config/ftp.properties";

    private static String SITE_IP_IDENTIFIER = ".ftp.site.ip";

    private static String SITE_PORT_IDENTIFIER = ".ftp.site.port";

    private static String SITE_USERNAME_IDENTIFIER = ".ftp.site.username";

    private static String SITE_PASSWORD_IDENTIFIER = ".ftp.site.password";

    private Properties prop;

    private String host;

    private int port;

    private String username;

    private String password;

    public FTPClient ftpClient;

    /**
     * Create a new FTPUtil Object<br>
     * 
     * @param site
     */
    public FTPUtil(String site)
    {
        try
        {
            init();

            if (null == site || site.equals(""))
            {
                site = "default";
            }

            this.host = this.prop.getProperty(site + SITE_IP_IDENTIFIER);
            this.port = Integer.parseInt(this.prop.getProperty(site + SITE_PORT_IDENTIFIER));
            this.username = this.prop.getProperty(site + SITE_USERNAME_IDENTIFIER);
            this.password = new String(CipherUtil.decrypt(this.prop.getProperty(site + SITE_PASSWORD_IDENTIFIER)));
        }
        catch (IOException ie)
        {
            logger.warn("load ftp.properties fail:" + ie.fillInStackTrace());
        }

        this.ftpClient = new FTPClient();
    }

    /**
     * Create a new FTPUtil Object<br>
     * define ftp properties yourself<br>
     * 
     * @param host
     * @param port
     * @param username
     * @param password
     */
    public FTPUtil(String host, int port, String username, String password)
    {

        this.host = host;
        this.port = port;
        this.username = username;
        this.password = new String(CipherUtil.decrypt(password));

        this.ftpClient = new FTPClient();
    }
    /**
     * initialize<br>
     * 
     * @throws IOException
     */
    private void init() throws IOException
    {
        this.prop = new Properties();
        FileInputStream fis = new FileInputStream(FTP_CONFIG);
        this.prop.load(fis);
    }

    /**
     * 設定無加密之密碼<br>
     * Set password in plain text<br>
     * 
     * @param password
     */
    public void setNoEncryptPassword(String password)
    {
        this.password = password;
    }

    /**
     * 與FTP Server連線並登入<br>
     * Connect FTP server<br>
     * 
     * @throws Exception
     */
    public void connect() throws Exception
    {
        this.ftpClient.connect(this.host, this.port);

        if (!this.ftpClient.login(this.username, this.password))
        {
            throw new Exception("login fail:" + this.ftpClient.getReplyString());
        }
    }

    /**
     * 登出FTP Server<br>
     * Logout FTP server<br>
     * 
     * @return boolean
     * @throws Exception
     */
    public boolean logout() throws Exception
    {
        return this.ftpClient.logout();
    }

    /**
     * 與FTP Server斷線<br>
     * Disconnect FTP server<br>
     * 
     * @throws Exception
     */
    public void disconnect() throws Exception
    {
        this.ftpClient.disconnect();
    }

    /**
     * 指定FTP Server的使用者名稱<br>
     * Set user name<br>
     * 
     * @param username
     */
    public void setUsername(String username)
    {
        this.username = username;
    }

    /**
     * 指定FTP Server已加密過的使用者密碼<br>
     * Set password in cipher text<br>
     * 
     * @param password
     */
    public void setPassword(String password)
    {
        this.password = new String(CipherUtil.decrypt(password));
    }

    /**
     * 取得FTP Server的回應字串<br>
     * Get interactive message from FTP server<br>
     * 
     * @return response string
     */
    public String getReplyString()
    {
        return this.ftpClient.getReplyString();
    }

    /**
     * 取得FTP Server的回應碼<br>
     * Get reply code from FTP server<br>
     * 
     * @return response code
     */
    public int getReplyCode()
    {
        return this.ftpClient.getReplyCode();
    }

    /**
     * 取得單一檔案<br>
     * Download a file at one time<br>
     * Working dir will be changed to remoteDir after executing<br>
     * 
     * @param remoteDir
     * @param localDir
     * @param remoteFilename
     * @param localFilename
     * @return replyCode
     */
    public int get(String remoteDir, String localDir, String remoteFilename, String localFilename) throws Exception
    {
        ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
        
        File localPath = new File(localDir);

        if (!localPath.exists())
        {
            throw new Exception("local directory not exist");
        }

        if (this.ftpClient.changeWorkingDirectory(remoteDir))
        {
            FTPFile[] files = this.ftpClient.listFiles();

            for (int i = 0; i < files.length; i++)
            {
                if (remoteFilename.equals(files[i].getName()))
                {
                    File localFile = new File(localDir + localFilename);
                    FileOutputStream fos = new FileOutputStream(localFile);
                    this.ftpClient.retrieveFile(files[i].getName(), fos);
                    fos.close();
                }
            }
        }
        else
        {
            logger.debug("change fail " + this.getReplyCode());
            throw new Exception("remote directory not exist");
        }

        return this.getReplyCode();
    }

    /**
     * 取得多個檔案(含子目錄)<br>
     * Download multi-file including sub-directories<br>
     * Working dir will be changed to remoteDir after executing<br>
     * 
     * @param remoteDir
     * @param localDir
     * @return replyCode
     * @throws Exception
     */
    public int mget(String remoteDir, String localDir) throws Exception
    {
        ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
        
        File localPath = new File(localDir);

        if (!localPath.exists()) // 檢查本地目錄是否存在
        {
            throw new Exception("local directory not exist");
        }

        // 切換目錄
        if (this.ftpClient.changeWorkingDirectory(remoteDir))
        {
            logger.debug("remoteDir=" + remoteDir);

            FTPFile[] files = this.ftpClient.listFiles();

            for (int i = 0; i < files.length; i++)
            {
                if (files[i].isFile()) // 檔案
                {
                    logger.debug("local file:" + localDir + files[i].getName());
                    File localFile = new File(localDir + files[i].getName());
                    FileOutputStream fos = new FileOutputStream(localFile);
                    this.ftpClient.retrieveFile(files[i].getName(), fos); // 抓檔
                    fos.close();
                }
                else
                // 目錄
                {
                    // 建立與遠端目錄同名之本地端目錄
                    String dir = localDir + files[i].getName() + File.separator;
                    File local = new File(dir);

                    if (!local.exists())
                    {
                        local.mkdir();
                    }

                    this.mget(files[i].getName(), dir); // 遞迴,進入子目錄抓取檔案
                    this.ftpClient.changeToParentDirectory(); // 回上層目錄
                }
            }
        }
        else
        {
            logger.warn("no such remote direcotry:" + remoteDir);
            throw new Exception("no such remote directory:" + remoteDir);
        }

        return this.getReplyCode();
    }

    /**
     * 上傳單一檔案至FTP Server<br>
     * Upload a file to FTP server at one time<br>
     * Working dir will be changed to remoteDir after executing<br>
     * 
     * @param localDir
     * @param remoteDir
     * @param localFilename
     * @param remoteFilename
     * @throws Exception
     */
    public void put(String localDir, String remoteDir, String localFilename, String remoteFilename) throws Exception
    {
        ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
        
        File localFile = new File(localDir + localFilename);

        if (!localFile.exists())
        {
            throw new Exception("no such local file:" + localDir + localFilename);
        }

        if (this.ftpClient.changeWorkingDirectory(remoteDir))
        {
            FileInputStream fis = new FileInputStream(localFile);
            this.ftpClient.appendFile(remoteFilename, fis); // put local file to
                                                            // ftp server
            fis.close();
        }
        else
        {
            logger.warn("no such remote direcotry:" + remoteDir);
            throw new Exception("no such remote directory:" + remoteDir);
        }
    }

    /**
     * 上傳多檔至FTP Server(含子目錄)<br>
     * Upload multi-file to FTP server including sub-directories<br>
     * Working dir will be changed to remoteDir after executing<br>
     * 
     * @param localDir
     * @param remoteDir
     * @throws Exception
     */
    public void mput(String localDir, String remoteDir) throws Exception
    {
        ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
        
        File locDir = new File(localDir);

        if (!locDir.exists())
        {
            throw new Exception("no such local directory:" + localDir);
        }

        if (this.ftpClient.changeWorkingDirectory(remoteDir))
        {
            File[] files = locDir.listFiles();

            for (int i = 0; i < files.length; i++)
            {
                if (files[i].isFile()) // 檔案
                {
                    File localFile = new File(localDir + files[i].getName());
                    FileInputStream fis = new FileInputStream(localFile);
                    this.ftpClient.appendFile(files[i].getName(), fis);
                    fis.close();
                }
                else
                // 目錄
                {
                    // 無此目錄則建立與本地端目錄同名之遠端目錄
                    if (!this.ftpClient.changeWorkingDirectory(files[i].getName()))
                    {
                        if (!this.ftpClient.makeDirectory(files[i].getName()))
                        {
                            throw new Exception("create remote directory fail:" + remoteDir);
                        }
                    }
                    else
                    {
                        this.ftpClient.changeToParentDirectory(); // 切回上層目錄避免遞迴時找不到目錄
                    }

                    this.mput(localDir + files[i].getName() + File.separator, files[i].getName());
                    this.ftpClient.changeToParentDirectory(); // 切回上層目錄
                }
            }
        }
        else
        {
            logger.warn("no such remote direcotry:" + remoteDir);
            throw new Exception("no such remote directory:" + remoteDir);
        }
    }

    /**
     * 對遠端檔案重新命名<br>
     * Rename remote file<br>
     * 
     * @param workingDir
     * @param from
     * @param to
     * @throws Exception
     */
    public void rename(String workingDir, String from, String to) throws Exception
    {
        if (this.ftpClient.changeWorkingDirectory(workingDir))
        {
            this.ftpClient.rename(from, to);
        }
        else
        {
            throw new Exception("rename fail : no such remote directory");
        }
    }

    /**
     * 刪除遠端檔案<br>
     * Delete remote file<br>
     * 
     * @param remoteDir
     * @param filename
     * @throws Exception
     */
    public void delete(String remoteDir, String filename) throws Exception
    {
        if (!this.ftpClient.deleteFile(remoteDir + filename))
        {
            throw new Exception("delete remote file fail:" + remoteDir + filename);
        }
    }

    /**
     * <pre>
     * just call getFileList(rdir, regex, flist, -1);
     * <pre>
     * @param rdir
     * @param regex
     * @param flist
     * @throws Exception
     */
    public void getFileList(String rdir, String regex, Vector flist) throws Exception {
        getFileList(rdir, regex, flist, -1);
    }

    /**
     * 依據 <em>dir</em> 將第 <em>level</em> 層目錄下的檔名符合此 <em>regex</em> 加到 <em>flist</em>
     * <em>level</em> = -1 時是所有目錄都處理
     * <em>level</em> = 0 時是 <em>rdir</em> 目錄下的檔名符合此 <em>regex</em> 加到 <em>flist</em>
     * <em>level</em> = 1 時是 <em>rdir</em> 第一層目錄下的檔名符合此 <em>regex</em> 加到 <em>flist</em>
     * ...
     * 依此類推
     * @param rdir 遠端目錄名稱
     * @param regex 檔名符合此 regex 才加
     * @param flist
     * @param level
     * @throws Exception
     */
    public void getFileList(String rdir, String regex, Vector flist, int level) throws Exception {
        FTPFile[] list = ftpClient.listFiles(rdir);
        if (list != null) {
            for (int i = 0; i < list.length; i++) {
                if (list[i].isDirectory()) {
                    if (level > 0 || level == -1) {
                        int dirLevel = level;
                        if (level == -1) {
                            dirLevel = -1;
                        }
                        else if (level == 0) {
                            dirLevel = 0;
                        }
                        else {
                            dirLevel = level - 1;
                        }
                        getFileList(rdir + "/" + list[i].getName(), regex, flist, dirLevel);
                    }
                }
                else {
                    if (level <= 0) {
                        Pattern p = Pattern.compile(regex);
                        Matcher m = p.matcher(list[i].getName());
                        boolean match = m.matches();
                        if (match) {
                            flist.add(rdir + "/" + list[i].getName());
                        }
                    }
                }
            }
        }
    }
}
