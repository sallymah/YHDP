package tw.com.hyweb.core.yhdp.batch.framework.impfiles;

import tw.com.hyweb.util.string.StringUtil;
import tw.com.hyweb.util.date.DateUtil;
import tw.com.hyweb.core.yhdp.common.misc.ExecuteSqlsInfo;

import java.util.List;

import org.apache.log4j.Logger;

/**
 */
public class ImpTest extends AbstractImpFile {
    private static Logger log = Logger.getLogger(ImpTest.class);
    private boolean isFirst = true;
    private long time1, time2;

    public ExecuteSqlsInfo beforeHandleDataLine() throws Exception {
        return null;
    }

    public List checkDataLine(DataLineInfo lineInfo) throws Exception {
        return super.checkDataLine(lineInfo);
    }

    public List handleDataLine(DataLineInfo lineInfo) throws Exception {
        if (isFirst) {
            isFirst = false;
            time1 = System.currentTimeMillis();
            time2 = System.currentTimeMillis();
        }
        time1 = time2;
        time2 = System.currentTimeMillis();
        log.info("It's cost " + (time2 - time1) + " ms!");
        return null;
    }

    public static void main(String[] args) {
        ImpTest impTest = null;
        try {
            String batchDate = System.getProperty("date");
            if (StringUtil.isEmpty(batchDate)) {
                batchDate = DateUtil.getTodayString().substring(0, 8);
            }
            impTest = new ImpTest();
            impTest.setFileName("NEWCARD");
            impTest.run(args);
        }
        catch (Exception ignore) {
            ignore.printStackTrace();
        }
    }

	public ExecuteSqlsInfo afterHandleDataLine() throws Exception {
		return null;
	}
}
