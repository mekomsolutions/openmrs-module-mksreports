package org.openmrs.module.commonreports.reports;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
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
import org.openmrs.api.ConceptService;
import org.openmrs.module.commonreports.reports.BaseModuleContextSensitiveMysqlBackedTest;
import org.openmrs.module.commonreports.reports.NewEpisodesOfDiseasesReportManager;
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
import org.springframework.beans.factory.annotation.Qualifier;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;

import static java.math.BigDecimal.ONE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class NewEpisodesOfDiseasesReportManagerTest extends BaseModuleContextSensitiveMysqlBackedTest {
	
	public NewEpisodesOfDiseasesReportManagerTest() throws SQLException {
		super();
	}
	
	@Autowired
	private InitializerService iniz;
	
	@Autowired
	private ReportService rs;
	
	@Autowired
	private ReportDefinitionService rds;
	
	@Autowired
	@Qualifier("conceptService")
	private ConceptService cs;
	
	@Autowired
	private NewEpisodesOfDiseasesReportManager manager;
	
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
		executeDataSet("org/openmrs/module/commonreports/include/newEpisodesOfDiseasesTestDataset.xml");
		
		String path = getClass().getClassLoader().getResource("testAppDataDir").getPath() + File.separator;
		System.setProperty("OPENMRS_APPLICATION_DATA_DIRECTORY", path);
		
		for (Loader loader : iniz.getLoaders()) {
			if (loader.getDomainName().equals(Domain.JSON_KEY_VALUES.getName())) {
				loader.load();
			}
		}
	}
	
	@Test
	public void setupReport_shouldCreateExcelTemplateDesign() throws Exception {
		// setup
		
		// replay
		ReportManagerUtil.setupReport(manager);
		
		// verif
		ReportDesign design = rs.getReportDesignByUuid("7688966e-fca5-4fde-abab-1b46a87a1185");
		
		Assert.assertEquals(NewEpisodesOfDiseasesReportManager.REPEATING_SECTION,
		    design.getProperties().get("repeatingSections"));
		Assert.assertEquals(1, design.getResources().size());
		
		ReportDefinition def = design.getReportDefinition();
		Assert.assertEquals("8b787bdc-c852-481c-b6fa-6683ec7e30d8", def.getUuid());
	}
	
	@Test
	public void testReport() throws Exception {
		// setup
		EvaluationContext context = new EvaluationContext();
		context.addParameterValue("startDate", DateUtil.parseDate("2008-08-01", "yyyy-MM-dd"));
		context.addParameterValue("endDate", DateUtil.parseDate("2009-09-30", "yyyy-MM-dd"));
		boolean malariaVerified = false;
		boolean feverVerified = false;
		boolean diabetesVerified = false;
		boolean allOtherDiagnoses = false;
		
		// replay
		ReportDefinition rd = manager.constructReportDefinition();
		ReportData data = rds.evaluate(rd, context);
		
		// verify
		for (Iterator<DataSetRow> itr = data.getDataSets().get(rd.getName()).iterator(); itr.hasNext();) {
			DataSetRow row = itr.next();
			if (row.getColumnValue("Maladies/Symptomes").equals("MALARIA")) {
				assertEquals(ONE, row.getColumnValue("F_25-49"));
				assertEquals(ONE, row.getColumnValue("M_1-4"));
				assertEquals(ONE, row.getColumnValue("M_Total"));
				assertEquals(ONE, row.getColumnValue("F_Total"));
				assertEquals(ONE, row.getColumnValue("TotalReferredCases"));
				malariaVerified = true;
			}
			if (row.getColumnValue("Maladies/Symptomes").equals("FEVER")) {
				assertEquals(ONE, row.getColumnValue("M_1-4"));
				assertEquals(ONE, row.getColumnValue("M_Total"));
				assertNull(row.getColumnValue("F_Total"));
				assertEquals(null, row.getColumnValue("TotalReferredCases"));
				feverVerified = true;
			}
			if (row.getColumnValue("Maladies/Symptomes").equals("DIABETES")) {
				assertEquals(ONE, row.getColumnValue("M_1-4"));
				assertEquals(ONE, row.getColumnValue("M_Total"));
				assertNull(row.getColumnValue("F_Total"));
				assertEquals(null, row.getColumnValue("TotalReferredCases"));
				diabetesVerified = true;
			}
			if (row.getColumnValue("Maladies/Symptomes").equals("All other diagnoses")) {
				assertEquals(ONE, row.getColumnValue("F_25-49"));
				assertEquals(ONE, row.getColumnValue("F_Total"));
				assertEquals(ONE, row.getColumnValue("TotalReferredCases"));
				allOtherDiagnoses = true;
			}
		}
		assertTrue(malariaVerified && feverVerified && diabetesVerified && allOtherDiagnoses);
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
