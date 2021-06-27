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
import org.openmrs.api.VisitService;
import org.openmrs.api.context.Context;
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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class VisitsReportManager extends ActivatedReportManager {
	
	@Autowired
	@Qualifier("visitService")
	private VisitService vs;
	
	@Autowired
	private InitializerService inizService;
	
	@Override
	public boolean isActivated() {
		return inizService.getBooleanFromKey("report.visits.active", false);
	}
	
	@Override
	public String getVersion() {
		return "1.0.0-SNAPSHOT";
	}
	
	@Override
	public String getUuid() {
		return "8613f380-8f39-492f-b46b-9802482bc315";
	}
	
	@Override
	public String getName() {
		return MessageUtil.translate("commonreports.report.visits.reportName");
	}
	
	@Override
	public String getDescription() {
		return MessageUtil.translate("commonreports.report.visits.reportDescription");
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
		sqlDsd.setName(getName());
		sqlDsd.setDescription("");
		
		String sql = getSqlString("org/openmrs/module/commonreports/sql/visits.sql");
		sql = applyMetadataReplacements(sql);
		
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
		ReportDesign reportDesign = ReportManagerUtil.createExcelTemplateDesign("4e1c1f7a-bbfe-44ec-a36a-3005e3fc50bd",
		    reportDefinition, "org/openmrs/module/commonreports/reportTemplates/visitsReportTemplate.xls");
		Properties designProperties = new Properties();
		designProperties.put("repeatingSections", "sheet:1,row:7,dataset:" + getName());
		designProperties.put("columnTranslationLocale", Context.getLocale().toString());
		designProperties.put("reportName.label", MessageUtil.translate("commonreports.report.visits.reportName"));
		designProperties.put("date.range.label", MessageUtil.translate("commonreports.report.visits.date.range.label"));
		designProperties.put("to.label", MessageUtil.translate("commonreports.report.visits.to.label"));
		designProperties.put("natureOfVisits.label",
		    MessageUtil.translate("commonreports.report.visits.natureOfVisits.label"));
		designProperties.put("newVisits.label", MessageUtil.translate("commonreports.report.visits.newVisits.label"));
		designProperties.put("subsequentVisits.label",
		    MessageUtil.translate("commonreports.report.visits.subsequentVisits.label"));
		designProperties.put("categories.label", MessageUtil.translate("commonreports.report.visits.categories.label"));
		reportDesign.setProperties(designProperties);
		return Arrays.asList(reportDesign);
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
	
	private String applyMetadataReplacements(String rawSql) {
		Map<String, String> metadataReplacements = getMetadataReplacements();
		
		for (String key : metadataReplacements.keySet()) {
			rawSql = rawSql.replaceAll(":" + key, metadataReplacements.get(key));
		}
		return rawSql;
	}
	
	private Map<String, String> getMetadataReplacements() {
		Map<String, String> map = new HashMap<String, String>();
		String prenatalVisitTypeUuid = inizService.getValueFromKey("report.visits.prenatal.visitType.uuid");
		String familyPlanningVisitTypeUuid = inizService.getValueFromKey("report.visits.familyPlanning.visitType.uuid");
		String[] properties = { "commonreports.report.visits.category1.label", "commonreports.report.visits.category2.label",
		        "commonreports.report.visits.category3.label", "commonreports.report.visits.category4.label",
		        "commonreports.report.visits.category5.label", "commonreports.report.visits.category6.label",
		        "commonreports.report.visits.category7.label", "commonreports.report.visits.category8.label",
		        "commonreports.report.visits.category9.label", "commonreports.report.visits.total.label" };
		
		for (String prop : properties) {
			map.put(prop, MessageUtil.translate(prop));
		}
		map.put("familyPlanningVisitTypeId", vs.getVisitTypeByUuid(familyPlanningVisitTypeUuid).getId().toString());
		map.put("prenatalVisitTypeId", vs.getVisitTypeByUuid(prenatalVisitTypeUuid).getId().toString());
		
		return map;
	}
}
