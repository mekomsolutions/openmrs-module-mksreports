package org.openmrs.module.commonreports.reports;

import java.util.List;

import org.openmrs.module.commonreports.ActivatedReportManager;
import org.openmrs.module.initializer.api.InitializerService;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component(TestReportManagerTest.COMPONENT_MOCK_REPORTMANAGER)
public class MockReportManager extends ActivatedReportManager {
	
	@Autowired
	private InitializerService iniz;
	
	@Override
	public boolean isActivated() {
		return iniz.getBooleanFromKey("report.test.active");
	}
	
	@Override
	public ReportDefinition constructReportDefinition() {
		return null;
	}
	
	@Override
	public List<ReportDesign> constructReportDesigns(ReportDefinition arg0) {
		return null;
	}
	
	@Override
	public String getDescription() {
		return null;
	}
	
	@Override
	public String getName() {
		return null;
	}
	
	@Override
	public String getUuid() {
		return null;
	}
	
	@Override
	public String getVersion() {
		return null;
	}
}
