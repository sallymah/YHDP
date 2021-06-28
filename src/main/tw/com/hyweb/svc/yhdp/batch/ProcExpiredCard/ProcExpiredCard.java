package tw.com.hyweb.svc.yhdp.batch.ProcExpiredCard;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import tw.com.hyweb.core.cp.batch.framework.AbstractBatchBasic;
import tw.com.hyweb.core.cp.batch.util.BatchUtil;
import tw.com.hyweb.service.db.DBService;
import tw.com.hyweb.service.db.info.TbBlacklistSettingPK;
import tw.com.hyweb.service.db.info.TbCardInfo;
import tw.com.hyweb.service.db.info.TbRejectAuthListPK;
import tw.com.hyweb.service.db.mgr.TbBlacklistCodeDefMgr;
import tw.com.hyweb.service.db.mgr.TbBlacklistSettingMgr;
import tw.com.hyweb.service.db.mgr.TbCardMgr;
import tw.com.hyweb.service.db.mgr.TbRejectAuthListMgr;
import tw.com.hyweb.util.ReleaseResource;
import tw.com.hyweb.util.string.StringUtil;

public class ProcExpiredCard extends AbstractBatchBasic {

	private static final Logger log = Logger.getLogger(ProcExpiredCard.class);

	private static final String SPRING_PATH = "config" + File.separator
			+ "batch" + File.separator + "ProcExpiredCard" + File.separator
			+ "spring.xml";

	public static final SimpleDateFormat dateFormat = new SimpleDateFormat(
			"yyyyMMdd");

	private Connection conn = null;
	private String batchDate = null;
	private String sysDate = null;
	private String sysTime = null;

	private int sleepTime = 500;
	private String executeDate = "1";
	private String blackListCode = "06";

	public static void main(String[] args) {

		ProcExpiredCard instance = getInstance();

		instance.setBatchDate(System.getProperty("date"));

		instance.run(null);

		System.exit(1);

	}

	public static ProcExpiredCard getInstance() {
		ApplicationContext apContext = new FileSystemXmlApplicationContext(
				SPRING_PATH);
		ProcExpiredCard instance = (ProcExpiredCard) apContext
				.getBean("ProcExpiredCard");
		return instance;
	}

	@Override
	public void process(String[] arg0) throws Exception {
		try {
			init();
			action();
		} finally {
			if (conn != null)
				ReleaseResource.releaseDB(conn);
		}
	}

	private void init() throws Exception {
		try {
			BatchUtil.getNow();
			if (StringUtil.isEmpty(batchDate)) {
				batchDate = BatchUtil.sysDay;
			} else if (!BatchUtil.checkChristianDate(batchDate)) {
				String msg = "Invalid date for option -Ddate!";
				throw new Exception(msg);
			}
			sysDate = BatchUtil.sysDay;
			sysTime = BatchUtil.sysTime;

			conn = BatchUtil.getConnection();

		} catch (Exception e) {
			throw new Exception("init():" + e);
		}
	}

	private void action() throws Exception {

		formatExecuteDate();
		log.info("batchDate is " + batchDate + ", " + "executeDate is "
				+ executeDate + ". ");

		if (!isValidExecuteDate()) {
			return;
		}

		if (!isValidCode()) {
			log.warn("TB_BLACKLIST_CODE_DEF.BLACKLIST_CODE = "
					+ StringUtil.toSqlValueWithSQuote(blackListCode)
					+ " is empty! ");
		}

		try {
			Vector<TbCardInfo> cardInfos = getCardInfos();
			for (TbCardInfo cardInfo : cardInfos) {
				updateLifeCycle(cardInfo);
				addBlackList(cardInfo);
				removeRejectAuth(cardInfo);
			}
			remarkSuccess();
		} catch (SQLException e) {
			remarkFail();
			throw new Exception("action() SQL execute failed. " + e);
		}
	}

	private void formatExecuteDate() throws Exception {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(dateFormat.parse(batchDate));
		Integer dayOfMonth = Integer.valueOf(executeDate);
		if ("99".equals(executeDate)
				| (dayOfMonth > calendar
						.getActualMaximum(Calendar.DAY_OF_MONTH))) {
			dayOfMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
		}
		calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
		setExecuteDate(dateFormat.format(calendar.getTime()));
	}

	private boolean isValidExecuteDate() {
		if (getBatchDate().equals(getExecuteDate())) {
			return true;
		} else {
			return false;
		}
	}

	private boolean isValidCode() throws SQLException {
		String pk = "BLACKLIST_CODE = "
				+ StringUtil.toSqlValueWithSQuote(blackListCode);
		boolean isExist = new TbBlacklistCodeDefMgr(conn).isExist(pk);
		return isExist;
	}

