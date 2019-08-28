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

import java.util.AbstractMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.openmrs.api.LocationService;
import org.openmrs.api.context.Context;
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

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

@Component
public class BasePatientDataLibrary extends BaseDefinitionLibrary<PatientDataDefinition> {
	
	/*
	 * XStream converter for unmarshalling the Address Template XML into a
	 * Map<String, String> of what is inside <nameMappings/>.
	 */
	/**
	 * @see {@link https://github.com/mekomsolutions/openmrs-module-coreapps/blob/
	 *      3603eaf433d1d426cd8c9748956a5a0eaebd7ef9/omod/src/main/java/org/openmrs/module/coreapps/fragment/controller/patientheader/RegistrationDataHelper.java#L146-
	 *      L 1 7 3 * }
	 */
	protected static class AddressTemplateConverter implements Converter {
		
		public boolean canConvert(Class clazz) {
			return AbstractMap.class.isAssignableFrom(clazz);
		}
		
		@Override
		public void marshal(Object arg0, HierarchicalStreamWriter writer, MarshallingContext context) {
		}
		
		@Override
		public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
			
			Map<String, String> map = new LinkedHashMap<String, String>(); // LinkedHashMap to keep the Address Template
			                                                               // XML order.
			reader.moveDown(); // Get down the nameMappings, sizeMappings... etc level
			
			while (reader.hasMoreChildren()) {
				if (reader.getNodeName().equals("nameMappings")) {
					while (reader.hasMoreChildren()) {
						reader.moveDown();
						String key = reader.getAttribute("name");
						String value = reader.getAttribute("value");
						map.put(key, value);
						reader.moveUp();
					}
				}
			}
			return map;
		}
	}
	
	protected Map<String, String> getAddressTemplateNameMappings(final LocationService locationService) {
		
		XStream xstream = new XStream();
		xstream.alias("org.openmrs.layout.address.AddressTemplate", java.util.Map.class);
		xstream.registerConverter(new AddressTemplateConverter());
		
		String addressTemplateXml = locationService.getAddressTemplate();
		
		@SuppressWarnings("unchecked")
		Map<String, String> nameMappings = (Map<String, String>) xstream.fromXML(addressTemplateXml);
		
		return nameMappings;
	}
	
	// @Autowired
	private DataFactory dataFactory = new DataFactory();
	
	// @Autowired
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
		return dataFactory.convert(new BirthdateDataDefinition(), new PropertyConverter(Birthdate.class, "birthdate"));
	}
	
	@DocumentedDefinition("addressFull")
	public PatientDataDefinition getAddressFull() {
		Map<String, String> nameMappings = getAddressTemplateNameMappings(Context.getLocationService());
		Set<String> addressLevels = nameMappings.keySet();
		
		PreferredAddressDataDefinition pdd = new PreferredAddressDataDefinition();
		return dataFactory.convert(pdd,
		    new ConcatenatedPropertyConverter(", ", addressLevels.toArray(new String[addressLevels.size()])));
		//		return dataFactory.convert(pdd, new ConcatenatedPropertyConverter(", ", "cityVillage", "countyDistrict", "stateProvince", "country"));
	}
	
	// Demographic Data
	
	@DocumentedDefinition("ageAtEndInYears")
	public PatientDataDefinition getAgeAtEndInYears() {
		PatientDataDefinition ageAtEnd = builtInPatientData.getAgeAtEnd();
		return dataFactory.convert(ageAtEnd, new AgeConverter(AgeConverter.YEARS));
	}
	
	@DocumentedDefinition("ageAtEndInMonths")
	public PatientDataDefinition getAgeAtEndInMonths() {
		PatientDataDefinition ageAtEnd = builtInPatientData.getAgeAtEnd();
		return dataFactory.convert(ageAtEnd, new AgeConverter(AgeConverter.MONTHS));
	}
	
	@DocumentedDefinition("preferredFamilyNames")
	public PatientDataDefinition getPreferredFamilyNames() {
		PreferredNameDataDefinition pdd = new PreferredNameDataDefinition();
		return dataFactory.convert(pdd, new ConcatenatedPropertyConverter(" ", "familyName", "familyName2"));
	}
	
}
