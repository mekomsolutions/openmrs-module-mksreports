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

public class EmergencyReportManagerTest extends BaseModuleContextSensitiveTest {
	
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
	@Qualifier("emergencyReportManager")
	private ActivatedReportManager manager;
	
	@Before
	public void setup() throws Exception {
		executeDataSet("org/openmrs/module/reporting/include/ReportTestDataset-openmrs-2.0.xml");
		executeDataSet("org/openmrs/module/commonreports/include/emergenciesReportTestDataSet.xml");
		
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
		Assert.assertNotNull(rs.getReportDesignByUuid("0c67c7f3-b4b3-4920-82b6-830d0a8b83a1"));
		
	}
	
	@Test
	public void testReport() throws Exception {
		
		EvaluationContext context = new EvaluationContext();
		context.addParameterValue("startDate", DateUtil.parseDate("2019-08-01", "yyyy-MM-dd"));
		context.addParameterValue("endDate", DateUtil.parseDate("2021-09-30", "yyyy-MM-dd"));
		
		ReportDefinition rd = manager.constructReportDefinition();
		ReportData data = rds.evaluate(rd, context);
		
		for (Iterator<DataSetRow> itr = data.getDataSets().get(rd.getName()).iterator(); itr.hasNext();) {
			DataSetRow row = itr.next();
			System.out.println(row);
			Map<String, Integer> columnValuePairs = getColumnValues();
			for (String column : columnValuePairs.keySet()) {
				assertThat(column,((Cohort) row.getColumnValue(column)).getSize(), is(columnValuePairs.get(column)));
			}
		}
	}
	
