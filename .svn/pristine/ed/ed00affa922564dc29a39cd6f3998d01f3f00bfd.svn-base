package tw.com.hyweb.core.yhdp.common.misc;

import java.io.*;
import java.util.zip.*;
import java.util.Enumeration;

/**
 * <pre>
 * ZipUtil, copy from K00
 * </pre>
 * author:duncan
 */
public class ZipUtil {
    /**
     * <pre>
     * 將 <em>zipFile</em> 壓縮至 <em>zipOut</em> 這個壓縮檔，並以 <em>rootPath</em> 為
     * 壓縮檔的目錄路徑
     * <pre>
     *
     * @param zipOut   壓縮檔，型別為ZipOutputStream
     * @param zipFile  要壓縮的檔案
     * @param rootPath 壓縮至壓縮檔的目錄路徑
     * @throws IOException
     */
    public static void ZipFile(ZipOutputStream zipOut, File zipFile, String rootPath) throws IOException {
        if (!zipFile.exists()) {
            throw new IOException("FileUtil.ZipFiles() can't find file:" + zipFile);
        }
        if (!zipFile.isFile()) {
            throw new IOException("FileUtil.ZipFiles() is not a file:" + zipFile);
        }
        FileInputStream in = new FileInputStream(zipFile);
        zipOut.putNextEntry(new ZipEntry(rootPath + zipFile.getName()));
        int len;
        byte[] buf = new byte[1024];
        if (rootPath.length() > 0 && !rootPath.endsWith("/")) {
            rootPath += "/";
        }
        while ((len = in.read(buf)) > 0) {
            zipOut.write(buf, 0, len);
            zipOut.flush();
        }
        zipOut.closeEntry();
        in.close();
    }

    /**
     * <pre>
     * 將 <em>zipPath</em> 壓縮至 <em>zipOut</em> 這個壓縮檔，並以 <em>rootPath</em> 為
     * 壓縮檔的目錄路徑。若 <em>zipPath</em> 為目錄時，則會遞迴將此目錄下所有檔案壓縮至
     * <em>zipOut</em>。
     * <pre>
     *
     * @param zipOut   壓縮檔，型別為ZipOutputStream
     * @param zipPath  要壓縮的目錄
     * @param rootPath 壓縮至壓縮檔的目錄路徑
     * @throws IOException
     */
    public static void ZipFolder(ZipOutputStream zipOut, File zipPath, String rootPath) throws IOException {
        if (!zipPath.exists()) {
            throw new IOException("FileUtil.ZipFiles() can't find directory:" + zipPath);
        }
        if (!zipPath.isDirectory()) {
            throw new IOException("FileUtil.ZipFiles() is not a directory:" + zipPath);
        }
        if (rootPath.length() > 0 && !rootPath.endsWith("/")) {
            rootPath += "/";
        }
        File fileList[] = zipPath.listFiles();
        for (int i = 0; i < fileList.length; i++) {
            if (fileList[i].isDirectory()) {
                ZipFolder(zipOut, fileList[i], rootPath + fileList[i].getName() + "/");
            }
            else {
                ZipFile(zipOut, fileList[i], rootPath);
            }
        }
    }

    /**
     * <pre>
     * 將 <em>zipPath</em> 壓縮至 <em>out</em> 這個檔案，並以 <em>rootPath</em> 為
     * 壓縮檔的目錄路徑。若 <em>zipPath</em> 為目錄時，則會遞迴將此目錄下所有檔案壓縮至
     * <em>out</em>。
     * <pre>
     *
     * @param out      壓縮檔，型別為OutputStream
     * @param zipPath  要壓縮的目錄
     * @param rootPath 壓縮至壓縮檔的目錄路徑
     * @throws IOException
     */
    public static void ZipFolder(OutputStream out, File zipPath, String rootPath) throws IOException {
        ZipOutputStream zipOut = new ZipOutputStream(out);
        ZipFolder(zipOut, zipPath, rootPath);
        zipOut.close();
    }

    /**
     * <pre>
     * 將 <em>srcDir</em> 壓縮至 <em>zipFile</em> 這個檔案，並以 <em>rootPath</em> 為
     * 壓縮檔的目錄路徑。若 <em>srcDir</em> 為目錄時，則會遞迴將此目錄下所有檔案壓縮至
     * <em>zipFile</em>。
     * <pre>
     *
     * @param zipFile  壓縮檔名稱
     * @param srcDir   要壓縮的目錄
     * @param rootPath 壓縮至壓縮檔的目錄路徑
     * @throws IOException
     */
    public static void ZipFolder(String zipFile, String srcDir, String rootPath) throws IOException {
        FileOutputStream out = new FileOutputStream(zipFile);
        File zipPath = new File(srcDir);
        ZipFolder(out, zipPath, rootPath);
        out.close();
    }

