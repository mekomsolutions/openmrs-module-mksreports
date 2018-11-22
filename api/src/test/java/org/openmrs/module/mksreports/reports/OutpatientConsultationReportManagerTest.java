package org.openmrs.module.mksreports.reports;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openmrs.Cohort;
import org.openmrs.Location;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
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
		context.addParameterValue("locationList", getLocationList());
		
		ReportDefinition rd = manager.constructReportDefinition();
		ReportData data = rds.evaluate(rd, context);
		
		for (Iterator<DataSetRow> itr = data.getDataSets().get(rd.getName()).iterator(); itr.hasNext();) {
			DataSetRow row = itr.next();
			
			Cohort _5To15yMalesWithMalaria = (Cohort) row
			        .getColumnValue("MALARIA." + OutpatientConsultationReportManager.col7);
			assertNotNull(_5To15yMalesWithMalaria);
			assertEquals(2, _5To15yMalesWithMalaria.getSize());
			assertThat(_5To15yMalesWithMalaria.contains(6), is(true));
			assertThat(_5To15yMalesWithMalaria.contains(2), is(true));
			
			Cohort _25To50yFemalesWithMalaria = (Cohort) row
			        .getColumnValue("MALARIA." + OutpatientConsultationReportManager.col12);
			assertNotNull(_25To50yFemalesWithMalaria);
			assertEquals(2, _25To50yFemalesWithMalaria.getSize());
			assertThat(_25To50yFemalesWithMalaria.contains(77), is(true));
			assertThat(_25To50yFemalesWithMalaria.contains(8), is(true));
			assertThat(_25To50yFemalesWithMalaria.contains(9), is(false));
			
			Cohort _5To15yMalesWithFever = (Cohort) row.getColumnValue("FEVER." + OutpatientConsultationReportManager.col7);
			assertNotNull(_5To15yMalesWithFever);
			assertEquals(1, _5To15yMalesWithFever.getSize());
			assertThat(_5To15yMalesWithFever.contains(6), is(true));
			
			Cohort _5To15yMalesWithDiabetes = (Cohort) row
			        .getColumnValue("DIABETES." + OutpatientConsultationReportManager.col7);
			assertThat(_5To15yMalesWithDiabetes, is(notNullValue()));
			assertThat(_5To15yMalesWithDiabetes.getSize(), is(1));
			assertThat(_5To15yMalesWithDiabetes.contains(6), is(true));
			
			// Total column
			Cohort allMalesWithMalaria = (Cohort) row.getColumnValue("MALARIA." + OutpatientConsultationReportManager.col17);
			assertNotNull(allMalesWithMalaria);
			assertThat(allMalesWithMalaria.getSize(), is(2));
			assertThat(allMalesWithMalaria.contains(6), is(true));
			assertThat(allMalesWithMalaria.contains(2), is(true));
			
			Cohort allFemalesWithMalaria = (Cohort) row
			        .getColumnValue("MALARIA." + OutpatientConsultationReportManager.col18);
			assertNotNull(allFemalesWithMalaria);
			assertEquals(2, allFemalesWithMalaria.getSize());
			assertThat(allFemalesWithMalaria.contains(77), is(true));
			assertThat(allFemalesWithMalaria.contains(8), is(true));
			assertThat(allFemalesWithMalaria.contains(9), is(false));
			
			Cohort allMalesWithFever = (Cohort) row.getColumnValue("FEVER." + OutpatientConsultationReportManager.col17);
			assertNotNull(allMalesWithFever);
			assertEquals(1, allMalesWithFever.getSize());
			assertThat(allMalesWithFever.contains(6), is(true));
			
			Cohort allMalesWithDiabetes = (Cohort) row
			        .getColumnValue("DIABETES." + OutpatientConsultationReportManager.col17);
			assertNotNull(allMalesWithDiabetes);
			assertEquals(1, allMalesWithDiabetes.getSize());
			assertThat(allMalesWithDiabetes.contains(6), is(true));
			
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
			assertThat(allWithMalaria.getSize(), is(4));
			assertThat(allWithMalaria.contains(6), is(true));
			assertThat(allWithMalaria.contains(2), is(true));
			assertThat(allWithMalaria.contains(77), is(true));
			assertThat(allWithMalaria.contains(8), is(true));
			
		}
		
		DataSetRow obsSummaryRow = data.getDataSets().get("Obs Summary").iterator().next();
		assertThat(obsSummaryRow, is(notNullValue()));
		
		List<Integer> _25To50yFemalesForAllDiagnosis = (List<Integer>) obsSummaryRow
		        .getColumnValue(OutpatientConsultationReportManager.col12);
		assertThat(_25To50yFemalesForAllDiagnosis, is(notNullValue()));
		assertThat(_25To50yFemalesForAllDiagnosis.size(), is(2));
		assertThat(_25To50yFemalesForAllDiagnosis.contains(77), is(true));
		assertThat(_25To50yFemalesForAllDiagnosis.contains(8), is(true));
		
		List<Integer> _5To15yMalesForAllDiagnosis = (List<Integer>) obsSummaryRow
		        .getColumnValue(OutpatientConsultationReportManager.col7);
		assertThat(_5To15yMalesForAllDiagnosis, is(notNullValue()));
		assertThat(_5To15yMalesForAllDiagnosis.size(), is(5));
		
		Set<Integer> _5To15yMalesForAllDiagnosisWithoutDuplicates = new HashSet<>(_5To15yMalesForAllDiagnosis);
		assertThat(_5To15yMalesForAllDiagnosisWithoutDuplicates.size(), is(2));
		assertThat(_5To15yMalesForAllDiagnosisWithoutDuplicates.contains(2), is(true));
		assertThat(_5To15yMalesForAllDiagnosisWithoutDuplicates.contains(6), is(true));
		
		List<Integer> totalDiagnosisRecordsForAllGenders = (List<Integer>) obsSummaryRow
		        .getColumnValue(OutpatientConsultationReportManager.col23);
		assertThat(totalDiagnosisRecordsForAllGenders, is(notNullValue()));
		assertThat(totalDiagnosisRecordsForAllGenders.size(), is(7));
		
		Set<Integer> totalDiagnosisRecordsForAllGendersWithoutDuplicates = new HashSet<>(totalDiagnosisRecordsForAllGenders);
		assertThat(totalDiagnosisRecordsForAllGendersWithoutDuplicates.size(), is(4));
		assertThat(totalDiagnosisRecordsForAllGendersWithoutDuplicates.contains(6), is(true));
		assertThat(totalDiagnosisRecordsForAllGendersWithoutDuplicates.contains(2), is(true));
		assertThat(totalDiagnosisRecordsForAllGendersWithoutDuplicates.contains(77), is(true));
		assertThat(totalDiagnosisRecordsForAllGendersWithoutDuplicates.contains(8), is(true));
		
		List<Integer> referredToFemales = (List<Integer>) obsSummaryRow
		        .getColumnValue(OutpatientConsultationReportManager.col20);
		assertThat(referredToFemales, is(notNullValue()));
		assertThat(referredToFemales.size(), is(1));
		assertThat(referredToFemales.contains(77), is(true));
	}
	
	@Test
	@Ignore
	public void test_multipleDiagnosisForTheSamePatientShouldBeCountedOnce() throws Exception {
		EvaluationContext context = new EvaluationContext();
		context.addParameterValue("startDate", DateUtil.parseDate("2008-08-01", "yyyy-MM-dd"));
		context.addParameterValue("endDate", DateUtil.parseDate("2009-09-30", "yyyy-MM-dd"));
		context.addParameterValue("locationList", getLocationList());
		
		ReportDefinition rd = manager.constructReportDefinition();
		ReportData data = rds.evaluate(rd, context);
		
		DataSetRow obsSummaryRow = data.getDataSets().get("Obs Summary").iterator().next();
		assertThat(obsSummaryRow, is(notNullValue()));
		
		List<Integer> _0To1mMalesForAllDiagnosis = (List<Integer>) obsSummaryRow.getColumnValue("5-14 years - Males");
		assertThat(_0To1mMalesForAllDiagnosis, is(notNullValue()));
		assertThat(_0To1mMalesForAllDiagnosis.size(), is(4));
		assertThat(_0To1mMalesForAllDiagnosis.contains(6), is(true));
	}
	
	private List<Location> getLocationList() {
		return Arrays.asList(1, 2).stream().map(Context.getLocationService()::getLocation).collect(Collectors.toList());
	}
}
