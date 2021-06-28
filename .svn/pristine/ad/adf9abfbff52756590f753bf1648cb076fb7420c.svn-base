package tw.com.hyweb.svc.yhdp.batch.daycut;

import java.sql.Connection;

public interface CutTransactionAction
{
    /**
     * 將符合條件之交易資料進行過檔
     * 
     * @param connection
     * @param dataCondition
     * @param cutDate
     * @param cutTime
     * @throws Exception
     */
    public void cutTransaction(Connection connection, String[] conditionFields, Object[] conditionValues, String cutDate, String cutTime) throws Exception;

    /**
     * 將符合條件之交易資料註記過檔成功
     * 
     * @param connection
     * @param dataCondition
     * @param cutDate
     * @param cutTime
     * @throws Exception
     */
    public void remarkSuccess(Connection connection, String[] conditionFields, Object[] conditionValues, String cutDate, String cutTime) throws Exception;

    /**
     * 將符合條件之交易資料註記過檔失敗
     * 
     * @param connection
     * @param dataCondition
     * @param cutDate
     * @param cutTime
     * @throws Exception
     */
    public void remarkFailure(Connection connection, String[] conditionFields, Object[] conditionValues, String cutDate, String cutTime) throws Exception;

}
