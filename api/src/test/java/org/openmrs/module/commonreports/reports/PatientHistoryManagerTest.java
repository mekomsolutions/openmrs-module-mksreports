package org.openmrs.module.commonreports.reports;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang3.StringUtils;
import org.hamcrest.Matcher;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.hamcrest.collection.IsCollectionWithSize;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Cohort;
import org.openmrs.Encounter;
import org.openmrs.api.EncounterService;
import org.openmrs.module.commonreports.ActivatedReportManager;
import org.openmrs.module.commonreports.CommonReportsConstants;
import org.openmrs.module.commonreports.renderer.PatientHistoryXmlReportRenderer;
import org.openmrs.module.patientsummary.PatientSummaryResult;
import org.openmrs.module.patientsummary.PatientSummaryTemplate;
import org.openmrs.module.patientsummary.api.PatientSummaryService;
import org.openmrs.module.reporting.dataset.DataSet;
import org.openmrs.module.reporting.dataset.DataSetRow;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.context.EncounterEvaluationContext;
import org.openmrs.module.reporting.query.encounter.EncounterIdSet;
import org.openmrs.module.reporting.report.ReportData;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.definition.service.ReportDefinitionService;
import org.openmrs.module.reporting.report.manager.ReportManagerUtil;
import org.openmrs.module.reporting.report.service.ReportService;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class PatientHistoryManagerTest extends BaseModuleContextSensitiveTest {
	
	@Autowired
	private ReportService reportService;
	
	@Autowired
	private ReportDefinitionService reportDefinitionService;
	
	@Autowired
	private PatientSummaryService patientSummaryService;
	
	@Autowired
	private EncounterService encounterService;
	
	@Autowired
	@Qualifier(CommonReportsConstants.COMPONENT_REPORTMANAGER_PATIENTHISTORY)
	private ActivatedReportManager reportManager;
	
	@Before
	public void setUp() throws Exception {
		executeDataSet("org/openmrs/module/reporting/include/ReportTestDataset-openmrs-2.0.xml");
		executeDataSet("org/openmrs/module/commonreports/include/patientHistoryManagerTestDataset.xml");
	}
	
	private ReportDesign setupAndReturnReportDesign() {
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
		        PatientHistoryReportManager.DATASET_KEY_OBS, PatientHistoryReportManager.DATASET_KEY_ENCOUNTERS));
		
		List<ReportDesign> reportDesigns = this.reportService.getReportDesigns(reportDefinition,
		    PatientHistoryXmlReportRenderer.class, false);
		Assert.assertNotNull(reportDesigns);
		MatcherAssert.assertThat(reportDesigns, IsCollectionWithSize.hasSize(1));
		
		return reportDesigns.get(0);
	}
	
	@Test
	public void setupReport_shouldSetupPatientHistory() throws Exception {
		ReportDesign reportDesign = setupAndReturnReportDesign();
		Assert.assertEquals(PatientHistoryReportManager.REPORT_DESIGN_UUID, reportDesign.getUuid());
	}
	
	@Test
	public void evaluate_shouldReturnAllDemographics() throws Exception {
		EvaluationContext context = new EvaluationContext();
		context.setBaseCohort(new Cohort("100"));
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
		
		assertEquals("6TS-4MZ", getStringValue(dataSetRow, "Patient Identifier"));
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
		context.setBaseCohort(new Cohort("100"));
		ReportDefinition rd = this.reportManager.constructReportDefinition();
		ReportData reportData = this.reportDefinitionService.evaluate(rd, context);
		
		assertNotNull(reportData.getDataSets());
		DataSet dataSet = reportData.getDataSets().get(PatientHistoryReportManager.DATASET_KEY_OBS);
		assertNotNull(dataSet);
		assertNotNull(dataSet.getMetaData());
		assertNotNull(dataSet.getMetaData().getColumns());
		MatcherAssert.assertThat(dataSet.getMetaData().getColumns(), Matchers.hasSize(7));
		
		@SuppressWarnings("unchecked")
		Matcher<Iterable<? extends Object>> containsInAnyOrder = Matchers.containsInAnyOrder(
		    Matchers.hasProperty("name", is(PatientHistoryReportManager.ENCOUNTER_UUID_LABEL)),
		    Matchers.hasProperty("name", is(PatientHistoryReportManager.ENCOUNTER_PROVIDER_LABEL)),
		    Matchers.hasProperty("name", is(PatientHistoryReportManager.OBS_DATETIME_LABEL)),
		    Matchers.hasProperty("name", is(PatientHistoryReportManager.OBS_DATATYPE_LABEL)),
		    Matchers.hasProperty("name", is(PatientHistoryReportManager.OBS_NAME_LABEL)),
		    Matchers.hasProperty("name", is(PatientHistoryReportManager.OBS_VALUE_LABEL)),
		    Matchers.hasProperty("name", is(PatientHistoryReportManager.OBS_ID_LABEL)));
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
		assertEquals("Super User", getStringValue(HeightDataSetRow, PatientHistoryReportManager.ENCOUNTER_PROVIDER_LABEL));
		assertEquals("2019-01-01 00:00:00.0",
		    getStringValue(HeightDataSetRow, PatientHistoryReportManager.OBS_DATETIME_LABEL));
		assertEquals("Numeric", getStringValue(HeightDataSetRow, PatientHistoryReportManager.OBS_DATATYPE_LABEL));
		
		DataSetRow weightDataSetRow = allDataSetRow.get(1);
		assertEquals("WEIGHT (KG)", getStringValue(weightDataSetRow, PatientHistoryReportManager.OBS_NAME_LABEL));
		assertEquals("100.0", getStringValue(weightDataSetRow, PatientHistoryReportManager.OBS_VALUE_LABEL));
		assertEquals("53acc256-a393-42f2-a32c-b45dae4efd25",
		    getStringValue(weightDataSetRow, PatientHistoryReportManager.ENCOUNTER_UUID_LABEL));
		assertEquals("Super User", getStringValue(weightDataSetRow, PatientHistoryReportManager.ENCOUNTER_PROVIDER_LABEL));
		assertEquals("2019-01-01 00:00:00.0",
		    getStringValue(weightDataSetRow, PatientHistoryReportManager.OBS_DATETIME_LABEL));
		assertEquals("Numeric", getStringValue(weightDataSetRow, PatientHistoryReportManager.OBS_DATATYPE_LABEL));
		
		DataSetRow temperatureDataSetRow = allDataSetRow.get(2);
		assertEquals("Temperature (C)", getStringValue(temperatureDataSetRow, PatientHistoryReportManager.OBS_NAME_LABEL));
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
		assertEquals("Super User", getStringValue(pulseDataSetRow, PatientHistoryReportManager.ENCOUNTER_PROVIDER_LABEL));
		assertEquals("2019-01-01 00:00:00.0",
		    getStringValue(pulseDataSetRow, PatientHistoryReportManager.OBS_DATETIME_LABEL));
		assertEquals("Numeric", getStringValue(pulseDataSetRow, PatientHistoryReportManager.OBS_DATATYPE_LABEL));
	}
	
	@Test
	public void evaluate_shouldReturnOnlySpecificEncounter() throws Throwable {
		
		// Setup
		ReportDesign reportDesign = setupAndReturnReportDesign();
		
		PatientSummaryTemplate patientSummaryTemplate = this.patientSummaryService
		        .getPatientSummaryTemplate(reportDesign.getId());
		
		String encounterUuidParam = "6519d653-393b-4118-9c83-a3715b82d4ac";
		
		EncounterEvaluationContext context = new EncounterEvaluationContext();
		
		if (!StringUtils.isBlank(encounterUuidParam)) {
			
			// support csv style list of encounters
			List<String> encounterUuidList = Arrays.asList(encounterUuidParam.split(","));
			List<Integer> encounterIdList = new ArrayList<Integer>();
			
			for (String encounterUuid : encounterUuidList) {
				Encounter encounter = encounterService.getEncounterByUuid(encounterUuid);
				encounterIdList.add(encounter.getEncounterId());
			}
			
			EncounterIdSet encIdSet = new EncounterIdSet(encounterIdList);
			
			context.addParameterValue("encounterIds", encIdSet);
			context.setBaseEncounters(encIdSet);
		}
		
		// patient 7 has 3 encounters
		Integer patientId = 7;
		
		// Replay
		PatientSummaryResult patientSummaryResult = this.patientSummaryService
		        .evaluatePatientSummaryTemplate(patientSummaryTemplate, patientId, context);
		
		// Verify
		if (patientSummaryResult.getErrorDetails() != null) {
			throw patientSummaryResult.getErrorDetails();
		} else {
			
			String xmlText = new String(patientSummaryResult.getRawContents());
			ByteArrayInputStream patientSummaryResultStream = new ByteArrayInputStream(
			        patientSummaryResult.getRawContents());
			
			Document xmlDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(patientSummaryResultStream);
			
			assertNotNull(xmlText);
			assertFalse(StringUtils.isBlank(xmlText));
			
			XPath encounterXmlPath = XPathFactory.newInstance().newXPath();
			
			String baseXmlPath = "/patientHistory";
			
			String expectedEncounterPath = baseXmlPath + "//encounter[@uuid=\"" + encounterUuidParam + "\"]";
			
			NodeList expectedResult = (NodeList) encounterXmlPath.evaluate(expectedEncounterPath, xmlDoc,
			    XPathConstants.NODESET);
			
			assertEquals("Only one encounter node with encounter uuid=" + encounterUuidParam + " should be returned", 1,
			    expectedResult.getLength());
			
			Element encounter = (Element) expectedResult.item(0);
			
			assertEquals("Encounter label should match", "Emergency", encounter.getAttribute("label"));
			
			NodeList children = encounter.getChildNodes();
			
			int numChildren = children.getLength();
			
			List<Node> obs = new ArrayList<Node>();
			
			for (int i = 0; i < numChildren; i++) {
				Node child = children.item(i);
				if (child.getNodeName().equals("obs")) {
					obs.add(child);
				}
			}
			
			// besides the two original obs in the standardDataSet.xml
			// api/src/test/resources/org/openmrs/module/commonreports/include/outpatientConsultationTestDataset.xml
			// also adds a third obs
			assertEquals(3, obs.size());
			
			String[] obsLabels = { "CD4 COUNT", "WEIGHT (KG)", "FOOD ASSISTANCE FOR ENTIRE FAMILY" };
			
			Set<String> labelSet = new HashSet<String>(Arrays.asList(obsLabels));
			
			for (int i = 0; i < obs.size(); i++) {
				Node child = obs.get(i);
				NamedNodeMap attrMap = child.getAttributes();
				assertTrue(labelSet.contains(attrMap.getNamedItem("label").getNodeValue()));
			}
			
			String[] uuidsNotReturned = { "eec646cb-c847-45a7-98bc-91c8c4f70add", "e403fafb-e5e4-42d0-9d11-4f52e89d148c" };
			
			for (String uuid : uuidsNotReturned) {
				
				String unexpectedEncounterPath = baseXmlPath + "//encounter[@uuid=\"" + uuid + "\"]";
				
				NodeList result = (NodeList) encounterXmlPath.evaluate(unexpectedEncounterPath, xmlDoc,
				    XPathConstants.NODESET);
				
				assertEquals("No encounter node with encounter uuid=" + uuid + " should be returned", 0, result.getLength());
				
			}
			
		}
	}
	
	private String getStringValue(DataSetRow row, String columnName) {
		Object value = row.getColumnValue(columnName);
		String strVal = StringUtils.EMPTY;
		if (value != null)
			return strVal = value.toString();
		return strVal;
	}
}
