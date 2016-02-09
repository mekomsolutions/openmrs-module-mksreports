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

import org.openmrs.module.reporting.common.Birthdate;
import org.openmrs.module.reporting.data.converter.AgeConverter;
import org.openmrs.module.reporting.data.converter.ConcatenatedPropertyConverter;
import org.openmrs.module.reporting.data.converter.PropertyConverter;
import org.openmrs.module.reporting.data.patient.definition.PatientDataDefinition;
import org.openmrs.module.reporting.data.patient.library.BuiltInPatientDataLibrary;
import org.openmrs.module.reporting.data.person.definition.BirthdateDataDefinition;
import org.openmrs.module.reporting.data.person.definition.PreferredAddressDataDefinition;
import org.openmrs.module.reporting.data.person.definition.PreferredNameDataDefinition;
import org.openmrs.module.reporting.definition.library.BaseDefinitionLibrary;
import org.openmrs.module.reporting.definition.library.DocumentedDefinition;
import org.springframework.stereotype.Component;

@Component
public class BasePatientDataLibrary extends BaseDefinitionLibrary<PatientDataDefinition> {

	//@Autowired
	private DataFactory df = new DataFactory();

	//@Autowired
	private BuiltInPatientDataLibrary builtInPatientData = new BuiltInPatientDataLibrary();

    @Override
    public String getKeyPrefix() {
        return "mksreports.patientData.";
    }

	@Override
	public Class<? super PatientDataDefinition> getDefinitionType() {
		return PatientDataDefinition.class;
	}

	// Address Data

	@DocumentedDefinition("birthdate")
	public PatientDataDefinition getBirthdate() {
		return df.convert(new BirthdateDataDefinition(), new PropertyConverter(Birthdate.class, "birthdate"));
	}

	@DocumentedDefinition("village")
	public PatientDataDefinition getVillage() {
		return df.getPreferredAddress("cityVillage");
	}

	@DocumentedDefinition("traditionalAuthority")
	public PatientDataDefinition getTraditionalAuthority() {
		return df.getPreferredAddress("countyDistrict");
	}

	@DocumentedDefinition("district")
	public PatientDataDefinition getDistrict() {
		return df.getPreferredAddress("stateProvince");
	}

	@DocumentedDefinition("addressFull")
	public PatientDataDefinition getAddressFull() {
		PreferredAddressDataDefinition pdd = new PreferredAddressDataDefinition();
		return df.convert(pdd, new ConcatenatedPropertyConverter(", ", "district", "traditionalAuthority", "village"));
	}

	// Demographic Data

	@DocumentedDefinition("ageAtEndInYears")
	public PatientDataDefinition getAgeAtEndInYears() {
		PatientDataDefinition ageAtEnd = builtInPatientData.getAgeAtEnd();
		return df.convert(ageAtEnd, new AgeConverter(AgeConverter.YEARS));
	}

	@DocumentedDefinition("ageAtEndInMonths")
	public PatientDataDefinition getAgeAtEndInMonths() {
		PatientDataDefinition ageAtEnd = builtInPatientData.getAgeAtEnd();
		return df.convert(ageAtEnd, new AgeConverter(AgeConverter.MONTHS));
	}

	@DocumentedDefinition("preferredFamilyNames")
	public PatientDataDefinition getPreferredFamilyNames() {
		PreferredNameDataDefinition pdd = new PreferredNameDataDefinition();
		return df.convert(pdd, new ConcatenatedPropertyConverter(" ", "familyName", "familyName2"));
	}

}
