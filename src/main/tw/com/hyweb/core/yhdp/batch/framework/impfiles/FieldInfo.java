/**
 * changelog
 * --------------------
 * 20090310
 * duncan
 * add index property, 因為若是分隔符號的檔案, start, length 不會填
 * --------------------
 * 20070411, add checkEmpty, checkDate, validValues setting
 * AbstractImpFile.checkDataLine(DataLineInfo lineInfo) has default rule depends on these setting
 * checkEmpty=[true | false], default is false
 * when checkEmpty=true, check this field value is empty
 * checkDate=[true | false], default is false
 * when checkDate=true, check this field value is valid date when the field value is not empty
 * validValues=[value1,value2,...,valueN], default is empty, separator can be ',', ' '
 * when validValues is not empty, check this field value by these separated values when the field value is not empty 
 * --------------------
 * 20070329, add defaultValue(default "") setting, 當取得的值是空的或 null, 將以此 defaultValue 值代替
 * jesse 建議
 * --------------------
 */
package tw.com.hyweb.core.yhdp.batch.framework.impfiles;

import java.io.Serializable;

/**
 * <pre>
 * FieldInfo javabean
 * name:String
 * start:int
 * length:int
 * encoding:String
 * type:String
 * trim:boolean
 * numFraction:int
 * desc:String
 * defaultValue:String
 * checkEmpty:boolean
 * checkDate:boolean
 * validValues:String
 * </pre>
 * author: duncan
 */
public class FieldInfo implements Serializable {
    public static final String TYPE_STRING = "string";
    public static final String TYPE_NUMBER = "number";
    /*
     name:String:attr
     start:int:attr
     length:int:attr
     encoding:String:attr
       if no set, default to MappingInfo.encoding
     type:String:attr
       {string|numer}
     trim:boolean:attr
       true is trim, false is not trim, for type = string
     numFraction:int:attr
       number of decimal fraction, for type = number
       ex: 2, after read data, will convert to double and divide 100.0 to get real value
     desc:String:attr
     defaultValue:String:attr
     checkEmpty:boolean:attr
     checkDate:boolean:attr
     validValues:String:attr
    */
    private String name = "";
    private int start = -1;
    private int length = -1;
    private String encoding = "";
    private String type = TYPE_STRING;
    private boolean trim = true;
    private int numFraction = 2;
    private String desc = "";
    private String defaultValue = "";

    // 20070411
    private boolean checkEmpty = false;
    private boolean checkDate = false;
    private String validValues = "";

    private int index = -1;

    public FieldInfo() {
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isTrim() {
        return trim;
    }

    public void setTrim(boolean trim) {
        this.trim = trim;
    }

    public int getNumFraction() {
        return numFraction;
    }

    public void setNumFraction(int numFraction) {
        this.numFraction = numFraction;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public boolean isCheckDate() {
        return checkDate;
    }

    public void setCheckDate(boolean checkDate) {
        this.checkDate = checkDate;
    }

    public boolean isCheckEmpty() {
        return checkEmpty;
    }

    public void setCheckEmpty(boolean checkEmpty) {
        this.checkEmpty = checkEmpty;
    }

    public String getValidValues() {
        return validValues;
    }

    public void setValidValues(String validValues) {
        this.validValues = validValues;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        FieldInfo fieldInfo = (FieldInfo) o;

        if (checkDate != fieldInfo.checkDate) {
            return false;
        }
        if (checkEmpty != fieldInfo.checkEmpty) {
            return false;
        }
        if (length != fieldInfo.length) {
            return false;
        }
        if (numFraction != fieldInfo.numFraction) {
            return false;
        }
        if (start != fieldInfo.start) {
            return false;
        }
        if (trim != fieldInfo.trim) {
            return false;
        }
        if (defaultValue != null ? !defaultValue.equals(fieldInfo.defaultValue) : fieldInfo.defaultValue != null) {
            return false;
        }
        if (desc != null ? !desc.equals(fieldInfo.desc) : fieldInfo.desc != null) {
            return false;
        }
        if (encoding != null ? !encoding.equals(fieldInfo.encoding) : fieldInfo.encoding != null) {
            return false;
        }
        if (name != null ? !name.equals(fieldInfo.name) : fieldInfo.name != null) {
            return false;
        }
        if (type != null ? !type.equals(fieldInfo.type) : fieldInfo.type != null) {
            return false;
        }
        if (validValues != null ? !validValues.equals(fieldInfo.validValues) : fieldInfo.validValues != null) {
            return false;
        }
        if (index != fieldInfo.index) {
            return false;
        }

        return true;
    }

    public int hashCode() {
        int result;
        result = (name != null ? name.hashCode() : 0);
        result = 31 * result + start;
        result = 31 * result + length;
        result = 31 * result + (encoding != null ? encoding.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (trim ? 1 : 0);
        result = 31 * result + numFraction;
        result = 31 * result + (desc != null ? desc.hashCode() : 0);
        result = 31 * result + (defaultValue != null ? defaultValue.hashCode() : 0);
        result = 31 * result + (checkEmpty ? 1 : 0);
        result = 31 * result + (checkDate ? 1 : 0);
        result = 31 * result + (validValues != null ? validValues.hashCode() : 0);
        result = 31 * result + index;
        return result;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("[FieldInfo: ");
        sb.append(" name:" + name);
        sb.append(" start:" + start);
        sb.append(" length:" + length);
        sb.append(" encoding:" + encoding);
        sb.append(" type:" + type);
        sb.append(" trim:" + trim);
        sb.append(" numFraction:" + numFraction);
        sb.append(" desc:" + desc);
        sb.append(" defaultValue:" + defaultValue);
        sb.append(" checkEmpty:" + checkEmpty);
        sb.append(" checkDate:" + checkDate);
        sb.append(" validValues:" + validValues);
        sb.append(" index:" + index);
        sb.append("]");
        return sb.toString();
    }
}
