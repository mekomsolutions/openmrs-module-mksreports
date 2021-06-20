package org.openmrs.module.commonreports.reports;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.dbunit.DatabaseUnitException;
import org.dbunit.DatabaseUnitRuntimeException;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.ext.mysql.MySqlDataTypeFactory;
import org.dbunit.operation.DatabaseOperation;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.commonreports.reports.BaseModuleContextSensitiveMysqlBackedTest;
import org.openmrs.module.initializer.Domain;
import org.openmrs.module.initializer.api.InitializerService;
import org.openmrs.module.initializer.api.loaders.Loader;
import org.openmrs.module.reporting.common.DateUtil;
import org.openmrs.module.reporting.dataset.DataSetRow;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.report.ReportData;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.definition.service.ReportDefinitionService;
import org.openmrs.module.reporting.report.manager.ReportManagerUtil;
import org.openmrs.module.reporting.report.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class VisitsReportManagerTest extends BaseModuleContextSensitiveMysqlBackedTest {
	
	public VisitsReportManagerTest() throws SQLException {
		super();
	}
	
	@Autowired
	private InitializerService iniz;
	
	@Autowired
	private ReportService rs;
	
	@Autowired
	private ReportDefinitionService rds;
	
	@Autowired
	private VisitsReportManager manager;
	
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
	
	@Before
	public void setUp() throws Exception {
		updateDatabase("org/openmrs/module/commonreports/liquibase/test-liquibase.xml");
		executeDataSet("org/openmrs/module/reporting/include/ReportTestDataset-openmrs-2.0.xml");
		executeDataSet("org/openmrs/module/commonreports/include/visitsReportTestDataset.xml");
		
		String path = getClass().getClassLoader().getResource("testAppDataDir").getPath() + File.separator;
		System.setProperty("OPENMRS_APPLICATION_DATA_DIRECTORY", path);
		
		for (Loader loader : iniz.getLoaders()) {
			if (loader.getDomainName().equals(Domain.JSON_KEY_VALUES.getName())) {
				loader.load();
			}
		}
	}
	
	@Test
	public void setupReport_shouldCreateExcelDesign() throws Exception {
		// setup
		
		// replay
		ReportManagerUtil.setupReport(manager);
		
		// verif
		ReportDesign design = rs.getReportDesignByUuid("4e1c1f7a-bbfe-44ec-a36a-3005e3fc50bd");
		
		Assert.assertEquals(VisitsReportManager.REPEATING_SECTION, design.getProperties().get("repeatingSections"));
		Assert.assertEquals(1, design.getResources().size());
		
		ReportDefinition def = design.getReportDefinition();
		Assert.assertEquals("8613f380-8f39-492f-b46b-9802482bc315", def.getUuid());
		
	}
	
	@Test
	public void testReport() throws Exception {
		// setup
		Map<String, String> expectedValuesMap = new HashMap<String, String>();
		expectedValuesMap.put("Children under one year old", "null:null");
		expectedValuesMap.put("Children aged 1 to 4 years old", "1.0:null");
		expectedValuesMap.put("Children aged 5 to 9 years old", "1.0:null");
		expectedValuesMap.put("Children aged 10 to 14", "4.0:null");
		expectedValuesMap.put("Children aged 10 to 14", "4.0:null");
		expectedValuesMap.put("Young adults aged 20 to 24", "3.0:1.0");
		expectedValuesMap.put("Pregnant women", "null:3.0");
		expectedValuesMap.put("Family planing clients", "null:2.0");
		expectedValuesMap.put("Other adults", "3.0:null");
		expectedValuesMap.put("Total", "12.0:6.0");
		
		EvaluationContext context = new EvaluationContext();
		context.addParameterValue("startDate", DateUtil.parseDate("2021-03-10", "yyyy-MM-dd"));
		context.addParameterValue("endDate", DateUtil.parseDate("2021-06-20", "yyyy-MM-dd"));
		
		// replay
		ReportDefinition rd = manager.constructReportDefinition();
		ReportData data = rds.evaluate(rd, context);
		
		// verify
		for (Iterator<DataSetRow> itr = data.getDataSets().get(rd.getName()).iterator(); itr.hasNext();) {
			DataSetRow row = itr.next();
			String entry = (String) row.getColumnValue("Categories");
			
			assertTrue(entry, expectedValuesMap.containsKey(entry));
			assertEquals(entry, expectedValuesMap.get(entry).split(":")[0],
			    String.valueOf(row.getColumnValue("New_visits")));
			assertEquals(entry, expectedValuesMap.get(entry).split(":")[1],
			    String.valueOf(row.getColumnValue("Subsequent_visits")));
		}
	}
	
	private void updateDatabase(String filename) throws Exception {
		Liquibase liquibase = getLiquibase(filename);
		liquibase.update("Modify column datatype to longblob on reporting_report_design_resource table");
		liquibase.getDatabase().getConnection().commit();
	}
	
	private Liquibase getLiquibase(String filename) throws Exception {
		Database liquibaseConnection = DatabaseFactory.getInstance()
		        .findCorrectDatabaseImplementation(new JdbcConnection(getConnection()));
		
		liquibaseConnection.setDatabaseChangeLogTableName("LIQUIBASECHANGELOG");
		liquibaseConnection.setDatabaseChangeLogLockTableName("LIQUIBASECHANGELOGLOCK");
		
		return new Liquibase(filename, new ClassLoaderResourceAccessor(getClass().getClassLoader()), liquibaseConnection);
	}
}