	private Vector<TbCardInfo> getCardInfos() throws SQLException {
		Vector<TbCardInfo> cardInfos = new Vector<TbCardInfo>();
		TbCardInfo qInfo = new TbCardInfo();
		qInfo.setExpiryDate(BatchUtil.getSomeDay(batchDate, -1));
		new TbCardMgr(conn).queryMultiple(qInfo, cardInfos);
		return cardInfos;
	}

	private void updateLifeCycle(TbCardInfo cardInfo) throws Exception {
		cardInfo.setPreviousLifeCycle(cardInfo.getLifeCycle());
		cardInfo.setLifeCycle("A");
		log.info(cardInfo.toUpdateSQL());
		DBService.getDBService().sqlAction(cardInfo.toUpdateSQL(), conn, false);
	}

	private void addBlackList(TbCardInfo cardInfo) throws Exception {
		TbBlacklistSettingPK pk = new TbBlacklistSettingPK();
		pk.setCardNo(cardInfo.getCardNo());
		pk.setExpiryDate(cardInfo.getExpiryDate());
		boolean isExist = new TbBlacklistSettingMgr(conn).isExist(pk);
		if (isExist) {
			log.warn(pk.toString() + " is exist!");
			return;
		} else {
			StringBuffer column = new StringBuffer();
			column.append("CARD_NO, EXPIRY_DATE, REG_DATE, REG_TIME, REG_USERID, STATUS, ");
			column.append("UPT_USERID, UPT_DATE, UPT_TIME, APRV_USERID, APRV_DATE, APRV_TIME, ");
			column.append("BLACKLIST_CODE ");

			StringBuffer sqlCmd = new StringBuffer();
			sqlCmd.append("Insert into TB_BLACKLIST_SETTING (");
			sqlCmd.append(column.toString());
			sqlCmd.append(") values (");
			sqlCmd.append(StringUtil.toSqlValueWithSQuote(cardInfo.getCardNo()))
					.append(", ");
			sqlCmd.append(
					StringUtil.toSqlValueWithSQuote(cardInfo.getExpiryDate()))
					.append(", ");
			sqlCmd.append(StringUtil.toSqlValueWithSQuote(sysDate))
					.append(", ");
			sqlCmd.append(StringUtil.toSqlValueWithSQuote(sysTime))
					.append(", ");
			sqlCmd.append(StringUtil.toSqlValueWithSQuote("BATCH"))
					.append(", ");
			sqlCmd.append(StringUtil.toSqlValueWithSQuote("1")).append(", ");
			sqlCmd.append(StringUtil.toSqlValueWithSQuote("BATCH"))
					.append(", ");
			sqlCmd.append(StringUtil.toSqlValueWithSQuote(sysDate))
					.append(", ");
			sqlCmd.append(StringUtil.toSqlValueWithSQuote(sysTime))
					.append(", ");
			sqlCmd.append(StringUtil.toSqlValueWithSQuote("BATCH"))
					.append(", ");
			sqlCmd.append(StringUtil.toSqlValueWithSQuote(sysDate))
					.append(", ");
			sqlCmd.append(StringUtil.toSqlValueWithSQuote(sysTime))
					.append(", ");
			sqlCmd.append(StringUtil.toSqlValueWithSQuote(blackListCode))
					.append(" ");
			sqlCmd.append(") ");
			log.info(sqlCmd.toString());
			DBService.getDBService().sqlAction(sqlCmd.toString(), conn, false);

			sqlCmd.delete(0, sqlCmd.length());
			sqlCmd.append("Insert into TB_BLACKLIST_SETTING_UPT (");
			sqlCmd.append(column.toString()).append(", ");
			sqlCmd.append("UPT_STATUS, APRV_STATUS ");
			sqlCmd.append(") select ");
			sqlCmd.append(column.toString()).append(", ");
			sqlCmd.append(StringUtil.toSqlValueWithSQuote("1")).append(", ");
			sqlCmd.append(StringUtil.toSqlValueWithSQuote("1")).append(" ");
			sqlCmd.append("from TB_BLACKLIST_SETTING where ");
			sqlCmd.append("CARD_NO = ");
			sqlCmd.append(StringUtil.toSqlValueWithSQuote(cardInfo.getCardNo()))
					.append(" and ");
			sqlCmd.append("EXPIRY_DATE = ");
			sqlCmd.append(
					StringUtil.toSqlValueWithSQuote(cardInfo.getExpiryDate()))
					.append(" ");
			log.info(sqlCmd.toString());
			DBService.getDBService().sqlAction(sqlCmd.toString(), conn, false);
		}
	}

