/**
 * changelog
 * --------------------
 * 20090310
 * duncan
 * add separatorString property, 為了加上 support 能吃以 "sep" 分隔符號的檔案
 * --------------------
 * 20070329, add ignoreChar(default ' ') setting, 可設定全部都是 ignoreChar 時, 將 ignore, 即轉成空字串
 * --------------------
 */
package tw.com.hyweb.svc.yhdp.batch.framework.impfiles;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * <pre>
 * MappingInfo javabean
 * name:String
 * encoding:String
 * dataLength:int
 * lineStyle:String
 * ignoreChar:String
 * separatorString:String
 * lineLength:int
 * lineSep:String
 * header:HeaderInfo
 * fields:List, each element is FieldInfo object
 * trailor:TrailorInfo
 * </pre>
 * author: duncan
 */
public class MappingInfo implements Serializable {
    public static final String LINESTYLE_DOS = "dos";
    public static final String LINESTYLE_UNIX = "unix";
    public static final String LINESTYLE_MAC = "mac";

    /*
     name:String:attr
       can set to TB_FILE_INFO.FILE_NAME
     encoding:String:attr
       if FieldInfo.encoding no set, default is this encoding
     dataLength:int:attr
       sum of length in fieldInfos must match TB_FILE_INFO.DATA_LEN or this recordLength
     lineStyle:String:attr
       {dos|unix|mac}, 決定換行與換行長度
     ignoreChar:String:attr
     separatorString:String:attr
     lineLength:int
     lineSep:String
     header:HeaderInfo
     fields:List, each element is FieldInfo object
     trailor:TrailorInfo
    */
    private String name = "";
    private String encoding = "";
    private boolean hasCustEncoding = false;
    private int dataLength = -1;
    private String lineStyle = LINESTYLE_DOS;
    // 一個空白
    private String ignoreChar = " ";
    private String separatorString = "";
    private String convertedSeparatorString = "";
    private int lineLength = 2;
    private String lineSep = "\r\n";
    private HeaderInfo header = null;
    private List fields = new ArrayList();
    private TrailorInfo trailor = null;

    public MappingInfo() {
    }

    public String getSeparatorString() {
        return separatorString;
    }

    public void setSeparatorString(String separatorString) {
        this.separatorString = separatorString;
    }

    public String getConvertedSeparatorString() {
        return convertedSeparatorString;
    }

    public void setConvertedSeparatorString(String convertedSeparatorString) {
        this.convertedSeparatorString = convertedSeparatorString;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }
    
    public boolean isHasCustEncoding() {
		return hasCustEncoding;
	}

	public void setHasCustEncoding(boolean hasCustEncoding) {
		this.hasCustEncoding = hasCustEncoding;
	}

	public int getDataLength() {
        return dataLength;
    }

    public void setDataLength(int dataLength) {
        this.dataLength = dataLength;
    }

    public String getLineStyle() {
        return lineStyle;
    }

    public void setLineStyle(String lineStyle) {
        this.lineStyle = lineStyle;
    }

    public String getIgnoreChar() {
        return ignoreChar;
    }

    public void setIgnoreChar(String ignoreChar) {
        if (ignoreChar == null || "".equals(ignoreChar)) {
            return;
        }
        else if (ignoreChar.length() != 1) {
            return;
        }
        this.ignoreChar = ignoreChar;
    }

    public int getLineLength() {
        return lineLength;
    }

    public void setLineLength(int lineLength) {
        this.lineLength = lineLength;
    }

    public String getLineSep() {
        return lineSep;
    }

    public void setLineSep(String lineSep) {
        this.lineSep = lineSep;
    }

    public HeaderInfo getHeader() {
        return header;
    }

    public void setHeader(HeaderInfo header) {
        this.header = header;
    }

    public List getFields() {
        return fields;
    }

    public FieldInfo getField(String name) {
        if (name == null || "".equals(name)) {
            return null;
        }
        FieldInfo ret = null;
        for (int i = 0; i < fields.size(); i++) {
            FieldInfo fieldInfo = (FieldInfo) fields.get(i);
            if (name.equals(fieldInfo.getName())) {
                ret = fieldInfo;
                break;
            }
        }
        return ret;
    }

    public void setFields(List fields) {
        this.fields = fields;
    }

