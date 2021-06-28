package tw.com.hyweb.svc.cp.batch;

import java.sql.Connection;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.dbunit.DataSourceDatabaseTester;
import org.dbunit.IDatabaseTester;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.operation.DatabaseOperation;
import org.springframework.context.support.FileSystemXmlApplicationContext;

public class NoAmbiguousDataSourceDatabaseTester extends DataSourceDatabaseTester
{
    private DataSource dataSource;
    private String schema = "OWNER";

    public NoAmbiguousDataSourceDatabaseTester(DataSource dataSource)
    {
        super(dataSource);

        this.dataSource = dataSource;
        
        super.setSetUpOperation(DatabaseOperation.REFRESH);
        super.setTearDownOperation(DatabaseOperation.DELETE);
    }

    public NoAmbiguousDataSourceDatabaseTester(DataSource dataSource, String schema)
    {
        super(dataSource);

        this.dataSource = dataSource;
        this.schema = schema;
        
        super.setSetUpOperation(DatabaseOperation.REFRESH);
        super.setTearDownOperation(DatabaseOperation.DELETE);
    }

    @Override
    public IDatabaseConnection getConnection() throws Exception
    {
        Connection connection = dataSource.getConnection();
        connection.setAutoCommit(true);
        
        return new DatabaseConnection(connection, schema);
    }
    
    public static IDatabaseTester getDatabaseTester()
    {
        BasicDataSource dataSource = ((BasicDataSource) new FileSystemXmlApplicationContext("config/batch/datasource.xml").getBean("dataSource"));
        dataSource.setDefaultAutoCommit(true);
        
        return new NoAmbiguousDataSourceDatabaseTester(dataSource);
    }
}
