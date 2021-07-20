package org.openmrs.module.commonreports.reports;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.apache.commons.io.IOUtils;
import org.openmrs.module.commonreports.ActivatedReportManager;
import org.openmrs.module.initializer.api.InitializerService;
import org.openmrs.module.reporting.common.MessageUtil;
import org.openmrs.module.reporting.dataset.definition.SqlDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.manager.ReportManagerUtil;
import org.openmrs.util.OpenmrsClassLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LabReportManager extends ActivatedReportManager {
	
	@Autowired
	private InitializerService inizService;
	
	@Override
	public boolean isActivated() {
		return inizService.getBooleanFromKey("report.lab.active", false);
	}
	
	@Override
	public String getVersion() {
		return "1.0.0-SNAPSHOT";
	}
	
	@Override
	public String getUuid() {
		return "14f9b6a6-e176-467c-83ee-6281e743834d";
	}
	
	@Override
	public String getName() {
		return MessageUtil.translate("commonreports.report.lab.reportName");
	}
	
	@Override
	public String getDescription() {
		return MessageUtil.translate("commonreports.report.lab.reportDescription");
	}
	
	private Parameter getStartDateParameter() {
		return new Parameter("startDate", "Start Date", Date.class);
	}
	
	private Parameter getEndDateParameter() {
		return new Parameter("endDate", "End Date", Date.class);
	}
	
	private String getSqlString(String resourceName) {
		
		InputStream is = null;
		try {
			is = OpenmrsClassLoader.getInstance().getResourceAsStream(resourceName);
			return IOUtils.toString(is, "UTF-8");
		}
		catch (Exception e) {
			throw new IllegalArgumentException("Unable to load resource: " + resourceName, e);
		}
		finally {
			IOUtils.closeQuietly(is);
		}
	}
	
	@Override
	public List<Parameter> getParameters() {
		List<Parameter> params = new ArrayList<Parameter>();
		params.add(getStartDateParameter());
		params.add(getEndDateParameter());
		return params;
	}
	
	@Override
	public ReportDefinition constructReportDefinition() {
		
		ReportDefinition rd = new ReportDefinition();
		
		rd.setName(getName());
		rd.setDescription(getDescription());
		rd.setParameters(getParameters());
		rd.setUuid(getUuid());
		
		SqlDataSetDefinition sqlDsd = new SqlDataSetDefinition();
		sqlDsd.setName("Lab SQL Dataset");
		sqlDsd.setDescription("Lab SQL Dataset");
		
		String rawSql = getSqlString("org/openmrs/module/commonreports/sql/lab.sql");
		String sql = applyMetadataReplacements(rawSql);
		sqlDsd.setSqlQuery(sql);
		sqlDsd.addParameters(getParameters());
		
		Map<String, Object> parameterMappings = new HashMap<String, Object>();
		parameterMappings.put("startDate", "${startDate}");
		parameterMappings.put("endDate", "${endDate}");
		
		rd.addDataSetDefinition(getName(), sqlDsd, parameterMappings);
		
		return rd;
	}
	
	@Override
	public List<ReportDesign> constructReportDesigns(ReportDefinition reportDefinition) {
		ReportDesign reportDesign = ReportManagerUtil.createExcelTemplateDesign("988d3408-9aac-4b6f-9cab-77c3f02beef5",
		    reportDefinition, "org/openmrs/module/commonreports/reportTemplates/labReportTemplate.xls");
		
		Properties designProperties = new Properties();
		
		reportDesign.setProperties(designProperties);
		return Arrays.asList(reportDesign);
	}
	
	private String applyMetadataReplacements(String rawSql) {
		String s = rawSql
		        .replace(":serialSputumBacilloscopy",
		            "'" + inizService.getValueFromKey("report.lab.serialSputumBacilloscopy") + "'")
		        .replace(":positive", "'" + inizService.getValueFromKey("report.lab.positive") + "'")
		        .replace(":negative", "'" + inizService.getValueFromKey("report.lab.negative") + "'")
		        .replace(":indeterminate", "'" + inizService.getValueFromKey("report.lab.indeterminate") + "'")
		        .replace(":zero", "'" + inizService.getValueFromKey("report.lab.zero") + "'")
		        .replace(":onePlus", "'" + inizService.getValueFromKey("report.lab.onePlus") + "'")
		        .replace(":twoPlus", "'" + inizService.getValueFromKey("report.lab.twoPlus") + "'")
		        .replace(":threePlus", "'" + inizService.getValueFromKey("report.lab.threePlus") + "'")
		        .replace(":fourPlus", "'" + inizService.getValueFromKey("report.lab.fourPlus") + "'")
		        .replace(":malaria", "'" + inizService.getValueFromKey("report.lab.malaria") + "'")
		        .replace(":completeBloodCount", "'" + inizService.getValueFromKey("report.lab.completeBloodCount") + "'")
		        .replace(":sicklingTest", "'" + inizService.getValueFromKey("report.lab.sicklingTest") + "'")
		        .replace(":bloodGroup", "'" + inizService.getValueFromKey("report.lab.bloodGroup") + "'")
		        .replace(":urinalysis", "'" + inizService.getValueFromKey("report.lab.urinalysis") + "'")
		        .replace(":prenatalVisitType", "'" + inizService.getValueFromKey("report.lab.prenatalVisitType") + "'");
		return s;
	}
	
}
