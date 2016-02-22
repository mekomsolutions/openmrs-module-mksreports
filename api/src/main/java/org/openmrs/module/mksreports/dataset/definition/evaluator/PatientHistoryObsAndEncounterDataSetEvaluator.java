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
package org.openmrs.module.mksreports.dataset.definition.evaluator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.annotation.Handler;
import org.openmrs.module.mksreports.dataset.definition.PatientHistoryObsAndEncounterDataSetDefinition;
import org.openmrs.module.reporting.dataset.DataSet;
import org.openmrs.module.reporting.dataset.definition.evaluator.ObsDataSetEvaluator;

/**
 * The logic that evaluates a {@link PatientHistoryObsAndEncounterDataSetDefinition} and produces an {@link DataSet}
 * @see PatientHistoryObsAndEncounterDataSetDefinition
 */
@Handler(supports = { PatientHistoryObsAndEncounterDataSetDefinition.class }, order = 25)
public class PatientHistoryObsAndEncounterDataSetEvaluator extends ObsDataSetEvaluator {

	protected static final Log log = LogFactory.getLog(PatientHistoryObsAndEncounterDataSetEvaluator.class);

}