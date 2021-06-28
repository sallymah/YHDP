/**
 * changelog
 * --------------------
 * 20090602
 * duncan,ivan
 * add fieldDataSize properties, for check number of fields
 * --------------------
 */
package tw.com.hyweb.core.yhdp.batch.framework.impfiles;

import java.io.Serializable;
import java.util.HashMap;

/**
 * <pre>
 * DataLineInfo javabean
 * mappingInfo:MappingInfo
 * lineNo:int
 * plainLine:String
 * fieldData:HashMap
 * </pre>
 * author:duncan
 */
public class DataLineInfo implements Serializable {
    private MappingInfo mappingInfo = null;
    private int lineNo = 0;
    private String plainLine = "";
    private HashMap fieldData = new HashMap();
    private int fieldDataSize = 0;

    public DataLineInfo() {
    }

    public int getFieldDataSize() {
        return fieldDataSize;
    }

    public void setFieldDataSize(int fieldDataSize) {
        this.fieldDataSize = fieldDataSize;
    }

    public MappingInfo getMappingInfo() {
        return mappingInfo;
    }

    public void setMappingInfo(MappingInfo mappingInfo) {
        this.mappingInfo = mappingInfo;
    }

    public int getLineNo() {
        return lineNo;
    }

    public void setLineNo(int lineNo) {
        this.lineNo = lineNo;
    }

    public String getPlainLine() {
        return plainLine;
    }

    public void setPlainLine(String plainLine) {
        this.plainLine = plainLine;
    }

    public HashMap getFieldData() {
        return fieldData;
    }

    public Object getFieldData(String fieldName) {
        if (fieldName == null || "".equals(fieldName)) {
            return null;
        }
        return fieldData.get(fieldName);
    }

    public void setFieldData(HashMap fieldData) {
        this.fieldData = fieldData;
    }

    public void addFieldData(String fieldName, Object value) {
        if (fieldName == null || "".equals(fieldName)) {
            return;
        }
        if (value == null) {
            return;
        }
        // 定義在 mappingInfo 才可加入
        for (int i = 0; i < mappingInfo.getFields().size(); i++) {
            FieldInfo fieldInfo = (FieldInfo) mappingInfo.getFields().get(i);
            if (fieldName.equals(fieldInfo.getName())) {
                fieldData.put(fieldName, value);
            }
        }
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("[DataLineInfo: ");
        sb.append(" mappingInfo:" + mappingInfo);
        sb.append(" lineNo:" + lineNo);
        sb.append(" plainLine:" + plainLine);
        sb.append(" fieldData:" + fieldData);
        sb.append(" fieldDataSize:" + fieldDataSize);
        sb.append("]");
        return sb.toString();
    }
}
