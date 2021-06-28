package tw.com.hyweb.core.yhdp.batch.framework.impfiles;

import java.io.Serializable;

/**
 * <pre>
 * ErrorDescInfo javabean
 * errorCode:String
 * errorMsg:String
 * position:int
 * field:String
 * content:String
 * </pre>
 * author: duncan
 */
public class ErrorDescInfo implements Serializable {
    private String errorCode = "";
    private String errorMsg = "";
    private int position = -1;
    private String field = "";
    private String content = "";

    public ErrorDescInfo() {
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ErrorDescInfo that = (ErrorDescInfo) o;

        if (position != that.position) {
            return false;
        }
        if (content != null ? !content.equals(that.content) : that.content != null) {
            return false;
        }
        if (errorCode != null ? !errorCode.equals(that.errorCode) : that.errorCode != null) {
            return false;
        }
        if (errorMsg != null ? !errorMsg.equals(that.errorMsg) : that.errorMsg != null) {
            return false;
        }
        if (field != null ? !field.equals(that.field) : that.field != null) {
            return false;
        }

        return true;
    }

    public int hashCode() {
        int result;
        result = (errorCode != null ? errorCode.hashCode() : 0);
        result = 31 * result + (errorMsg != null ? errorMsg.hashCode() : 0);
        result = 31 * result + position;
        result = 31 * result + (field != null ? field.hashCode() : 0);
        result = 31 * result + (content != null ? content.hashCode() : 0);
        return result;
    }

    private String toNA(String s) {
        if (s == null || "".equals(s)) {
            return "N/A";
        }
        else {
            return s;
        }
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("[errorCode]=" + errorCode);
        sb.append(" [errorMsg]=" + errorMsg);
        sb.append(" [position]=" + position);
        sb.append(" [field]=" + toNA(field));
        sb.append(" [content]=" + toNA(content));
        return sb.toString();
    }
}
