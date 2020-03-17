package org.openmrs.module.mksreports.web.controller;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.hamcrest.text.StringContainsInOrder;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.api.context.Context;
import org.openmrs.module.mksreports.MKSReportManager;
import org.openmrs.module.mksreports.MKSReportsConstants;
import org.openmrs.module.reporting.report.manager.ReportManagerUtil;
import org.openmrs.web.test.BaseModuleWebContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.ui.ModelMap;

import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.parser.PdfTextExtractor;

public class PatientDataPdfExportControllerTest extends BaseModuleWebContextSensitiveTest {
	
	@Autowired
	private PatientDataPdfExportController ctrl;
	
	@Autowired
	@Qualifier(MKSReportsConstants.COMPONENT_REPORTMANAGER_PATIENTHISTORY)
	private MKSReportManager reportManager;
	
	@Before
	public void setup() throws Exception {
		executeDataSet("ControllerTestDataset.xml");
		ReportManagerUtil.setupReport(this.reportManager);
	}
	
	@Test
	public void getPatientHistory_shouldL10nEnglish() throws IOException {
		// setup
		ModelMap model = new ModelMap();
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		
		Context.setLocale(Locale.ENGLISH);
		
		// replay
		ctrl.getPatientHistory(model, request, response, "1115086f-b525-4199-afb9-729d9088ae89", "");
		
		// verify // insure unknown patients with minimal info do not cause any NPEs
		
		byte[] pdfData = response.getContentAsByteArray();
		
		assertNotNull(pdfData);
		
		PdfReader reader = new PdfReader(pdfData);
		PdfTextExtractor extractor = new PdfTextExtractor(reader, true);
		
		String allText = "";
		
		for (Integer pageNum = 1; pageNum < reader.getNumberOfPages() + 1; pageNum++) {
			allText += extractor.getTextFromPage(pageNum) + "\n\r";
		}
		
		List<String> encounterValues = Arrays.asList("Encounter", "Type", "Name:", "English", "Translation");
		List<String> visitValues = Arrays.asList("Visit", "Type", "Name:", "English", "Translation");
		
		assertThat(allText, StringContainsInOrder.stringContainsInOrder(encounterValues));
		assertThat(allText, StringContainsInOrder.stringContainsInOrder(visitValues));
		
		reader.close();
	}
	
	@Test
	public void getPatientHistory_shouldL10nSpanish() throws IOException {
		// setup
		ModelMap model = new ModelMap();
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		
		Context.setLocale(new Locale("es", "ES"));
		
		// replay
		ctrl.getPatientHistory(model, request, response, "1115086f-b525-4199-afb9-729d9088ae89", "");
		
		// verify // insure unknown patients with minimal info do not cause any NPEs
		
		byte[] pdfData = response.getContentAsByteArray();
		
		assertNotNull(pdfData);
		
		PdfReader reader = new PdfReader(pdfData);
		PdfTextExtractor extractor = new PdfTextExtractor(reader, true);
		
		String allText = "";
		
		for (Integer pageNum = 1; pageNum < reader.getNumberOfPages() + 1; pageNum++) {
			allText += extractor.getTextFromPage(pageNum) + "\n\r";
		}
		
		List<String> encValues = Arrays.asList("Nombre", "del", "tipo", "de", "encuentro:", "traducción", "al", "español");
		List<String> visitValues = Arrays.asList("Nombre", "del", "tipo", "de", "visita:", "Traducción", "al", "español");
		
		assertThat(allText, StringContainsInOrder.stringContainsInOrder(encValues));
		assertThat(allText, StringContainsInOrder.stringContainsInOrder(visitValues));
		
		reader.close();
	}
}
