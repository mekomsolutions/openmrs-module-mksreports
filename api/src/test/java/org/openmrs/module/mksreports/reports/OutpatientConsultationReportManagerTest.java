package org.openmrs.module.mksreports.reports;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Cohort;
import org.openmrs.api.ConceptService;
import org.openmrs.module.initializer.api.InitializerService;
import org.openmrs.module.mksreports.MKSReportManager;
import org.openmrs.module.mksreports.MKSReportsConstants;
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

public class OutpatientConsultationReportManagerTest extends BaseReportTest {
	
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
	@Qualifier(MKSReportsConstants.COMPONENT_REPORTMANAGER_OPDCONSULT)
	private MKSReportManager manager;
	
	protected static final String XML_DATASET_PATH = "org/openmrs/module/mksreports/include/";
	
	protected static final String XML_REPORT_TEST_DATASET_2 = "outpatientConsultationTestDataset.xml";
	
	@Before
	public void setUp() throws Exception {
		String path = getClass().getClassLoader().getResource("testAppDataDir").getPath() + File.separator;
		System.setProperty("OPENMRS_APPLICATION_DATA_DIRECTORY", path);
		executeDataSet(XML_DATASET_PATH + XML_REPORT_TEST_DATASET_2);
		iniz.loadJsonKeyValues();
	}
	
	@Test
	public void setupReport_shouldSetupOPDRecBook() {
		
		// replay
		ReportManagerUtil.setupReport(manager);
		
		// verif
		List<ReportDesign> designs = rs.getAllReportDesigns(false);
		Assert.assertEquals(1, rs.getAllReportDesigns(false).size());
		ReportDefinition def = designs.get(0).getReportDefinition();
		Assert.assertEquals("58d7a2ba-5b62-4e21-ac21-090e3758cce7", def.getUuid());
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
			Cohort col1 = (Cohort) row.getColumnValue("MALARIA." + OutpatientConsultationReportManager.col7);
			assertNotNull(col1);
			assertEquals(1, col1.getSize());
			Cohort col2 = (Cohort) row.getColumnValue("MALARIA." + OutpatientConsultationReportManager.col12);
			assertNotNull(col2);
			assertEquals(0, col2.getSize());
			Cohort col3 = (Cohort) row.getColumnValue("FEVER." + OutpatientConsultationReportManager.col7);
			assertNotNull(col3);
			assertEquals(1, col3.getSize());
			Cohort col4 = (Cohort) row.getColumnValue("FEVER." + OutpatientConsultationReportManager.col12);
			assertNotNull(col4);
			assertEquals(0, col4.getSize());
			
			// Total column
			Cohort total1 = (Cohort) row.getColumnValue("MALARIA." + OutpatientConsultationReportManager.col17);
			assertNotNull(total1);
			assertEquals(1, total1.getSize());
			assertTrue(total1.getMemberIds().contains(6));
			Cohort total2 = (Cohort) row.getColumnValue("MALARIA." + OutpatientConsultationReportManager.col18);
			assertNotNull(total2);
			assertEquals(0, total2.getSize());
			Cohort total3 = (Cohort) row.getColumnValue("FEVER." + OutpatientConsultationReportManager.col17);
			assertNotNull(total3);
			assertEquals(1, total3.getSize());
			Cohort total4 = (Cohort) row.getColumnValue("FEVER." + OutpatientConsultationReportManager.col18);
			assertNotNull(total4);
			assertEquals(0, total4.getSize());
			
			Cohort col5 = (Cohort) row.getColumnValue("DIABETES." + OutpatientConsultationReportManager.col17);
			assertNotNull(col5);
			assertEquals(1, col5.getSize());
			Cohort col6 = (Cohort) row.getColumnValue("DIABETES." + OutpatientConsultationReportManager.col18);
			assertNotNull(col6);
			assertEquals(0, col6.getSize());
			
			// Referred To column
			Cohort referredTo1 = (Cohort) row.getColumnValue("MALARIA." + OutpatientConsultationReportManager.col19);
			assertNotNull(referredTo1);
			assertEquals(0, referredTo1.getSize());
			Cohort referredTo2 = (Cohort) row.getColumnValue("MALARIA." + OutpatientConsultationReportManager.col20);
			assertNotNull(referredTo2);
			assertEquals(0, referredTo2.getSize());
			
		}
	}
	
}
