package tw.com.hyweb.core.yhdp.batch.framework.impfiles;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * <pre>
 * HeaderInfo javabean
 * hasHeader:boolean
 * hasCustHeader:boolean
 * numLines:int
 * lines:List object, each element is String object
 * </pre>
 * author: duncan
 */
public class HeaderInfo implements Serializable {
    /*
     hasHeader:boolean:attr
       false, no check header, true check by hasCustHeader
     hasCustHeader:boolean:attr
       false, check header by filespec, true check by numLines and lines (has the same lines)
     numLines:int:attr
     lines:List, each element is String:element
    */
    private boolean hasHeader = true;
    private boolean hasCustHeader = false;
    private int numLines = 0;
    private List lines = new ArrayList();

    public HeaderInfo() {
    }

    public boolean isHasHeader() {
        return hasHeader;
    }

    public void setHasHeader(boolean hasHeader) {
        this.hasHeader = hasHeader;
    }

    public boolean isHasCustHeader() {
        return hasCustHeader;
    }

    public void setHasCustHeader(boolean hasCustHeader) {
        this.hasCustHeader = hasCustHeader;
    }

    public int getNumLines() {
        return numLines;
    }

    public void setNumLines(int numLines) {
        this.numLines = numLines;
    }

    public List getLines() {
        return lines;
    }

    public void setLines(List lines) {
        this.lines = lines;
    }

    public void addLine(String line) {
        if (line == null || "".equals(line)) {
            return;
        }
        lines.add(line);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        HeaderInfo that = (HeaderInfo) o;

        if (hasCustHeader != that.hasCustHeader) {
            return false;
        }
        if (hasHeader != that.hasHeader) {
            return false;
        }
        if (numLines != that.numLines) {
            return false;
        }
        if (lines != null ? !lines.equals(that.lines) : that.lines != null) {
            return false;
        }

        return true;
    }

    public int hashCode() {
        int result;
        result = (hasHeader ? 1 : 0);
        result = 31 * result + (hasCustHeader ? 1 : 0);
        result = 31 * result + numLines;
        result = 31 * result + (lines != null ? lines.hashCode() : 0);
        return result;
    }

//    public void reorder() {
//        if (hasHeader) {
//            if (hasCustHeader) {
//                // has cust header
//                numLines = lines.size();
//            }
//            else {
//                // default header
//                hasCustHeader = false;
//                numLines = 1;
//                lines.clear();
//            }
//        }
//        else {
//            // no header
//            hasCustHeader = false;
//            numLines = 0;
//            lines.clear();
//        }
//    }

    //

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("[HeaderInfo: ");
        sb.append(" hasHeader:" + hasHeader);
        sb.append(" hasCustHeader:" + hasCustHeader);
        sb.append(" numLines:" + numLines);
        sb.append(" lines:" + lines);
        sb.append("]");
        return sb.toString();
    }
}