    /**
     * 傳回此檔案 fn 的 CRC32 code．
     * <pre>
     * 此 method 會自己檢查檔案是否存在，若不存在，傳回 0．
     * 若檔案存在，傳回此檔案的 CRC32 code
     * </pre>
     *
     * @param fn 要做 CRC32 的檔案
     * @return <pre>
     *         0：不合法的 fn 參數
     *         others：傳回此檔案 fn 的 CRC32 code．
     *         </pre>
     */
    public static long getCRC32Code(String fn) {
        fn = fn.replace('/', File.separatorChar);
        long ret = 0;
        File f = new File(fn);
        if (!f.exists() || f.isDirectory()) { // illegal state
            ret = 0;
        }
        else { // fn is a file
            BufferedInputStream bis = null;
            try {
                CRC32 crc32 = new CRC32();
                bis = new BufferedInputStream(new FileInputStream(f));
                // modified for performance
                int buffsize = 0;
                if (f.length() > 1024 * 512) { // 最大 buffsize 為 512 KB
                    buffsize = 1024 * 512;
                }
                else {
                    buffsize = (int) f.length();
                }
                byte [] buff = new byte[buffsize];
                int len = 0;
                while ((len = bis.read(buff)) != -1) {
                    crc32.update(buff, 0, len);
                }
                bis.close();
                ret = crc32.getValue();
            }
            catch (Exception ignore) {
                ;
            }
            finally {
                if (bis != null) {
                    try {
                        bis.close();
                    }
                    catch (Exception ignore) {
                        ;
                    }
                }
            }
        }
        return ret;
    }

    /**
     * 傳回此檔案 fn 的 Adler32 code．
     * <pre>
     * 此 method 會自己檢查檔案是否存在，若不存在，傳回 0．
     * 若檔案存在，傳回此檔案的 Adler32 code
     * </pre>
     *
     * @param fn 要做 Adler32 的檔案
     * @return <pre>
     *         0：不合法的 fn 參數
     *         others：傳回此檔案 fn 的 Adler32 code．
     *         </pre>
     */
    public static long getAdler32Code(String fn) {
        fn = fn.replace('/', File.separatorChar);
        long ret = 0;
        File f = new File(fn);
        if (!f.exists() || f.isDirectory()) { // illegal state
            ret = 0;
        }
        else { // fn is a file
            BufferedInputStream bis = null;
            try {
                Adler32 adler32 = new Adler32();
                bis = new BufferedInputStream(new FileInputStream(f));
                // modified for performance
                int buffsize = 0;
                if (f.length() > 512 * 1024) { // 最大 buffsize 為 512 KB
                    buffsize = 512 * 1024;
                }
                else {
                    buffsize = (int) f.length();
                }
                byte [] buff = new byte[buffsize];
                int len = 0;
                while ((len = bis.read(buff)) != -1) {
                    adler32.update(buff, 0, len);
                }
                bis.close();
                ret = adler32.getValue();
            }
            catch (Exception ignore) {
                ;
            }
            finally {
                if (bis != null) {
                    try {
                        bis.close();
                    }
                    catch (Exception ignore) {
                        ;
                    }
                }
            }
        }
        return ret;
    }

    /**
     * Unzip <i>zipfile</i> parameter to <i>dir</i> parameter．
     * <pre>
     * 註：
     * 　　1. 此 method 會自動檢查此 <i>zipfile</i> 是否為 zip 格式．
     * 　　2. 若 <i>dir</i> 不存在，將會自動建立此目錄．
     * 範例：
     * 　　win：
     * 　　　　unzipFilesToDir("d:/pathtoyouwant/test.zip", "d:/pathtoyouwant");
     * 　　unix-like：
     * 　　　　unzipFilesToDir("/pathtoyouwant/test.zip", "/pathtoyouwant");
     * </pre>
     *
     * @param zipfile 想要解壓縮的檔案．
     * @param dir     解壓縮的檔案要放到那個目錄．必須以 file.separator 為結束．
     */
    public static void unzipFilesToDir(String zipfile, String dir) {
        zipfile = zipfile.replace('/', File.separatorChar);
        dir = dir.replace('/', File.separatorChar);
        File f = new File(zipfile);
        if (!f.exists() || f.isDirectory()) { // illegal
            return;
        }
        if (!dir.endsWith("" + File.separatorChar)) { // add file separator if not end with file separator
            dir += File.separatorChar;
        }
        File d = new File(dir);
        if (!d.exists()) { // dir doesn't exists
            d.mkdirs();
        }
        ZipFile zf = null;
        try {
            zf = new ZipFile(f);
            Enumeration e = zf.entries();
            while (e.hasMoreElements()) {
                ZipEntry ze = (ZipEntry) e.nextElement();
                if (ze.isDirectory()) { // create subdir of zipfile in dir parameter
                    File subdir = new File(dir + ze.getName());
                    if (!subdir.exists()) {
                        subdir.mkdirs();
                    }
                }
                else { // write file of zipfile in dir parameter
                    if (ze.getName().lastIndexOf('/') != -1) {
                        String subdirs = ze.getName().substring(0, ze.getName().lastIndexOf('/'));
                        (new File(dir + subdirs)).mkdirs();
                    }
                    File subfile = new File(dir + ze.getName());
                    BufferedInputStream bis = new BufferedInputStream(zf.getInputStream(ze));
                    BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(subfile), 4096);
                    int buffsize = 0;
                    if (f.length() > 512 * 1024) { // 最大 buffsize 為 512 KB
                        buffsize = 512 * 1024;
                    }
                    else {
                        buffsize = (int) f.length();
                    }
                    byte [] buff = new byte[buffsize];
                    int len = 0;
                    while ((len = bis.read(buff)) != -1) {
                        bos.write(buff, 0, len);
                    }
                    bis.close();
                    bos.close();
                }
            }
            //System.out.println("There are " + zf.size() + " entries in " + zipfile);
            zf.close();
        }
        catch (IOException ioe) { // ignore
            ;
        }
        finally {
            if (zf != null) {
                try {
                    zf.close();
                }
                catch (Exception ignore) {
                    ;
                }
            }
        }
    }
}
