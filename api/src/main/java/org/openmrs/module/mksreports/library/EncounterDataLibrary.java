/*
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

package org.openmrs.module.mksreports.library;

import java.util.Map;

import org.openmrs.module.mksreports.common.Helper;
import org.openmrs.module.reporting.data.encounter.definition.EncounterDataDefinition;
import org.openmrs.module.reporting.data.encounter.definition.SqlEncounterDataDefinition;
import org.openmrs.module.reporting.data.patient.library.BuiltInPatientDataLibrary;
import org.openmrs.module.reporting.definition.library.BaseDefinitionLibrary;
import org.openmrs.module.reporting.definition.library.DocumentedDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 */
@Component
public class EncounterDataLibrary extends BaseDefinitionLibrary<EncounterDataDefinition> {
	
	public static final String PREFIX = "mksreports.encounterDataCalculation.";
	
	@Autowired
	BuiltInPatientDataLibrary builtInPatientDataLibrary;
	
	@Override
	public Class<? super EncounterDataDefinition> getDefinitionType() {
		return EncounterDataDefinition.class;
	}
	
	@Override
	public String getKeyPrefix() {
		return PREFIX;
	}
	
	@DocumentedDefinition("visit.id")
	public EncounterDataDefinition getVisitId() {
		return sqlEncounterDataDefinition("visitId.sql", null);
	}
	
	@DocumentedDefinition("visit.startDatetime")
	public EncounterDataDefinition getVisitStartDatetime() {
		return sqlEncounterDataDefinition("visitStartDate.sql", null);
	}
	
	@DocumentedDefinition("visit.stopDatetime")
	public EncounterDataDefinition getVisitStopDatetime() {
		return sqlEncounterDataDefinition("visitStopDate.sql", null);
	}
	
	private EncounterDataDefinition sqlEncounterDataDefinition(String resourceName, Replacements replacements) {
		String sql = Helper.getStringFromResource("org/openmrs/module/mksreports/sql/encounterData/" + resourceName);
		if (replacements != null) {
			for (Map.Entry<String, String> entry : replacements.entrySet()) {
				sql = sql.replaceAll(":" + entry.getKey(), entry.getValue());
			}
		}
		
		SqlEncounterDataDefinition definition = new SqlEncounterDataDefinition();
		definition.setSql(sql);
		return definition;
	}
	
}
