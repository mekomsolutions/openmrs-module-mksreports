package org.openmrs.module.mksreports.reports;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.hamcrest.Matcher;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.hamcrest.collection.IsCollectionWithSize;
import org.junit.Assert;
import org.junit.Test;
import org.openmrs.Cohort;
import org.openmrs.module.mksreports.MKSReportManager;
import org.openmrs.module.mksreports.MKSReportsConstants;
import org.openmrs.module.mksreports.renderer.PatientHistoryXmlReportRenderer;
import org.openmrs.module.reporting.dataset.DataSet;
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

public class PatientHistoryManagerTest extends BaseReportTest {

	@Autowired
	private ReportService reportService;

	@Autowired
	private ReportDefinitionService reportDefinitionService;

	@Autowired
	@Qualifier(MKSReportsConstants.COMPONENT_REPORTMANAGER_PATIENTHISTORY)
	private MKSReportManager reportManager;

	@Test
	public void setupReport_shouldSetupPatientHistory() throws Exception {

		ReportManagerUtil.setupReport(this.reportManager);

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
						PatientHistoryReportManager.DATASET_KEY_OBS,
						PatientHistoryReportManager.DATASET_KEY_ENCOUNTERS));

		List<ReportDesign> reportDesigns = this.reportService.getReportDesigns(reportDefinition,
				PatientHistoryXmlReportRenderer.class, false);
		Assert.assertNotNull(reportDesigns);
		MatcherAssert.assertThat(reportDesigns, IsCollectionWithSize.hasSize(1));
		ReportDesign reportDesign = reportDesigns.get(0);
		Assert.assertEquals(PatientHistoryReportManager.REPORT_DESIGN_NAME, reportDesign.getName());
	}

	@Test
	public void evaluate_shouldReturnAllDemographics() throws Exception {
		EvaluationContext context = new EvaluationContext();
		context.setBaseCohort(new Cohort("7"));
		ReportDefinition rd = this.reportManager.constructReportDefinition();

		ReportData reportData = this.reportDefinitionService.evaluate(rd, context);
		assertNotNull(reportData.getDataSets());
		DataSet dataSet = reportData.getDataSets().get("demographics");
		assertNotNull(dataSet);
		assertNotNull(dataSet.getMetaData());
		assertNotNull(dataSet.getMetaData().getColumns());
		MatcherAssert.assertThat(dataSet.getMetaData().getColumns(), Matchers.hasSize(7));

		@SuppressWarnings("unchecked")
		Matcher<Iterable<? extends Object>> containsInAnyOrder = Matchers.containsInAnyOrder(
				Matchers.hasProperty("name", is("Patient Identifier")), Matchers.hasProperty("name", is("First Name")),
				Matchers.hasProperty("name", is("Last Name")), Matchers.hasProperty("name", is("Date of Birth")),
				Matchers.hasProperty("name", is("Current Age")), Matchers.hasProperty("name", is("Gender")),
				Matchers.hasProperty("name", is("Address")));

		MatcherAssert.assertThat(dataSet.getMetaData().getColumns(), containsInAnyOrder);

		DataSetRow dataSetRow = dataSet.iterator().next();

		assertEquals("6TS-4", getStringValue(dataSetRow, "Patient Identifier"));
		assertEquals("Collet", getStringValue(dataSetRow, "First Name"));
		assertEquals("Chebaskwony", getStringValue(dataSetRow, "Last Name"));
		assertEquals("1976-08-25 00:00:00.0", getStringValue(dataSetRow, "Date of Birth"));
		assertEquals("43", getStringValue(dataSetRow, "Current Age"));
		assertEquals("F", getStringValue(dataSetRow, "Gender"));
		assertEquals("Kapina", getStringValue(dataSetRow, "Address"));
	}

	private String getStringValue(DataSetRow row, String columnName) {
		Object value = row.getColumnValue(columnName);
		String strVal = StringUtils.EMPTY;
		if (value != null)
			return strVal = value.toString();
		return strVal;
	}
}
