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
package org.openmrs.module.mksreports.definition.data.evaluator;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.Cohort;
import org.openmrs.Encounter;
import org.openmrs.Visit;
import org.openmrs.api.ConceptService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.ObsService;
import org.openmrs.api.VisitService;
import org.openmrs.module.mksreports.definition.data.CalculatedObsDataDefinition;
import org.openmrs.module.mksreports.definition.data.CalculatedObsDataDefinition.Operator;
import org.openmrs.module.mksreports.reports.BaseReportTest;
import org.openmrs.module.reporting.common.TimeQualifier;
import org.openmrs.module.reporting.data.visit.EvaluatedVisitData;
import org.openmrs.module.reporting.data.visit.definition.ObsForVisitDataDefinition;
import org.openmrs.module.reporting.data.visit.definition.VisitDataDefinition;
import org.openmrs.module.reporting.data.visit.evaluator.ObsForVisitDataEvaluator;
import org.openmrs.module.reporting.data.visit.service.VisitDataService;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.springframework.beans.factory.annotation.Autowired;

public class CalculatedObsDataEvaluatorTest extends BaseReportTest {
	
	@Autowired
	private VisitService visitService;
	
	@Autowired
	private EncounterService encounterService;
	
	@Autowired
	private VisitDataService visitDataService;
	
	@Autowired
	private ConceptService conceptService;
	
	@Autowired
	private ObsService obsService;
	
	/**
	 * @see ObsForVisitDataEvaluator#evaluate(VisitDataDefinition,EvaluationContext)
	 * @verifies returns the calculated value that matches the operation and passed operands
	 */
	@Test
	public void evaluate_shouldCalcuateObservations() throws Exception {
		
		EvaluationContext context = new EvaluationContext();
		context.setBaseCohort(new Cohort("6"));
		
		ObsForVisitDataDefinition obsDD1 = new ObsForVisitDataDefinition();
		obsDD1.setQuestion(conceptService.getConcept(5089));
		obsDD1.setWhich(TimeQualifier.LAST);
		Mapped<ObsForVisitDataDefinition> mappedObsDD1 = new Mapped<ObsForVisitDataDefinition>();
		mappedObsDD1.setParameterizable(obsDD1);
		
		ObsForVisitDataDefinition obsDD2 = new ObsForVisitDataDefinition();
		obsDD2.setQuestion(conceptService.getConcept(5497));
		obsDD2.setWhich(TimeQualifier.LAST);
		Mapped<ObsForVisitDataDefinition> mappedObsDD2 = new Mapped<ObsForVisitDataDefinition>();
		mappedObsDD2.setParameterizable(obsDD2);
		
		// Assign Visit 5 to Encounter 4
		Visit visit5 = visitService.getVisit(5);
		Encounter encounter4 = encounterService.getEncounter(4);
		encounter4.setVisit(visit5);
		
		Double o10 = obsService.getObs(10).getValueNumeric();
		Double o11 = obsService.getObs(11).getValueNumeric();
		{
			CalculatedObsDataDefinition d = new CalculatedObsDataDefinition(Operator.ADDITION);
			d.setObsDefinition1(mappedObsDD1);
			d.setObsDefinition2(mappedObsDD2);
			EvaluatedVisitData vd = visitDataService.evaluate(d, context);
			Assert.assertEquals(o10 + o11, vd.getData().get(5));
		}
		{
			CalculatedObsDataDefinition d = new CalculatedObsDataDefinition(Operator.SUBSTRACTION);
			d.setObsDefinition1(mappedObsDD1);
			d.setObsDefinition2(mappedObsDD2);
			EvaluatedVisitData vd = visitDataService.evaluate(d, context);
			Assert.assertEquals(o10 - o11, vd.getData().get(5));
		}
		{
			CalculatedObsDataDefinition d = new CalculatedObsDataDefinition(Operator.MULTIPLICATION);
			d.setObsDefinition1(mappedObsDD1);
			d.setObsDefinition2(mappedObsDD2);
			EvaluatedVisitData vd = visitDataService.evaluate(d, context);
			Assert.assertEquals(o10 * o11, vd.getData().get(5));
		}
		{
			CalculatedObsDataDefinition d = new CalculatedObsDataDefinition(Operator.DIVISION);
			d.setObsDefinition1(mappedObsDD1);
			d.setObsDefinition2(mappedObsDD2);
			EvaluatedVisitData vd = visitDataService.evaluate(d, context);
			Assert.assertEquals(o10 / o11, vd.getData().get(5));
		}
		
	}
	
	/**
	 * @see ObsForVisitDataEvaluator#evaluate(VisitDataDefinition,EvaluationContext)
	 * @verifies returns null when one of the operand is null
	 */
	@Test
	public void evaluate_shouldReturnNullWhenOneOperandIsNull() throws Exception {
		
		EvaluationContext context = new EvaluationContext();
		context.setBaseCohort(new Cohort("6"));
		
		ObsForVisitDataDefinition obsDD1 = new ObsForVisitDataDefinition();
		obsDD1.setQuestion(conceptService.getConcept(5089));
		obsDD1.setWhich(TimeQualifier.LAST);
		Mapped<ObsForVisitDataDefinition> mappedObsDD1 = new Mapped<ObsForVisitDataDefinition>();
		mappedObsDD1.setParameterizable(obsDD1);
		
		ObsForVisitDataDefinition obsDD2 = new ObsForVisitDataDefinition();
		obsDD2.setQuestion(conceptService.getConcept(5497));
		obsDD2.setWhich(TimeQualifier.LAST);
		Mapped<ObsForVisitDataDefinition> mappedObsDD2 = new Mapped<ObsForVisitDataDefinition>();
		mappedObsDD2.setParameterizable(obsDD2);
		
		// Assign Visit 5 to Encounter 5 (which has only 1 numeric obs)
		Visit visit5 = visitService.getVisit(5);
		Encounter encounter5 = encounterService.getEncounter(5);
		encounter5.setVisit(visit5);
		
		{
			CalculatedObsDataDefinition d = new CalculatedObsDataDefinition(Operator.ADDITION);
			d.setObsDefinition1(mappedObsDD1);
			d.setObsDefinition2(mappedObsDD2);
			EvaluatedVisitData vd = visitDataService.evaluate(d, context);
			Assert.assertNull(vd.getData().get(5));
		}
		
	}
	
