package org.openmrs.module.mksreports.reports;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.openmrs.module.mksreports.MKSReportManager;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

@Ignore
public class TestReportManagerTest extends BaseModuleContextSensitiveTest {
	
	public static final String COMPONENT_REPORTMANAGER_TEST = "mksreports.testReportManager";
	
	@Autowired
	@Qualifier(COMPONENT_REPORTMANAGER_TEST)
	private MKSReportManager testReportManager;
	
	@Test
	public void isActive_shouldParseFromJsonConfig() {
		Assert.assertFalse(testReportManager.isActive());
	}
}
