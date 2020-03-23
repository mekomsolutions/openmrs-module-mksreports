/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.commonreports.definition.data.evaluator;

import static org.junit.Assert.assertTrue;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Cohort;
import org.openmrs.api.ConceptService;
import org.openmrs.module.commonreports.definition.data.ObsOnAgeDataDefinition;
import org.openmrs.module.reporting.common.DateUtil;
import org.openmrs.module.reporting.common.TimeQualifier;
import org.openmrs.module.reporting.data.person.definition.AgeDataDefinition;
import org.openmrs.module.reporting.data.visit.EvaluatedVisitData;
import org.openmrs.module.reporting.data.visit.definition.ObsForVisitDataDefinition;
import org.openmrs.module.reporting.data.visit.service.VisitDataService;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

public class ObsOnAgeDataEvaluatorTest extends BaseModuleContextSensitiveTest {
	
	@Autowired
	private VisitDataService visitDataService;
	
	@Autowired
	private ConceptService conceptService;
	
	@Before
	public void setup() throws Exception {
		executeDataSet("org/openmrs/module/commonreports/include/obsOnAgeTestDataset.xml");
	}
	
	@Test
	public void evaluate_shouldReturnAllObsForVisit() throws Exception {
		
		EvaluationContext context = new EvaluationContext();
		context.setBaseCohort(new Cohort("7"));
		
		ObsOnAgeDataDefinition d = new ObsOnAgeDataDefinition();
		
		ObsForVisitDataDefinition obsVisitDD = new ObsForVisitDataDefinition();
		obsVisitDD.setQuestion(conceptService.getConcept(5089));
		obsVisitDD.setWhich(TimeQualifier.LAST);
		Mapped<ObsForVisitDataDefinition> mappedObsDD = new Mapped<ObsForVisitDataDefinition>();
		mappedObsDD.setParameterizable(obsVisitDD);
		
		AgeDataDefinition agePersonDD = new AgeDataDefinition();
		agePersonDD.setEffectiveDate(DateUtil.getDateTime(2017, 7, 10));
		Mapped<AgeDataDefinition> mappedAgeDD = new Mapped<AgeDataDefinition>();
		mappedAgeDD.setParameterizable(agePersonDD);
		
		d.setObsDefinition(mappedObsDD);
		d.setAgeDefinition(mappedAgeDD);
		
		EvaluatedVisitData vd = visitDataService.evaluate(d, context);
		Assert.assertEquals(new Double(82) / new Double(40), vd.getData().get(242));
		
		assertTrue(true);
	}
}
