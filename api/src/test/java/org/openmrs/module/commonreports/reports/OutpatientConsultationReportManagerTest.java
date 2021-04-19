package org.openmrs.module.commonreports.reports;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Cohort;
import org.openmrs.api.ConceptService;
import org.openmrs.module.commonreports.ActivatedReportManager;
import org.openmrs.module.commonreports.CommonReportsConstants;
import org.openmrs.module.commonreports.reports.OutpatientConsultationReportManager;
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
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class OutpatientConsultationReportManagerTest extends BaseModuleContextSensitiveTest {
	
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
	@Qualifier(CommonReportsConstants.COMPONENT_REPORTMANAGER_OPDCONSULT)
	private ActivatedReportManager manager;
	
	@Before
	public void setUp() throws Exception {
		executeDataSet("org/openmrs/module/reporting/include/ReportTestDataset-openmrs-2.0.xml");
		executeDataSet("org/openmrs/module/commonreports/include/outpatientConsultationTestDataset.xml");
		
		String path = getClass().getClassLoader().getResource("testAppDataDir").getPath() + File.separator;
		System.setProperty("OPENMRS_APPLICATION_DATA_DIRECTORY", path);
		
		for (Loader loader : iniz.getLoaders()) {
			if (loader.getDomainName().equals(Domain.JSON_KEY_VALUES.getName())) {
				loader.load();
			}
		}
	}
	
	@Test
	public void setupReport_shouldSetupOPDRecBook() {
		
		// replay
		ReportManagerUtil.setupReport(manager);
		
		// verif
		Assert.assertNotNull(rs.getReportDesignByUuid("42b32ac1-fcd0-473d-8fdb-71fd6fc2e26d"));
		
	}
	
	@Test
	public void testReport() throws Exception {
		
		EvaluationContext context = new EvaluationContext();
		context.addParameterValue("startDate", DateUtil.parseDate("2008-08-01", "yyyy-MM-dd"));
		context.addParameterValue("endDate", DateUtil.parseDate("2009-09-30", "yyyy-MM-dd"));
		
		ReportDefinition rd = manager.constructReportDefinition();
		ReportData data = rds.evaluate(rd, context);
		
		for (Iterator<DataSetRow> itr = data.getDataSets().get(rd.getName()).iterator(); itr.hasNext();) {
			DataSetRow row = itr.next();
			
			// In CrossTabDataSet reports all rows and columns are in fact just columns of
			// one row
			
			// Ensure that the report contains 4 possible combinations
			Cohort _5To15yMalesWithMalaria = (Cohort) row
			        .getColumnValue("MALARIA." + OutpatientConsultationReportManager.col7);
			assertNotNull(_5To15yMalesWithMalaria);
			assertEquals(1, _5To15yMalesWithMalaria.getSize());
			Cohort _25To50yFemalesWithMalaria = (Cohort) row
			        .getColumnValue("MALARIA." + OutpatientConsultationReportManager.col12);
			assertNotNull(_25To50yFemalesWithMalaria);
			assertEquals(1, _25To50yFemalesWithMalaria.getSize());
			Cohort _5To15yMalesWithFever = (Cohort) row.getColumnValue("FEVER." + OutpatientConsultationReportManager.col7);
			assertNotNull(_5To15yMalesWithFever);
			assertEquals(1, _5To15yMalesWithFever.getSize());
			Cohort _25To50yFemalesWithFever = (Cohort) row
			        .getColumnValue("FEVER." + OutpatientConsultationReportManager.col12);
			assertNotNull(_25To50yFemalesWithFever);
			assertEquals(0, _25To50yFemalesWithFever.getSize());
			
			// Total column
			Cohort allMalesWithMalaria = (Cohort) row.getColumnValue("MALARIA." + OutpatientConsultationReportManager.col17);
			assertNotNull(allMalesWithMalaria);
			assertEquals(1, allMalesWithMalaria.getSize());
			assertTrue(allMalesWithMalaria.getMemberIds().contains(6));
			Cohort allFemalesWithMalaria = (Cohort) row
			        .getColumnValue("MALARIA." + OutpatientConsultationReportManager.col18);
			assertNotNull(allFemalesWithMalaria);
			assertEquals(1, allFemalesWithMalaria.getSize());
			assertTrue(allFemalesWithMalaria.getMemberIds().contains(7));
			Cohort allMalesWithFever = (Cohort) row.getColumnValue("FEVER." + OutpatientConsultationReportManager.col17);
			assertNotNull(allMalesWithFever);
			assertEquals(1, allMalesWithFever.getSize());
			Cohort allFemalesWithFever = (Cohort) row.getColumnValue("FEVER." + OutpatientConsultationReportManager.col18);
			assertNotNull(allFemalesWithFever);
			assertEquals(0, allFemalesWithFever.getSize());
			
			Cohort allMalesWithDiabetes = (Cohort) row
			        .getColumnValue("DIABETES." + OutpatientConsultationReportManager.col17);
			assertNotNull(allMalesWithDiabetes);
			assertEquals(1, allMalesWithDiabetes.getSize());
			Cohort allFemalesWithDiabetes = (Cohort) row
			        .getColumnValue("DIABETES." + OutpatientConsultationReportManager.col18);
			assertNotNull(allFemalesWithDiabetes);
			assertEquals(0, allFemalesWithDiabetes.getSize());
			
			// Referred To column
			Cohort referredTo1 = (Cohort) row.getColumnValue("MALARIA." + OutpatientConsultationReportManager.col19);
			assertNotNull(referredTo1);
			assertEquals(0, referredTo1.getSize());
			Cohort referredTo2 = (Cohort) row.getColumnValue("MALARIA." + OutpatientConsultationReportManager.col20);
			assertNotNull(referredTo2);
			assertEquals(1, referredTo2.getSize());
			
			Cohort allWithMalaria = (Cohort) row.getColumnValue("MALARIA." + OutpatientConsultationReportManager.col23);
			assertThat(allWithMalaria, is(notNullValue()));
			assertThat(allWithMalaria.getSize(), is(2));
		}
	}
	
}
