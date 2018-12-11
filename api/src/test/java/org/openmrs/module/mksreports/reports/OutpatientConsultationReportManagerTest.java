package org.openmrs.module.mksreports.reports;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
	public void evaluate_shouldProduceDataset() throws Exception {
		//
		// Setup
		//
		EvaluationContext context = new EvaluationContext();
		context.addParameterValue("startDate", DateUtil.parseDate("2008-08-01", "yyyy-MM-dd"));
		context.addParameterValue("endDate", DateUtil.parseDate("2009-09-30", "yyyy-MM-dd"));
		context.addParameterValue("locationList", getLocationList());
		
		//
		// Replay
		//
		ReportDefinition rd = manager.constructReportDefinition();
		ReportData dataSet = rds.evaluate(rd, context);
		
		//
		// Verif
		//
		
		// Main part of the report: cross tab data set between cohorts
		{
			DataSetRow row = dataSet.getDataSets().get(rd.getName()).iterator().next();
			Map<String, Object> cohortsMap = row.getColumnValuesByKey();
			
			Set<String> emptyColNames = new HashSet<>(cohortsMap.keySet());
			String colName = "";
			
			colName = "MALARIA." + OutpatientConsultationReportManager.col7; // _5To15y, males
			emptyColNames.remove(colName);
			{
				Cohort c = (Cohort) cohortsMap.get(colName);
				assertNotNull(c);
				assertThat(c.getSize(), is(2));
				assertThat(c.contains(6), is(true));
				assertThat(c.contains(2), is(true));
			}
			
			colName = "MALARIA." + OutpatientConsultationReportManager.col17; // total, males
			emptyColNames.remove(colName);
			{
				Cohort c = (Cohort) cohortsMap.get(colName);
				assertNotNull(c);
				assertThat(c.getSize(), is(2));
				assertThat(c.contains(6), is(true));
				assertThat(c.contains(2), is(true));
			}
			
			colName = "MALARIA." + OutpatientConsultationReportManager.col12; // _25To50y, females
			emptyColNames.remove(colName);
			{
				Cohort c = (Cohort) cohortsMap.get(colName);
				assertNotNull(c);
				assertThat(c.getSize(), is(2));
				assertThat(c.contains(77), is(true));
				assertThat(c.contains(8), is(true));
			}
			
			colName = "MALARIA." + OutpatientConsultationReportManager.col18; // total, females
			emptyColNames.remove(colName);
			{
				Cohort c = (Cohort) cohortsMap.get(colName);
				assertNotNull(c);
				assertThat(c.getSize(), is(2));
				assertThat(c.contains(77), is(true));
				assertThat(c.contains(8), is(true));
			}
			
			colName = "MALARIA." + OutpatientConsultationReportManager.col23; // total
			emptyColNames.remove(colName);
			{
				Cohort c = (Cohort) cohortsMap.get(colName);
				assertThat(c, is(notNullValue()));
				assertThat(c.getSize(), is(4));
				assertThat(c.contains(6), is(true));
				assertThat(c.contains(2), is(true));
				assertThat(c.contains(77), is(true));
				assertThat(c.contains(8), is(true));
			}
			
			colName = "MALARIA." + OutpatientConsultationReportManager.col20; // referredTo, females
			emptyColNames.remove(colName);
			{
				Cohort c = (Cohort) cohortsMap.get(colName);
				assertNotNull(c);
				assertThat(c.getSize(), is(1));
				assertThat(c.contains(77), is(true));
			}
			
			colName = "FEVER." + OutpatientConsultationReportManager.col7; // _5To15y, males
			emptyColNames.remove(colName);
			{
				Cohort c = (Cohort) cohortsMap.get(colName);
				assertNotNull(c);
				assertThat(c.getSize(), is(1));
				assertThat(c.contains(6), is(true));
			}
			
			colName = "FEVER." + OutpatientConsultationReportManager.col17; // total, males
			emptyColNames.remove(colName);
			{
				Cohort c = (Cohort) cohortsMap.get(colName);
				assertNotNull(c);
				assertThat(c.getSize(), is(1));
				assertThat(c.contains(6), is(true));
			}
			
			colName = "FEVER." + OutpatientConsultationReportManager.col23; // total
			emptyColNames.remove(colName);
			{
				Cohort c = (Cohort) cohortsMap.get(colName);
				assertNotNull(c);
				assertThat(c.getSize(), is(1));
				assertThat(c.contains(6), is(true));
			}
			
			colName = "DIABETES." + OutpatientConsultationReportManager.col7; // _5To15y, males
			emptyColNames.remove(colName);
			{
				Cohort c = (Cohort) cohortsMap.get(colName);
				assertNotNull(c);
				assertThat(c.getSize(), is(1));
				assertThat(c.contains(6), is(true));
			}
			
			colName = "DIABETES." + OutpatientConsultationReportManager.col17; // total, males
			emptyColNames.remove(colName);
			{
				Cohort c = (Cohort) cohortsMap.get(colName);
				assertNotNull(c);
				assertThat(c.getSize(), is(1));
				assertThat(c.contains(6), is(true));
			}
			
			colName = "DIABETES." + OutpatientConsultationReportManager.col23; // total
			emptyColNames.remove(colName);
			{
				Cohort c = (Cohort) cohortsMap.get(colName);
				assertNotNull(c);
				assertThat(c.getSize(), is(1));
				assertThat(c.contains(6), is(true));
			}
			
			// All other columns should point to empty cohorts
			List<Cohort> emptyCohortsList = emptyColNames.stream().map(clName -> (Cohort) cohortsMap.get(clName))
			        .collect(Collectors.toList());
			
			for (Cohort c : emptyCohortsList) {
				assertNotNull(c);
				assertThat(c.getSize(), is(0));
			}
		}
		
		// Second part of the report: obs summary row
		{
			DataSetRow row = dataSet.getDataSets().get(OutpatientConsultationReportManager.OBS_SUMMARY_DATASET_DEF)
			        .iterator().next();
			assertThat(row, is(notNullValue()));
			
			Map<String, Object> idListsMap = row.getColumnValuesByKey(); // those are in fact patient ID lists, not
			                                                             // cohorts
			Set<String> emptyColNames = new HashSet<>(idListsMap.keySet());
			String colName = "";
			
			colName = OutpatientConsultationReportManager.col12; // _25To50, females
			emptyColNames.remove(colName);
			{
				List<Integer> idList = (List<Integer>) row.getColumnValue(colName);
				assertThat(idList, is(notNullValue()));
				assertThat(idList.size(), is(2));
				assertThat(Collections.frequency(idList, 77), is(1));
				assertThat(Collections.frequency(idList, 8), is(1));
			}
			
			colName = OutpatientConsultationReportManager.col7; // _5To15y, males
			emptyColNames.remove(colName);
			{
				List<Integer> idList = (List<Integer>) row.getColumnValue(colName);
				assertThat(idList, is(notNullValue()));
				assertThat(idList.size(), is(5));
				assertThat(Collections.frequency(idList, 6), is(4));
				assertThat(Collections.frequency(idList, 2), is(1));
			}
			
			colName = OutpatientConsultationReportManager.col23; // total
			emptyColNames.remove(colName);
			{
				List<Integer> idList = (List<Integer>) row.getColumnValue(colName);
				assertThat(idList, is(notNullValue()));
				assertThat(idList.size(), is(7));
				assertThat(Collections.frequency(idList, 6), is(4));
				assertThat(Collections.frequency(idList, 2), is(1));
				assertThat(Collections.frequency(idList, 77), is(1));
				assertThat(Collections.frequency(idList, 8), is(1));
			}
			
			colName = OutpatientConsultationReportManager.col20; // referred, females
			emptyColNames.remove(colName);
			{
				List<Integer> idList = (List<Integer>) row.getColumnValue(colName);
				assertThat(idList, is(notNullValue()));
				assertThat(idList.size(), is(1));
				assertThat(Collections.frequency(idList, 77), is(1));
			}
			
			// All other columns should point to empty patient ID lists
			List<List<Integer>> emptyIdLists = emptyColNames.stream().map(clName -> (List<Integer>) idListsMap.get(clName))
			        .collect(Collectors.toList());
			
			for (List<Integer> l : emptyIdLists) {
				assertNotNull(l);
				assertThat(l.size(), is(0));
			}
		}
	}
	
	@Test
	@Ignore
	public void evaluate_summaryOfSameDiagnosisShouldBeCountedOnce() throws Exception {
		//
		// Setup
		//
		EvaluationContext context = new EvaluationContext();
		context.addParameterValue("startDate", DateUtil.parseDate("2008-08-01", "yyyy-MM-dd"));
		context.addParameterValue("endDate", DateUtil.parseDate("2009-09-30", "yyyy-MM-dd"));
		context.addParameterValue("locationList", getLocationList());
		
		//
		// Replay
		//
		ReportDefinition rd = manager.constructReportDefinition();
		ReportData data = rds.evaluate(rd, context);
		
		//
		// Verif
		//
		DataSetRow row = data.getDataSets().get(OutpatientConsultationReportManager.OBS_SUMMARY_DATASET_DEF).iterator()
		        .next();
		assertThat(row, is(notNullValue()));
		
		List<Integer> idList = (List<Integer>) row.getColumnValue(OutpatientConsultationReportManager.col7); // _5To15y,
		                                                                                                     // males
		assertThat(idList, is(notNullValue()));
		assertThat(idList.size(), is(4));
		assertThat(idList.contains(6), is(true));
	}
	
	private List<Location> getLocationList() {
		return Arrays.asList(1, 2).stream().map(Context.getLocationService()::getLocation).collect(Collectors.toList());
	}
}
