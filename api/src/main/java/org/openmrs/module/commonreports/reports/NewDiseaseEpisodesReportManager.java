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
import org.openmrs.api.ConceptService;
import org.openmrs.module.commonreports.ActivatedReportManager;
import org.openmrs.module.initializer.api.InitializerService;
import org.openmrs.module.reporting.dataset.definition.SqlDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.manager.ReportManagerUtil;
import org.openmrs.util.OpenmrsClassLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component()
public class NewDiseaseEpisodesReportManager extends ActivatedReportManager {
	
	private static final String REPEATING_SECTION = "sheet:1,row:4,dataset:New Disease Episodes";
	
	@Autowired
	private InitializerService inizService;
	
	@Autowired
	private ConceptService conceptService;
	
	@Override
	public boolean isActivated() {
		return true;
	}
	
	@Override
	public String getVersion() {
		return "1.0.0-SNAPSHOT";
	}
	
	@Override
	public String getUuid() {
		return "434c50e8-ee54-429d-9aad-b5014ce513d2";
	}
	
	@Override
	public String getName() {
		return "New Disease Episodes";
	}
	
	@Override
	public String getDescription() {
		return "";
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
		
		rd.setName("Report Definition Name");
		rd.setDescription("Report Definition Description");
		rd.setParameters(getParameters());
		rd.setUuid(getUuid());
		
		SqlDataSetDefinition sqlDsd = new SqlDataSetDefinition();
		sqlDsd.setName("SQL Dataset Name");
		sqlDsd.setDescription("SQL Dataset Description");
		
		String rawSql = getSqlString("org/openmrs/module/commonreports/sql/NewEpisodesOfDiseases.sql");
		
		sqlDsd.setSqlQuery(rawSql);
		sqlDsd.addParameters(getParameters());
		
		Map<String, Object> parameterMappings = new HashMap<String, Object>();
		parameterMappings.put("startDate", "${startDate}");
		parameterMappings.put("endDate", "${endDate}");
		
		rd.addDataSetDefinition(getName(), sqlDsd, parameterMappings);
		
		return rd;
	}
	
	@Override
	public List<ReportDesign> constructReportDesigns(ReportDefinition reportDefinition) {
		ReportDesign reportDesign = ReportManagerUtil.createExcelTemplateDesign("7688966e-fca5-4fde-abab-1b46a87a1185",
		    reportDefinition, "org/openmrs/module/commonreports/reportTemplates/NewEpisodesOfDiseasesReportTemplate.xls");
		Properties designProperties = new Properties();
		designProperties.put("repeatingSections", REPEATING_SECTION);
		reportDesign.setProperties(designProperties);
		return Arrays.asList(reportDesign);
	}
}
