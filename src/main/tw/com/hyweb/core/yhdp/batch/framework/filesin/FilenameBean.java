package tw.com.hyweb.core.yhdp.batch.framework.filesin;

import java.io.Serializable;

/**
 * <pre>
 * FilenameBean javabean
 * fileName:String
 * memIdStart:int
 * memIdEnd:int
 * fileDateStart:int
 * fileDateEnd:int
 * seqnoStart:int
 * seqnoEnd:int
 * </pre>
 * author: duncan
 */
public class FilenameBean implements Serializable
{
    private String fileName = "";
    private int memIdStart = -1;
    private int memIdEnd = -1;
    private int fileDateStart = -1;
    private int fileDateEnd = -1;
    private int seqnoStart = -1;
    private int seqnoEnd = -1;

    public FilenameBean()
    {
    }

    public String getFileName()
    {
        return fileName;
    }

    public void setFileName(String fileName)
    {
        this.fileName = fileName;
    }

    public int getFileDateEnd()
    {
        return fileDateEnd;
    }

    public void setFileDateEnd(int fileDateEnd)
    {
        this.fileDateEnd = fileDateEnd;
    }

    public int getFileDateStart()
    {
        return fileDateStart;
    }

    public void setFileDateStart(int fileDateStart)
    {
        this.fileDateStart = fileDateStart;
    }

    public int getMemIdEnd()
    {
        return memIdEnd;
    }

    public void setMemIdEnd(int memIdEnd)
    {
        this.memIdEnd = memIdEnd;
    }

    public int getMemIdStart()
    {
        return memIdStart;
    }

    public void setMemIdStart(int memIdStart)
    {
        this.memIdStart = memIdStart;
    }

    public int getSeqnoEnd()
    {
        return seqnoEnd;
    }

    public void setSeqnoEnd(int seqnoEnd)
    {
        this.seqnoEnd = seqnoEnd;
    }

    public int getSeqnoStart()
    {
        return seqnoStart;
    }

    public void setSeqnoStart(int seqnoStart)
    {
        this.seqnoStart = seqnoStart;
    }

    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append("[FilenameBean: ");
        sb.append(" fileName:" + fileName);
        sb.append(" memIdStart:" + memIdStart);
        sb.append(" memIdEnd:" + memIdEnd);
        sb.append(" fileDateStart:" + fileDateStart);
        sb.append(" fileDateEnd:" + fileDateEnd);
        sb.append(" seqnoStart:" + seqnoStart);
        sb.append(" seqnoEnd:" + seqnoEnd);
        sb.append("]");
        return sb.toString();
    }
}
