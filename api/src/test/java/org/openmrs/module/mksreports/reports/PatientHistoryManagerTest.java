package org.openmrs.module.mksreports.reports;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.hamcrest.Matcher;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.hamcrest.collection.IsCollectionWithSize;
import org.junit.Assert;
import org.junit.Before;
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

	private static final String XML_DATASET_PATH = "org/openmrs/module/mksreports/include/";

	private static final String XML_REPORT_TEST_DATASET = "patientHistoryManagerTestDataset.xml";

	@Before
	public void setUp() throws Exception {
		executeDataSet(XML_DATASET_PATH + XML_REPORT_TEST_DATASET);
	}

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
		DataSet dataSet = reportData.getDataSets().get(PatientHistoryReportManager.DATASET_KEY_DEMOGRAPHICS);
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

	@Test
	public void evaluate_shouldReturnObsVitals() throws Exception {

		EvaluationContext context = new EvaluationContext();
		context.setBaseCohort(new Cohort("2"));
		ReportDefinition rd = this.reportManager.constructReportDefinition();
		ReportData reportData = this.reportDefinitionService.evaluate(rd, context);

		assertNotNull(reportData.getDataSets());
		DataSet dataSet = reportData.getDataSets().get(PatientHistoryReportManager.DATASET_KEY_OBS);
		assertNotNull(dataSet);
		assertNotNull(dataSet.getMetaData());
		assertNotNull(dataSet.getMetaData().getColumns());
		MatcherAssert.assertThat(dataSet.getMetaData().getColumns(), Matchers.hasSize(6));

		@SuppressWarnings("unchecked")
		Matcher<Iterable<? extends Object>> containsInAnyOrder = Matchers.containsInAnyOrder(
				Matchers.hasProperty("name", is(PatientHistoryReportManager.ENCOUNTER_UUID_LABEL)),
				Matchers.hasProperty("name", is(PatientHistoryReportManager.ENCOUNTER_PROVIDER_LABEL)),
				Matchers.hasProperty("name", is(PatientHistoryReportManager.OBS_DATETIME_LABEL)),
				Matchers.hasProperty("name", is(PatientHistoryReportManager.OBS_DATATYPE_LABEL)),
				Matchers.hasProperty("name", is(PatientHistoryReportManager.OBS_NAME_LABEL)),
				Matchers.hasProperty("name", is(PatientHistoryReportManager.OBS_VALUE_LABEL)));
		MatcherAssert.assertThat(dataSet.getMetaData().getColumns(), containsInAnyOrder);

		List<DataSetRow> allDataSetRow = new ArrayList<DataSetRow>();
		for (DataSetRow dataSetRow : dataSet) {
			allDataSetRow.add(dataSetRow);
		}

		DataSetRow HeightDataSetRow = allDataSetRow.get(0);
		assertEquals("Height (cm)", getStringValue(HeightDataSetRow, PatientHistoryReportManager.OBS_NAME_LABEL));
		assertEquals("180.0", getStringValue(HeightDataSetRow, PatientHistoryReportManager.OBS_VALUE_LABEL));
		assertEquals("53acc256-a393-42f2-a32c-b45dae4efd25",
				getStringValue(HeightDataSetRow, PatientHistoryReportManager.ENCOUNTER_UUID_LABEL));
		assertEquals("Super User",
				getStringValue(HeightDataSetRow, PatientHistoryReportManager.ENCOUNTER_PROVIDER_LABEL));
		assertEquals("2019-01-01 00:00:00.0",
				getStringValue(HeightDataSetRow, PatientHistoryReportManager.OBS_DATETIME_LABEL));
		assertEquals("Numeric", getStringValue(HeightDataSetRow, PatientHistoryReportManager.OBS_DATATYPE_LABEL));

		DataSetRow weightDataSetRow = allDataSetRow.get(1);
		assertEquals("WEIGHT (KG)", getStringValue(weightDataSetRow, PatientHistoryReportManager.OBS_NAME_LABEL));
		assertEquals("100.0", getStringValue(weightDataSetRow, PatientHistoryReportManager.OBS_VALUE_LABEL));
		assertEquals("53acc256-a393-42f2-a32c-b45dae4efd25",
				getStringValue(weightDataSetRow, PatientHistoryReportManager.ENCOUNTER_UUID_LABEL));
		assertEquals("Super User",
				getStringValue(weightDataSetRow, PatientHistoryReportManager.ENCOUNTER_PROVIDER_LABEL));
		assertEquals("2019-01-01 00:00:00.0",
				getStringValue(weightDataSetRow, PatientHistoryReportManager.OBS_DATETIME_LABEL));
		assertEquals("Numeric", getStringValue(weightDataSetRow, PatientHistoryReportManager.OBS_DATATYPE_LABEL));

		DataSetRow temperatureDataSetRow = allDataSetRow.get(2);
		assertEquals("Temperature (C)",
				getStringValue(temperatureDataSetRow, PatientHistoryReportManager.OBS_NAME_LABEL));
		assertEquals("34.0", getStringValue(temperatureDataSetRow, PatientHistoryReportManager.OBS_VALUE_LABEL));
		assertEquals("53acc256-a393-42f2-a32c-b45dae4efd25",
				getStringValue(temperatureDataSetRow, PatientHistoryReportManager.ENCOUNTER_UUID_LABEL));
		assertEquals("Super User",
				getStringValue(temperatureDataSetRow, PatientHistoryReportManager.ENCOUNTER_PROVIDER_LABEL));
		assertEquals("2019-01-01 00:00:00.0",
				getStringValue(temperatureDataSetRow, PatientHistoryReportManager.OBS_DATETIME_LABEL));
		assertEquals("Numeric", getStringValue(temperatureDataSetRow, PatientHistoryReportManager.OBS_DATATYPE_LABEL));

		DataSetRow pulseDataSetRow = allDataSetRow.get(3);
		assertEquals("Pulse", getStringValue(pulseDataSetRow, PatientHistoryReportManager.OBS_NAME_LABEL));
		assertEquals("90.0", getStringValue(pulseDataSetRow, PatientHistoryReportManager.OBS_VALUE_LABEL));
		assertEquals("53acc256-a393-42f2-a32c-b45dae4efd25",
				getStringValue(pulseDataSetRow, PatientHistoryReportManager.ENCOUNTER_UUID_LABEL));
		assertEquals("Super User",
				getStringValue(pulseDataSetRow, PatientHistoryReportManager.ENCOUNTER_PROVIDER_LABEL));
		assertEquals("2019-01-01 00:00:00.0",
				getStringValue(pulseDataSetRow, PatientHistoryReportManager.OBS_DATETIME_LABEL));
		assertEquals("Numeric", getStringValue(pulseDataSetRow, PatientHistoryReportManager.OBS_DATATYPE_LABEL));
	}

	private String getStringValue(DataSetRow row, String columnName) {
		Object value = row.getColumnValue(columnName);
		String strVal = StringUtils.EMPTY;
		if (value != null)
			return strVal = value.toString();
		return strVal;
	}
}