	/**
	 * @see ObsForVisitDataEvaluator#evaluate(VisitDataDefinition,EvaluationContext)
	 * @verifies returns null when dividing by zero
	 */
	@Test
	public void evaluate_shouldHandleDivisionByZero() throws Exception {
		
		EvaluationContext context = new EvaluationContext();
		context.setBaseCohort(new Cohort("6"));
		
		ObsForVisitDataDefinition obsDD1 = new ObsForVisitDataDefinition();
		obsDD1.setQuestion(conceptService.getConcept(5089));
		obsDD1.setWhich(TimeQualifier.LAST);
		Mapped<ObsForVisitDataDefinition> mappedObsDD1 = new Mapped<ObsForVisitDataDefinition>();
		mappedObsDD1.setParameterizable(obsDD1);
		
		ObsForVisitDataDefinition obsDD2 = new ObsForVisitDataDefinition();
		obsDD2.setQuestion(conceptService.getConcept(18));
		obsDD2.setWhich(TimeQualifier.LAST);
		Mapped<ObsForVisitDataDefinition> mappedObsDD2 = new Mapped<ObsForVisitDataDefinition>();
		mappedObsDD2.setParameterizable(obsDD2);
		
		// Assign Visit 5 to Encounter 3 (which has a one obs of "0" numeric value)
		Visit visit5 = visitService.getVisit(5);
		Encounter encounter7 = encounterService.getEncounter(7);
		encounter7.setVisit(visit5);
		
		{
			CalculatedObsDataDefinition d = new CalculatedObsDataDefinition(Operator.DIVISION);
			d.setObsDefinition1(mappedObsDD1);
			d.setObsDefinition2(mappedObsDD2);
			EvaluatedVisitData vd = visitDataService.evaluate(d, context);
			Assert.assertNull(vd.getData().get(5));
		}
	}
	
	/**
	 * @see ObsForVisitDataEvaluator#evaluate(VisitDataDefinition,EvaluationContext)
	 * @verifies returns null when dividing by zero
	 */
	@Test
	public void evaluate_shouldUseMostRecentObsByDefault() throws Exception {
		
		EvaluationContext context = new EvaluationContext();
		context.setBaseCohort(new Cohort("6"));
		
		ObsForVisitDataDefinition obsDD1 = new ObsForVisitDataDefinition();
		obsDD1.setQuestion(conceptService.getConcept(5089));
		// Do not specify any TimeQualifier on the ObsForVisitDataDefinition (no
		// setWhich())
		Mapped<ObsForVisitDataDefinition> mappedObsDD1 = new Mapped<ObsForVisitDataDefinition>();
		mappedObsDD1.setParameterizable(obsDD1);
		
		ObsForVisitDataDefinition obsDD2 = new ObsForVisitDataDefinition();
		obsDD2.setQuestion(conceptService.getConcept(5497));
		// Do not specify any TimeQualifier on the ObsForVisitDataDefinition (no
		// setWhich())
		Mapped<ObsForVisitDataDefinition> mappedObsDD2 = new Mapped<ObsForVisitDataDefinition>();
		mappedObsDD2.setParameterizable(obsDD2);
		
		// Assign Visit 5 to Encounter 4 and Encounter 3
		Visit visit5 = visitService.getVisit(5);
		Encounter encounter4 = encounterService.getEncounter(4);
		encounter4.setVisit(visit5);
		
		Double o10 = obsService.getObs(10).getValueNumeric();
		Double o11 = obsService.getObs(11).getValueNumeric();
		{
			CalculatedObsDataDefinition d = new CalculatedObsDataDefinition(Operator.ADDITION);
			d.setObsDefinition1(mappedObsDD1);
			d.setObsDefinition2(mappedObsDD2);
			EvaluatedVisitData vd = visitDataService.evaluate(d, context);
			Assert.assertEquals(o10 + o11, vd.getData().get(5));
		}
		
		// Ensure that one can override the default TimeQualifier.LAST
		obsDD2.setWhich(TimeQualifier.FIRST);
		obsDD1.setWhich(TimeQualifier.FIRST);
		// Encounter 3 has 2 older observations. They should be returned
		Encounter encounter3 = encounterService.getEncounter(3);
		encounter3.setVisit(visit5);
		
		Double o7 = obsService.getObs(7).getValueNumeric();
		Double o9 = obsService.getObs(9).getValueNumeric();
		
		{
			CalculatedObsDataDefinition d = new CalculatedObsDataDefinition(Operator.ADDITION);
			d.setObsDefinition1(mappedObsDD1);
			d.setObsDefinition2(mappedObsDD2);
			EvaluatedVisitData vd = visitDataService.evaluate(d, context);
			Assert.assertEquals(o7 + o9, vd.getData().get(5));
		}
	}
}
