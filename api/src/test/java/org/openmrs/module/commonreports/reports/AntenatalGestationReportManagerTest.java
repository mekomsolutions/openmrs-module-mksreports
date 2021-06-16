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
import org.openmrs.module.commonreports.reports.BaseModuleContextSensitiveMysqlBackedTest;
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

public class AntenatalGestationReportManagerTest extends BaseModuleContextSensitiveMysqlBackedTest {
	
	public AntenatalGestationReportManagerTest() throws SQLException {
		super();
		// TODO Auto-generated constructor stub
	}
	
	@Autowired
	private InitializerService iniz;
	
	@Autowired
	private ReportService rs;
	
	@Autowired
	private ReportDefinitionService rds;
	
	@Autowired
	@Qualifier(CommonReportsConstants.COMPONENT_REPORTMANAGER_ANTENATALGESTATION)
	private ActivatedReportManager manager;
	
	@Before
	public void setUp() throws Exception {
		executeDataSet("org/openmrs/module/reporting/include/ReportTestDataset-openmrs-2.0.xml");
		executeDataSet("org/openmrs/module/commonreports/include/antenatalTestDataset.xml");
		
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
		Assert.assertNotNull(rs.getReportDesignByUuid("a0ebe3c1-d6a3-4761-bff0-6a03626d2c75"));
		
	}
	
	@Test
	public void testReport() throws Exception {
		
		EvaluationContext context = new EvaluationContext();
		context.addParameterValue("startDate", DateUtil.parseDate("2021-06-01", "yyyy-MM-dd"));
		context.addParameterValue("endDate", DateUtil.parseDate("2021-06-30", "yyyy-MM-dd"));
		
		ReportDefinition rd = manager.constructReportDefinition();
		ReportData data = rds.evaluate(rd, context);
		
		for (Iterator<DataSetRow> itr = data.getDataSets().get(rd.getName()).iterator(); itr.hasNext();) {
			DataSetRow row = itr.next();
			
			// In CrossTabDataSet reports all rows and columns are in fact just columns of
			// one row
			Cohort visitTwo = (Cohort) row
			        .getColumnValue("14-27 weeks of pregnancy." + AntenatalGestationReportManager.col2);
			assertNotNull(visitTwo);
			assertEquals(1, visitTwo.getSize());
			
			Cohort visitThree = (Cohort) row
			        .getColumnValue("14-27 weeks of pregnancy." + AntenatalGestationReportManager.col3);
			assertNotNull(visitThree);
			assertEquals(0, visitThree.getSize());
			
			Cohort visitTwoTotal = (Cohort) row
			        .getColumnValue("14-27 weeks of pregnancy." + AntenatalGestationReportManager.col2);
			assertNotNull(visitTwoTotal);
			assertEquals(1, visitTwoTotal.getSize());
			
			Cohort TotalVisitTwo = (Cohort) row.getColumnValue("Total." + AntenatalGestationReportManager.col2);
			assertNotNull(TotalVisitTwo);
			assertEquals(1, TotalVisitTwo.getSize());
			
			Cohort TotalTotalVisit = (Cohort) row.getColumnValue("Total." + AntenatalGestationReportManager.col6);
			assertNotNull(TotalTotalVisit);
			assertEquals(1, TotalTotalVisit.getSize());
		}
	}
	
}
