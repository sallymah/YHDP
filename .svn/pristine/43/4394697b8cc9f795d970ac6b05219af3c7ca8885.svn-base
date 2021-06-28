/**
 * changelog
 * --------------------
 * 20090606
 * duncan
 * 從相關路徑讀 MappingInfos.xml 時讀不到, 改用 classloader 方式來讀
 * --------------------
 * 20090310
 * duncan
 * add separatorString property, 為了加上 support 能吃以 "sep" 分隔符號的檔案
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
 * 20070329, add ignoreChar(default ' ') setting, 可設定全部都是 ignoreChar 時, 將 ignore, 即轉成空字串
 * 20070329, add defaultValue(default "") setting, 當取得的值是空的或 null, 將以此 defaultValue 值代替
 * 處理 xml 設定檔的 ignoreChar 和 defaultValue
 * --------------------
 */
package tw.com.hyweb.svc.yhdp.batch.framework.impfiles;

import org.apache.log4j.Logger;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import tw.com.hyweb.util.ReleaseResource;
import tw.com.hyweb.util.string.StringUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;

/**
 * <pre>
 * MappingLoader
 * </pre>
 * author:duncan
 */
public class MappingLoader {
    private static Logger log = Logger.getLogger(MappingLoader.class);
    private String configFilename = "";
    private File file = null;
    private String encoding = System.getProperty("file.encoding", "UTF-8");
    private String xmlContent = "";
    private HashMap mappingInfos = new HashMap();
    private HashMap encodingInfos = new HashMap();

    // element name constants
    private static final String EN_MAPPINGINFO = "MappingInfo";
    private static final String EN_ENCODINGINFO = "EncodingInfo";
    private static final String EN_HEADERINFO = "HeaderInfo";
    private static final String EN_FIELDINFO = "FieldInfo";
    private static final String EN_TRAILORINFO = "TrailorInfo";
    private static final String EN_LINES = "lines";

    // attribute name constants
    // MappingInfo
    private static final String AN_NAME = "name";
    private static final String AN_ENCODING = "encoding";
    private static final String AN_HASCUSTENCODING = "hasCustEncoding";
    private static final String AN_DATALENGTH = "dataLength";
    private static final String AN_LINESTYLE = "lineStyle";
    private static final String AN_IGNORECHAR = "ignoreChar";
    private static final String AN_SEPARATORSTRING = "separatorString";
    // HeaderInfo
    private static final String AN_HASHEADER = "hasHeader";
    private static final String AN_HASCUSTHEADER = "hasCustHeader";
    private static final String AN_NUMLINES = "numLines";
    // FieldInfo
    private static final String AN_START = "start";
    private static final String AN_LENGTH = "length";
    private static final String AN_TYPE = "type";
    private static final String AN_TRIM = "trim";
    private static final String AN_NUMFRACTION = "numFraction";
    private static final String AN_DESC = "desc";
    private static final String AN_DEFAULTVALUE = "defaultValue";
    // 20070411
    private static final String AN_CHECKEMPTY = "checkEmpty";
    private static final String AN_CHECKDATE = "checkDate";
    private static final String AN_VALIDVALUES = "validValues";

    // TrailorInfo
    private static final String AN_HASTRAILOR = "hasTrailor";
    private static final String AN_HASCUSTTRAILOR = "hasCustTrailor";

    private static final char[] SPECIAL_CHARS = {'\\', '[', ']', '{', '}', '^', '-', '&', '.', '$', '?',
            '*', '+', ',', '|', '(', ')', ':', '!', '<', '>', '='};

    public MappingLoader() {
    }

    public String getConfigFilename() {
        return configFilename;
    }

