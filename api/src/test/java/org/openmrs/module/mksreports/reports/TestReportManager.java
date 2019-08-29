package org.openmrs.module.mksreports.reports;

import java.util.List;

import org.openmrs.module.initializer.api.InitializerService;
import org.openmrs.module.mksreports.MKSReportManager;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component(TestReportManagerTest.COMPONENT_REPORTMANAGER_TEST)
public class TestReportManager extends MKSReportManager {
	
	@Autowired
	private InitializerService iniz;
	
	@Override
	public boolean isActive() {
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