    public void addField(FieldInfo field) {
        if (field != null) {
            fields.add(field);
        }
    }

    public TrailorInfo getTrailor() {
        return trailor;
    }

    public void setTrailor(TrailorInfo trailor) {
        this.trailor = trailor;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        MappingInfo that = (MappingInfo) o;

        if (dataLength != that.dataLength) {
            return false;
        }
        if (lineLength != that.lineLength) {
            return false;
        }
        if (encoding != null ? !encoding.equals(that.encoding) : that.encoding != null) {
            return false;
        }
        if (hasCustEncoding != that.hasCustEncoding) {
            return false;
        }
        if (fields != null ? !fields.equals(that.fields) : that.fields != null) {
            return false;
        }
        if (header != null ? !header.equals(that.header) : that.header != null) {
            return false;
        }
        if (lineSep != null ? !lineSep.equals(that.lineSep) : that.lineSep != null) {
            return false;
        }
        if (lineStyle != null ? !lineStyle.equals(that.lineStyle) : that.lineStyle != null) {
            return false;
        }
        if (ignoreChar != null ? !ignoreChar.equals(that.ignoreChar) : that.ignoreChar != null) {
            return false;
        }
        if (separatorString != null ? !separatorString.equals(that.separatorString) : that.separatorString != null) {
            return false;
        }
        if (name != null ? !name.equals(that.name) : that.name != null) {
            return false;
        }
        if (trailor != null ? !trailor.equals(that.trailor) : that.trailor != null) {
            return false;
        }

        return true;
    }

    public int hashCode() {
        int result;
        result = (name != null ? name.hashCode() : 0);
        result = 31 * result + (encoding != null ? encoding.hashCode() : 0);
        result = 31 * result + (hasCustEncoding ? 1 : 0);
        result = 31 * result + dataLength;
        result = 31 * result + (lineStyle != null ? lineStyle.hashCode() : 0);
        result = 31 * result + (ignoreChar != null ? ignoreChar.hashCode() : 0);
        result = 31 * result + (separatorString != null ? separatorString.hashCode() : 0);
        result = 31 * result + lineLength;
        result = 31 * result + (lineSep != null ? lineSep.hashCode() : 0);
        result = 31 * result + (header != null ? header.hashCode() : 0);
        result = 31 * result + (fields != null ? fields.hashCode() : 0);
        result = 31 * result + (trailor != null ? trailor.hashCode() : 0);
        return result;
    }

    public void setFieldsEncoding() {
        for (int i = 0; i < fields.size(); i++) {
            FieldInfo fieldInfo = (FieldInfo) fields.get(i);
            // set index property
            fieldInfo.setIndex(i);
//            if (fieldInfo.getEncoding() == null || "".equals(fieldInfo.getEncoding())) {
//                fieldInfo.setEncoding(this.encoding);
//            }
        }
    }

    public void resetDataLength() {
        int dataLength = 0;
        if (separatorString == null || "".equals(separatorString)) {
            for (int i = 0; i < fields.size(); i++) {
                FieldInfo fieldInfo = (FieldInfo) fields.get(i);
                dataLength += fieldInfo.getLength();
            }
            dataLength += lineLength;
            this.dataLength = dataLength;
        }
        else {
            this.dataLength = 0;
        }
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("[MappingInfo: ");
        sb.append(" name:" + name);
        sb.append(" encoding:" + encoding);
        sb.append(" hasCustEncoding:" + hasCustEncoding);
        sb.append(" dataLength:" + dataLength);
        sb.append(" lineStyle:" + lineStyle);
        sb.append(" ignoreChar:'" + ignoreChar + "'");
        sb.append(" separatorString:'" + separatorString + "'");
        sb.append(" lineLength:" + lineLength);
        String show = lineSep;
        if ("\r\n".equals(lineSep)) {
            show = "\\r\\n";
        }
        else if ("\n".equals(lineSep)) {
            show = "\\n";
        }
        else if ("\r".equals(lineSep)) {
            show = "\\r";
        }
        sb.append(" lineSep:" + show);
        sb.append(" header:" + header);
        sb.append(" fields:" + fields);
        sb.append(" trailor:" + trailor);
        sb.append("]");
        return sb.toString();
    }
}
