package org.openmrs.module.commonreports.reports;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Cohort;
import org.openmrs.api.ConceptService;
import org.openmrs.module.commonreports.ActivatedReportManager;
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
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class ChronicIllnessesReportManagerTest extends BaseModuleContextSensitiveTest {
	
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
	@Qualifier("chronicIllnessesReportManager")
	private ActivatedReportManager manager;
	
	@Before
	public void setup() throws Exception {
		executeDataSet("org/openmrs/module/reporting/include/ReportTestDataset-openmrs-2.0.xml");
		executeDataSet("org/openmrs/module/commonreports/include/chronicIllnesses.xml");
		
		String path = getClass().getClassLoader().getResource("testAppDataDir").getPath() + File.separator;
		System.setProperty("OPENMRS_APPLICATION_DATA_DIRECTORY", path);
		
		for (Loader loader : iniz.getLoaders()) {
			if (loader.getDomainName().equals(Domain.JSON_KEY_VALUES.getName())) {
				loader.load();
			}
		}
	}
	
	@Test
	public void setupReport_shouldSetupChronicIllnessesReport() {
		
		// replay
		ReportManagerUtil.setupReport(manager);
		
		// verify
		Assert.assertNotNull(rs.getReportDesignByUuid("aaaf86d1-4db5-45da-9861-bcfcc6e58994"));
		
	}
	
	@Test
	public void testReport() throws Exception {
		
		EvaluationContext context = new EvaluationContext();
		context.addParameterValue("startedOnOrAfter", DateUtil.parseDate("2008-08-01", "yyyy-MM-dd"));
		context.addParameterValue("startDate", DateUtil.parseDate("2021-06-01", "yyyy-MM-dd"));
		context.addParameterValue("endDate", DateUtil.parseDate("2021-06-30", "yyyy-MM-dd"));
		
		ReportDefinition rd = manager.constructReportDefinition();
		ReportData data = rds.evaluate(rd, context);
		
		for (Iterator<DataSetRow> itr = data.getDataSets().get(rd.getName()).iterator(); itr.hasNext();) {
			DataSetRow row = itr.next();
			Map<String, Integer> columnValuePairs = getColumnValues();
			for (String column : columnValuePairs.keySet()) {
				assertThat(column, ((Cohort) row.getColumnValue(column)).getSize(), is(columnValuePairs.get(column)));
			}
		}
	}
	
	private Map<String, Integer> getColumnValues() {
		Map<String, Integer> map = new HashMap<String, Integer>();
		map.put("Diabetes - New Diagnoses.<10 years - Females", 0);
		map.put("Diabetes - New Diagnoses.<10 years - Males", 0);
		map.put("Diabetes - New Diagnoses.10-14 years - Females", 0);
		map.put("Diabetes - New Diagnoses.10-14 years - Males", 1);
		map.put("Diabetes - New Diagnoses.15-19 years - Females", 0);
		map.put("Diabetes - New Diagnoses.15-19 years - Males", 1);
		map.put("Diabetes - New Diagnoses.20-24 years - Females", 0);
		map.put("Diabetes - New Diagnoses.20-24 years - Males", 0);
		map.put("Diabetes - New Diagnoses.25-49 years - Females", 0);
		map.put("Diabetes - New Diagnoses.25-49 years - Males", 0);
		map.put("Diabetes - New Diagnoses._> 50 years - Females", 0);
		map.put("Diabetes - New Diagnoses._> 50 years - Males", 1);
		map.put("Diabetes - New Diagnoses.Total referred cases - Females", 0);
		map.put("Diabetes - New Diagnoses.Total referred cases - Males", 3);
		map.put("Diabetes - Conditions.<10 years - Females", 0);
		map.put("Diabetes - Conditions.<10 years - Males", 0);
		map.put("Diabetes - Conditions.10-14 years - Females", 1);
		map.put("Diabetes - Conditions.10-14 years - Males", 0);
		map.put("Diabetes - Conditions.15-19 years - Females", 1);
		map.put("Diabetes - Conditions.15-19 years - Males", 0);
		map.put("Diabetes - Conditions.20-24 years - Females", 0);
		map.put("Diabetes - Conditions.20-24 years - Males", 0);
		map.put("Diabetes - Conditions.25-49 years - Females", 0);
		map.put("Diabetes - Conditions.25-49 years - Males", 0);
		map.put("Diabetes - Conditions._> 50 years - Females", 1);
		map.put("Diabetes - Conditions._> 50 years - Males", 0);
		map.put("Diabetes - Conditions.Total referred cases - Females", 1);
		map.put("Diabetes - Conditions.Total referred cases - Males", 0);
		map.put("Hypertension (I10) - New Diagnoses.<10 years - Females", 0);
		map.put("Hypertension (I10) - New Diagnoses.<10 years - Males", 1);
		map.put("Hypertension (I10) - New Diagnoses.10-14 years - Females", 0);
		map.put("Hypertension (I10) - New Diagnoses.10-14 years - Males", 0);
		map.put("Hypertension (I10) - New Diagnoses.15-19 years - Females", 0);
		map.put("Hypertension (I10) - New Diagnoses.15-19 years - Males", 0);
		map.put("Hypertension (I10) - New Diagnoses.20-24 years - Females", 1);
		map.put("Hypertension (I10) - New Diagnoses.20-24 years - Males", 0);
		map.put("Hypertension (I10) - New Diagnoses.25-49 years - Females", 0);
		map.put("Hypertension (I10) - New Diagnoses.25-49 years - Males", 0);
		map.put("Hypertension (I10) - New Diagnoses._> 50 years - Females", 1);
		map.put("Hypertension (I10) - New Diagnoses._> 50 years - Males", 0);
		map.put("Hypertension (I10) - New Diagnoses.Total referred cases - Females", 0);
		map.put("Hypertension (I10) - New Diagnoses.Total referred cases - Males", 1);
		map.put("Hypertension (I10) - Conditions.<10 years - Females", 0);
		map.put("Hypertension (I10) - Conditions.<10 years - Males", 0);
		map.put("Hypertension (I10) - Conditions.10-14 years - Females", 0);
		map.put("Hypertension (I10) - Conditions.10-14 years - Males", 0);
		map.put("Hypertension (I10) - Conditions.15-19 years - Females", 0);
		map.put("Hypertension (I10) - Conditions.15-19 years - Males", 0);
		map.put("Hypertension (I10) - Conditions.20-24 years - Females", 0);
		map.put("Hypertension (I10) - Conditions.20-24 years - Males", 0);
		map.put("Hypertension (I10) - Conditions.25-49 years - Females", 0);
		map.put("Hypertension (I10) - Conditions.25-49 years - Males", 1);
		map.put("Hypertension (I10) - Conditions._> 50 years - Females", 0);
		map.put("Hypertension (I10) - Conditions._> 50 years - Males", 0);
		map.put("Hypertension (I10) - Conditions.Total referred cases - Females", 0);
		map.put("Hypertension (I10) - Conditions.Total referred cases - Males", 0);
		map.put("Obesity (E66.9) - New Diagnoses.<10 years - Females", 0);
		map.put("Obesity (E66.9) - New Diagnoses.<10 years - Males", 1);
		map.put("Obesity (E66.9) - New Diagnoses.10-14 years - Females", 0);
		map.put("Obesity (E66.9) - New Diagnoses.10-14 years - Males", 0);
		map.put("Obesity (E66.9) - New Diagnoses.15-19 years - Females", 0);
		map.put("Obesity (E66.9) - New Diagnoses.15-19 years - Males", 0);
		map.put("Obesity (E66.9) - New Diagnoses.20-24 years - Females", 0);
		map.put("Obesity (E66.9) - New Diagnoses.20-24 years - Males", 1);
		map.put("Obesity (E66.9) - New Diagnoses.25-49 years - Females", 0);
		map.put("Obesity (E66.9) - New Diagnoses.25-49 years - Males", 0);
		map.put("Obesity (E66.9) - New Diagnoses._> 50 years - Females", 0);
		map.put("Obesity (E66.9) - New Diagnoses._> 50 years - Males", 0);
		map.put("Obesity (E66.9) - New Diagnoses.Total referred cases - Females", 0);
		map.put("Obesity (E66.9) - New Diagnoses.Total referred cases - Males", 0);
		map.put("Obesity (E66.9) - Conditions.<10 years - Females", 0);
		map.put("Obesity (E66.9) - Conditions.<10 years - Males", 1);
		map.put("Obesity (E66.9) - Conditions.10-14 years - Females", 0);
		map.put("Obesity (E66.9) - Conditions.10-14 years - Males", 0);
		map.put("Obesity (E66.9) - Conditions.15-19 years - Females", 0);
		map.put("Obesity (E66.9) - Conditions.15-19 years - Males", 0);
		map.put("Obesity (E66.9) - Conditions.20-24 years - Females", 0);
		map.put("Obesity (E66.9) - Conditions.20-24 years - Males", 0);
		map.put("Obesity (E66.9) - Conditions.25-49 years - Females", 1);
		map.put("Obesity (E66.9) - Conditions.25-49 years - Males", 0);
		map.put("Obesity (E66.9) - Conditions._> 50 years - Females", 0);
		map.put("Obesity (E66.9) - Conditions._> 50 years - Males", 0);
		map.put("Obesity (E66.9) - Conditions.Total referred cases - Females", 0);
		map.put("Obesity (E66.9) - Conditions.Total referred cases - Males", 1);
		return map;
		
	}
}
