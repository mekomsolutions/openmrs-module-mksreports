package org.openmrs.module.commonreports.reports;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.ZoneId;
import java.util.TimeZone;

import org.dbunit.DatabaseUnitException;
import org.dbunit.DatabaseUnitRuntimeException;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.ext.mysql.MySqlDataTypeFactory;
import org.dbunit.operation.DatabaseOperation;
import org.hibernate.dialect.MySQLDialect;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import com.wix.mysql.EmbeddedMysql;
import com.wix.mysql.config.Charset;
import com.wix.mysql.config.MysqldConfig;
import com.wix.mysql.config.SchemaConfig;

import static com.wix.mysql.distribution.Version.v5_6_latest;

public abstract class BaseModuleContextSensitiveMysqlBackedTest extends BaseModuleContextSensitiveTest {
	
	public BaseModuleContextSensitiveMysqlBackedTest() throws SQLException {
		super();
	}
	
	private static EmbeddedMysql embeddedMysql;
	
	private static String databaseUrl = "jdbc:mysql://localhost:3344/openmrs?autoReconnect=true&sessionVariables=default_storage_engine%3DInnoDB&useUnicode=true&characterEncoding=UTF-8";
	
	private static String databaseUsername = "test";
	
	private static String databaseUserPasswword = "password";
	
	private static String databaseDialect = MySQLDialect.class.getName();
	
	private static String databaseDriver = "com.mysql.jdbc.Driver";
	
	@Override
	public void executeDataSet(IDataSet dataset) {
		try {
			Connection connection = getConnection();
			IDatabaseConnection dbUnitConn = setupDatabaseConnection(connection);
			DatabaseOperation.REFRESH.execute(dbUnitConn, dataset);
		}
		catch (Exception e) {
			throw new DatabaseUnitRuntimeException(e);
		}
	}
	
	private IDatabaseConnection setupDatabaseConnection(Connection connection) throws DatabaseUnitException {
		IDatabaseConnection dbUnitConn = new DatabaseConnection(connection);
		
		DatabaseConfig config = dbUnitConn.getConfig();
		config.setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new MySqlDataTypeFactory());
		
		return dbUnitConn;
	}
	
	@BeforeClass
	public static void setupMySqlDb() {
		MysqldConfig config = MysqldConfig.aMysqldConfig(v5_6_latest).withPort(3344).withCharset(Charset.UTF8)
		        .withTimeZone(TimeZone.getTimeZone(ZoneId.of("UTC"))).withUser(databaseUsername, databaseUserPasswword)
		        .build();
		
		SchemaConfig schemaConfig = SchemaConfig.aSchemaConfig("openmrs").build();
		
		embeddedMysql = EmbeddedMysql.anEmbeddedMysql(config).addSchema(schemaConfig).start();
		
		System.setProperty("databaseUrl", databaseUrl);
		System.setProperty("databaseUsername", databaseUsername);
		System.setProperty("databasePassword", databaseUserPasswword);
		System.setProperty("databaseDialect", databaseDialect);
		System.setProperty("databaseDriver", databaseDriver);
		System.setProperty("useInMemoryDatabase", "false");
		
	}
	
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		if (null != embeddedMysql) {
			embeddedMysql.stop();
		}
	}
	
}