	private void removeRejectAuth(TbCardInfo cardInfo) throws Exception {
		TbRejectAuthListPK pk = new TbRejectAuthListPK();
		pk.setCardNo(cardInfo.getCardNo());
		pk.setExpiryDate(cardInfo.getExpiryDate());
		boolean isExist = new TbRejectAuthListMgr(conn).isExist(pk);
		if (!isExist) {
			log.warn(pk.toString() + " is empty!");
			return;
		} else {
			StringBuffer column = new StringBuffer();
			column.append("CARD_NO, EXPIRY_DATE, LMS_INVOICE_NO, REJECT_REASON, END_DATE, ");
			column.append("IMP_FILE_NAME, IMP_FILE_DATE, IMP_FILE_TIME ");
			
			StringBuffer sqlCmd = new StringBuffer();
			sqlCmd.append("Insert into TB_REJECT_AUTH_LIST_UPT (");
			sqlCmd.append(column.toString()).append(", ");
			sqlCmd.append("UPT_USERID, UPT_DATE, UPT_TIME, APRV_USERID, APRV_DATE, APRV_TIME, ");
			sqlCmd.append("UPT_STATUS, APRV_STATUS ");
			sqlCmd.append(") select ");
			sqlCmd.append(column.toString()).append(", ");
			sqlCmd.append(StringUtil.toSqlValueWithSQuote("BATCH"))
					.append(", ");
			sqlCmd.append(StringUtil.toSqlValueWithSQuote(sysDate))
					.append(", ");
			sqlCmd.append(StringUtil.toSqlValueWithSQuote(sysTime))
					.append(", ");
			sqlCmd.append(StringUtil.toSqlValueWithSQuote("BATCH"))
					.append(", ");
			sqlCmd.append(StringUtil.toSqlValueWithSQuote(sysDate))
					.append(", ");
			sqlCmd.append(StringUtil.toSqlValueWithSQuote(sysTime))
					.append(", ");
			sqlCmd.append(StringUtil.toSqlValueWithSQuote("3")).append(", ");
			sqlCmd.append(StringUtil.toSqlValueWithSQuote("1")).append(" ");
			sqlCmd.append("from TB_REJECT_AUTH_LIST where ");
			sqlCmd.append("CARD_NO = ");
			sqlCmd.append(StringUtil.toSqlValueWithSQuote(cardInfo.getCardNo()))
					.append(" and ");
			sqlCmd.append("EXPIRY_DATE = ");
			sqlCmd.append(
					StringUtil.toSqlValueWithSQuote(cardInfo.getExpiryDate()))
					.append(" ");
			log.info(sqlCmd.toString());
			DBService.getDBService().sqlAction(sqlCmd.toString(), conn, false);

			sqlCmd.delete(0, sqlCmd.length());
			sqlCmd.append("Delete TB_REJECT_AUTH_LIST where ");
			sqlCmd.append("CARD_NO = ");
			sqlCmd.append(StringUtil.toSqlValueWithSQuote(cardInfo.getCardNo()))
					.append(" and ");
			sqlCmd.append("EXPIRY_DATE = ");
			sqlCmd.append(
					StringUtil.toSqlValueWithSQuote(cardInfo.getExpiryDate()))
					.append(" ");
			log.info(sqlCmd.toString());
			DBService.getDBService().sqlAction(sqlCmd.toString(), conn, false);
		}
	}

	private void remarkSuccess() throws Exception {
		try {
			conn.commit();
			log.info("commit success");
			Thread.sleep(getSleepTime());
		} catch (SQLException e) {
			throw new SQLException("remarkSuccess():conn.commit failed." + e);
		} catch (InterruptedException e) {
			throw new Exception("remarkSuccess():Thread.sleep() failed." + e);
		}
	}

	private void remarkFail() throws SQLException {
		try {
			conn.rollback();
			log.info("rollback!");
		} catch (SQLException e) {
			throw new SQLException("remarkFail() rollback failed." + e);
		}
	}

	public String getBatchDate() {
		return batchDate;
	}

	public void setBatchDate(String batchDate) {
		this.batchDate = batchDate;
	}

	public String getSysDate() {
		return sysDate;
	}

	public void setSysDate(String sysDate) {
		this.sysDate = sysDate;
	}

	public String getSysTime() {
		return sysTime;
	}

	public void setSysTime(String sysTime) {
		this.sysTime = sysTime;
	}

	public int getSleepTime() {
		return sleepTime;
	}

	public void setSleepTime(int sleepTime) {
		this.sleepTime = sleepTime;
	}

	public String getExecuteDate() {
		return executeDate;
	}

	public void setExecuteDate(String executeDate) {
		this.executeDate = executeDate;
	}

	public String getBlackListCode() {
		return blackListCode;
	}

	public void setBlackListCode(String blackListCode) {
		this.blackListCode = blackListCode;
	}
}
