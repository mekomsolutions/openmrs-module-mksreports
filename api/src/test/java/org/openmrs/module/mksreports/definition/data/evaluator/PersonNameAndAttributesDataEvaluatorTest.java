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

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.Cohort;
import org.openmrs.Order;
import org.openmrs.api.PersonService;
import org.openmrs.module.mksreports.definition.data.PersonNameAndAttributesDataDefinition;
import org.openmrs.module.reporting.data.person.EvaluatedPersonData;
import org.openmrs.module.reporting.data.person.definition.PersonAttributeDataDefinition;
import org.openmrs.module.reporting.data.person.definition.PreferredNameDataDefinition;
import org.openmrs.module.reporting.data.person.service.PersonDataService;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

public class PersonNameAndAttributesDataEvaluatorTest extends BaseModuleContextSensitiveTest {
	
	@Autowired
	private PersonService personService;
	
	@Autowired
	private PersonDataService personDataService;
	
	@Before
	public void setUp() throws Exception {
		executeDataSet("org/openmrs/module/reporting/include/ReportTestDataset-openmrs-2.0.xml");
	}
	
	@Test
	public void evaluate_shouldCalcuateObservations() throws Exception {
		
		EvaluationContext context = new EvaluationContext();
		context.setBaseCohort(new Cohort("6,2,1"));
		
		PersonNameAndAttributesDataDefinition nameDD = new PersonNameAndAttributesDataDefinition();
		
		// Preferred Name
		PreferredNameDataDefinition preferredNameDD = new PreferredNameDataDefinition();
		Mapped<PreferredNameDataDefinition> mappedPreferredNameDD = new Mapped<PreferredNameDataDefinition>();
		mappedPreferredNameDD.setParameterizable(preferredNameDD);
		
		// Create the list of mapped PersonAttributeDataDefinition to be fed to the
		// PersonNameAndAttributesDD
		// attr1
		PersonAttributeDataDefinition attr1DD = new PersonAttributeDataDefinition();
		attr1DD.setPersonAttributeType(personService.getPersonAttributeType(1));
		Mapped<PersonAttributeDataDefinition> mappedAttr1DD = new Mapped<PersonAttributeDataDefinition>();
		mappedAttr1DD.setParameterizable(attr1DD);
		
		// attr2
		PersonAttributeDataDefinition attr2DD = new PersonAttributeDataDefinition();
		attr2DD.setPersonAttributeType(personService.getPersonAttributeType(2));
		Mapped<PersonAttributeDataDefinition> mappedAttr2DD = new Mapped<PersonAttributeDataDefinition>();
		mappedAttr2DD.setParameterizable(attr2DD);
		
		List<Mapped<? extends PersonAttributeDataDefinition>> attributes = new ArrayList<Mapped<? extends PersonAttributeDataDefinition>>();
		attributes.add(mappedAttr1DD);
		attributes.add(mappedAttr2DD);
		
		// Configure the PersonNameAndAttributesDataDefinition before being evaluated
		
		nameDD.setPreferredNameDefinition(mappedPreferredNameDD);
		nameDD.setPersonAttributeDefinitions(attributes);
		
		EvaluatedPersonData evaluatedPNAADD = personDataService.evaluate(Mapped.mapStraightThrough(nameDD), context);
		
		assertEquals("Johnny Test Doe (100 Meter, Jamaica)", evaluatedPNAADD.getData().get(6));
	}
	
}
