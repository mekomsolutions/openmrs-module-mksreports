package org.openmrs.module.mksreports.reports;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.module.mksreports.MKSReportManager;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class TestReportManagerTest extends BaseModuleContextSensitiveTest {
	
	public static final String COMPONENT_MOCK_REPORTMANAGER = "mksreports.mockReportManager";
	
	@Autowired
	@Qualifier(COMPONENT_MOCK_REPORTMANAGER)
	private MKSReportManager testReportManager;
	
	@Test
	public void isActive_shouldParseFromJsonConfig() {
		Assert.assertFalse(testReportManager.isActive());
	}
}
