package tw.com.hyweb.svc.cp.batch;

import java.sql.Connection;

import javax.sql.DataSource;

import org.dbunit.DataSourceDatabaseTester;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.ext.oracle.OracleConnection;
import org.dbunit.operation.DatabaseOperation;

public class HywebDataSourceDatabaseTester extends DataSourceDatabaseTester
{
    private final DataSource dataSource;

    public HywebDataSourceDatabaseTester(DataSource dataSource)
    {
        super(dataSource);

        this.dataSource = dataSource;

        super.setSetUpOperation(DatabaseOperation.REFRESH);
        super.setTearDownOperation(DatabaseOperation.DELETE);
    }

    @Override
    public IDatabaseConnection getConnection() throws Exception
    {
        Connection connection = dataSource.getConnection();
        connection.setAutoCommit(true);

        return new OracleConnection(connection, getSchema());
    }
}
