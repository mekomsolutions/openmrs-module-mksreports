package org.openmrs.module.mksreports.reports;

import org.junit.After;
import org.junit.Before;
import org.openmrs.test.BaseModuleContextSensitiveTest;

abstract public class BaseReportTest extends BaseModuleContextSensitiveTest {
	
	public static final String XML_REPORT_DATASET = "ReportTestDataset-openmrs-2.0.xml";
	
	public static final String XML_REPORT_DATASET_PATH = "org/openmrs/module/reporting/include/";
	
	@Before
	public void initializeDatasets() throws Exception {
		executeDataSet(XML_REPORT_DATASET_PATH + XML_REPORT_DATASET);
	}
	
	@After
	public void tearDown() throws Exception {
		deleteAllData();
	}
}
