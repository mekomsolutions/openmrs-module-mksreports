package org.openmrs.module.commonreports.reports;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.math.NumberUtils;
import org.openmrs.module.commonreports.ActivatedReportManager;
import org.openmrs.module.commonreports.CommonReportsConstants;
import org.openmrs.module.initializer.api.InitializerService;
import org.openmrs.module.reporting.cohort.definition.AgeCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CompositionCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.GenderCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
import org.openmrs.module.reporting.common.DurationUnit;
import org.openmrs.module.reporting.common.MessageUtil;
import org.openmrs.module.reporting.dataset.definition.CohortCrossTabDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.manager.ReportManagerUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component(CommonReportsConstants.COMPONENT_REPORTMANAGER_ECV)
public class EcvReportManager extends ActivatedReportManager {
	
	@Autowired
	private InitializerService inizService;
	
	@Override
	public boolean isActivated() {
		return inizService.getBooleanFromKey("report.ecv.active", false);
	}
	
	@Override
	public String getVersion() {
		return "1.0.0-SNAPSHOT";
	}
	
	@Override
	public String getUuid() {
		return "54c0b1ed-b6fe-440f-8144-e1be4a256823";
	}
	
	@Override
	public String getName() {
		return MessageUtil.translate("commonreports.report.ecv.reportName");
	}
	
	@Override
	public String getDescription() {
		return MessageUtil.translate("commonreports.report.ecv.reportDescription");
	}
	
	private Parameter getEndDateParameter() {
		return new Parameter("endDate", "End Date", Date.class);
	}
	
	public static String col1 = "";
	
	public static String col2 = "";
	
	public static String col3 = "";
	
	public static String col4 = "";
	
	@Override
	public List<Parameter> getParameters() {
		List<Parameter> params = new ArrayList<Parameter>();
		params.add(getEndDateParameter());
		return params;
	}
	
	@Override
	public ReportDefinition constructReportDefinition() {
		
		ReportDefinition rd = new ReportDefinition();
		rd.setUuid(getUuid());
		rd.setName(getName());
		rd.setDescription(getDescription());
		
		rd.setParameters(getParameters());
		
		CohortCrossTabDataSetDefinition vaccination = new CohortCrossTabDataSetDefinition();
		vaccination.addParameters(getParameters());
		rd.addDataSetDefinition(getName(), Mapped.mapStraightThrough(vaccination));
		
		Map<String, Object> parameterMappings = new HashMap<String, Object>();
		parameterMappings.put("onOrBefore", "${endDate}");
		
		String[] ecvList = inizService.getValueFromKey("report.ecvList").split(",");
		String st = "SELECT DISTINCT person_id FROM obs where 1=1";
		
		for (String member : ecvList) {
			String[] bit = member.split(" ");
			String lastOne = bit[bit.length - 1];
			if (!NumberUtils.isNumber(lastOne)) {
				st = st + " AND person_id IN (SELECT person_id FROM obs where obs_group_id IN (SELECT obs_group_id FROM obs where concept_id= (select DISTINCT concept_id from concept where uuid='"
				        + inizService.getValueFromKey("report.vaccinations")
				        + "') and value_coded=(select DISTINCT concept_id from concept where uuid='" + member + "')))";
				
			} else {
				int lastIndex = Integer.parseInt(lastOne);
				String vacName = member.substring(0, member.lastIndexOf(" "));
				st = st + " AND person_id IN (SELECT person_id FROM obs where obs_group_id IN (SELECT obs_group_id FROM obs where concept_id= (select DISTINCT concept_id from concept where uuid='"
				        + inizService.getValueFromKey("report.vaccinations")
				        + "') and value_coded=(select DISTINCT concept_id from concept where uuid='" + vacName
				        + "')) AND concept_id=(select DISTINCT concept_id from concept where uuid='"
				        + inizService.getValueFromKey("report.vaccinationSequenceNumber") + "') and value_numeric="
				        + lastIndex + ")";
			}
			
		}
		st = st + ";";
		SqlCohortDefinition sql = new SqlCohortDefinition(st);
		
		sql.addParameter(new Parameter("onOrBefore", "On Or Before", Date.class));
		vaccination.addRow("ECV", sql, parameterMappings);
		
		setColumnNames();
		
		GenderCohortDefinition males = new GenderCohortDefinition();
		males.setMaleIncluded(true);
		
		GenderCohortDefinition females = new GenderCohortDefinition();
		females.setFemaleIncluded(true);
		
		AgeCohortDefinition _0mTo1y = new AgeCohortDefinition();
		_0mTo1y.setMinAge(0);
		_0mTo1y.setMinAgeUnit(DurationUnit.MONTHS);
		_0mTo1y.setMaxAge(11);
		_0mTo1y.setMaxAgeUnit(DurationUnit.MONTHS);
		
		AgeCohortDefinition _1To2y = new AgeCohortDefinition();
		_1To2y.setMinAge(11);
		_1To2y.setMinAgeUnit(DurationUnit.MONTHS);
		_1To2y.setMaxAge(23);
		_1To2y.setMaxAgeUnit(DurationUnit.MONTHS);
		
		vaccination.addColumn(col1, createCohortComposition(_0mTo1y, females), null);
		vaccination.addColumn(col2, createCohortComposition(_1To2y, females), null);
		vaccination.addColumn(col3, createCohortComposition(_0mTo1y, males), null);
		vaccination.addColumn(col4, createCohortComposition(_1To2y, males), null);
		
		return rd;
	}
	
	private void setColumnNames() {
		col1 = MessageUtil.translate("commonreports.report.vaccination.ageCategory1.label") + " - "
		        + MessageUtil.translate("commonreports.report.vaccination.females.label");
		col2 = MessageUtil.translate("commonreports.report.vaccination.ageCategory2.label") + " - "
		        + MessageUtil.translate("commonreports.report.vaccination.females.label");
		col3 = MessageUtil.translate("commonreports.report.vaccination.ageCategory1.label") + " - "
		        + MessageUtil.translate("commonreports.report.vaccination.males.label");
		col4 = MessageUtil.translate("commonreports.report.vaccination.ageCategory2.label") + " - "
		        + MessageUtil.translate("commonreports.report.vaccination.males.label");
		
	}
	
	private CompositionCohortDefinition createCohortComposition(Object... elements) {
		CompositionCohortDefinition compCD = new CompositionCohortDefinition();
		compCD.initializeFromElements(elements);
		return compCD;
	}
	
	@Override
	public List<ReportDesign> constructReportDesigns(ReportDefinition reportDefinition) {
		return Arrays
		        .asList(ReportManagerUtil.createCsvReportDesign("2dfba707-3a90-445a-8288-9843f037c3b6", reportDefinition));
	}
}
