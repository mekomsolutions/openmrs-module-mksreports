package org.openmrs.module.mksreports.renderer;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.openmrs.Encounter;
import org.openmrs.api.EncounterService;
import org.openmrs.module.mksreports.reports.PatientHistoryPdfReport;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class PatientHistoryPdfReportTest extends BaseModuleContextSensitiveTest {
	
	@Autowired
	private PatientHistoryPdfReport pdfReport;
	
	@Autowired
	@Qualifier("encounterService")
	private EncounterService es;
	
	@Test(expected = IllegalArgumentException.class)
	public void getBytes_shouldThrowWhenEncountersMismatch() throws Exception {
		
		// setup
		Set<Encounter> encounters = new HashSet<Encounter>();
		encounters.add(es.getEncounter(3));
		encounters.add(es.getEncounter(6));
		
		// replay
		pdfReport.getBytes(null, encounters);
	}
	
}
