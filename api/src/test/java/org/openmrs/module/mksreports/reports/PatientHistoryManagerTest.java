package org.openmrs.module.mksreports.reports;

import java.util.List;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.hamcrest.collection.IsCollectionWithSize;
import org.junit.Assert;
import org.junit.Test;
import org.openmrs.module.mksreports.MKSReportManager;
import org.openmrs.module.mksreports.MKSReportsConstants;
import org.openmrs.module.mksreports.renderer.PatientHistoryXmlReportRenderer;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.definition.service.ReportDefinitionService;
import org.openmrs.module.reporting.report.manager.ReportManagerUtil;
import org.openmrs.module.reporting.report.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class PatientHistoryManagerTest extends BaseReportTest {
	
	@Autowired
	private ReportService reportService;
	
	@Autowired
	private ReportDefinitionService reportDefinitionService;
	
	@Autowired
	@Qualifier(MKSReportsConstants.COMPONENT_REPORTMANAGER_PATIENTHISTORY)
	private MKSReportManager manager;
	
	@Test
	public void setupReport_shouldSetupPatientHistory() throws Exception {
		
		ReportManagerUtil.setupReport(manager);
		
		List<ReportDefinition> reportDefinitions = this.reportDefinitionService
		        .getDefinitions(PatientHistoryReportManager.REPORT_DEFINITION_NAME, true);
		
		Assert.assertNotNull(reportDefinitions);
		MatcherAssert.assertThat(reportDefinitions, IsCollectionWithSize.hasSize(1));
		ReportDefinition reportDefinition = reportDefinitions.get(0);
		Assert.assertNotNull(reportDefinition);
		Assert.assertEquals(PatientHistoryReportManager.REPORT_DEFINITION_NAME, reportDefinition.getName());
		Assert.assertNotNull(reportDefinition.getDataSetDefinitions());
		MatcherAssert.assertThat(reportDefinition.getDataSetDefinitions().keySet(),
		    Matchers.contains(PatientHistoryReportManager.DATASET_KEY_DEMOGRAPHICS,
		        PatientHistoryReportManager.DATASET_KEY_OBS, PatientHistoryReportManager.DATASET_KEY_ENCOUNTERS));
		
		List<ReportDesign> reportDesigns = this.reportService.getReportDesigns(reportDefinition,
		    PatientHistoryXmlReportRenderer.class, false);
		Assert.assertNotNull(reportDesigns);
		MatcherAssert.assertThat(reportDesigns, IsCollectionWithSize.hasSize(1));
		ReportDesign reportDesign = reportDesigns.get(0);
		Assert.assertEquals(PatientHistoryReportManager.REPORT_DESIGN_NAME, reportDesign.getName());
		
		// ReportData data = rds.evaluate(reportDefinition, new EvaluationContext());
	}
}
