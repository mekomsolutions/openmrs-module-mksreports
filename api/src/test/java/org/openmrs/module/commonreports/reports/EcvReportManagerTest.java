package org.openmrs.module.commonreports.reports;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.sql.SQLException;
import java.util.Iterator;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Cohort;
import org.openmrs.module.commonreports.ActivatedReportManager;
import org.openmrs.module.commonreports.CommonReportsConstants;
import org.openmrs.module.initializer.Domain;
import org.openmrs.module.initializer.api.InitializerService;
import org.openmrs.module.initializer.api.loaders.Loader;
import org.openmrs.module.reporting.common.DateUtil;
import org.openmrs.module.reporting.dataset.DataSetRow;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.report.ReportData;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.definition.service.ReportDefinitionService;
import org.openmrs.module.reporting.report.manager.ReportManagerUtil;
import org.openmrs.module.reporting.report.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class EcvReportManagerTest extends BaseModuleContextSensitiveMysqlBackedTest {
	
	public EcvReportManagerTest() throws SQLException {
		super();
		//TODO Auto-generated constructor stub
	}
	
	@Autowired
	private InitializerService iniz;
	
	@Autowired
	private ReportService rs;
	
	@Autowired
	private ReportDefinitionService rds;
	
	@Autowired
	@Qualifier(CommonReportsConstants.COMPONENT_REPORTMANAGER_ECV)
	private ActivatedReportManager manager;
	
	@Before
	public void setUp() throws Exception {
		executeDataSet("org/openmrs/module/reporting/include/ReportTestDataset-openmrs-2.0.xml");
		executeDataSet("org/openmrs/module/commonreports/include/vaccinationTestDataset.xml");
		
		String path = getClass().getClassLoader().getResource("testAppDataDir").getPath() + File.separator;
		System.setProperty("OPENMRS_APPLICATION_DATA_DIRECTORY", path);
		
		for (Loader loader : iniz.getLoaders()) {
			if (loader.getDomainName().equals(Domain.JSON_KEY_VALUES.getName())) {
				loader.load();
			}
		}
	}
	
	@Test
	public void setupReport_shouldSetUpReport() {
		
		// replay
		ReportManagerUtil.setupReport(manager);
		
		// verif
		Assert.assertNotNull(rs.getReportDesignByUuid("2dfba707-3a90-445a-8288-9843f037c3b6"));
		
	}
	
	@Test
	public void testReport() throws Exception {
		
		EvaluationContext context = new EvaluationContext();
		context.addParameterValue("startDate", DateUtil.parseDate("2021-05-01", "yyyy-MM-dd"));
		context.addParameterValue("endDate", DateUtil.parseDate("2021-06-30", "yyyy-MM-dd"));
		
		ReportDefinition rd = manager.constructReportDefinition();
		ReportData data = rds.evaluate(rd, context);
		
		for (Iterator<DataSetRow> itr = data.getDataSets().get(rd.getName()).iterator(); itr.hasNext();) {
			DataSetRow row = itr.next();
			
			// In CrossTabDataSet reports all rows and columns are in fact just columns of
			// one row
			
			Cohort _0mTo1yFemalesECV = (Cohort) row.getColumnValue("ECV." + EcvReportManager.col1);
			assertNotNull(_0mTo1yFemalesECV);
			assertEquals(0, _0mTo1yFemalesECV.getSize());
			
			Cohort _1To2yFemalesECV = (Cohort) row.getColumnValue("ECV." + EcvReportManager.col2);
			assertNotNull(_1To2yFemalesECV);
			assertEquals(0, _1To2yFemalesECV.getSize());
			
			Cohort _0mTo1yMalesECV = (Cohort) row.getColumnValue("ECV." + EcvReportManager.col3);
			assertNotNull(_0mTo1yMalesECV);
			assertEquals(0, _0mTo1yMalesECV.getSize());
			
			Cohort _1To2yMalesECV = (Cohort) row.getColumnValue("ECV." + EcvReportManager.col4);
			assertNotNull(_1To2yMalesECV);
			assertEquals(0, _1To2yMalesECV.getSize());
			
			Cohort _prenatalFemalesECV = (Cohort) row.getColumnValue("ECV." + EcvReportManager.col5);
			assertNotNull(_prenatalFemalesECV);
			assertEquals(1, _prenatalFemalesECV.getSize());
		}
	}
	
}
