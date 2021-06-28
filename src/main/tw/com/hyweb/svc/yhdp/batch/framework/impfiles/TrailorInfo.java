package tw.com.hyweb.svc.yhdp.batch.framework.impfiles;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * <pre>
 * TrailorInfo javabean
 * hasTrailor:boolean
 * hasCustTrailor:boolean
 * numLines:int
 * lines:List object, each element is String object
 * </pre>
 * author: duncan
 */
public class TrailorInfo implements Serializable {
    /*
     hasTrailor:boolean:attr
       false, no check trailor, true check by hasCustTrailor
     hasCustTrailor:boolean:attr
       false, no check, true check by numLines and lines (has the same lines)
     numLines:int:attr
     lines:List, each element is String:element
    */
    private boolean hasTrailor = false;
    private boolean hasCustTrailor = false;
    private int numLines = 0;
    private List lines = new ArrayList();

    public TrailorInfo() {
    }

    public boolean isHasTrailor() {
        return hasTrailor;
    }

    public void setHasTrailor(boolean hasTrailor) {
        this.hasTrailor = hasTrailor;
    }

    public boolean isHasCustTrailor() {
        return hasCustTrailor;
    }

    public void setHasCustTrailor(boolean hasCustTrailor) {
        this.hasCustTrailor = hasCustTrailor;
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

        TrailorInfo that = (TrailorInfo) o;

        if (hasCustTrailor != that.hasCustTrailor) {
            return false;
        }
        if (hasTrailor != that.hasTrailor) {
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
        result = (hasTrailor ? 1 : 0);
        result = 31 * result + (hasCustTrailor ? 1 : 0);
        result = 31 * result + numLines;
        result = 31 * result + (lines != null ? lines.hashCode() : 0);
        return result;
    }

//    public void reorder() {
//        if (hasTrailor) {
//            if (hasCustTrailor) {
//                // has cust trailor
//                numLines = lines.size();
//            }
//            else {
//                // no this case
//            }
//        }
//        else {
//            // no trailor
//            hasCustTrailor = false;
//            numLines = 0;
//            lines.clear();
//        }
//    }

    //

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("[TrailorInfo: ");
        sb.append(" hasTrailor:" + hasTrailor);
        sb.append(" hasCustTrailor:" + hasCustTrailor);
        sb.append(" numLines:" + numLines);
        sb.append(" lines:" + lines);
        sb.append("]");
        return sb.toString();
    }
}
