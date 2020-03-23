package org.openmrs.module.commonreports.reports;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.module.commonreports.ActivatedReportManager;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class TestReportManagerTest extends BaseModuleContextSensitiveTest {
	
	public static final String COMPONENT_MOCK_REPORTMANAGER = "commonreports.mockReportManager";
	
	@Autowired
	@Qualifier(COMPONENT_MOCK_REPORTMANAGER)
	private ActivatedReportManager testReportManager;
	
	@Test
	public void isActive_shouldParseFromJsonConfig() {
		Assert.assertFalse(testReportManager.isActivated());
	}
}
