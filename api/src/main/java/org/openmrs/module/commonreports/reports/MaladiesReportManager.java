package org.openmrs.module.commonreports.reports;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openmrs.Concept;
import org.openmrs.ConceptAnswer;
import org.openmrs.Location;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.module.commonreports.ActivatedReportManager;
import org.openmrs.module.commonreports.CommonReportsConstants;
import org.openmrs.module.initializer.api.InitializerService;
import org.openmrs.module.reporting.cohort.definition.AgeCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CodedObsCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CompositionCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.GenderCohortDefinition;
import org.openmrs.module.reporting.common.DurationUnit;
import org.openmrs.module.reporting.common.MessageUtil;
import org.openmrs.module.reporting.common.SetComparator;
import org.openmrs.module.reporting.dataset.definition.CohortCrossTabDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.manager.ReportManagerUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component()
public class MaladiesReportManager extends ActivatedReportManager {
	
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
		return "55301c99-628b-4412-96f3-e113688dda94";
	}
	
	@Override
	public String getName() {
		return "Patients with Maladies/Symptoms";
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
	
	private Parameter getLocationParameter() {
		return new Parameter("locationList", "Visit Location", Location.class, List.class, null);
	}
	
	public static String col1 = "";
	
	public static String col2 = "";
	
	@Override
	public List<Parameter> getParameters() {
		List<Parameter> params = new ArrayList<Parameter>();
		params.add(getStartDateParameter());
		params.add(getEndDateParameter());
		params.add(getLocationParameter());
		return params;
	}
	
	@Override
	public ReportDefinition constructReportDefinition() {
		
		ReportDefinition rd = new ReportDefinition();
		rd.setUuid(getUuid());
		rd.setName(getName());
		rd.setDescription(getDescription());
		
		rd.setParameters(getParameters());
		
		CohortCrossTabDataSetDefinition maladies = new CohortCrossTabDataSetDefinition();
		maladies.addParameters(getParameters());
		rd.addDataSetDefinition(getName(), Mapped.mapStraightThrough(maladies));
		
		Concept allDiags = conceptService.getConceptByUuid("7a84d506-7523-11eb-8077-0242ac120009");
		
		Map<String, Object> parameterMappings = new HashMap<String, Object>();
		parameterMappings.put("onOrAfter", "${startDate}");
		parameterMappings.put("onOrBefore", "${endDate}");
		parameterMappings.put("locationList", "${locationList}");
		
		// Add a row for each member of allDiags concept
		for (ConceptAnswer member : allDiags.getAnswers(false)) {
			CodedObsCohortDefinition diag = new CodedObsCohortDefinition();
			diag.addParameter(new Parameter("onOrAfter", "On Or After", Date.class));
			diag.addParameter(new Parameter("onOrBefore", "On Or Before", Date.class));
			diag.addParameter(new Parameter("locationList", "Visit Location", Location.class, List.class, null));
			diag.setOperator(SetComparator.IN);
			diag.setQuestion(allDiags);
			
			diag.setValueList(Arrays.asList(member.getAnswerConcept()));
			maladies.addRow(member.getAnswerConcept().getDisplayString(), diag, parameterMappings);
		}
		
		setColumnNames();
		
		GenderCohortDefinition males = new GenderCohortDefinition();
		males.setMaleIncluded(true);
		
		GenderCohortDefinition females = new GenderCohortDefinition();
		females.setFemaleIncluded(true);
		
		AgeCohortDefinition _0To1y = new AgeCohortDefinition();
		_0To1y.setMinAge(0);
		_0To1y.setMinAgeUnit(DurationUnit.MONTHS);
		_0To1y.setMaxAge(12);
		_0To1y.setMaxAgeUnit(DurationUnit.MONTHS);
		maladies.addColumn(col1, createCohortComposition(_0To1y, males), null);
		maladies.addColumn(col2, createCohortComposition(_0To1y, females), null);
		
		return rd;
	}
	
	private void setColumnNames() {
		col1 = "0-1y -> Males";
		col2 = "0-1y -> Females";
		
	}
	
	private CompositionCohortDefinition createCohortComposition(Object... elements) {
		CompositionCohortDefinition compCD = new CompositionCohortDefinition();
		compCD.initializeFromElements(elements);
		return compCD;
	}
	
	@Override
	public List<ReportDesign> constructReportDesigns(ReportDefinition reportDefinition) {
		return Arrays
		        .asList(ReportManagerUtil.createCsvReportDesign("1e6983a4-0698-4d1a-bdab-3fec01c7359e", reportDefinition));
	}
}