	private Map<String, Integer> getColumnValues() {
		Map<String, Integer> map = new HashMap<String, Integer>();
		map.put("Motorcycle Road Accident.Total number", 0);
		map.put("Motorcycle Road Accident.Referred", 0);
		map.put("Motorcycle Road Accident.Deceased", 0);
		map.put("Motorcycle Road Accident.Left without permission", 0);
		map.put("Motorcycle Road Accident.Cared for", 0);
		map.put("Vehicle Road Accident.Total number", 0);
		map.put("Vehicle Road Accident.Referred", 0);
		map.put("Vehicle Road Accident.Deceased", 0);
		map.put("Vehicle Road Accident.Left without permission", 0);
		map.put("Vehicle Road Accident.Cared for", 0);
		map.put("Other Road Accident Type.Total number", 1);
		map.put("Other Road Accident Type.Referred", 0);
		map.put("Other Road Accident Type.Deceased", 0);
		map.put("Other Road Accident Type.Left without permission", 1);
		map.put("Other Road Accident Type.Cared for", 0);
		map.put("Work Accident.Total number", 1);
		map.put("Work Accident.Referred", 0);
		map.put("Work Accident.Deceased", 0);
		map.put("Work Accident.Left without permission", 0);
		map.put("Work Accident.Cared for", 1);
		map.put("Sexual Violence - Boys (0-14 years).Total number", 0);
		map.put("Sexual Violence - Boys (0-14 years).Referred", 0);
		map.put("Sexual Violence - Boys (0-14 years).Deceased", 0);
		map.put("Sexual Violence - Boys (0-14 years).Left without permission", 0);
		map.put("Sexual Violence - Boys (0-14 years).Cared for", 0);
		map.put("Sexual Violence - Girls (0-14 years).Total number", 1);
		map.put("Sexual Violence - Girls (0-14 years).Referred", 0);
		map.put("Sexual Violence - Girls (0-14 years).Deceased", 0);
		map.put("Sexual Violence - Girls (0-14 years).Left without permission", 0);
		map.put("Sexual Violence - Girls (0-14 years).Cared for", 1);
		map.put("Sexual Violence - Women (15 years and above).Total number", 0);
		map.put("Sexual Violence - Women (15 years and above).Referred", 0);
		map.put("Sexual Violence - Women (15 years and above).Deceased", 0);
		map.put("Sexual Violence - Women (15 years and above).Left without permission", 0);
		map.put("Sexual Violence - Women (15 years and above).Cared for", 0);
		map.put("Physical Violence - Children (0-14 years).Total number", 1);
		map.put("Physical Violence - Children (0-14 years).Referred", 0);
		map.put("Physical Violence - Children (0-14 years).Deceased", 1);
		map.put("Physical Violence - Children (0-14 years).Left without permission", 0);
		map.put("Physical Violence - Children (0-14 years).Cared for", 0);
		map.put("Physical Violence - Men (15 years and above).Total number", 1);
		map.put("Physical Violence - Men (15 years and above).Referred", 0);
		map.put("Physical Violence - Men (15 years and above).Deceased", 1);
		map.put("Physical Violence - Men (15 years and above).Left without permission", 0);
		map.put("Physical Violence - Men (15 years and above).Cared for", 0);
		map.put("Physical Violence - Women (15 years and above).Total number", 1);
		map.put("Physical Violence - Women (15 years and above).Referred", 0);
		map.put("Physical Violence - Women (15 years and above).Deceased", 1);
		map.put("Physical Violence - Women (15 years and above).Left without permission", 0);
		map.put("Physical Violence - Women (15 years and above).Cared for", 0);
		map.put("Other Violence Type - Children (0-14 years).Total number", 0);
		map.put("Other Violence Type - Children (0-14 years).Referred", 0);
		map.put("Other Violence Type - Children (0-14 years).Deceased", 0);
		map.put("Other Violence Type - Children (0-14 years).Left without permission", 0);
		map.put("Other Violence Type - Children (0-14 years).Cared for", 0);
		map.put("Other Violence Type - Men (15 years and above).Total number", 0);
		map.put("Other Violence Type - Men (15 years and above).Referred", 0);
		map.put("Other Violence Type - Men (15 years and above).Deceased", 0);
		map.put("Other Violence Type - Men (15 years and above).Left without permission", 0);
		map.put("Other Violence Type - Men (15 years and above).Cared for", 0);
		map.put("Other Violence Type - Women (15 years and above).Total number", 1);
		map.put("Other Violence Type - Women (15 years and above).Referred", 0);
		map.put("Other Violence Type - Women (15 years and above).Deceased", 1);
		map.put("Other Violence Type - Women (15 years and above).Left without permission", 0);
		map.put("Other Violence Type - Women (15 years and above).Cared for", 0);
		map.put("Medical and surgical emergency - Digestive.Total number", 2);
		map.put("Medical and surgical emergency - Digestive.Referred", 0);
		map.put("Medical and surgical emergency - Digestive.Deceased", 1);
		map.put("Medical and surgical emergency - Digestive.Left without permission", 1);
		map.put("Medical and surgical emergency - Digestive.Cared for", 0);
		map.put("Medical and surgical emergency - Cardiovascular.Total number", 0);
		map.put("Medical and surgical emergency - Cardiovascular.Referred", 0);
		map.put("Medical and surgical emergency - Cardiovascular.Deceased", 0);
		map.put("Medical and surgical emergency - Cardiovascular.Left without permission", 0);
		map.put("Medical and surgical emergency - Cardiovascular.Cared for", 0);
		map.put("Medical and surgical emergency - Respiratory.Total number", 0);
		map.put("Medical and surgical emergency - Respiratory.Referred", 0);
		map.put("Medical and surgical emergency - Respiratory.Deceased", 0);
		map.put("Medical and surgical emergency - Respiratory.Left without permission", 0);
		map.put("Medical and surgical emergency - Respiratory.Cared for", 0);
		map.put("Other emergencies.Total number", 1);
		map.put("Other emergencies.Referred", 0);
		map.put("Other emergencies.Deceased", 0);
		map.put("Other emergencies.Left without permission", 1);
		map.put("Other emergencies.Cared for", 0);
		return map;
		
	}
}
