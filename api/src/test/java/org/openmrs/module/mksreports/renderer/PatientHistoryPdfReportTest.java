package org.openmrs.module.mksreports.renderer;

import java.util.HashSet;
import java.util.Set;

import javax.xml.transform.TransformerException;

import org.apache.fop.apps.FOPException;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openmrs.Encounter;
import org.openmrs.Patient;
import org.openmrs.api.EncounterService;
import org.openmrs.api.PatientService;
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
	
	@Autowired
	@Qualifier("patientService")
	private PatientService ps;
	
	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	
	@Test
	public void getBytes_shouldThrowWhenEncountersMismatch() throws FOPException, TransformerException {
		
		// setup
		Set<Encounter> encounters = new HashSet<Encounter>();
		encounters.add(es.getEncounter(3));
		encounters.add(es.getEncounter(6));
		
		// replay
		try {
			pdfReport.getBytes(null, encounters);
		}
		catch (IllegalArgumentException e) {
			Assert.assertEquals("The report could not be run because not all encounters belong to the same patient.",
			    e.getMessage());
		}
	}
	
	@Test
	public void getBytes_shouldThrowWhenPatientMismatches() throws FOPException, TransformerException {
		
		// setup
		Patient patient = ps.getPatient(2);
		Set<Encounter> encounters = new HashSet<Encounter>();
		encounters.add(es.getEncounter(3));
		encounters.add(es.getEncounter(4));
		
		// replay
		try {
			pdfReport.getBytes(patient, encounters);
		}
		catch (IllegalArgumentException e) {
			Assert.assertEquals(
			    "The report could not be run because the encounters do not correspond to the specified patient: '"
			            + patient.getUuid() + "'",
			    e.getMessage());
		}
	}
	
	@Test
	public void getBytes_shouldThrowWhenPatientAndEncountersMissing() throws FOPException, TransformerException {
		
		// replay
		try {
			pdfReport.getBytes(null, null);
		}
		catch (IllegalArgumentException e) {
			Assert.assertEquals("The report could not be run because neither the patient nor the encounters were provided.",
			    e.getMessage());
		}
	}
	
}
