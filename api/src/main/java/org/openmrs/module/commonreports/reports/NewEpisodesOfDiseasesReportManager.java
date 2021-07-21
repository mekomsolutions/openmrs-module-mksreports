package org.openmrs.module.commonreports.reports;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.openmrs.Concept;
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
import org.springframework.stereotype.Component;

@Component
public class NewEpisodesOfDiseasesReportManager extends ActivatedReportManager {
	
	@Autowired
	private InitializerService inizService;
	
	@Override
	public boolean isActivated() {
		return inizService.getBooleanFromKey("report.newEpisodesOfDiseases.active", false);
	}
	
	@Override
	public String getVersion() {
		return "1.0.0-SNAPSHOT";
	}
	
	@Override
	public String getUuid() {
		return "8b787bdc-c852-481c-b6fa-6683ec7e30d8";
	}
	
	@Override
	public String getName() {
		return MessageUtil.translate("commonreports.report.newEpisodesOfDiseases.reportName");
	}
	
	@Override
	public String getDescription() {
		return MessageUtil.translate("commonreports.report.newEpisodesOfDiseases.reportDescription");
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
		sqlDsd.setName(getName());
		sqlDsd.setDescription("");
		
		String rawSql = getSqlString("org/openmrs/module/commonreports/sql/newEpisodesOfDiseases.sql");
		Concept allMaladies = inizService.getConceptFromKey("report.newEpisodesOfDiseases.diagnosisList.conceptSet");
		
		String sql = applyMetadataReplacements(rawSql, allMaladies);
		
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
		ReportDesign reportDesign = ReportManagerUtil.createExcelTemplateDesign("7688966e-fca5-4fde-abab-1b46a87a1185",
		    reportDefinition, "org/openmrs/module/commonreports/reportTemplates/newEpisodesOfDiseasesReportTemplate.xls");
		
		Properties designProperties = new Properties();
		designProperties.put("repeatingSections", "sheet:1,row:4,dataset:" + getName());
		designProperties.put("title.label", MessageUtil.translate("commonreports.report.newEpisodesOfDiseases.title.label"));
		designProperties.put("maladies.label",
		    MessageUtil.translate("commonreports.report.newEpisodesOfDiseases.maladies.label"));
		designProperties.put("ageCategory1.label",
		    MessageUtil.translate("commonreports.report.newEpisodesOfDiseases.ageCategory1.label"));
		designProperties.put("ageCategory2.label",
		    MessageUtil.translate("commonreports.report.newEpisodesOfDiseases.ageCategory2.label"));
		designProperties.put("ageCategory3.label",
		    MessageUtil.translate("commonreports.report.newEpisodesOfDiseases.ageCategory3.label"));
		designProperties.put("ageCategory4.label",
		    MessageUtil.translate("commonreports.report.newEpisodesOfDiseases.ageCategory4.label"));
		designProperties.put("ageCategory5.label",
		    MessageUtil.translate("commonreports.report.newEpisodesOfDiseases.ageCategory5.label"));
		designProperties.put("ageCategory6.label",
		    MessageUtil.translate("commonreports.report.newEpisodesOfDiseases.ageCategory6.label"));
		designProperties.put("ageCategory7.label",
		    MessageUtil.translate("commonreports.report.newEpisodesOfDiseases.ageCategory7.label"));
		designProperties.put("total.label", MessageUtil.translate("commonreports.report.newEpisodesOfDiseases.total.label"));
		designProperties.put("males.label", MessageUtil.translate("commonreports.report.newEpisodesOfDiseases.males.label"));
		designProperties.put("females.label",
		    MessageUtil.translate("commonreports.report.newEpisodesOfDiseases.females.label"));
		designProperties.put("totalReferredCases.label",
		    MessageUtil.translate("commonreports.report.newEpisodesOfDiseases.totalReferredCases.label"));
		
		reportDesign.setProperties(designProperties);
		return Arrays.asList(reportDesign);
	}
	
	private String applyMetadataReplacements(String rawSql, Concept conceptSet) {
		Concept questionsConcept = inizService.getConceptFromKey("report.newEpisodesOfDiseases.questions.conceptSet");
		Concept referralConcept = inizService.getConceptFromKey("report.newEpisodesOfDiseases.referral.concept");
		String s = rawSql.replace(":selectStatements", constructSelectUnionAllStatements(conceptSet))
		        .replace(":whenStatements", constructWhenThenStatements(conceptSet))
		        .replace(":conceptIds",
		            questionsConcept.getSetMembers().stream().map(Concept::getId).map(Object::toString)
		                    .collect(Collectors.joining(",")))
		        .replace(":referralConcept", referralConcept.getId().toString());
		return s;
	}
	
	private String constructWhenThenStatements(Concept con) {
		Concept allDiagnosesSet = inizService.getConceptFromKey("report.newEpisodesOfDiseases.allDiagnoses.conceptSet");
		List<Concept> allOtherDiagnoses = null;
		
		if (allDiagnosesSet != null) {
			allOtherDiagnoses = new ArrayList<Concept>();
			if (allDiagnosesSet.getSetMembers().get(0).getSet()) {
				for (Concept diagnosesSet : allDiagnosesSet.getSetMembers()) {
					allOtherDiagnoses.addAll(diagnosesSet.getSetMembers());
				}
			} else {
				allOtherDiagnoses.addAll(allDiagnosesSet.getSetMembers());
			}
			
		}
		
		String st = "";
		
		for (Concept c : con.getSetMembers()) {
			if (c.getSet()) {
				for (Concept setMember : c.getSetMembers()) {
					st = st + " when o.value_coded = " + setMember.getId() + " then '"
					        + c.getPreferredName(Context.getLocale()) + "'";
				}
				if (CollectionUtils.isNotEmpty(allOtherDiagnoses)) {
					allOtherDiagnoses.removeAll(c.getSetMembers());
				}
			} else {
				st = st + " when o.value_coded = " + c.getId() + " then '" + c.getPreferredName(Context.getLocale()) + "'";
				if (CollectionUtils.isNotEmpty(allOtherDiagnoses)) {
					allOtherDiagnoses.remove(c);
				}
			}
		}
		
		// Adding entries for all other diagnoses
		if (CollectionUtils.isNotEmpty(allOtherDiagnoses)) {
			for (Concept otherDiagnosis : allOtherDiagnoses) {
				st = st + " when o.value_coded = " + otherDiagnosis.getId() + " then '"
				        + MessageUtil.translate("commonreports.report.newEpisodesOfDiseases.allOtherDiagnoses.label") + "'";
			}
		}
		
		return st;
	}
	
	private String constructSelectUnionAllStatements(Concept con) {
		String st = "";
		List<Concept> set = con.getSetMembers();
		for (int i = 0; i < set.size(); i++) {
			if (i == 0 && st.isEmpty()) {
				st = "select '" + set.get(i).getPreferredName(Context.getLocale()) + "' as \"name\"";
			} else {
				st = st + " UNION ALL select '" + set.get(i).getPreferredName(Context.getLocale()) + "'";
			}
		}
		// Adding entry for all other diagnoses
		if (inizService.getConceptFromKey("report.newEpisodesOfDiseases.allDiagnoses.conceptSet") != null) {
			st = st + " UNION ALL select '"
			        + MessageUtil.translate("commonreports.report.newEpisodesOfDiseases.allOtherDiagnoses.label") + "'";
		}
		
		return st;
	}
}
