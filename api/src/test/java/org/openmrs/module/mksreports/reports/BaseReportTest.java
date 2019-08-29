package org.openmrs.module.mksreports.reports;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.test.BaseModuleContextSensitiveTest;

public class BaseReportTest extends BaseModuleContextSensitiveTest {
	
	public static final String XML_REPORT_DATASET = "ReportTestDataset-openmrs-2.0.xml";
	
	public static final String XML_REPORT_DATASET_PATH = "org/openmrs/module/reporting/include/";
	
	@Before
	public void initializeDatasets() throws Exception {
		executeDataSet(XML_REPORT_DATASET_PATH + XML_REPORT_DATASET);
	}
	
	@Test
	public void should_loadDataset() {
	}
}