    public void setConfigFilename(String configFilename) {
        this.configFilename = configFilename;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public String getXmlContent() {
        return xmlContent;
    }

    public void setXmlContent(String xmlContent) {
        this.xmlContent = xmlContent;
    }

    public HashMap getMappingInfos() {
        return mappingInfos;
    }

    public MappingInfo getMappingInfo(String name) {
        if (StringUtil.isEmpty(name)) {
            return null;
        }
        return (MappingInfo) mappingInfos.get(name);
    }

    public void setMappingInfos(HashMap mappingInfos) {
        this.mappingInfos = mappingInfos;
    }

    public void addMappingInfo(String name, MappingInfo mapping) {
        if (StringUtil.isEmpty(name)) {
            return;
        }
        if (mapping == null) {
            return;
        }
        mappingInfos.put(name, mapping);
    }

    public HashMap getEncodingInfos() {
		return encodingInfos;
	}
    
	public void setEncodingInfos(HashMap encodingInfos) {
		this.encodingInfos = encodingInfos;
	}
	
    private Document parseDocument() throws Exception {
        BufferedReader br = null;
        Document doc = null;
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(file), encoding));
        }
        catch (Exception ignore) {
            br = new BufferedReader(new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream(configFilename), encoding));
        }
        try {
            SAXReader reader = new SAXReader();
            reader.setIgnoreComments(true);
            reader.setMergeAdjacentText(true);
            reader.setStripWhitespaceText(true);
            reader.setValidation(false);
            doc = reader.read(br);
        }
        finally {
            ReleaseResource.releaseIO(br);
        }
        return doc;
    }

    public boolean startLoading() {
        if (file == null) {
            throw new IllegalArgumentException("file is null!");
        }
//        if (!file.exists()) {
//            throw new IllegalArgumentException("file not exists!");
//        }
//        if (file.isDirectory()) {
//            throw new IllegalArgumentException("file is a directory!");
//        }
        boolean ret = false;
        Document doc = null;
        try {
            if (file.exists() && file.isFile()) {
                xmlContent = StringUtil.readFile2String(file, encoding);
            }
            else {
                BufferedReader br = null;
                StringBuffer sb = new StringBuffer();
                try {
                    String lineSep = System.getProperty("line.separator", "\n");
                    br = new BufferedReader(new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream(configFilename), encoding));
                    String line = "";
                    while ((line = br.readLine()) != null) {
                        sb.append(line);
                        sb.append(lineSep);
                    }
                }
                finally {
                    ReleaseResource.releaseIO(br);
                }
                xmlContent = sb.toString();
            }
            doc = parseDocument();
            List content = doc.getRootElement().content();
            for (int i = 0; i < content.size(); i++) {
                Node node = (Node) content.get(i);
                if (node instanceof Element) {
                    Element mappingElement = (Element) node;
                    if (EN_MAPPINGINFO.equals(mappingElement.getName())) {
                        MappingInfo mappingInfo = handleMappingInfo(mappingElement);
                        if (checkMappingInfo(mappingInfo)) {
                            addMappingInfo(mappingInfo.getName(), mappingInfo);
                        }
                    }
                    if (EN_ENCODINGINFO.equals(mappingElement.getName())) {
                    	encodingInfos = handleEncodingInfo(mappingElement);
                    	log.debug(encodingInfos);
                    }
                }
            }
            ret = true;
        }
        catch (Exception ignore) {
            ret = false;
            log.warn("startLoading fail:" + ignore.getMessage(), ignore);
        }
        return ret;
    }

    private String convertSpecialCharacter(String value) {
        StringBuffer sb = new StringBuffer();
        if (!StringUtil.isEmpty(value)) {
            for (int i = 0; i < value.length(); i++) {
                boolean isSpecial = false;
                char c = value.charAt(i);
                for (int j = 0; j < SPECIAL_CHARS.length; j++) {
                    if (c == SPECIAL_CHARS[j]) {
                        isSpecial = true;
                        break;
                    }
                }
                if (isSpecial) {
                    sb.append('\\');
                }
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private MappingInfo handleMappingInfo(Element mappingElement) {
        MappingInfo mappingInfo = new MappingInfo();
        // handle attributes
        List attrs = mappingElement.attributes();
        for (int i = 0; i < attrs.size(); i++) {
            Attribute attr = (Attribute) attrs.get(i);
            if (AN_NAME.equals(attr.getName())) {
                // name attr
                mappingInfo.setName(attr.getValue());
            }
            else if (AN_ENCODING.equals(attr.getName())) {
                // encoding attr
                mappingInfo.setEncoding(attr.getValue());
            }
            else if (AN_HASCUSTENCODING.equals(attr.getName())) {
                // hasCustEncoding attr
                mappingInfo.setHasCustEncoding(isTrue(attr.getValue()));
            }
            else if (AN_DATALENGTH.equals(attr.getName())) {
                // dataLength attr
                mappingInfo.setDataLength(Integer.parseInt(attr.getValue()));
            }
            else if (AN_LINESTYLE.equals(attr.getName())) {
                // lineStyle attr
                if (MappingInfo.LINESTYLE_UNIX.equals(attr.getValue())) {
                    // unix style
                    mappingInfo.setLineStyle(MappingInfo.LINESTYLE_UNIX);
                    mappingInfo.setLineLength(1);
                    mappingInfo.setLineSep("\n");
                }
                else if (MappingInfo.LINESTYLE_MAC.equals(attr.getValue())) {
                    // mac style
                    mappingInfo.setLineStyle(MappingInfo.LINESTYLE_MAC);
                    mappingInfo.setLineLength(1);
                    mappingInfo.setLineSep("\r");
                }
                else {
                    // dos and default style
                    mappingInfo.setLineStyle(MappingInfo.LINESTYLE_DOS);
                    mappingInfo.setLineLength(2);
                    mappingInfo.setLineSep("\r\n");
                }
            }
            else if (AN_IGNORECHAR.equals(attr.getName())) {
                // ignoreChar attr
                mappingInfo.setIgnoreChar(attr.getValue());
            }
            else if (AN_SEPARATORSTRING.equals(attr.getName())) {
                // separatorString attr
                String convertValue = convertSpecialCharacter(attr.getValue());
                mappingInfo.setSeparatorString(attr.getValue());
                mappingInfo.setConvertedSeparatorString(convertValue);
            }
        }
        // handle elements
        List content = mappingElement.content();
        for (int i = 0; i < content.size(); i++) {
            Node node = (Node) content.get(i);
            if (node instanceof Element) {
                Element element = (Element) node;
                if (EN_HEADERINFO.equals(element.getName())) {
                    // HeaderInfo
                    HeaderInfo header = handleHeaderInfo(element);
                    mappingInfo.setHeader(header);
                }
                else if (EN_FIELDINFO.equals(element.getName())) {
                    // FieldInfo
                    FieldInfo field = handleFieldInfo(element);
                    mappingInfo.addField(field);
                }
                else if (EN_TRAILORINFO.equals(element.getName())) {
                    // TrailorInfo
                    TrailorInfo trailor = handleTrailorInfo(element);
                    mappingInfo.setTrailor(trailor);
                }
            }
        }
        // handle field encoding by using mapping encoding
        mappingInfo.setFieldsEncoding();
        // reset dataLength by sum of fields length
        mappingInfo.resetDataLength();
        return mappingInfo;
    }

    private FieldInfo handleFieldInfo(Element fieldElement) {
        FieldInfo fieldInfo = new FieldInfo();
        // handle attributes
        List attrs = fieldElement.attributes();
        for (int i = 0; i < attrs.size(); i++) {
            Attribute attr = (Attribute) attrs.get(i);
            if (AN_NAME.equals(attr.getName())) {
                // name
                fieldInfo.setName(attr.getValue());
            }
            else if (AN_START.equals(attr.getName())) {
                // start
                fieldInfo.setStart(Integer.parseInt(attr.getValue()));
            }
            else if (AN_LENGTH.equals(attr.getName())) {
                // length
                fieldInfo.setLength(Integer.parseInt(attr.getValue()));
            }
            else if (AN_TYPE.equals(attr.getName())) {
                // type
                if (FieldInfo.TYPE_NUMBER.equals(attr.getValue())) {
                    // number type
                    fieldInfo.setType(FieldInfo.TYPE_NUMBER);
                }
                else {
                    // string type and default type
                    fieldInfo.setType(FieldInfo.TYPE_STRING);
                }
            }
            else if (AN_TRIM.equals(attr.getName())) {
                // trim
                fieldInfo.setTrim(isTrue(attr.getValue()));
            }
            else if (AN_NUMFRACTION.equals(attr.getName())) {
                // numFraction
                fieldInfo.setNumFraction(Integer.parseInt(attr.getValue()));
            }
            else if (AN_ENCODING.equals(attr.getName())) {
                // encoding
                fieldInfo.setEncoding(attr.getValue());
            }
            else if (AN_DESC.equals(attr.getName())) {
                // desc
                fieldInfo.setDesc(attr.getValue());
            }
            else if (AN_DEFAULTVALUE.equals(attr.getName())) {
                // defaultValue
                fieldInfo.setDefaultValue(attr.getValue());
            }
            // 20070411
            else if (AN_CHECKEMPTY.equals(attr.getName())) {
                // checkEmpty
                fieldInfo.setCheckEmpty(isTrue(attr.getValue()));
            }
            else if (AN_CHECKDATE.equals(attr.getName())) {
                // checkDate
                fieldInfo.setCheckDate(isTrue(attr.getValue()));
            }
            else if (AN_VALIDVALUES.equals(attr.getName())) {
                // validValues
                fieldInfo.setValidValues(attr.getValue());
            }
        }
        return fieldInfo;
    }
    
    private HashMap handleEncodingInfo(Element mappingElement) {
    	
    	HashMap encodingInfos = new HashMap<>();
    	
		List content = mappingElement.content();
        for (int i = 0; i < content.size(); i++) {
            Node node = (Node) content.get(i);
            if (node instanceof Element) {
                Element element = (Element) node;
                if (EN_FIELDINFO.equals(element.getName())) {
                    // FieldInfo
                    List attrs = element.attributes();
					String name = "";
					String encoding = "";
					for (int j = 0; j < attrs.size(); j++) {
						Attribute attr = (Attribute) attrs.get(j);
						if (AN_NAME.equals(attr.getName())) {
							// name
							name = attr.getValue();
						}
						else if (AN_ENCODING.equals(attr.getName())) {
							// start
							encoding = attr.getValue();
						}
					}
					if (!StringUtil.isEmpty(name) && !StringUtil.isEmpty(encoding)){
						encodingInfos.put(name, encoding);
					}
                }
            }
        }
		return encodingInfos;
    }

    private TrailorInfo handleTrailorInfo(Element trailorElement) {
        TrailorInfo trailorInfo = new TrailorInfo();
        // handle attributes
        List attrs = trailorElement.attributes();
        for (int i = 0; i < attrs.size(); i++) {
            Attribute attr = (Attribute) attrs.get(i);
            if (AN_HASTRAILOR.equals(attr.getName())) {
                // hasTrailor
                trailorInfo.setHasTrailor(isTrue(attr.getValue()));
            }
            else if (AN_HASCUSTTRAILOR.equals(attr.getName())) {
                // hasCustTrailor
                trailorInfo.setHasCustTrailor(isTrue(attr.getValue()));
            }
            else if (AN_NUMLINES.equals(attr.getName())) {
                // numLines
                trailorInfo.setNumLines(Integer.parseInt(attr.getValue()));
            }
        }
        // handle elements
        List content = trailorElement.content();
        for (int i = 0; i < content.size(); i++) {
            Node node = (Node) content.get(i);
            if (node instanceof Element) {
                Element element = (Element) node;
                if (EN_LINES.equals(element.getName())) {
                    trailorInfo.addLine(element.getText());
                }
            }
        }
        return trailorInfo;
    }

    private static boolean isTrue(String value) {
        boolean ret = false;
        if (StringUtil.isEmpty(value)) {
            ret = false;
        }
        else {
            if ("1".equalsIgnoreCase(value) || "yes".equalsIgnoreCase(value) ||
                    "true".equalsIgnoreCase(value)) {
                ret = true;
            }
            else {
                ret = false;
            }
        }
        return ret;
    }

    private HeaderInfo handleHeaderInfo(Element headerElement) {
        HeaderInfo headerInfo = new HeaderInfo();
        // handle attributes
        List attrs = headerElement.attributes();
        for (int i = 0; i < attrs.size(); i++) {
            Attribute attr = (Attribute) attrs.get(i);
            if (AN_HASHEADER.equals(attr.getName())) {
                // hasHeader
                headerInfo.setHasHeader(isTrue(attr.getValue()));
            }
            else if (AN_HASCUSTHEADER.equals(attr.getName())) {
                // hasCustHeader
                headerInfo.setHasCustHeader(isTrue(attr.getValue()));
            }
            else if (AN_NUMLINES.equals(attr.getName())) {
                // numLines
                headerInfo.setNumLines(Integer.parseInt(attr.getValue()));
            }
        }
        // handle elements
        List content = headerElement.content();
        for (int i = 0; i < content.size(); i++) {
            Node node = (Node) content.get(i);
            if (node instanceof Element) {
                Element element = (Element) node;
                if (EN_LINES.equals(element.getName())) {
                    headerInfo.addLine(element.getText());
                }
            }
        }
        return headerInfo;
    }

    /**
     * <pre>
     * 做以下的檢查:
     * 1. MappingInfo has name attr.
     * 2. each FieldInfo has name attr.
     * 3. sum of length attr. in fieldInfos equals to MappingInfo dataLength attr.
     * 檢查 OK 傳回 true, 失敗傳回 false
     * </pre>
     *
     * @param mappingInfo mappingInfo
     * @return 檢查 OK 傳回 true, 失敗傳回 false
     */
    public boolean checkMappingInfo(MappingInfo mappingInfo) {
        if (StringUtil.isEmpty(mappingInfo.getName())) {
            log.warn("MappingInfo name attr. missing!");
            log.info("mappingInfo:" + mappingInfo);
            return false;
        }
        for (int i = 0; i < mappingInfo.getFields().size(); i++) {
            FieldInfo field = (FieldInfo) mappingInfo.getFields().get(i);
            if (StringUtil.isEmpty(field.getName())) {
                log.warn("FieldInfo name attr. missing!");
                log.info("fieldInfo:" + field);
                return false;
            }
        }
        if (StringUtil.isEmpty(mappingInfo.getSeparatorString())) {
            // 沒有用 separatorString 時才檢查
            int totalLen = 0;
            for (int i = 0; i < mappingInfo.getFields().size(); i++) {
                FieldInfo field = (FieldInfo) mappingInfo.getFields().get(i);
                totalLen += field.getLength();
            }
            totalLen += mappingInfo.getLineLength();
            if (totalLen != mappingInfo.getDataLength()) {
                log.warn("totalLen (" + totalLen + ") not equal to dataLength (" + mappingInfo.getDataLength() + ")!");
                return false;
            }
        }
        return true;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("[MappingLoader: ");
        sb.append(" file:" + file);
        sb.append(" encoding:" + encoding);
        sb.append(" xmlContent:" + xmlContent);
        sb.append(" mappingInfos:" + mappingInfos);
        sb.append("]");
        return sb.toString();
    }
}
