package org.openmrs.module.mksreports.web.controller;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.hamcrest.core.IsNot;
import org.hamcrest.core.StringContains;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.api.VisitService;
import org.openmrs.module.mksreports.MKSReportManager;
import org.openmrs.module.mksreports.MKSReportsConstants;
import org.openmrs.module.patientsummary.api.PatientSummaryService;
import org.openmrs.module.reporting.report.manager.ReportManagerUtil;
import org.openmrs.module.reporting.report.service.ReportService;
import org.openmrs.web.test.BaseModuleWebContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.ui.ModelMap;

import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.parser.PdfTextExtractor;

public class MKSReportsManageControllerTest extends BaseModuleWebContextSensitiveTest {
	
	@Autowired
	private MKSReportsManageController ctrl;
	
	@Autowired
	VisitService visitService;
	
	@Autowired
	ReportService reportService;
	
	@Autowired
	PatientSummaryService patientSummaryService;
	
	@Autowired
	@Qualifier(MKSReportsConstants.COMPONENT_REPORTMANAGER_PATIENTHISTORY)
	private MKSReportManager reportManager;
	
	@Before
	public void setup() {
		ReportManagerUtil.setupReport(this.reportManager);
	}
	
	@Test
	public void renderPatientHistory_shouldRenderAllEncounters() throws IOException {
		// setup
		ModelMap model = new ModelMap();
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		
		Integer patientId = 7;
		
		// replay
		ctrl.renderPatientHistory(model, request, response, patientId, null);
		
		String inputXml = ctrl.getPatientSummaryResultText();
		
		assertTrue(inputXml.contains("e403fafb-e5e4-42d0-9d11-4f52e89d148c"));
		assertTrue(inputXml.contains("eec646cb-c847-45a7-98bc-91c8c4f70add"));
		assertTrue(inputXml.contains("6519d653-393b-4118-9c83-a3715b82d4ac"));
		
		// verify // insure unknown patients with minimal info do not cause any NPEs
		
		byte[] pdfData = response.getContentAsByteArray();
		PdfReader reader = new PdfReader(pdfData);
		PdfTextExtractor extractor = new PdfTextExtractor(reader, true);
		
		String allText = "";
		
		for (Integer pageNum = 1; pageNum < reader.getNumberOfPages() + 1; pageNum++) {
			allText += extractor.getTextFromPage(pageNum) + "\n\r";
		}
		
		List<String> values = Arrays.asList("61.0", "55.0", "175.0", "PB", "and", "J", "150.0", "50.0");
		
		for (String value : values) {
			assertThat(allText, StringContains.containsString(value));
		}
		
		reader.close();
	}
	
	@Test
	public void renderPatientHistory_shouldRenderOnlySpecificEncounter() throws IOException {
		// setup
		ModelMap model = new ModelMap();
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		
		Integer patientId = 7;
		
		// replay
		ctrl.renderPatientHistory(model, request, response, patientId, "6519d653-393b-4118-9c83-a3715b82d4ac");
		
		String inputXml = ctrl.getPatientSummaryResultText();
		
		assertFalse(inputXml.contains("e403fafb-e5e4-42d0-9d11-4f52e89d148c"));
		assertFalse(inputXml.contains("eec646cb-c847-45a7-98bc-91c8c4f70add"));
		assertTrue(inputXml.contains("6519d653-393b-4118-9c83-a3715b82d4ac"));
		
		// verify // insure unknown patients with minimal info do not cause any NPEs
		byte[] pdfData = response.getContentAsByteArray();
		PdfReader reader = new PdfReader(pdfData);
		PdfTextExtractor extractor = new PdfTextExtractor(reader, true);
		
		String allText = "";
		
		for (Integer pageNum = 1; pageNum < reader.getNumberOfPages() + 1; pageNum++) {
			allText += extractor.getTextFromPage(pageNum) + "\n\r";
		}
		
		List<String> doesContain = Arrays.asList("150.0", "50.0");
		List<String> doesNotContain = Arrays.asList("61.0", "55.0", "175.0", "PB", "and", "J");
		
		for (String value : doesContain) {
			assertThat(allText, StringContains.containsString(value));
		}
		
		for (String value : doesNotContain) {
			assertThat(allText, IsNot.not(StringContains.containsString(value)));
		}
		
		reader.close();
	}
	
}
